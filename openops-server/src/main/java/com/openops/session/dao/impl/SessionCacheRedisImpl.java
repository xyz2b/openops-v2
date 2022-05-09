package com.openops.session.dao.impl;

import com.openops.session.dao.SessionCacheDAO;
import com.openops.session.entity.SessionCache;
import com.openops.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Repository("SessionCacheRedisImpl")
public class SessionCacheRedisImpl implements SessionCacheDAO {
    public static final String REDIS_PREFIX = "SessionCache:SessionId:";
    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(final SessionCache sessionCache) {
        String key = REDIS_PREFIX + sessionCache.getSessionId();
        String value = JsonUtil.pojoToJson(sessionCache);
        // TODO: 设置过期时间，收到心跳自动续期
        stringRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public SessionCache get(final String sessionId) {
        String key = REDIS_PREFIX + sessionId;
        String value = (String) stringRedisTemplate.opsForValue().get(key);

        if (!StringUtils.isEmpty(value))
        {
            return JsonUtil.jsonToPojo(value, SessionCache.class);
        }
        return null;
    }

    @Override
    public void remove(String sessionId) {
        String key = REDIS_PREFIX + sessionId;
        stringRedisTemplate.delete(key);
    }

}