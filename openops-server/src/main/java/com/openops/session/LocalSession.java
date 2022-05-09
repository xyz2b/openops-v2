package com.openops.session;

import com.openops.util.JsonUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalSession extends ServerSession {
    public LocalSession(Channel channel) {
        super(channel);
    }

    public ServerSession bind() {
        Channel channel = channel();
        log.info(" LocalSession 绑定会话 " + channel.remoteAddress());
        // 通过channel找到session
        channel.attr(ServerSession.SESSION_KEY).set(this);
        channel.attr(ServerSession.CHANNEL_NAME).set(JsonUtil.pojoToJson(client()));
        logged(true);
        return this;
    }


}
