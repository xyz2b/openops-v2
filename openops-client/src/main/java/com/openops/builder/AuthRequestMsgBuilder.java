package com.openops.builder;

import com.openops.common.Client;
import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;

public class AuthRequestMsgBuilder extends ProtoBufMsgBuilder {
    private Client client;

    public AuthRequestMsgBuilder(Client client) {
        super(ProtoMsg.HeadType.AUTH_REQUEST, client.getClientId());
        this.client = client;
    }

    @Override
    protected Object assembleMsgInner() {
        return ProtoMsg.AuthRequest.newBuilder()
                .setPlatform(client.getPlatform())
                .setToken(client.getToken())
                .setClientVersion(client.getClientVersion());
    }
}
