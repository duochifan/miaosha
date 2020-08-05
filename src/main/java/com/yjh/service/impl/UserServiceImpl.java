package com.yjh.service.impl;

import com.yjh.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * @Author mua
 * @Date 2020/8/5 13:57
 */
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public int saveUserCount(Integer userId) {
        //根据不同用户id生成调用次数的key
        String limitKey = "LIMIT_" + userId;
        //获取redis中指定Key的调用次数
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        int limit = -1;
        if (limitNum == null) {
            //第一次调用,放入redis中设置为0
            stringRedisTemplate.opsForValue().set(limitKey, "0", 3600, TimeUnit.SECONDS);
        } else {
            //不是第一次调用,每次加一
            limit = Integer.valueOf(limitNum) + 1;
            stringRedisTemplate.opsForValue().set(limitKey, String.valueOf(limit), 3600, TimeUnit.SECONDS);
        }
        //返回调用次数
        return limit;
    }

    @Override
    public boolean getUserCount(Integer userId) {
        String limitKey = "LIMIT_" + userId;
        //根据用户调用次数的key获取redis中调用次数
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        if (limitNum == null) {
            //为空直接抛弃说明key出现异常
            log.error("该用户没有访问申请验证值记录，疑似异常");
            return true;
        }
        //false代表没超过
        return Integer.parseInt(limitNum) > 10;
    }
}
