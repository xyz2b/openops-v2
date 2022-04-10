package com.openops.builder;

import com.openops.common.builder.ProtoBufMsgBuilder;

public class AuthResponseMsgBuilder extends ProtoBufMsgBuilder {
    public AuthResponseMsgBuilder(int type, String clientId) {
        super(type, clientId);
    }

    @Override
    protected Object buildMsgInner() {
        return null;
    }
}
