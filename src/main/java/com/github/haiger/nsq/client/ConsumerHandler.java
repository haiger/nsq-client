package com.github.haiger.nsq.client;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:30:18
 */
public interface ConsumerHandler {

    public void handle(byte[] msg) throws Exception;

}