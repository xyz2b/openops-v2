package com.openops.session;

import com.openops.common.session.AbstractSession;
import io.netty.channel.Channel;

public class ClientSession extends AbstractSession {
    public ClientSession(Channel channel, String clientId) {
        super(channel, clientId);
    }

    @Override
    public String sessionId() {
        return null;
    }
}
