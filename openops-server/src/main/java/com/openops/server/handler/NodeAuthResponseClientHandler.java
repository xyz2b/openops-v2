package com.openops.server.handler;

import com.openops.server.builder.AuthRequestMsgBuilder;
import com.openops.common.Client;
import com.openops.common.ProtoInstant;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.server.distributed.Node;
import com.openops.server.session.ClientSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class NodeAuthResponseClientHandler extends ChannelInboundHandlerAdapter {
    private Node localNode;

    public NodeAuthResponseClientHandler(Node localNode) {
        this.localNode = localNode;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 节点和节点之间进行通信的报文，clientId为发送节点的IP和端口
        // 客户端和节点之间进行通信的报文，clientId为客户端的IP
        Client client = new Client(localNode.getHost() + ":" + localNode.getPort(), "", "f0Bo7qXvXjj-3hCdFAh1E", "", "");
        ctx.writeAndFlush(new AuthRequestMsgBuilder(client).build());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProtoMsg.Message message = (ProtoMsg.Message) msg;

        // 如果是握手应答消息，需要判断是否认证成功
        if (message.getAuthResponse() != null && message.getType() == ProtoMsg.HeadType.AUTH_RESPONSE) {

            // 判断握手应答结果，如果非0，说明认证失败，关闭链路，重新发起连接
            if (message.getAuthResponse().getCode() == ProtoInstant.AuthResultCode.SUCCESS.getCode()) {
                log.error("验证失败，关闭连接");
                // 握手失败，关闭连接
                ctx.close();
            } else {
                log.info("Login ok");

                ClientSession session = ctx.channel().attr(ClientSession.SESSION_KEY).get();
                session.setSessionId(message.getSessionId());
                Client client = new Client(localNode.getHost() + ":" + localNode.getPort(), "", "f0Bo7qXvXjj-3hCdFAh1E", "", message.getSessionId());
                session.setClient(client);
                session.setLogin(true);

                NodeHeartBeatClientHandler nodeHeartBeatClientHandler = new NodeHeartBeatClientHandler();
                ctx.channel().pipeline().addAfter("nodeLogin", "nodeHeartBeatClient", nodeHeartBeatClientHandler);
                nodeHeartBeatClientHandler.channelActive(ctx);
                ctx.channel().pipeline().remove("nodeLogin");
            }
        } else {
            //  不是握手应答消息，直接在pipeline往下传给下个Handler
            ctx.fireChannelRead(msg);
        }
    }
}
