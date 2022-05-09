package com.openops.common;

public class ProtoInstant {
    /**
     * 魔数，可以通过配置获取
     */
    public static final int MAGIC_CODE = 0x8686;
    /**
     * 版本号
     */
    public static final short VERSION_CODE = 0x01;


    /**
     * 处理器类型
     * */
    public static final int AUTH_PROCESSOR = 0;
    public static final int COMMAND_EXECUTE_REQUEST_PROCESS = 1;
    public static final int COMMAND_EXECUTE_RESPONSE_PROCESS = 2;


    public enum ProcessorType {
        AUTH(0, "登录处理器"),
        COMMAND_EXECUTE_REQUEST(1, "命令请求处理器"),
        COMMAND_EXECUTE_RESPONSE(2, "命令响应处理器");

        private Integer type;
        private String desc;

        ProcessorType(int type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        public Integer getType()
        {
            return type;
        }

        public String getDesc()
        {
            return desc;
        }

    }



    public enum AuthResultCode {

        SUCCESS(0, "Success"),  // 成功
        AUTH_FAILED(1, "登录失败"),
        NO_TOKEN(2, "没有授权码"),
        UNKNOW_ERROR(3, "未知错误");

        private Integer code;
        private String desc;

        AuthResultCode(Integer code, String desc)
        {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode()
        {
            return code;
        }

        public String getDesc()
        {
            return desc;
        }

    }
}
