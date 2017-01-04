package com.github.haiger.nsq.client.protocol;

import java.util.List;

/**
 * @author haiger
 * @since 2017年1月5日 上午1:48:57
 */
public class RequestBuilder {
    private static final String IDENTIFY = "IDENTIFY";
    private static final String PUB = "PUB";
    private static final String MPUB = "MPUB";
    private static final String SUB = "SUB";
    private static final String RDY = "RDY";
    private static final String FIN = "FIN";
    private static final String REQ = "REQ";
    private static final String TOUCH = "TOUCH";
    private static final String NOP = "NOP";
    private static final String CLS = "CLS";
    
    public static Request buildMagic() {
        String commandLine = "  V2";
        return new Request(commandLine.getBytes());
    }
    
    public static Request buildIdentify(NSQConfig config) {
        String commandLine = String.format("%s\n", IDENTIFY);
        return new Request(commandLine.getBytes(), config.toString().getBytes());
    }
    
    public static Request buildPub(String topic, byte[] data) {
        String commandLine = String.format("%s %s\n", PUB, topic);
        return new Request(commandLine.getBytes(), data);
    }
    
    public static Request buildMPub(String topic, List<byte[]> datas) {
        String commandLine = String.format("%s %s\n", MPUB, topic);
        return new Request(commandLine.getBytes(), datas);
    }
    
    public static Request buildSub(String topic, String channel) {
        String commandLine = String.format("%s %s %s\n", SUB, topic, channel);
        return new Request(commandLine.getBytes());
    }
    
    public static Request buildRdy(int count) {
        String commandLine = String.format("%s %s\n", RDY, count);
        return new Request(commandLine.getBytes());
    }
    
    public static Request buildFin(byte[] messageId) {
        String commandLine = String.format("%s %s\n", FIN, new String(messageId));
        return new Request(commandLine.getBytes());
    }
    
    public static Request buildReq(byte[] messageId, int timeout) {
        String commandLine = String.format("%s %s %s\n", REQ, new String(messageId), timeout);
        return new Request(commandLine.getBytes());
    }
    
    public static Request buildTouch(byte[] messageId) {
        String commandLine = String.format("%s %s\n", TOUCH, new String(messageId));
        return new Request(commandLine.getBytes());
    }
    
    public static Request buildNop() {
        String commandLine = NOP + "\n";
        return new Request(commandLine.getBytes());
    }
    
    public static Request buildCls() {
        String commandLine = CLS + "\n";
        return new Request(commandLine.getBytes());
    }
}
