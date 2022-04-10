package com.openops.builder;

import com.openops.common.builder.ProtoBufMsgBuilder;

public class CommandExecRequestMsgBuilder extends ProtoBufMsgBuilder {
    public CommandExecRequestMsgBuilder(int type, String clientId) {
        super(type, clientId);
    }

    @Override
    protected Object buildMsgInner() {
        return null;
    }
}
