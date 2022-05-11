package com.openops.client.process;

import com.openops.common.ProtoInstant;
import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;

public class CommandExecuteRequestClientProcessor extends AbstractProcessor {
    public CommandExecuteRequestClientProcessor() {
        super(ProtoInstant.ProcessorType.COMMAND_EXECUTE_REQUEST);
    }

    @Override
    public boolean action(Session session, Object message) {
        return false;
    }
}
