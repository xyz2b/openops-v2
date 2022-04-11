package com.openops.process;

import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;

public class CommandExecuteRequestClientProcessor extends AbstractProcessor {
    public CommandExecuteRequestClientProcessor(int type) {
        super(type);
    }

    @Override
    public boolean action(Session session, Object message) {
        return false;
    }
}
