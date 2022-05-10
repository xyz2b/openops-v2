package com.openops.session;

import com.openops.common.Client;
import com.openops.common.sender.ProtoMsgSender;
import com.openops.common.session.AbstractSession;
import com.openops.util.JsonUtil;
import com.openops.util.NanoIdUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerSession extends AbstractSession {
    public ServerSession(Channel channel) {
        super(channel);
        if (channel != null) {
            super.setSessionId(NanoIdUtils.randomNanoId());
            sender = new ProtoMsgSender(this);
        }
    }

    @Override
    public ChannelFuture writeAndFlush(Object pkg) {
        return super.writeAndFlush(pkg);
    }

    @Override
    public ChannelFuture close() {
        ChannelFuture future = super.close();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    log.error("CHANNEL_CLOSED error ");
                }
            }
        });
        return future;
    }
}
