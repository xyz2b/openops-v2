package com.openops.common.session;

import io.netty.channel.ChannelFuture;

public interface Session {
    String sessionId();
    String clientId();

    ChannelFuture writeAndFlush(Object pkg);
    ChannelFuture close();

    boolean isValid();
    boolean isConnected();
    boolean isLogin();
}
