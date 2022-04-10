package com.openops.common.builder;

import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;

public class TestBuilder extends ProtoBufMsgBuilder {
    private final String token;
    public TestBuilder(int type, String clientId, String token) {
        super(type, clientId);
        this.token = token;
    }

    @Override
    protected Object buildMsgInner() {
        ProtoMsg.AuthRequest.Builder lb =
                ProtoMsg.AuthRequest.newBuilder()
                        .setPlatform("")
                        .setToken(token);
        return lb.buildPartial();
    }

    public static void main(String[] args) {
        TestBuilder testBuilder = new TestBuilder(1, "1", "12323");
        ProtoMsg.Message msg = (ProtoMsg.Message) testBuilder.build();
        System.out.println(msg.getAuthRequest().getToken());

    }
}
