package com.github.haiger.nsq.client.command;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:15:13
 */
public class Nop implements NSQCommand {

    @Override
    public String getCommandString() {
        return "NOP\n";
    }

    @Override
    public byte[] getCommandBytes() {
        return getCommandString().getBytes();
    }

}
