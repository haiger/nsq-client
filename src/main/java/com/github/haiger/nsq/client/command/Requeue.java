package com.github.haiger.nsq.client.command;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:16:42
 */
public class Requeue implements NSQCommand {
    private final byte[] msgId;
    private final int timeout;

    public Requeue(byte[] msgId, int timeout) {
        this.msgId = msgId;
        this.timeout = timeout;
    }

    @Override
    public String getCommandString() {
        return "REQ " + new String(msgId) + " " + timeout + "\n";
    }

    @Override
    public byte[] getCommandBytes() {
        return getCommandString().getBytes();
    }

}
