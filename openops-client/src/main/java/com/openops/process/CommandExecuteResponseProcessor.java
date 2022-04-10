package com.openops.process;

import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;

public class CommandExecuteResponseProcessor extends AbstractProcessor {
    public CommandExecuteResponseProcessor(int type) {
        super(type);
    }

    @Override
    public boolean action(Session session, Object message) {
        return false;
    }
}
