package com.openops.common.msg;

import lombok.Data;

@Data
public class Notification<T> {
    public static final int SESSION_ON = 10;//上线的通知
    public static final int SESSION_OFF = 20;//下线的通知
    public static final int CONNECT_FINISHED = 30;//节点的链接成功
    private int type;
    private T data;

    public Notification() { }

    public Notification(int type, T t) {
        this.type = type;
        data = t;
    }

    public static Notification<ContentWrapper> wrapContent(int type, String content) {
        ContentWrapper wrapper = new ContentWrapper();
        wrapper.setContent(content);
        return new Notification<ContentWrapper>(type, wrapper);
    }

    @Data
    public static class ContentWrapper {
        String content;
    }

    public String getWrapperContent() {
        if (data instanceof ContentWrapper) {
            return ((ContentWrapper) data).getContent();
        }
        return null;
    }
}
