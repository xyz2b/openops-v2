package com.openops.common.builder;

import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;

public class TestBuilder extends ProtoBufMsgBuilder {
    public TestBuilder(int type, String clientId) {
        super(type, clientId);
    }

    @Override
    protected Object buildInner() {
        ProtoMsg.AuthRequest.Builder lb =
                ProtoMsg.AuthRequest.newBuilder()
                        .setPlatform("")
                        .setToken("");
        return lb.buildPartial();
    }

    @Override
    protected Object buildMsg() {
        ProtoMsg.Message message = buildMsgOuter(1);
        Object innerMsg = buildInner();
        if (innerMsg instanceof ProtoMsg.AuthRequest) {
            return message.toBuilder().setAuthRequest(((ProtoMsg.AuthRequest) innerMsg).toBuilder()).build();
        }
        return null;
    }
}
