package com.example.demofirst.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demofirst.dto.UserRegisterDTO;
import com.example.demofirst.entity.User;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务接口，继承IService获得更丰富的CRUD能力
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询
     *
     * @param pageNum
     * @param pageSize
     * @param username
     * @return
     */
    IPage<User> pageUser(Integer pageNum, Integer pageSize, String username);

    /**
     * 新增：用户注册方法
     */
    boolean register(UserRegisterDTO registerDTO);

    /**
     * 根据条件查询
     *
     * @param username
     * @param phone
     * @param startTime
     * @param endTime
     * @return
     */
    List<User> getUserByCondition(String username, String phone, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 批量插入
     * @param userList
     * @return
     */
    int batchInsertUsers(List<User> userList);
}