package com.openops.handler;

import com.google.gson.reflect.TypeToken;
import com.openops.common.ServerConstants;
import com.openops.common.msg.Notification;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.distributed.Node;
import com.openops.session.LocalSession;
import com.openops.session.entity.SessionCache;
import com.openops.session.service.SessionManager;
import com.openops.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("RemoteNotificationHandler")
@ChannelHandler.Sharable
public class RemoteNotificationHandler extends ChannelInboundHandlerAdapter {
    /**
     * 收到消息
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;

        //取得请求类型,如果不是通知消息，直接跳过
        ProtoMsg.HeadType headType = pkg.getType();

        if (!headType.equals(ProtoMsg.HeadType.MESSAGE_NOTIFICATION)) {
            ctx.fireChannelRead(msg);
            return;
        }

        //处理消息的内容
        ProtoMsg.MessageNotification notificationPkg = pkg.getMessageNotification();
        String json = notificationPkg.getJson();

        log.info("收到通知, json={}", json);
        Notification notification = JsonUtil.jsonToPojo(json, Notification.class);

         //下线的通知
        if (notification.getType() == Notification.SESSION_OFF) {
            String clientId = JsonUtil.jsonToPojo((String) notification.getData(), String.class);

            log.info("收到用户下线通知, cid={}", clientId);
            SessionManager.getSessionManger().removeRemoteSession(clientId);
        }
        //上线的通知
        if (notification.getType() == Notification.SESSION_ON) {
            SessionCache sessionCache = JsonUtil.jsonToPojo((String) notification.getData(), SessionCache.class);
            log.info("收到用户上线通知, cid={}", sessionCache.getClientId());

            SessionManager.getSessionManger().addRemoteSession(sessionCache);
        }


        //节点的链接成功
        if (notification.getType() == Notification.CONNECT_FINISHED) {

            Notification<Node> nodInfo = JsonUtil.jsonToPojo(json, new TypeToken<Notification<Node>>() {}.getType());

            log.info("收到分布式节点连接成功通知, node={}", json);

            //ctx.pipeline().remove("loginRequest");
            ctx.pipeline().remove("login");
            ctx.channel().attr(ServerConstants.CHANNEL_NAME).set(JsonUtil.pojoToJson(nodInfo));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LocalSession session = LocalSession.getSession(ctx);

        if (null != session) {
            session.unbind();
        }
    }
}
