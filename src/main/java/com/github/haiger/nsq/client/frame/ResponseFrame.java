package com.github.haiger.nsq.client.frame;

import com.github.haiger.nsq.client.constant.ResponseType;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:19:45
 */
public class ResponseFrame implements NSQFrame {
    private String response;
    private ResponseType responseType;
    
    public ResponseFrame(ResponseType resp) {
        this.responseType = resp;
    }
    
    public ResponseFrame(String resp) {
        this.response = resp;
    }
    
    public ResponseType getResponseType() {
        return responseType;
    }
    
    public String getResponse() {
        return response;
    }

    @Override
    public String getMessage() {
        if (responseType == null) return response;
        return responseType.getCode();
    }
}
