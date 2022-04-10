package com.openops.sender;

import com.openops.common.sender.AbstractSender;
import com.openops.common.session.Session;

public class AuthRequestSender extends AbstractSender {
    protected AuthRequestSender(Session session) {
        super(session);
    }

    @Override
    public void send(Object message) {

    }

    @Override
    protected void sendSucceed(Object message) {
        super.sendSucceed(message);
    }

    @Override
    protected void sendFailed(Object message, Throwable t) {
        super.sendFailed(message, t);
    }

    @Override
    protected void sendCancel(Object message) {
        super.sendCancel(message);
    }
}
