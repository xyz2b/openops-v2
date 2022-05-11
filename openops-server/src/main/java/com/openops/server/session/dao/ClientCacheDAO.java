package com.openops.server.session.dao;

import com.openops.server.session.entity.ClientCache;

public interface ClientCacheDAO {
    // 保存用户缓存
    void save(ClientCache clientCache);

    // 获取用户缓存
    ClientCache get(String clientId);

    // 删除用户缓存
    void remove(String clientId);
}
