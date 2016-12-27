package com.github.haiger.nsq.client.exception;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:17:50
 */
public class NSQException extends Exception {
    private static final long serialVersionUID = 9035255049714054051L;

    public NSQException(String message) {
        super(message);
    }
    
    public NSQException(String message, Throwable cause) {
        super(message, cause);
    }
}
