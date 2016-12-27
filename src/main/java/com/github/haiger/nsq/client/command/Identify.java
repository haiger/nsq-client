package com.github.haiger.nsq.client.command;

import java.nio.ByteBuffer;

import com.github.haiger.nsq.client.constant.CommandType;
import com.github.haiger.nsq.client.remoting.connector.NSQConfig;


/**
 * @author haiger
 * @since 2016年12月27日 下午2:14:04
 */
public class Identify implements NSQCommand {
    private String configString;
    private byte[] configData;
    
    public Identify(NSQConfig config) {
        configString = config.toString();
        configData = config.toString().getBytes();
    }

    @Override
    public String getCommandString() {
        return String.format("%s\n%s", CommandType.IDENTIFY.getCode(), configString);
    }

    @Override
    public byte[] getCommandBytes() {
        String header = String.format("%s\n", CommandType.IDENTIFY.getCode());

        int size = configData.length;
        ByteBuffer bb = ByteBuffer.allocate(header.length() + 4 + size);
        bb.put(header.getBytes());
        bb.putInt(size);
        bb.put(configData);
        return bb.array();
    }
}
