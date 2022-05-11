package com.openops.process.dao.impl;

import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.process.dao.CommandExecuteResponseDAO;
import com.openops.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

public class CommandExecuteResponseRedisImpl implements CommandExecuteResponseDAO {
    public static final String REDIS_PREFIX = "Command:Response:";
    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(ProtoMsg.CommandExecuteResponse commandExecuteResponse) {
        String uuid = commandExecuteResponse.getUuid();
        String host = commandExecuteResponse.getHost();

        String key = REDIS_PREFIX + host + ":" + uuid;
        String value = JsonUtil.pojoToJson(commandExecuteResponse);
        // TODO: 命令执行结果设置过期时间
        stringRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public ProtoMsg.CommandExecuteResponse get(String host, String uuid) {
        String key = REDIS_PREFIX + host + ":" + uuid;
        String value = stringRedisTemplate.opsForValue().get(key);

        if (!StringUtils.isEmpty(value)) {
            return JsonUtil.jsonToPojo(value, ProtoMsg.CommandExecuteResponse.class);
        }
        return null;
    }

    @Override
    public void remove(String host, String uuid) {
        String key = REDIS_PREFIX + host + ":" + uuid;
        stringRedisTemplate.delete(key);
    }
}
