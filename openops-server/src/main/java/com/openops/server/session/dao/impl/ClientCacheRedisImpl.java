package com.openops.server.session.dao.impl;

import com.openops.server.session.entity.ClientCache;
import com.openops.util.JsonUtil;
import com.openops.server.session.dao.ClientCacheDAO;
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
@Repository("ClientCacheRedisImpl")
public class ClientCacheRedisImpl implements ClientCacheDAO {
    // 缓存过期时间，120s+随机值
    private static final int TIMEOUT = 120;
    private static final String REDIS_PREFIX = "ClientCache:ClientId:";
    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(final ClientCache clientCache) {
        String key = REDIS_PREFIX + clientCache.getClientId();
        String value = JsonUtil.pojoToJson(clientCache);
        stringRedisTemplate.opsForValue().set(key, value, TIMEOUT + RandomUtil.randomInt(0, 120), TimeUnit.SECONDS);
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
    public List<ClientCache> multiGet(List<String> clientIds) {
        if(clientIds == null || clientIds.isEmpty()) {
            return null;
        }

        List<String> keys = new ArrayList<>(clientIds.size());
        for(String clientId : clientIds) {
            keys.add(REDIS_PREFIX + clientId);
        }

        List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);

        List<ClientCache> clientCaches = new ArrayList<>(values.size());
        if (!values.isEmpty()) {
            for(String value : values) {
                clientCaches.add(JsonUtil.jsonToPojo(value, ClientCache.class));
            }
        }

        return clientCaches;
    }

    @Override
    public void remove(final String clientId) {
        String key = REDIS_PREFIX + clientId;
        stringRedisTemplate.delete(key);
    }

    @Override
    public void flush(String clientId) {
        String key = REDIS_PREFIX + clientId;
        stringRedisTemplate.expire(key, TIMEOUT + RandomUtil.randomInt(0, 120), TimeUnit.SECONDS);
    }

}