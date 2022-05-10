package com.openops.session;

import com.openops.session.service.SessionManager;
import com.openops.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalSession extends ServerSession {
    public static final AttributeKey<ServerSession> SESSION_KEY =
            AttributeKey.valueOf("SESSION_KEY");
    public static final AttributeKey<String> CHANNEL_NAME =
            AttributeKey.valueOf("CHANNEL_NAME");

    public LocalSession(Channel channel) {
        super(channel);
    }

    public LocalSession bind() {
        Channel channel = channel();
        log.info(" LocalSession 绑定会话 " + channel.remoteAddress());
        // 通过channel找到session
        channel.attr(LocalSession.SESSION_KEY).set(this);
        channel.attr(LocalSession.CHANNEL_NAME).set(JsonUtil.pojoToJson(client()));
        setLogin(true);
        return this;
    }

    public static LocalSession getSession(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        return (LocalSession) channel.attr(LocalSession.SESSION_KEY).get();
    }

    public LocalSession unbind() {
        setLogin(false);
        SessionManager.getSessionManger().removeLocalSession(sessionId());
        this.close();
        return this;
    }
}
