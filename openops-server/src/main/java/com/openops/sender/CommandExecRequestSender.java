package com.openops.sender;

import com.openops.common.sender.AbstractSender;
import com.openops.common.session.Session;

// 仅用于注册不同的回调函数
public class CommandExecRequestSender extends AbstractSender {
    protected CommandExecRequestSender(Session session) {
        super(session);
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