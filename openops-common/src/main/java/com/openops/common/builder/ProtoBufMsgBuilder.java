package com.openops.common.builder;

import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ProtoBufMsgBuilder extends AbstractMsgBuilder {
    protected final ProtoMsg.HeadType type;
    private final static AtomicLong sequence = new AtomicLong(0);
    protected final String sessionId;

    protected ProtoBufMsgBuilder(ProtoMsg.HeadType type, String sessionId) {
        this.type = type;
        this.sessionId = sessionId;
    }

    protected ProtoMsg.Message.Builder assembleMsgOuter() {
        return ProtoMsg.Message
                .newBuilder()
                .setType(type)
                .setSessionId(sessionId)
                .setSequence(sequence.getAndIncrement());
    }

    protected abstract Object assembleMsgInner();

    @Override
    protected Object buildMsg() {
        ProtoMsg.Message.Builder outerMsgBuilder = assembleMsgOuter();

        Object innerMsgBuilder = assembleMsgInner();
        if (innerMsgBuilder != null) {
            Class innerMsgClass = innerMsgBuilder.getClass();
            String[] innerMsgClassNames = innerMsgClass.getName().split("\\$");
            String innerMsgClassName = innerMsgClassNames[innerMsgClassNames.length - 2];

            try {
                Class builder = Class.forName(innerMsgClass.getName());
                Method outerMsgSetMethod = outerMsgBuilder.getClass().getMethod("set" + innerMsgClassName, builder);
                outerMsgSetMethod.invoke(outerMsgBuilder, innerMsgBuilder);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return outerMsgBuilder.build();

    }
}
