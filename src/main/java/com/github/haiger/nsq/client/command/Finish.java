package com.github.haiger.nsq.client.command;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:13:42
 */
public class Finish implements NSQCommand {

    private final byte[] msgId;

    public Finish(byte[] msgId) {
        this.msgId = msgId;
    }

    @Override
    public String getCommandString() {
        return "FIN " + new String(msgId) + "\n";
    }

    @Override
    public byte[] getCommandBytes() {
        return getCommandString().getBytes();
    }

}
