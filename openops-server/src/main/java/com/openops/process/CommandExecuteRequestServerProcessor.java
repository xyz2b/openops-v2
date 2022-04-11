package com.openops.process;

import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;

// 接收apiserver请求的处理器
// apiserver的请求，client_id为0.0.0.0，通过client_id判断是apiserver发送的请求还是真实client发送的请求
public class CommandExecuteRequestServerProcessor extends AbstractProcessor {
    public CommandExecuteRequestServerProcessor(int type) {
        super(type);
    }

    @Override
    public boolean action(Session session, Object message) {
        return false;
    }
}
