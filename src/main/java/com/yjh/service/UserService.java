package com.yjh.service;

/**
 * @Author mua
 * @Date 2020/8/5 13:57
 */
public interface UserService {

    /**
     * 向redis中写入用户访问次数
     */
    int saveUserCount(Integer userId);

    /**
     *判断单位时间调用次数是否大于阈值
     */
    boolean getUserCount(Integer userId);
}
