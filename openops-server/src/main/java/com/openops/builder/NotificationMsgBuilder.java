package com.openops.builder;

import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.Notification;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.util.JsonUtil;

public class NotificationMsgBuilder extends ProtoBufMsgBuilder {
    private Notification msg;

    public NotificationMsgBuilder(String clientId, Notification msg) {
        super(ProtoMsg.HeadType.MESSAGE_NOTIFICATION, clientId);
        this.msg = msg;
    }

    @Override
    protected Object assembleMsgInner() {
        String json = JsonUtil.pojoToJson(msg);

        return ProtoMsg.MessageNotification.newBuilder().setMsgType(msg.getType()).setJson(json);
    }
}
