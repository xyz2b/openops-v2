package com.openops.process;

import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;

// 接收apiserver请求的处理器
public class CommandExecuteRequestServerProcessor extends AbstractProcessor {
    public CommandExecuteRequestServerProcessor(int type) {
        super(type);
    }

    @Override
    public boolean action(Session session, Object message) {
        return false;
    }
}
