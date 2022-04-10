package com.openops.common.sender;

import com.openops.cocurrent.CallbackTask;
import com.openops.cocurrent.CallbackTaskScheduler;
import com.openops.cocurrent.FutureTaskScheduler;
import com.openops.common.msg.ProtoMsgFactory;
import com.openops.common.session.Session;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSender implements Sender {
    protected final Session session;

    protected AbstractSender(Session session) {
        this.session = session;
    }

    @Override
    public void send(Object message) {
        FutureTaskScheduler.add(() -> {
                if (null == session) {
                    log.error("session is null");
                    throw new Exception("session is null");
                }

                if (!session.isConnected()) {
                    log.error("连接还没成功");
                    throw new Exception("连接还没成功");
                }

                final Boolean[] isSuccess = { false };

                ChannelFuture f = session.writeAndFlush(message);
                f.addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        // 回调
                        if (future.isSuccess()) {
                            sendSucceed(message);
                        } else if (future.isCancelled()) {
                            sendCancel(message);
                        } else {
                            sendFailed(message, future.cause());
                        }
                    }
                });

            return null;
        });
    }

    protected void sendSucceed(Object message) { log.info("发送成功"); }

    protected void sendCancel(Object message) { log.info("发送取消"); }

    protected void sendFailed(Object message, Throwable t) { log.info("发送消息出现异常"); }
}
