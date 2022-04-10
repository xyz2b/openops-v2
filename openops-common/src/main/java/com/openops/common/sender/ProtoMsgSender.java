package com.openops.common.sender;

import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.common.session.Session;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtoMsgSender extends AbstractSender {
    protected ProtoMsgSender(Session session) {
        super(session);
    }

    public void send(ProtoMsg.Message msg) {
        super.send(msg);
    }

    @Override
    protected void sendSucceed(Object message) {
        if (message instanceof ProtoMsg.Message) {
            ProtoMsg.Message msg = (ProtoMsg.Message) message;

            log.info(msg.getType() + "发送成功");
        }
    }

    @Override
    protected void sendCancel(Object message) {
        if (message instanceof ProtoMsg.Message) {
            ProtoMsg.Message msg = (ProtoMsg.Message) message;

            log.info(msg.getType() + "发送取消");
        }
    }

    @Override
    protected void sendFailed(Object message, Throwable t) {
        if (message instanceof ProtoMsg.Message) {
            ProtoMsg.Message msg = (ProtoMsg.Message) message;

            log.info(msg.getType() + "发送出现异常");
        }
    }
}
