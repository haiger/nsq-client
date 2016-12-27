package com.github.haiger.nsq.client.command;

import com.github.haiger.nsq.client.constant.CommandType;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:16:56
 */
public class Subscribe implements NSQCommand {
    private String topic;
    private String channel;

    public Subscribe(String topic, String channel) {
        this.topic = topic;
        this.channel = channel;
    }

    @Override
    public String getCommandString() {
        return String.format("%s %s %s\n", CommandType.SUBSCRIBE.getCode(), topic, channel);
    }

    @Override
    public byte[] getCommandBytes() {
        return getCommandString().getBytes();
    }

}
