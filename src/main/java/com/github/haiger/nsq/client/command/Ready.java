package com.github.haiger.nsq.client.command;

import com.github.haiger.nsq.client.constant.CommandType;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:16:30
 */
public class Ready implements NSQCommand {
    private int count;

    public Ready(int count) {
        this.count = count;
    }

    @Override
    public String getCommandString() {
        return String.format("%s %s\n", CommandType.READY.getCode(), count);
    }

    @Override
    public byte[] getCommandBytes() {
        return getCommandString().getBytes();
    }

}
