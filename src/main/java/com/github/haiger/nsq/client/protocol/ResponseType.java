package com.github.haiger.nsq.client.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author haiger
 * @since 2017年1月5日 上午4:38:00
 */
public enum ResponseType {
    RESPONSE(0), ERROR(1), MESSAGE(2);
    private static Map<Integer, ResponseType> mappings;

    static {
        mappings = new HashMap<Integer, ResponseType>();
        for (ResponseType t : ResponseType.values()) {
            mappings.put(t.getCode(), t);
        }
    }

    private int code;

    private ResponseType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ResponseType fromCode(int code) {
        return mappings.get(code);
    }
}
