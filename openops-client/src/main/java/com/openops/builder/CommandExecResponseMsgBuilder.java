package com.openops.builder;

import com.openops.common.builder.ProtoBufMsgBuilder;

public class CommandExecResponseMsgBuilder extends ProtoBufMsgBuilder {
    public CommandExecResponseMsgBuilder(int type, String clientId) {
        super(type, clientId);
    }

    @Override
    protected Object buildMsgInner() {
        return null;
    }
}
