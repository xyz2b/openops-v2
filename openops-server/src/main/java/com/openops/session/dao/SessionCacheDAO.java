package com.openops.session.dao;


import com.openops.session.entity.SessionCache;

public interface SessionCacheDAO {
    //保存会话到缓存
    void save(SessionCache s);

    //从缓存获取会话
    SessionCache get(String sessionId);

    //删除会话
    void remove(String sessionId);

}