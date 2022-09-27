package com.xxj.qqbot.util.common.redis.children;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * String操作工具
 */
@Component
public class StringUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 设置指定 key 的值
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

//    /**
//     * 通过枚举值和键自身的值形成总键 + 值存储
//     *
//     * @param enumKey  枚举值
//     * @param keyValue 键自身的值
//     * @param value    值
//     */
//    public void setByEnum(String enumKey, String keyValue, String value) {
//        set(enumKey + keyValue, value);
//    }

    /**
     * 获取指定 key 的值
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

//    /**
//     * 通过枚举值和键自身的值形成总键获取
//     *
//     * @param enumKey  枚举值
//     * @param keyValue 键自身的值
//     * @return 总键的值
//     */
//    public String getByEnum(String enumKey, String keyValue) {
//        return get(enumKey + keyValue);
//    }

    /**
     * 将给定 key 的值设为 value 并返回 old value
     */
    public String getAndSet(String key, String value) {
        return stringRedisTemplate.opsForValue().getAndSet(key, value);
    }

    /**
     * 批量获取
     */
    public List<String> multiGet(Collection<String> keys) {
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 设置键值对并将 key 的过期时间设为 timeout
     */
    public void setEx(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * key 不存在时设置值 已存在 false 不存在 true
     */
    public boolean setIfAbsent(String key, String value) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 根据 key 获取字符串长度
     */
    public Long size(String key) {
        return stringRedisTemplate.opsForValue().size(key);
    }

    /**
     * 批量添加
     */
    public void multiSet(Map<String, String> maps) {
        stringRedisTemplate.opsForValue().multiSet(maps);
    }

    /**
     * 当且仅当 所有 给定 key 都不存在时批量添加
     */
    public boolean multiSetIfAbsent(Map<String, String> maps) {
        return stringRedisTemplate.opsForValue().multiSetIfAbsent(maps);
    }

    /**
     * 增加或自减
     */
    public Long incrBy(String key, long increment) {
        return stringRedisTemplate.opsForValue().increment(key, increment);
    }

    /**
     * 末尾追加
     */
    public Integer append(String key, String value) {
        return stringRedisTemplate.opsForValue().append(key, value);
    }

}