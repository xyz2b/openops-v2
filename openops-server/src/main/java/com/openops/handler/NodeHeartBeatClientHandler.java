package com.openops.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class NodeHeartBeatClientHandler extends ChannelInboundHandlerAdapter {
}
