package com.openops.process.dao;

import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;

public interface CommandExecuteResponseDAO {
    // 保存用户缓存
    void save(ProtoMsg.CommandExecuteResponse commandExecuteResponse);

    // 获取用户缓存
    ProtoMsg.CommandExecuteResponse get(String host, String uuid);

    // 删除用户缓存
    void remove(String host, String uuid);
}
