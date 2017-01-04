package com.github.haiger.nsq.client.protocol;

/**
 * @author haiger
 * @since 2017年1月5日 上午1:43:10
 */
public class Message {
    // message format defined as 8 byte TS, 2 byte attempts, 16 byte msg ID, N byte body
    public static final int MIN_SIZE_BYTES = 26;
    // nano second
    private long timestamp;
    // really a uint16 but java doesnt do unsigned 
    private int attempts;
    // 16 bytes
    private byte[] messageId;
    private byte[] body;
    
    public Message(){}

    public Message(long timestamp, int attempts, byte[] msgId, byte[] body) {
        this.timestamp = timestamp;
        this.attempts = attempts;
        this.messageId = msgId;
        this.body = body;
    }
    
    public int getSize() {
        return MIN_SIZE_BYTES + body.length;
    }
    
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public byte[] getMessageId() {
        return messageId;
    }

    public void setMessageId(byte[] messageId) {
        this.messageId = messageId;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
