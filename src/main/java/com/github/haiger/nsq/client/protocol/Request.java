package com.github.haiger.nsq.client.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author haiger
 * @since 2017年1月5日 上午1:42:21
 */
public class Request {
    private byte[] commandLine;
    private List<byte[]> datas;
    
    public Request(byte[] commandLine) {
        this.commandLine = commandLine;
    }
    
    public Request(byte[] commandLine, byte[] data) {
        this.commandLine = commandLine;
        this.datas = new ArrayList<byte[]>(1);
        this.datas.add(data);
    }
    
    public Request(byte[] commandLine, List<byte[]> datas) {
        this.commandLine = commandLine;
        this.datas = datas;
    }
    
    public byte[] encode() {
        if (datas == null || datas.size() == 0) {
            return commandLine;
        } else if (datas.size() == 1) {
            byte[] data = datas.get(0);
            int size = data.length;
            ByteBuffer buf = ByteBuffer.allocate(commandLine.length + 4 + size);
            buf.put(commandLine);
            buf.putInt(size);
            buf.put(data);
            return buf.array();
            
        } else {
            int bodySize = 4;// for dataCount int
            for (byte[] data : datas) {
                bodySize += 4;// data length frame
                bodySize += data.length;
            }
            int dataCount = datas.size();
            
            ByteBuffer buf = ByteBuffer.allocate(commandLine.length + 4 + bodySize);
            buf.put(commandLine);
            buf.putInt(bodySize);
            buf.putInt(dataCount);
            for (byte[] data : datas) {
                buf.putInt(data.length);
                buf.put(data);
            }
            return buf.array();
        }
    }
}
