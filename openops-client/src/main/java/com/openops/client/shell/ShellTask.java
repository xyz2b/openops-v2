package com.openops.client.shell;

import com.openops.client.builder.CommandExecResponseMsgBuilder;
import com.openops.common.Client;
import com.openops.common.msg.TerminalOutput;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ShellTask  implements Runnable {
    private String uuid;
    private String remoteHost;
    private String[] cmd;
    private Channel channel;
    private Shell shell;
    private Client client;

    public ShellTask(String[] cmd, long timeout, TimeUnit unit, int outputCharNum, Channel channel, String remoteHost, String uuid, Client client) {
        shell = new Shell(cmd, timeout, unit, outputCharNum);
        this.cmd = cmd;
        this.channel = channel;
        this.client = client;
        this.uuid = uuid;
        this.remoteHost = remoteHost;
    }

    @Override
    public void run() {
        StringBuilder errMsg = new StringBuilder();

        try {
            shell.start();
        } catch (InterruptedException | IOException e) {
            log.error("UUID: {}, 执行命令: {}, 出错: {}", uuid, cmd, e.getMessage());
            errMsg.append(e.getMessage());
        }

        TerminalOutput cmdResult = new TerminalOutput();
        cmdResult.setStart(shell.startTime());
        cmdResult.setEnd(shell.endTime());
        cmdResult.setCode(shell.exitValue());
        cmdResult.setStdout(shell.stdOut());
        cmdResult.setStderr(shell.stdErr());
        cmdResult.setExtraInfo(errMsg.toString());
        cmdResult.setUuid(uuid);
        cmdResult.setHost(remoteHost);

        log.info("UUID: {}, 执行结果: {}", uuid, cmdResult);
        log.info("UUID: {}, channel: {}", uuid, channel.id().asLongText());

        ChannelFuture future = channel.writeAndFlush(new CommandExecResponseMsgBuilder(client, cmdResult).build());
        future.addListener((ChannelFuture channelFuture) -> {
            if (future.isDone()) {
                if (future.isSuccess()) {
                    log.info("UUID: {}, 发送成功!!!!", uuid);
                } else if (future.isCancelled()) {
                    log.info("UUID: {}, 取消发送!!!!", uuid);
                } else  {
                    log.error("UUID: {}, 发送出错: {}.", uuid, future.cause().getMessage());
                }
            } else {
                log.error("UUID: {}, 正在发送中...", uuid);
            }
        });
    }
}
