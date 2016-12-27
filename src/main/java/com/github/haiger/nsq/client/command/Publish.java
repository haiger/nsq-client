package com.github.haiger.nsq.client.command;

import java.nio.ByteBuffer;

import com.github.haiger.nsq.client.constant.CommandType;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:15:57
 */
public class Publish implements NSQCommand {
    private String topic;
    private byte[] data;

    public Publish(String topic, byte[] data) {
        this.topic = topic;
        this.data = data;
    }

    @Override
    public String getCommandString() {
        return String.format("%s %s\n%s%s", CommandType.PUBLISH.getCode(), topic, data.length, data);
    }

    @Override
    public byte[] getCommandBytes() {
        String header = String.format("%s %s\n", CommandType.PUBLISH.getCode(), topic);

        int size = data.length;
        ByteBuffer bb = ByteBuffer.allocate(header.length() + 4 + size);
        bb.put(header.getBytes());
        bb.putInt(size);
        bb.put(data);
        return bb.array();
    }

}
