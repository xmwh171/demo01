package com.example.demofirst.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demofirst.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户Mapper接口，继承BaseMapper获得CRUD能力
 */
@Mapper  // 标记为MyBatis的Mapper接口，SpringBoot会自动扫描
public interface UserMapper extends BaseMapper<User> {

    // 原有方法保留，新增这个动态查询方法
    List<User> selectUserByCondition(
            @Param("username") String username,
            @Param("phone") String phone,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * foreach标签：批量查询启用状态的用户（根据多个ID）
     * @param ids 用户ID列表
     * @return 启用状态的用户列表
     */
    List<User> batchSelectByIds(@Param("ids") List<Long> ids);

    /**
     * foreach标签：批量插入用户
     * @param userList 用户列表
     * @return 插入成功的条数
     */
    int batchInsertUsers(@Param("userList") List<User> userList);

    /**
     * choose/when/otherwise：优先级查询启用状态的用户
     * @param username 用户名（模糊查询，优先级最高）
     * @return 启用状态的用户列表
     */
    List<User> selectEnableUserByCondition(@Param("username") String username);

    /**
     * set + if：动态更新用户（只更新非空字段）
     * @param user 用户对象（需包含id，以及要更新的username/phone）
     * @return 更新成功的条数
     */
    int updateUserSelective(User user);
}
