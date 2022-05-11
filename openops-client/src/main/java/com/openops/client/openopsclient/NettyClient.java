package com.openops.client.openopsclient;

import com.openops.common.codec.ProtobufDecoder;
import com.openops.common.codec.ProtobufEncoder;
import com.openops.client.config.ClientConfig;
import com.openops.client.handler.AuthResponseClientHandler;
import com.openops.client.handler.ClientExceptionHandler;
import com.openops.client.session.ClientSession;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClient {
    @Autowired
    ClientConfig clientConfig;

    // 重连次数
    private  int reConnectCount = 0;

    private ClientSession clientSession;

    GenericFutureListener<ChannelFuture> closeListener = (ChannelFuture f) -> {
        clientSession = null;
    };

    private GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f) -> {
        final EventLoop eventLoop = f.channel().eventLoop();
        if (!f.isSuccess() && ++reConnectCount < 3) {
            log.info("连接失败! 在10s之后准备尝试第{}次重连!",reConnectCount);
            eventLoop.schedule(() -> NettyClient.this.doConnect(), 10, TimeUnit.SECONDS);
        } else {
            log.info(new Date() + "节点连接成功:{}", clientConfig.getServerIp() + ":" + clientConfig.getServerPort());

            Channel channel = f.channel();
            clientSession = new ClientSession(channel);
            channel.closeFuture().addListener(closeListener);
        }
    };


    private Bootstrap b;
    private EventLoopGroup g;

    public NettyClient() {
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

        // 其他节点的ip
        String host = clientConfig.getServerIp();
        // 其他节点的端口
        int port = clientConfig.getServerPort();

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
                                ch.pipeline().addLast("login", new AuthResponseClientHandler());
                                ch.pipeline().addLast("exceptionHandler", new ClientExceptionHandler());
                            }
                        }
                );

                ChannelFuture f = b.connect();
                f.addListener(connectedListener);

            } else if (b.group() != null) {
                log.info(new Date() + "再一次开始连接节点{}", clientConfig.getServerIp() + ":" + clientConfig.getServerPort());
                ChannelFuture f = b.connect();
                f.addListener(connectedListener);
            }
        } catch (Exception e) {
            log.info("客户端连接失败!" + e.getMessage());
        }

    }
}
