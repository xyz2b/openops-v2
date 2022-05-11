package com.openops.server.session;

import com.openops.common.sender.ProtoMsgSender;
import com.openops.common.session.AbstractSession;
import com.openops.server.session.service.SessionManager;
import com.openops.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientSession extends AbstractSession {
    public static final AttributeKey<ClientSession> SESSION_KEY = AttributeKey.valueOf("SESSION_KEY");

    public ClientSession(Channel channel) {
        super(channel);
        channel.attr(ClientSession.SESSION_KEY).set(this);
        sender = new ProtoMsgSender(this);
        setSessionId(String.valueOf(-1));
    }

    @Override
    public ChannelFuture close() {
        ChannelFuture future = super.close();
        future.addListener(new ChannelFutureListener()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if (future.isSuccess()) {
                    log.error("连接顺利断开");
                }
            }
        });
        return future;
    }

    @Override
    public ChannelFuture writeAndFlush(Object pkg) {
        return super.writeAndFlush(pkg);
    }

    public static ClientSession getSession(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        return (ClientSession) channel.attr(ClientSession.SESSION_KEY).get();
    }

    public ClientSession bind() {
        Channel channel = channel();
        log.info(" ClientSession 绑定会话 " + channel.remoteAddress());
        // 通过channel找到session
        channel.attr(ClientSession.SESSION_KEY).set(this);
        return this;
    }

    public void unbind() {
        setLogin(false);
        this.close();
    }
}
