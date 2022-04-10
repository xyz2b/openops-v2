package com.openops.builder;

import com.openops.common.builder.ProtoBufMsgBuilder;

public class AuthRequestMsgBuilder extends ProtoBufMsgBuilder {
    public AuthRequestMsgBuilder(int type, String clientId) {
        super(type, clientId);
    }

    @Override
    protected Object buildMsgInner() {
        return null;
    }
}
