package com.github.haiger.nsq.client.frame;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:18:08
 */
public class ErrorFrame implements NSQFrame {
    private String error;
    
    public ErrorFrame(String error) {
        this.error = error;
    }
    
    public String getError() {
        return error;
    }

    @Override
    public String getMessage() {
        return error;
    }
}
