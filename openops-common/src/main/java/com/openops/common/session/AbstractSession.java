package com.openops.common.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSession implements Session {
    private final Channel channel;
    private final String clientId;

    private boolean connected;
    private boolean logged;

    public AbstractSession(Channel channel, String clientId) {
        this.channel = channel;
        this.clientId = clientId;
    }

    public String clientId() {
        return clientId;
    }

    public ChannelFuture writeAndFlush(Object pkg) {
        return channel.writeAndFlush(pkg);
    }

    public ChannelFuture close() {
        return channel.close();
    }

    public boolean isConnected() {
        return connected;
    }

    public void connected(boolean connected) {
        this.connected = connected;
    }

    public boolean isLogin() {
        return logged;
    }

    public void logged(boolean logged) {
        this.logged = logged;
    }

    public boolean isValid() {
        return !clientId.isEmpty();
    }
}
