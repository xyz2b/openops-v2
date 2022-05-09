package com.openops.common.process;

import com.openops.common.ProtoInstant;
import com.openops.common.exception.InvalidMsgTypeException;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.common.session.Session;

/**
 * 业务处理器接口
 * */
public interface Processor {
    /**
     * 处理器的类型
     * */
    ProtoInstant.ProcessorType type();
    /**
     * 处理器的处理任务
     * @param session session（绑定channel和client_id）
     * @param message 消息报文
     * @return 任务处理结果（成功、失败）
     * */
    boolean action(Session session, Object message) throws Exception;
}
