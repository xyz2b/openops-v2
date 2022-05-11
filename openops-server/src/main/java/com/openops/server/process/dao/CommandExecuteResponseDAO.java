package com.openops.server.process.dao;

import com.openops.common.msg.ProtoMsgFactory;

public interface CommandExecuteResponseDAO {
    // 保存用户缓存
    void save(ProtoMsgFactory.ProtoMsg.CommandExecuteResponse commandExecuteResponse);

    // 获取用户缓存
    ProtoMsgFactory.ProtoMsg.CommandExecuteResponse get(String host, String uuid);

    // 删除用户缓存
    void remove(String host, String uuid);
}
