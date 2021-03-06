package com.openops.server.handler;

import com.google.gson.reflect.TypeToken;
import com.openops.common.ServerConstants;
import com.openops.common.msg.Notification;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.server.distributed.Node;
import com.openops.server.session.LocalSession;
import com.openops.server.session.entity.SessionCache;
import com.openops.server.session.service.SessionManager;
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

        // 取得请求类型, 如果不是通知消息，直接跳过
        ProtoMsg.HeadType headType = pkg.getType();

        if (!headType.equals(ProtoMsg.HeadType.MESSAGE_NOTIFICATION)) {
            ctx.fireChannelRead(msg);
            return;
        }

        // 处理消息的内容
        ProtoMsg.MessageNotification notificationPkg = pkg.getMessageNotification();
        String json = notificationPkg.getJson();

        log.info("收到通知, json={}", json);
        Notification notification = JsonUtil.jsonToPojo(json, Notification.class);

        // 别的节点所管理的Client下线的通知
        if (notification.getType() == Notification.SESSION_OFF) {
            Notification<SessionCache> sessionCacheInfo = JsonUtil.jsonToPojo(json, new TypeToken<Notification<SessionCache>>() {}.getType());

            SessionCache sessionCache = sessionCacheInfo.getData();

            log.info("收到客户端下线通知, cid={}", sessionCache.getClientId());
            SessionManager.getSessionManger().removeRemoteSession(sessionCache.getSessionId());
        }
        // 别的节点所管理的Client上线的通知
        if (notification.getType() == Notification.SESSION_ON) {
            Notification<SessionCache> sessionCacheInfo = JsonUtil.jsonToPojo(json, new TypeToken<Notification<SessionCache>>() {}.getType());

            SessionCache sessionCache = sessionCacheInfo.getData();
            log.info("收到客户端上线通知, cid={}", sessionCache.getClientId());

            SessionManager.getSessionManger().addRemoteSession(sessionCache);
        }

        // 别的节点连接本节点成功的通知
        if (notification.getType() == Notification.CONNECT_FINISHED) {
            // 获取对端的节点信息
            Notification<Node> nodInfo = JsonUtil.jsonToPojo(json, new TypeToken<Notification<Node>>() {}.getType());
            Node node = nodInfo.getData();

            log.info("收到分布式节点连接成功通知, node={}", json);

//            ctx.pipeline().remove("login");
            // 用对端的节点信息来表示这条连接
            ctx.channel().attr(ServerConstants.CHANNEL_NAME).set(JsonUtil.pojoToJson(node));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LocalSession session = LocalSession.getSession(ctx);

        if (null != session && session.isValid()) {
            session.unbind();
        }
    }
}
