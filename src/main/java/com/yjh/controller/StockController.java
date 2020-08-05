package com.yjh.controller;

        import com.google.common.util.concurrent.RateLimiter;
        import com.yjh.service.OrderService;
        import com.yjh.service.UserService;
        import lombok.extern.slf4j.Slf4j;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;

        import java.util.concurrent.TimeUnit;

/**
 * @Author mua
 * @Date 2020/8/4 9:42
 */
@RestController
@RequestMapping("stock")
@Slf4j
public class StockController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;


    /**
     * 创建令牌桶实例
     */
    private RateLimiter rateLimiter = RateLimiter.create(10);

    /**
     * 令牌桶基本实现
     *
     * @param id
     * @return
     */
    @GetMapping("sale")
    public String sale(Integer id) {
        //1.没有获取到token请求，一直请求直到获取到token令牌
        //log.info("等待的时间：" + rateLimiter.acquire());
        //2.设置一个等待时间
        if (!rateLimiter.tryAcquire(2, TimeUnit.SECONDS)) {
            System.out.println("当前请求被限流，无法参与秒杀");
            return "秒杀失败";
        }
        System.out.println("处理业务............");
        return "秒杀成功";
    }

    /**
     * 秒杀方法，使用乐观锁防止超卖
     *
     * @param id
     * @return
     */
    @GetMapping("/kill")
    public String kill(Integer id) {
        System.out.println("秒杀商品的id=" + id);
        try {
            //根据秒杀商品id调用秒杀业务
            int orderId = orderService.kill(id);
            return "秒杀成功，订单id为：" + orderId;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }

    }

    /**
     * 秒杀方法，使用乐观锁防止超卖 + 令牌桶限流 + md5签名 +单用户访问频率限制
     * @param id
     * @return
     */
    @GetMapping("/killtokenmd5limit")
    public String killtokenmd5limit(Integer id,Integer userId,String md5) {
        //加入令牌桶的限流措施
        if (!rateLimiter.tryAcquire(3, TimeUnit.SECONDS)) {
            log.info("抛弃请求：抢购失败，当前活动过于火爆，请重试");
            System.out.println("抢购失败，当前活动过于火爆，请重试！");
        }
        try {
            //加入单用户限制调用频率
            int count = userService.saveUserCount(userId);
            log.info("用户截至该次的访问次数为：[{}]",count);
            //进行调用次数判断
            boolean isBanned = userService.getUserCount(userId);
            if (isBanned){
                log.info("购买失败，超过频率限制!");
                return "购买失败，超过频率限制!";
            }

            //根据秒杀商品id调用秒杀业务
            int orderId = orderService.kill(id,userId,md5);
            return "秒杀成功，订单id为：" + orderId;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * 秒杀方法，使用乐观锁防止超卖 + 令牌桶限流 + md5签名
     * @param id
     * @return
     */
    @GetMapping("/killtokenmd5")
    public String killtokenmd5(Integer id,Integer userId,String md5) {
        System.out.println("秒杀商品的id=" + id);
        //加入令牌桶的限流措施
        if (!rateLimiter.tryAcquire(3, TimeUnit.SECONDS)) {
            log.info("抛弃请求：抢购失败，当前活动过于火爆，请重试");
            System.out.println("抢购失败，当前活动过于火爆，请重试！");
        }
        try {
            //根据秒杀商品id调用秒杀业务
            int orderId = orderService.kill(id,userId,md5);
            return "秒杀成功，订单id为：" + orderId;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * 秒杀方法，使用乐观锁防止超卖+令牌桶限流
     * @param id
     * @return
     */
    @GetMapping("/killtoken")
    public String killtoken(Integer id) {
        System.out.println("秒杀商品的id=" + id);
        //加入令牌桶的限流措施
        if (!rateLimiter.tryAcquire(3, TimeUnit.SECONDS)) {
            System.out.println("抢购失败，当前活动过于火爆，请重试！");
        }
        try {
            //根据秒杀商品id调用秒杀业务
            int orderId = orderService.kill(id);
            return "秒杀成功，订单id为：" + orderId;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * 生成md5
     * @param id
     * @param userId
     * @return
     */
    @GetMapping("/md5")
    public String getMd5(Integer id,Integer userId){
        String md5;
        try {
            md5 = orderService.getMd5(id,userId);
        } catch (Exception e) {
            e.printStackTrace();
            return "获取MD5失败:"+e.getMessage();
        }
        return "获取MD5信息为："+md5;
    }
}
