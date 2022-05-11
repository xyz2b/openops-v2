package com.openops.client.process;

import com.openops.common.ProtoInstant;
import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;
import org.springframework.stereotype.Service;

@Service("CommandExecuteRequestClientProcessor")
public class CommandExecuteRequestClientProcessor extends AbstractProcessor {
    public CommandExecuteRequestClientProcessor() {
        super(ProtoInstant.ProcessorType.COMMAND_EXECUTE_REQUEST);
    }

    @Override
    public boolean action(Session session, Object message) {
        return false;
    }
}
