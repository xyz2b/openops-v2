package com.openops.session.entity;

import lombok.Data;

@Data
public class ClientCache {
    private String clientId;
    private SessionCache sessionCache;

    public ClientCache(String clientId, SessionCache sessionCache) {
        this.clientId = clientId;
        this.sessionCache = sessionCache;
    }
}
