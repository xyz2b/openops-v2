package com.openops.common.sender;

import com.openops.common.session.Session;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSender implements Sender {
    protected final Session session;

    public abstract void sendMsg(Object message);

    public AbstractSender(Session session) {
        this.session = session;
    }

    public void send(Object message) {
        sendMsg(message);
    }
}
