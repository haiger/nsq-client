package com.github.haiger.nsq.client.constant;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:17:13
 */
public enum CommandType {
    SUBSCRIBE("SUB"), PUBLISH("PUB"), READY("RDY"), FINISH("FIN"), REQUEUE("REQ"),
    IDENTIFY("IDENTIFY"), CLOSE("CLS"), NOP("NOP");

    private String code;

    private CommandType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
