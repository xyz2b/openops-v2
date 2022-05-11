package com.openops.server.session;

import com.openops.common.msg.ProtoMsgFactory;
import com.openops.server.distributed.Node;
import com.openops.server.distributed.WorkerRouter;
import com.openops.server.session.entity.SessionCache;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;

/**
 * 不在本节点所管理的Client的Session信息
 * */
@Slf4j
public class RemoteSession extends ServerSession implements Serializable {
    private static final long serialVersionUID = -400010884211394846L;

    private SessionCache cache;

    public RemoteSession(SessionCache cache) {
        // RemoteSession在本地没有channel，需要通过其他节点转发
        super(null);

        this.cache = cache;

        Node node = cache.getNode();
        // 可能为null，执行RemoteSession实例方式时都会先去WorkerRouter去获取一下sender，作为延后补偿，
        //      如果执行writeAndFlush补偿完之后还获取不到sender，就代表转发连接建立有问题，此时就直接记录错误日志，消息丢失
        // 获取转发的sender
        this.sender = WorkerRouter.getWorkerRouter().route(node);
    }

    @Override
    public String sessionId() {
        tryGetSender();
        //委托
        return cache.getSessionId();
    }

    @Override
    public boolean isValid() {
        tryGetSender();
        return sender != null && sender.isValid();
    }

    public String getUserId() {
        tryGetSender();
        //委托
        return cache.getClientId();
    }

    /**
     * 通过远程节点，转发
     */
    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        tryGetSender();

        if (sender != null && sender.isValid()) {
            sender.send(msg);
        }

        ProtoMsgFactory.ProtoMsg.Message message = null;
        if (msg instanceof ProtoMsgFactory.ProtoMsg.Message) {
            message = (ProtoMsgFactory.ProtoMsg.Message) msg;
        }
        // TODO: 如果此时转发的连接并未建立好，消息会丢失，不过这种情况很少发生，除非远端节点连不上
        log.error("not ready for remote node sender, sender is not valid, the message is lost {}", message != null ? message.toString() : msg.toString());

        return null;
    }

    private void tryGetSender() {
        if (null == sender || !sender.isValid()) {
            sender = WorkerRouter.getWorkerRouter().route(cache.getNode());
        }
    }
}
