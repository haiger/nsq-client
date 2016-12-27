package com.github.haiger.nsq.client.command;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:13:26
 */
public class Close implements NSQCommand {

    @Override
    public String getCommandString() {
        return "CLS\n";
    }

    @Override
    public byte[] getCommandBytes() {
        return getCommandString().getBytes();
    }

}
