package com.openops.common.session;

import com.openops.common.Client;
import io.netty.channel.ChannelFuture;

public interface Session {
    Session session();
    Client client();

    ChannelFuture writeAndFlush(Object pkg);
    ChannelFuture close();

    boolean isValid();
    boolean isConnected();
    boolean isLogin();
}
