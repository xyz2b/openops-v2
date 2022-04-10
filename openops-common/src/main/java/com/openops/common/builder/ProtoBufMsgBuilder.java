package com.openops.common.builder;

import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ProtoBufMsgBuilder extends AbstractMsgBuilder {
    protected final int type;
    private final AtomicLong sequence = new AtomicLong(0);
    protected final String clientId;

    protected ProtoBufMsgBuilder(int type, String clientId) {
        this.type = type;
        this.clientId = clientId;
    }

    protected ProtoMsg.Message buildMsgOuter() {
        ProtoMsg.Message.Builder mb =
                ProtoMsg.Message
                        .newBuilder()
                        .setType(ProtoMsg.HeadType.forNumber(type))
                        .setSessionId(clientId)
                        .setSequence(sequence.getAndIncrement());
        return mb.buildPartial();
    }

    protected abstract Object buildMsgInner();

    @Override
    protected Object buildMsg() {
        ProtoMsg.Message message = buildMsgOuter();
        Object innerMsg = buildMsgInner();
        Class innerMsgClass = innerMsg.getClass();

        ProtoMsg.Message.Builder outerMsgBuilder = message.toBuilder();

        ((ProtoMsg.AuthRequest)innerMsg).toBuilder();

        try {
            Class builder = Class.forName(innerMsgClass.getName() + "$Builder");
            Method outerMsgSetMethod = outerMsgBuilder.getClass().getMethod("set" + innerMsgClass.getSimpleName(), builder);

            Method innerMsgBuilderMethod = innerMsgClass.getMethod("toBuilder");

            outerMsgSetMethod.invoke(outerMsgBuilder, innerMsgBuilderMethod.invoke(innerMsg));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return outerMsgBuilder.build();
    }
}
