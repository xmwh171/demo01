package com.example.demofirst.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demofirst.dto.UserRegisterDTO;
import com.example.demofirst.entity.User;
import com.example.demofirst.mapper.UserMapper;
import com.example.demofirst.service.UserService;
import com.example.demofirst.utils.CollectionUtils;
import com.example.demofirst.utils.PasswordUtils;
import com.example.demofirst.utils.RedisCacheUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Service  // 标记为Spring服务组件，可被注入
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisCacheUtils redisCacheUtils;

    @Resource
    private RedissonClient redissonClient;

    private static final String USER_CACHE_KEY = "user:";

    // 带缓存的查询
    public User getUserByIdWithCache(Long id) {
        // 1 先查 Redis
        User cacheUser = (User) redisTemplate.opsForValue().get(USER_CACHE_KEY + id);
        if (cacheUser != null) {
            return cacheUser;
        }
        // 2 查数据库
        User dbUser = getById(id);
        if (dbUser == null) {
            // 缓存空值，防穿透
            redisTemplate.opsForValue().set(USER_CACHE_KEY + id, null, 5, TimeUnit.MINUTES);
            return null;
        }

        // 3 写入缓存，30分钟过期 + 随机值防雪崩
        long expire = 30 + (long) (Math.random() * 10);
        redisTemplate.opsForValue().set(USER_CACHE_KEY + id, dbUser, expire, TimeUnit.MINUTES);

        return dbUser;
    }

    // 更新时删缓存
    public boolean updateUserAndClearCache(User user) {
        boolean success = updateById(user);
        if (success) {
            redisTemplate.delete(USER_CACHE_KEY + user.getId());
        }
        return success;
    }


    // 缓存击穿，用互斥锁（setnx）
    public User getUserWithLock(Long id){
        String key = "user:" + id;
        User user = (User) redisTemplate.opsForValue().get(key);
        if (user != null) return user;

        String lockKey = "lock:user:" + id;
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(lock)) {
            try {
                user = getById(id);
                if (user != null) {
                    redisTemplate.opsForValue().set(key, user, 30, TimeUnit.MINUTES);
                }
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            // 自旋重试
            try { Thread.sleep(50); } catch (Exception e) {}
            return getUserWithLock(id);
        }
        return user;

    }


    @Cacheable(value = "user", key = "#id", unless = "#result == null")
    public User getUserById(Long id) {
        return getById(id);
    }

    @CacheEvict(value = "user", key = "#user.id")
    public boolean updateUser(User user) {
        return updateById(user);
    }

    /**
     * 优化后的分页查询（指定ArrayList容量，避免扩容）
     */
    @Override
    public IPage<User> pageUser(Integer pageNum, Integer pageSize, String username) {
        // 1. 初始化分页对象
        Page<User> page = new Page<>(pageNum, pageSize);

        // 2. 构建查询条件
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        wrapper.orderByDesc("id");

        // 3. 执行查询
        IPage<User> userPage = userMapper.selectPage(page, wrapper);

        // 4. 优化结果列表（指定容量，避免后续操作扩容）
        List<User> records = userPage.getRecords();
        List<User> optimizedRecords = CollectionUtils.newArrayListWithCapacity(records.size());
        optimizedRecords.addAll(records);
        userPage.setRecords(optimizedRecords);

        return userPage;
    }


    /**
     * 新增：用户注册方法
     */
    public boolean register(UserRegisterDTO registerDTO) {
        // 1. 检查用户名是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", registerDTO.getUsername());
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            return false;
        }

        // 2. 新增用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(PasswordUtils.encrypt(registerDTO.getPassword()));
        user.setEnable(1);
        user.setPhone(registerDTO.getPhone());
        user.setCreateTime(LocalDateTime.now());

        return userMapper.insert(user) > 0;
    }

    public List<User> getUserByCondition(String username, String phone, LocalDateTime startTime, LocalDateTime endTime) {
        return userMapper.selectUserByCondition(username, phone, startTime, endTime);
    }

    // 批量插入用户，开启事务并优化批次
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int batchInsertUsers(List<User> userList) {
        // 分批次插入（避免一次性插入过多数据，锁表）
        int batchSize = 100;
        int total = 0;
        for (int i = 0; i < userList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, userList.size());
            List<User> batch = userList.subList(i, end);
            total += baseMapper.batchInsertUsers(batch);
        }
        return total;
    }


    /**
     * 扣减库存，测试手写的分布式锁
     * 手写锁存在的问题
     * 1.锁过期释放，业务还没执行完 → 锁失效
     * 2.不可重入 → 同一线程多次加锁死锁
     * 3.未主从同步 → 主挂了锁丢失（极端）
     * @param productId
     */
    public void deductStock(Long productId) {
        String lockKey = "lock:product:" + productId;
        String requestId = UUID.randomUUID().toString();

        try {
            // 加锁30秒
            boolean locked = redisCacheUtils.lock(lockKey, requestId, 30);
            if (!locked) {
                throw new RuntimeException("请求频繁，请稍后再试");
            }

            // 执行业务：减库存、扣余额...
            System.out.println("拿到锁，执行业务");

        } finally {
            // 必须释放锁
            redisCacheUtils.unlock(lockKey, requestId);
        }

    }


    /**
     *  Redisson 加锁 / 解锁
     * @param userId
     */
    public void doWithLock(Long userId) {
        String lockKey = "lock:user:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试等待3秒，锁10秒自动过期
            boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }

            // 业务逻辑：更新、扣减、防重复...
            System.out.println("Redisson 加锁成功，执行业务");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // 只能解锁自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


}
