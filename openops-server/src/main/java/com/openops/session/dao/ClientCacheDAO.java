package com.openops.session.dao;

import com.openops.session.entity.ClientCache;
import com.openops.session.entity.SessionCache;

public interface ClientCacheDAO {
    // 保存用户缓存
    void save(ClientCache clientCache);

    // 获取用户缓存
    ClientCache get(String clientId);

    // 删除用户缓存
    void remove(String clientId);
}
