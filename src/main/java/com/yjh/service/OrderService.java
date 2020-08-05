package com.yjh.service;

/**
 * @Author mua
 * @Date 2020/8/4 9:48
 */
public interface OrderService {

    /**
     * 处理秒杀的下单方法，并返回订单id
     */
    int kill(Integer id);

    /**
     * md5签名方法
     */
    String getMd5(Integer id, Integer userId);

    /**
     * 处理秒杀的下单方法，并返回订单id ，加入md5接口隐藏
     */
    int kill(Integer id, Integer userId, String md5);
}
