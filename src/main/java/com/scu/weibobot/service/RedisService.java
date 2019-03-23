package com.scu.weibobot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "com:scu:weibobot";


    //=============================common============================

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        String actualKey = KEY_PREFIX + key;
        try {
            if (time > 0) {
                redisTemplate.expire(actualKey, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        String actualKey = KEY_PREFIX + key;
        return redisTemplate.getExpire(actualKey, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            String actualKey = KEY_PREFIX + key;
            return redisTemplate.hasKey(actualKey);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param keys 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... keys) {
        if (keys != null && keys.length > 0) {
            for (String key : keys) {
                String actualKey = KEY_PREFIX + key;
                redisTemplate.delete(actualKey);
            }
        }
    }

    //============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        if (key == null) {
            return null;
        }
        String actualKey = KEY_PREFIX + key;
        return redisTemplate.opsForValue().get(actualKey);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            String actualKey = KEY_PREFIX + key;
            redisTemplate.opsForValue().set(actualKey, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        String actualKey = KEY_PREFIX + key;
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(actualKey, value, time, TimeUnit.SECONDS);
            } else {
                set(actualKey, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta) {
        String actualKey = KEY_PREFIX + key;
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(actualKey, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) {
        String actualKey = KEY_PREFIX + key;
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(actualKey, -delta);
    }

    // ============================ Set =============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return 295
     */
    public Set<Object> sGet(String key) {
        String actualKey = KEY_PREFIX + key;
        try {
            return redisTemplate.opsForSet().members(actualKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            String actualKey = KEY_PREFIX + key;
            return redisTemplate.opsForSet().isMember(actualKey, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            String actualKey = KEY_PREFIX + key;
            return redisTemplate.opsForSet().add(actualKey, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            String actualKey = KEY_PREFIX + key;
            Long count = redisTemplate.opsForSet().add(actualKey, values);
            if (time > 0){
                expire(key, time);
            }

            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            String actualKey = KEY_PREFIX + key;
            return redisTemplate.opsForSet().size(actualKey);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        try {
            String actualKey = KEY_PREFIX + key;
            Long count = redisTemplate.opsForSet().remove(actualKey, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
