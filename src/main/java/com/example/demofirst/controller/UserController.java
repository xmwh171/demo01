package com.example.demofirst.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demofirst.Result;
import com.example.demofirst.annotation.NoRepeatSubmit;
import com.example.demofirst.annotation.RateLimit;
import com.example.demofirst.annotation.RecordLog;
import com.example.demofirst.constant.ErrorCodeEnum;
import com.example.demofirst.dto.LoginDTO;
import com.example.demofirst.dto.UserPageDTO;
import com.example.demofirst.dto.UserRegisterDTO;
import com.example.demofirst.entity.User;
import com.example.demofirst.exception.BusinessException;
import com.example.demofirst.service.AsyncService;
import com.example.demofirst.service.UserService;
import com.example.demofirst.utils.CountUtils;
import com.example.demofirst.utils.PasswordUtils;
import com.example.demofirst.utils.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户接口控制器，提供CRUD接口
 */
@Tag(name = "用户管理接口", description = "用户增删改查、登录、退出登录等操作")
@RestController
@RequestMapping("/user")  // 所有接口前缀为/user
public class UserController {

    // 注入UserService
    @Resource
    private UserService userService;

    @Resource
    private AsyncService asyncService;

    @Resource
    private TokenUtils tokenUtils;  // 注入Token工具类

    // 动态条件查询用户
    @GetMapping("/search")
    public Result<List<User>> searchUser( @RequestParam(required = false) String username,
                                          @RequestParam(required = false) String phone,
                                          @RequestParam(required = false) String startTime,
                                          @RequestParam(required = false) String endTime) {

        LocalDateTime start = startTime == null ? null : LocalDateTime.parse(startTime.replace(" ", "T"));
        LocalDateTime end = endTime == null ? null : LocalDateTime.parse(endTime.replace(" ", "T"));
        List<User> userList = userService.getUserByCondition(username, phone, start, end);
        return Result.success(userList);
    }

    /**
     * 1. 新增用户（POST请求）
     */
    @NoRepeatSubmit(expire = 5)
    @RecordLog("新增用户")
    @PostMapping("/add")
    public Result<Boolean> addUser(@Valid @RequestBody User user) {
        // 1. 密码加密（明文→密文）
        String password = PasswordUtils.encrypt(user.getPassword());
        user.setPassword(password);
        // 设置创建/更新时间
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        // 保存用户到数据库
        boolean save = userService.save(user);
        return Result.success(save);
    }

    /**
     * 2. 根据ID查询用户（GET请求）
     */
    @GetMapping("/get/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    /**
     * 3. 查询所有用户（GET请求）
     */
    @GetMapping("/list")
    public Result<List<User>> listAllUser() {
        List<User> userList = userService.list();
        return Result.success(userList);
    }

    /**
     * 4. 根据ID修改用户（PUT请求）
     */
    @PutMapping("/update")
    public Result<Boolean> updateUser(@RequestBody User user) {
        user.setUpdateTime(LocalDateTime.now());
        boolean update = userService.updateById(user);
        return Result.success(update);
    }

    /**
     * 5. 根据ID删除用户（DELETE请求）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        boolean remove = userService.removeById(id);
        return Result.success(remove);
    }

    /**
     * 6. 根据用户名查询用户（GET请求）
     */
    @GetMapping("/getByUsername")
    public Result<User> getUserByUsername(@RequestParam String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);  // 等值查询：username = 参数值
        User user = userService.getOne(queryWrapper);
        return Result.success(user);
    }

    /**
     * 用户登录接口
     * @param loginDTO 登录参数（带校验）
     * @return 登录结果
     */
    @RateLimit(window = 60, limit = 5, prefix = "login_limit:")
    @RecordLog("分页查询用户")
    @Operation(summary = "用户登录", description = "输入用户名密码，返回登录Token，有效期2小时")
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO loginDTO) {
        // 1. 根据用户名查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginDTO.getUsername());
        User dbUser = userService.getOne(queryWrapper);

        // 2. 判断用户是否存在，抛自定义业务异常（替代原有 Result.error）
        if (dbUser == null) {
           throw new BusinessException(ErrorCodeEnum.BUSINESS_ERROR,"用户名不存在");
        }

        // 3. 校验密码（明文 vs 数据库密文）
        boolean passwordMatch = PasswordUtils.match(loginDTO.getPassword(), dbUser.getPassword());
        if (!passwordMatch) {
            throw new BusinessException(ErrorCodeEnum.PARAM_ERROR, "密码错误");
        }

        // 4. 生成Token
        String token = tokenUtils.generateToken(dbUser.getId());

        // 5. 登录成功,返回Token给前端
        return Result.success("登录成功！欢迎你：" + token);
    }

    /**
     * 退出登录（删除Redis中的Token）
     */
    @RecordLog("用户退出登录")
    @Operation(summary = "退出登录", description = "删除Redis中的Token，失效登录态")
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("token") String token) {
        // 删除Token
        tokenUtils.deleteToken(token);
        return Result.success("退出登录成功");
    }


    // 分页查询用户列表
    @RateLimit(limit = 20)
    @RecordLog("分页查询用户")
    @Operation(summary = "分页查询用户", description = "支持用户名模糊搜索、创建时间范围筛选，返回分页数据")
    @GetMapping("/page")
    public Result<IPage<User>> pageUser(
            @Parameter(description = "分页查询参数（页码/条数/关键词/时间范围）")
            @Valid UserPageDTO pageDTO) {

        // 1. 构建查询条件
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        // 创建时间大于等于起始时间
        if (pageDTO.getStartTime() != null) {
            wrapper.ge("create_time", pageDTO.getStartTime());
        }
        // 创建时间小于等于结束时间
        if (pageDTO.getEndTime() != null) {
            wrapper.le("create_time", pageDTO.getEndTime());
        }

        // 2. 执行分页查询
        IPage<User> page = userService.pageUser(pageDTO.getPageNum(), pageDTO.getPageSize(),pageDTO.getUsername());

        // 3. 返回统一格式结果
        return Result.success(page);
    }

    @GetMapping("/getApiCount")
    public String getApiCount(@RequestParam String uri) {
        long count = CountUtils.getApiCount(uri);
        return "接口[" + uri + "]访问量：" + count;
    }

    /**
     * 新增：用户注册接口（触发异步短信发送）
     */
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        try {
            // 1. 核心业务：注册用户（你已有的用户入库逻辑，这里简化）
            boolean registerSuccess = userService.register(registerDTO);
            if (!registerSuccess) {
                return Result.error(ErrorCodeEnum.BUSINESS_ERROR,"注册失败：用户名已存在");
            }

            // 2. 异步任务：发送注册成功短信（不阻塞主线程）
            String smsContent = "【demo01】恭喜你注册成功！用户名：" + registerDTO.getUsername();
            asyncService.sendSms(registerDTO.getPhone(), smsContent);

            // 3. 异步任务：记录注册操作日志
            asyncService.saveOperationLog(registerDTO.getUsername(), "/user/register", 100);

            return Result.success("注册成功！短信已发送");
        } catch (Exception e) {
            return Result.error(ErrorCodeEnum.BUSINESS_ERROR,"注册失败：" + e.getMessage());
        }
    }

}