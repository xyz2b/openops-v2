package com.openops.client.nettyclient;

import com.openops.client.handler.CommandExecuteRequestClientHandler;
import com.openops.client.handler.HeartBeatClientHandler;
import com.openops.common.Client;
import com.openops.common.codec.ProtobufDecoder;
import com.openops.common.codec.ProtobufEncoder;
import com.openops.client.config.ClientConfig;
import com.openops.client.handler.AuthResponseClientHandler;
import com.openops.client.handler.ClientExceptionHandler;
import com.openops.client.session.ClientSession;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("OpenopsClient")
public class OpenopsClient {
    @Autowired
    ClientConfig clientConfig;

    @Autowired
    private AuthResponseClientHandler authResponseClientHandler;

    @Autowired
    private ClientExceptionHandler clientExceptionHandler;

    @Autowired
    private CommandExecuteRequestClientHandler commandExecuteRequestClientHandler;

    // 重连次数
    private  int reConnectCount = 0;

    private ClientSession clientSession;

    GenericFutureListener<ChannelFuture> closeListener = (ChannelFuture f) -> {
        final EventLoop eventLoop = f.channel().eventLoop();
        if (f.isSuccess()) {
            reConnectCount++;
            log.error("netty client [" + f.channel().localAddress().toString() + "] 连接 netty server [" + clientConfig.getServerIp() + ":" + clientConfig.getServerPort() + "] 失败，等待10秒钟进行第{}次重连", reConnectCount);
            eventLoop.schedule(() -> OpenopsClient.this.doConnect(), 10, TimeUnit.SECONDS);
        } else {
            log.error("netty client channel 关闭失败" + "，退出程序");
            System.exit(-1);
        }
        clientSession = null;
    };

    private GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f) -> {
        final EventLoop eventLoop = f.channel().eventLoop();
        if (!f.isSuccess()) {
            reConnectCount++;
            log.info("连接失败! 在10s之后准备尝试第{}次重连!", reConnectCount);
            eventLoop.schedule(() -> OpenopsClient.this.doConnect(), 10, TimeUnit.SECONDS);
        } else {
            log.info(new Date() + " 节点连接成功:{}", clientConfig.getServerIp() + ":" + clientConfig.getServerPort());

            Channel channel = f.channel();
            clientSession = new ClientSession(channel);
            clientSession.bind();
            channel.closeFuture().addListener(closeListener);
        }
    };


    private Bootstrap b;
    private EventLoopGroup g;

    public OpenopsClient() {
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

    public void doConnect() {
        String serverIp = clientConfig.getServerIp();
        int serverPort = clientConfig.getServerPort();

        String localIp = clientConfig.getClientIp();

        try {
            if (b != null && b.group() == null) {
                b.group(g);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(EpollChannelOption.SO_REUSEPORT, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                b.remoteAddress(serverIp, serverPort);
                b.localAddress(localIp, 0);

                // 设置通道初始化
                b.handler(
                        new ChannelInitializer<SocketChannel>()
                        {
                            public void initChannel(SocketChannel ch)
                            {
                                ch.pipeline().addLast("decoder", new ProtobufDecoder());
                                ch.pipeline().addLast("encoder", new ProtobufEncoder());
                                ch.pipeline().addLast("login", authResponseClientHandler);
                                ch.pipeline().addLast("CommandExecuteRequestClientHandler", commandExecuteRequestClientHandler);
                                ch.pipeline().addLast("exceptionHandler", clientExceptionHandler);
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
