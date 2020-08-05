package com.yjh.dao;

import com.yjh.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author mua
 * @Date 2020/8/4 9:53
 */
@Mapper
public interface OrderDao {

    /**
     * 生成订单
     * @param order
     */
    void createOrder(Order order);
}
