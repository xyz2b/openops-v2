package com.openops.server.process;

import com.openops.common.msg.AuthResponse;
import com.openops.common.msg.ProtoMsgFactory;
import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;
import com.openops.common.Client;
import com.openops.common.ProtoInstant;
import com.openops.common.exception.InvalidMsgTypeException;
import com.openops.server.builder.AuthResponseMsgBuilder;
import com.openops.server.session.LocalSession;
import com.openops.server.session.service.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("AuthRequestProcessor")
public class AuthRequestProcessor extends AbstractProcessor {
    public AuthRequestProcessor() {
        super(ProtoInstant.ProcessorType.AUTH);
    }

    @Override
    public boolean action(Session session, Object message) throws InvalidMsgTypeException {
        if (message instanceof ProtoMsgFactory.ProtoMsg.Message) {
            ProtoMsgFactory.ProtoMsg.Message msg = (ProtoMsgFactory.ProtoMsg.Message) message;

            checkMsgType(msg);

            // 取出token进行验证
            ProtoMsgFactory.ProtoMsg.AuthRequest info = msg.getAuthRequest();

            Client client = new Client(info.getClientId(), info.getClientVersion(), info.getToken(), info.getPlatform(), session.sessionId());

            long seqNo = msg.getSequence();
            if (!checkToken(info.getToken())) {
                ProtoInstant.AuthResultCode resultCode =
                        ProtoInstant.AuthResultCode.NO_TOKEN;
                Object response = new AuthResponseMsgBuilder(client, new AuthResponse(resultCode, "token验证失败")).build();
                //发送之后，断开连接
                session.send(response);
                return false;
            }

            // 将Session、Client关联
            // 不管这个连接的Client是其他节点，还是真正的客户端，都会加入到LocalSession
            LocalSession localSession = (LocalSession) session;

            localSession.setClient(client);

            localSession.bind();

            SessionManager.getSessionManger().addLocalSession(localSession);

            ProtoInstant.AuthResultCode resultCode = ProtoInstant.AuthResultCode.SUCCESS;
            Object response = new AuthResponseMsgBuilder(client, new AuthResponse(resultCode, "")).build();
            session.send(response);
            return true;
        }
        return false;
    }

    private boolean checkToken(String token) {
        return token.equals("f0Bo7qXvXjj-3hCdFAh1E");
    }

    private void checkMsgType(ProtoMsgFactory.ProtoMsg.Message msg) throws InvalidMsgTypeException {
       if(msg.getType() != ProtoMsgFactory.ProtoMsg.HeadType.AUTH_REQUEST) {
           throw new InvalidMsgTypeException("消息类型错误");
       }
    }
}
