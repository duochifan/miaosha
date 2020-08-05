package com.yjh.service.impl;

import com.yjh.dao.OrderDao;
import com.yjh.dao.StockDao;
import com.yjh.dao.UserDao;
import com.yjh.entity.Order;
import com.yjh.entity.Stock;
import com.yjh.entity.User;
import com.yjh.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Author mua
 * @Date 2020/8/4 9:52
 */
@Service
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private StockDao stockDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public int kill(Integer id) {
        //校验redis中秒杀商品是否超时
        if (!stringRedisTemplate.hasKey("kill" + id)) {
            throw new RuntimeException("当前商品的抢购活动已经结束！！");
        }
        //根据商品id校验库存
        Stock stock = checkStock(id);
        //扣除库存
        updateSale(stock);
        //创建订单
        return createOrder(stock);
    }

    @Override
    public int kill(Integer id, Integer userId, String md5) {
        //校验redis中秒杀商品是否超时
//        if (!stringRedisTemplate.hasKey("kill" + id)) {
//            throw new RuntimeException("当前商品的抢购活动已经结束！！");
//        }

        //验证签名
        String hashKey = "KEY_" + userId + "_" + id;
        String s = stringRedisTemplate.opsForValue().get(hashKey);
        if (s == null){
            throw new RuntimeException("没有携带验证签名，请求不合法");
        }
        if (!s.equals(md5)) {
            throw new RuntimeException("当前数据不合法，请稍后再试！");
        }

        //根据商品id校验库存
        Stock stock = checkStock(id);
        //扣除库存
        updateSale(stock);
        //创建订单
        return createOrder(stock);
    }

    @Override
    public String getMd5(Integer id, Integer userId) {
        //验证userId存在用户信息
        User user = userDao.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户信息不存在！");
        }
        log.info("用户信息：[{}]", user.toString());
        //验证id    存在商品信息
        Stock stock = stockDao.checkStock(id);
        if (stock == null) {
            throw new RuntimeException("商品信息不存在");
        }
        log.info("商品信息：[{}]", stock.toString());
        //生成hashKey
        String hashKey = "KEY_" + userId + "_" + id;
        //生成md5签名放入redis服务
        String key = DigestUtils.md5DigestAsHex((userId + id + "!Q*js#").getBytes());
        stringRedisTemplate.opsForValue().set(hashKey, key, 120, TimeUnit.SECONDS);
        log.info("Redis写入信息：[{}] [{}]", hashKey, key);
        return key;
    }

    /**
     * 校验库存
     */
    private Stock checkStock(Integer id) {
        Stock stock = stockDao.checkStock(id);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("库存不足！！！");
        }
        return stock;
    }

    /**
     * 扣除库存
     *
     * @param stock
     */
    private void updateSale(Stock stock) {
        //在sql层面完成销量的+1 和版本号的+1， 并且根据商品id和版本号同时查询更新的商品
        int updateRows = stockDao.updateSale(stock);
        if (updateRows == 0) {
            throw new RuntimeException("抢购失败！");
        }

    }

    /**
     * 创建订单
     *
     * @param stock
     * @return
     */
    private int createOrder(Stock stock) {
        Order order = new Order();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        order.setCreateTime(new Date());
        orderDao.createOrder(order);
        return order.getId();
    }
}
