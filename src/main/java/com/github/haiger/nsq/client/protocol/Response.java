package com.github.haiger.nsq.client.protocol;

import java.nio.ByteBuffer;

/**
 * @author haiger
 * @since 2017年1月5日 上午1:42:38
 */
public class Response {
    private ResponseType type;
    private byte[] data;
    
    private static final String OK = "OK";
    private static final String HB = "_heartbeat_";
    
    public Response(ResponseType type, byte[] data) {
        this.type = type;
        this.data = data;
    }
    
    public ResponseType getType() {
        return this.type;
    }
    
    public boolean isOK() {
        return OK.equalsIgnoreCase(decodeString());
    }
    
    public boolean isHeartbeat() {
        return HB.equalsIgnoreCase(decodeString());
    }
    
    public String decodeString() {
        return new String(data);
    }
    
    public Message decodeMessage() {
        Message msg = new Message();
        ByteBuffer buf = ByteBuffer.wrap(data);
        msg.setTimestamp(buf.getLong());
        msg.setAttempts(buf.getShort());
        byte[] msgId = new byte[16];
        buf.get(msgId);
        msg.setMessageId(msgId);
        byte[] body = new byte[data.length - Message.MIN_SIZE_BYTES];
        buf.get(body);
        msg.setBody(body);
        return msg;
    }
}
