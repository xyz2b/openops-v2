package com.openops.server.distributed;

import com.openops.common.codec.ProtobufDecoder;
import com.openops.common.codec.ProtobufEncoder;
import com.openops.common.sender.Sender;
import com.openops.common.msg.Notification;
import com.openops.server.builder.NotificationMsgBuilder;
import com.openops.server.handler.NodeAuthResponseClientHandler;
import com.openops.server.session.ClientSession;
import com.openops.server.handler.NodeExceptionClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 与其他节点的连接
 * */
@Slf4j
@Data
public class PeerSender implements Sender {
    // 重连次数
    private  int reConnectCount = 0;

    // 所连接的远端节点信息
    private Node rmNode;

    // 本节点作为客户端与其他节点的连接信息
    private ClientSession clientSession;

    GenericFutureListener<ChannelFuture> closeListener = (ChannelFuture f) -> {
        log.info("分布式连接已经断开……{}", rmNode.toString());
        clientSession = null;
    };

    private GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f) -> {
        final EventLoop eventLoop = f.channel().eventLoop();
        if (!f.isSuccess() && ++reConnectCount < 3) {
            log.info("连接失败! 在10s之后准备尝试第{}次重连!",reConnectCount);
            eventLoop.schedule(() -> PeerSender.this.doConnect(), 10, TimeUnit.SECONDS);
        } else {
            log.info(new Date() + "分布式节点连接成功:{}", rmNode.toString());

            Channel channel = f.channel();
            // 本节点连接其他节点，本节点是作为客户端的
            clientSession = new ClientSession(channel);
            clientSession.bind();
            channel.closeFuture().addListener(closeListener);

            // 向所连接的节点发送连接成功的通知
            Node localNode = Worker.getWorker().getLocalNodeInfo();
            Notification<Node> notification = new Notification<Node>(Notification.CONNECT_FINISHED, localNode);
            // 节点和节点之间进行通信的报文，clientId为发送节点的IP和端口
            // 客户端和节点之间进行通信的报文，clientId为客户端的IP
            Object pkg = new NotificationMsgBuilder(clientSession.sessionId(), notification).build();
            writeAndFlush(pkg);
        }
    };


    private Bootstrap b;
    private EventLoopGroup g;

    public PeerSender(Node remoteNode) {
        this.rmNode = remoteNode;

        /**
         * 客户端的是Bootstrap，服务端的则是 ServerBootstrap。
         * 都是AbstractBootstrap的子类。
         **/

        b = new Bootstrap();
        /**
         * 通过nio方式来接收连接和处理连接
         */

        g = new NioEventLoopGroup();
    }

    /**
     * 重连
     */
    public void doConnect() {
        // 获取本节点的信息
        Node localNode = Worker.getWorker().getLocalNodeInfo();

        // 其他节点的ip
        String host = rmNode.getHost();
        // 其他节点的端口
        int port = rmNode.getPort();

        try {
            if (b != null && b.group() == null) {
                b.group(g);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                b.remoteAddress(host, port);

                // 设置通道初始化
                b.handler(
                        new ChannelInitializer<SocketChannel>()
                        {
                            public void initChannel(SocketChannel ch)
                            {
                                ch.pipeline().addLast("decoder", new ProtobufDecoder());
                                ch.pipeline().addLast("encoder", new ProtobufEncoder());
                                ch.pipeline().addLast("nodeLogin", new NodeAuthResponseClientHandler(localNode));
                                ch.pipeline().addLast("exceptionHandler", new NodeExceptionClientHandler());
                            }
                        }
                );
                log.info(new Date() + "开始连接分布式节点:{}", rmNode.toString());

                ChannelFuture f = b.connect();
                f.addListener(connectedListener);

            } else if (b.group() != null) {
                log.info(new Date() + "再一次开始连接分布式节点", rmNode.toString());
                ChannelFuture f = b.connect();
                f.addListener(connectedListener);
            }
        } catch (Exception e) {
            log.info("客户端连接失败!" + e.getMessage());
        }

    }

    public void stopConnecting() {
        g.shutdownGracefully();
    }

    public void writeAndFlush(Object pkg) {
        if (clientSession == null || !clientSession.isValid()) {
            log.error("分布式节点未连接:", rmNode.toString());
            return;
        }
        clientSession.writeAndFlush(pkg);
    }

    @Override
    public void send(Object message) {
        writeAndFlush(message);
    }

    @Override
    public boolean isValid() {
        return (null != clientSession) && clientSession.isValid();
    }

    public boolean isConnected() {
        return (null != clientSession) && clientSession.isConnected();
    }
}
