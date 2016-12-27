package com.github.haiger.nsq.client.frame;

import com.github.haiger.nsq.client.remoting.handler.NSQMessage;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:18:53
 */
public class MessageFrame implements NSQFrame {
    private NSQMessage message;
    
    public MessageFrame(NSQMessage message) {
        this.message = message;
    }

    public NSQMessage getNSQMessage() {
        return message;
    }

    @Override
    public String getMessage() {
        return null;
    }
}
