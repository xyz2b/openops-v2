package com.openops.process;

import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;

public class CommandExecuteRequestServerProcessor extends AbstractProcessor {
    public CommandExecuteRequestServerProcessor(int type) {
        super(type);
    }

    @Override
    public boolean action(Session session, Object message) {
        return false;
    }
}
