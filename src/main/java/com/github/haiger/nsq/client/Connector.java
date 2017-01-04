package com.github.haiger.nsq.client;

import com.github.haiger.nsq.client.exception.NSQException;
import com.github.haiger.nsq.client.protocol.Request;
import com.github.haiger.nsq.client.protocol.Response;

import io.netty.channel.ChannelFuture;

/**
 * @author haiger
 * @since 2017年1月5日 上午6:41:36
 */
public interface Connector {

    public void dealMsg(Object msg);

    public Response writeAndWait(Request request) throws NSQException, InterruptedException;
    
    public ChannelFuture sub(String topic, String channel);
    
    public ChannelFuture rdy(int count);

    public void close();

    public boolean isConnected();
    
    public int getDefaultRdyCount();

    public String getHost();

    public int getPort();
}
