package com.openops.common.sender;

import com.openops.cocurrent.CallbackTask;
import com.openops.cocurrent.CallbackTaskScheduler;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.common.session.Session;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtoMsgSender extends AbstractSender {

    public ProtoMsgSender(Session session) {
        super(session);
    }

    @Override
    public void sendMsg(Object message) {
        if (message instanceof ProtoMsg.Message) {
            ProtoMsg.Message msg = (ProtoMsg.Message) message;
            CallbackTaskScheduler.add(new CallbackTask<Boolean>()
            {
                @Override
                public Boolean execute() throws Exception
                {
                    if (null == session)
                    {
                        throw new Exception("session is null");
                    }

                    if (!session.isConnected())
                    {
                        log.info("连接还没成功");
                        throw new Exception("连接还没成功");
                    }

                    final Boolean[] isSuccess = {false};

                    ChannelFuture f = session.writeAndFlush(msg);
                    f.addListener(new GenericFutureListener<Future<? super Void>>()
                    {
                        @Override
                        public void operationComplete(Future<? super Void> future)
                                throws Exception
                        {
                            // 回调
                            if (future.isSuccess())
                            {
                                isSuccess[0] = true;

                                log.info("操作成功");
                            }
                        }

                    });


                    try
                    {
                        f.sync();
                    } catch (InterruptedException e)
                    {
                        isSuccess[0] = false;
                        e.printStackTrace();
                        throw new Exception("error occur");
                    }

                    return isSuccess[0];
                }

                @Override
                public void onSuccess(Boolean b)
                {
                    if (b)
                    {
                        sendSucced(msg);

                    } else
                    {
                        sendfailed(msg);

                    }

                }

                @Override
                public void onFailure(Throwable t)
                {
                    sendException(msg);
                }
            });
        }
    }

    protected void sendSucced(ProtoMsg.Message message)
    {
        log.info("发送成功");

    }

    protected void sendfailed(ProtoMsg.Message message)
    {
        log.info("发送失败");
    }

    protected void sendException(ProtoMsg.Message message)
    {
        log.info("发送消息出现异常");

    }
}
