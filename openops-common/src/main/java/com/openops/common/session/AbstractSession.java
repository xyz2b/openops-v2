package com.openops.common.session;

import com.openops.common.sender.Sender;
import com.openops.common.Client;
import com.openops.util.NanoIdUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSession implements Session {
    // 通过session找到channel
    private final Channel channel;
    private Client client;
    private String sessionId;

    private boolean connected;
    private boolean isLogin;

    protected Sender sender;

    public AbstractSession(Channel channel) {
        this.channel = channel;
        connected = true;
        isLogin = false;
    }

    public void send(Object msg) {
        sender.send(msg);
    }

    public Client client() {
        return client;
    }
    public void setClient(Client client) {
        this.client = client;
    }

    public Session session() {
        return this;
    }

    public void setSessionId(String sessionId) {
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
        isLogin = false;
        return channel.close();
    }

    public boolean isConnected() {
        return connected;
    }

    protected void connected(boolean connected) {
        this.connected = connected;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean logged) {
        this.isLogin = logged;
    }

    public boolean isValid() {
        return connected && isLogin;
    }

    protected Channel channel() {
        return channel;
    }

    private static String buildNewSessionId() {
        return NanoIdUtils.randomNanoId();
    }
}
