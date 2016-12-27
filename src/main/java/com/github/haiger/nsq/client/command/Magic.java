package com.github.haiger.nsq.client.command;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:14:59
 */
public class Magic implements NSQCommand {

    @Override
    public String getCommandString() {
        return "  V2";
    }

    @Override
    public byte[] getCommandBytes() {
        return getCommandString().getBytes();
    }

}
