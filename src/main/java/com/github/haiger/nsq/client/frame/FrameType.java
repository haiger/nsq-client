package com.github.haiger.nsq.client.frame;

import java.util.HashMap;
import java.util.Map;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:18:20
 */
public enum FrameType {
    RESPONSE(0), ERROR(1), MESSAGE(2);
    private static Map<Integer, FrameType> mappings;

    static {
        mappings = new HashMap<Integer, FrameType>();
        for (FrameType t : FrameType.values()) {
            mappings.put(t.getCode(), t);
        }
    }

    private int code;

    private FrameType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static FrameType fromCode(int code) {
        return mappings.get(code);
    }
}
