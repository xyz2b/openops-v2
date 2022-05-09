package com.openops.builder;

import com.openops.common.Client;
import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.AuthResponse;
import com.openops.common.msg.ProtoMsgFactory;

public class AuthResponseMsgBuilder extends ProtoBufMsgBuilder {
    private AuthResponse authResponse;

    public AuthResponseMsgBuilder(Client client, AuthResponse authResponse) {
        super(ProtoMsgFactory.ProtoMsg.HeadType.AUTH_RESPONSE, client.getClientId());
        this.authResponse = authResponse;
    }

    @Override
    protected Object assembleMsgInner() {
        return ProtoMsgFactory.ProtoMsg.AuthResponse.newBuilder()
                .setCode(authResponse.getCode().ordinal())
                .setInfo(authResponse.getInfo());
    }
}
