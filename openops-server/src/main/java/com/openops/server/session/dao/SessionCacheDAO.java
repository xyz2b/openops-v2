package com.openops.server.session.dao;

import com.openops.server.session.entity.SessionCache;

import java.util.List;

public interface SessionCacheDAO {
    //保存会话到缓存
    void save(SessionCache s);

    //从缓存获取会话
    SessionCache get(String sessionId);

    // 批量获取会话缓存
    List<SessionCache> multiGet(List<String> sessionIds);

    //删除会话
    void remove(String sessionId);

    // 刷新会话缓存
    void flush(String clientId);
}
