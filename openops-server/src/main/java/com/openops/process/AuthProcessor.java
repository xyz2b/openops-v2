package com.openops.process;

import com.openops.builder.AuthResponseMsgBuilder;
import com.openops.common.Client;
import com.openops.common.ProtoInstant;
import com.openops.common.exception.InvalidMsgTypeException;
import com.openops.common.msg.AuthResponse;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;
import com.openops.session.LocalSession;
import com.openops.session.service.SessionManager;

public class AuthProcessor extends AbstractProcessor {
    public AuthProcessor() {
        super(ProtoInstant.ProcessorType.AUTH);
    }

    @Override
    public boolean action(Session session, Object message) throws InvalidMsgTypeException {
        if (message instanceof ProtoMsg.Message) {
            ProtoMsg.Message msg = (ProtoMsg.Message) message;

            checkMsgType(msg);

            // 取出token进行验证
            ProtoMsg.AuthRequest info = msg.getAuthRequest();

            Client client = new Client(info.getClientId(), info.getClientVersion(), info.getToken(), info.getPlatform(), session.sessionId());

            long seqNo = msg.getSequence();

            if (checkToken(info.getToken())) {
                ProtoInstant.AuthResultCode resultCode =
                        ProtoInstant.AuthResultCode.NO_TOKEN;
                Object response =
                        new AuthResponseMsgBuilder(client, new AuthResponse(resultCode, "token验证失败")).build();
                //发送之后，断开连接
                session.writeAndFlush(response);
                session.close();
                return false;
            }

            LocalSession localSession = (LocalSession) session;

            localSession.setClient(client);

            localSession.bind();

            SessionManager.getSessionManger().addLocalSession(localSession);

            ProtoInstant.AuthResultCode resultCode = ProtoInstant.AuthResultCode.SUCCESS;
            Object response = new AuthResponseMsgBuilder(client, new AuthResponse(resultCode, "")).build();
            session.writeAndFlush(response);
            return true;
        }
        return false;
    }

    private boolean checkToken(String token) {
        return token.equals("f0Bo7qXvXjj-3hCdFAh1E");
    }

    private void checkMsgType(ProtoMsg.Message msg) throws InvalidMsgTypeException {
       if(msg.getType() != ProtoMsg.HeadType.AUTH_REQUEST) {
           throw new InvalidMsgTypeException("消息类型错误");
       }
    }
}
