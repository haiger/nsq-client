package com.github.haiger.nsq.client.remoting.listener;

import java.util.EventListener;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:30:18
 */
public interface ConnectorListener extends EventListener{


    public void handleEvent(NSQEvent event) throws Exception;

}