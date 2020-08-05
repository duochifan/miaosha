package com.yjh.dao;

import com.yjh.entity.User;

/**
 * @Author mua
 * @Date 2020/8/5 15:04
 */
public interface UserDao {

    User getById(Integer userId);
}
