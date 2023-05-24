package com.openops.client.process;

import com.openops.client.session.ClientSession;
import com.openops.client.shell.ShellTask;
import com.openops.common.ProtoInstant;
import com.openops.common.exception.InvalidMsgTypeException;
import com.openops.common.msg.ProtoMsgFactory;
import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("CommandExecuteRequestClientProcessor")
public class CommandExecuteRequestClientProcessor extends AbstractProcessor {
    // TODO: 线程池数量动态计算，执行时长较长和执行时间较短的任务（通过超时时间判断，任务执行超时取消任务）
    // TODO: 任务优先级
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    public CommandExecuteRequestClientProcessor() {
        super(ProtoInstant.ProcessorType.COMMAND_EXECUTE_REQUEST);
    }

    @Override
    public boolean action(Session session, Object message) throws InvalidMsgTypeException {
        if (message instanceof ProtoMsgFactory.ProtoMsg.Message) {
            ProtoMsgFactory.ProtoMsg.Message msg = (ProtoMsgFactory.ProtoMsg.Message) message;
            
            checkMsgType(msg);

            ProtoMsgFactory.ProtoMsg.CommandExecuteRequest commandExecuteRequest = msg.getCommandExecuteRequest();

            log.info("执行命令：{}", commandExecuteRequest.getCmd());
            log.info("channel: {}", ((ClientSession)session).channel().id().asLongText());

            String[] cmd = {"/bin/bash", "-c", commandExecuteRequest.getCmd()};

            executor.submit(new ShellTask(cmd, 60, TimeUnit.SECONDS, 1024, ((ClientSession)session).channel(), commandExecuteRequest.getHost(), commandExecuteRequest.getUuid(), session.client()));

            return true;
        }
        return false;
    }

    private void checkMsgType(ProtoMsgFactory.ProtoMsg.Message msg) throws InvalidMsgTypeException {
        if(msg.getType() != ProtoMsgFactory.ProtoMsg.HeadType.COMMAND_EXECUTE_REQUEST) {
            throw new InvalidMsgTypeException("消息类型错误");
        }
    }
}
