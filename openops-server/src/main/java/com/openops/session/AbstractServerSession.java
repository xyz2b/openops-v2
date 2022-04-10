package com.openops.session;

import com.openops.common.session.AbstractSession;
import io.netty.channel.Channel;

public abstract class AbstractServerSession extends AbstractSession {
    public AbstractServerSession(Channel channel, String clientId) {
        super(channel, clientId);
    }
}
