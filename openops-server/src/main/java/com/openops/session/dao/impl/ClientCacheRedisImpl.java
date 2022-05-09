package com.openops.session.dao.impl;

import com.openops.session.dao.ClientCacheDAO;
import com.openops.session.entity.ClientCache;
import com.openops.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository("ClientCacheRedisImpl")
public class ClientCacheRedisImpl implements ClientCacheDAO {
    public static final String REDIS_PREFIX = "ClientCache:ClientId:";
    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(final ClientCache clientCache) {
        String key = REDIS_PREFIX + clientCache.getClientId();
        String value = JsonUtil.pojoToJson(clientCache);
        // TODO: 设置过期时间，收到心跳自动续期
        stringRedisTemplate.opsForValue().set(key, value);
    }


    @Override
    public ClientCache get(final String clientId) {
        String key = REDIS_PREFIX + clientId;
        String value = stringRedisTemplate.opsForValue().get(key);

        if (!StringUtils.isEmpty(value)) {
            return JsonUtil.jsonToPojo(value, ClientCache.class);
        }
        return null;
    }

    @Override
    public void remove(final String clientId) {
        String key = REDIS_PREFIX + clientId;
        stringRedisTemplate.delete(key);
    }

}