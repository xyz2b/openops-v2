package com.openops.server.session.dao.impl;

import com.openops.server.session.entity.ClientCache;
import com.openops.server.session.entity.SessionCache;
import com.openops.util.JsonUtil;
import com.openops.server.session.dao.SessionCacheDAO;
import com.openops.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository("SessionCacheRedisImpl")
public class SessionCacheRedisImpl implements SessionCacheDAO {
    // 缓存过期时间，120s+随机值
    private static final int TIMEOUT = 120;
    private static final String REDIS_PREFIX = "SessionCache:SessionId:";
    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(final SessionCache sessionCache) {
        String key = REDIS_PREFIX + sessionCache.getSessionId();
        String value = JsonUtil.pojoToJson(sessionCache);
        stringRedisTemplate.opsForValue().set(key, value, TIMEOUT + RandomUtil.randomInt(0, 120), TimeUnit.SECONDS);
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
    public List<SessionCache> multiGet(List<String> sessionIds) {
        if(sessionIds == null || sessionIds.isEmpty()) {
            return null;
        }

        List<String> keys = new ArrayList<>(sessionIds.size());
        for(String sessionId : sessionIds) {
            keys.add(REDIS_PREFIX + sessionId);
        }

        List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);

        List<SessionCache> sessionCaches = new ArrayList<>(values.size());
        if (!values.isEmpty()) {
            for(String value : values) {
                sessionCaches.add(JsonUtil.jsonToPojo(value, SessionCache.class));
            }
        }

        return sessionCaches;
    }

    @Override
    public void remove(String sessionId) {
        String key = REDIS_PREFIX + sessionId;
        stringRedisTemplate.delete(key);
    }

    @Override
    public void flush(String sessionId) {
        String key = REDIS_PREFIX + sessionId;
        stringRedisTemplate.expire(key, TIMEOUT + RandomUtil.randomInt(0, 120), TimeUnit.SECONDS);
    }

}