package com.openops.common.session;

import com.openops.common.Client;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSession implements Session {
    private final Channel channel;
    private final Client client;
    private String sessionId;

    private boolean connected;
    private boolean logged;

    public AbstractSession(Channel channel, Client client) {
        this.channel = channel;
        this.client = client;
        connected = true;
        logged = false;
    }

    public Client client() {
        return client;
    }

    public Session session() {
        return this;
    }

    protected void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String clientId() {
        return client.getClientId();
    }

    public String sessionId() {
        return sessionId;
    }

    public ChannelFuture writeAndFlush(Object pkg) {
        return channel.writeAndFlush(pkg);
    }

    public ChannelFuture close() {
        connected = false;
        logged = false;
        return channel.close();
    }

    public boolean isConnected() {
        return connected;
    }

    protected void connected(boolean connected) {
        this.connected = connected;
    }

    public boolean isLogin() {
        return logged;
    }

    protected void logged(boolean logged) {
        this.logged = logged;
    }

    public boolean isValid() {
        return connected && logged;
    }

    protected Channel channel() {
        return channel;
    }
}
