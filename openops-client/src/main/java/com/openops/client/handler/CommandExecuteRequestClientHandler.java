package com.openops.client.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@ChannelHandler.Sharable
@Service("CommandExecuteRequestClientHandler")
public class CommandExecuteRequestClientHandler extends ChannelInboundHandlerAdapter {


}
