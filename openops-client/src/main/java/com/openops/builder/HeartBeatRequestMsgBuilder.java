package com.openops.builder;

import com.openops.common.builder.ProtoBufMsgBuilder;

public class HeartBeatRequestMsgBuilder extends ProtoBufMsgBuilder {
    public HeartBeatRequestMsgBuilder(int type, String clientId) {
        super(type, clientId);
    }

    @Override
    protected Object buildMsgInner() {
        return null;
    }
}
