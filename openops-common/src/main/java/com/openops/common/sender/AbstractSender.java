package com.openops.common.sender;

import com.openops.cocurrent.CallbackTask;
import com.openops.cocurrent.CallbackTaskScheduler;
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
        CallbackTaskScheduler.add(new CallbackTask<Boolean>() {
            @Override
            public Boolean execute() throws Exception {
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
                            isSuccess[0] = true;
                            log.info("发送消息成功");
                        }
                    }
                });

                try {
                    f.sync();
                } catch (InterruptedException e) {
                    isSuccess[0] = false;
                    e.printStackTrace();
                    throw new Exception("error occur");
                }

                return isSuccess[0];
            }

            @Override
            public void onSuccess(Boolean b) {
                if (b) {
                    sendSucceed(message);
                } else {
                    sendFailed(message);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                sendException(message);
            }
        });
    }

    protected void sendSucceed(Object message) { log.info("发送成功"); }

    protected void sendFailed(Object message) { log.info("发送失败"); }

    protected void sendException(Object message) { log.info("发送消息出现异常"); }
}
