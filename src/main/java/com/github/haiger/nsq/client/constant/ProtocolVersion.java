package com.github.haiger.nsq.client.constant;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:17:22
 */
public enum ProtocolVersion {
    V2("V2");
    private String version;

    private ProtocolVersion(String version) {
        this.version = version;
    }

    public String getCode() {
        return this.version;
    }
}
