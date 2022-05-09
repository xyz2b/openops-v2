package com.openops.session;

import com.openops.common.Client;
import com.openops.common.sender.ProtoMsgSender;
import com.openops.common.sender.Sender;
import com.openops.common.session.AbstractSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientSession extends AbstractSession {
    public static final AttributeKey<ClientSession> SESSION_KEY = AttributeKey.valueOf("SESSION_KEY");

    public ClientSession(Channel channel) {
        super(channel);
        channel.attr(ClientSession.SESSION_KEY).set(this);
        sender = new ProtoMsgSender(this);
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
}
