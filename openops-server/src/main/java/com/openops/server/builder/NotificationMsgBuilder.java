package com.openops.server.builder;

import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.Notification;
import com.openops.common.msg.ProtoMsgFactory;
import com.openops.util.JsonUtil;

public class NotificationMsgBuilder extends ProtoBufMsgBuilder {
    private Notification msg;

    public NotificationMsgBuilder(String sessionId, Notification msg) {
        super(ProtoMsgFactory.ProtoMsg.HeadType.MESSAGE_NOTIFICATION, sessionId);
        this.msg = msg;
    }

    @Override
    protected Object assembleMsgInner() {
        String json = JsonUtil.pojoToJson(msg);

        return ProtoMsgFactory.ProtoMsg.MessageNotification.newBuilder().setMsgType(msg.getType()).setJson(json);
    }
}
