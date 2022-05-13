package com.openops.server.session.dao;

import com.openops.server.session.entity.ClientCache;

import java.util.List;

public interface ClientCacheDAO {
    // 保存用户缓存
    void save(ClientCache clientCache);

    // 获取用户缓存
    ClientCache get(String clientId);

    // 批量获取用户缓存
    List<ClientCache> multiGet(List<String> clientIds);

    // 删除用户缓存
    void remove(String clientId);

    // 刷新用户缓存
    void flush(String clientId);
}
