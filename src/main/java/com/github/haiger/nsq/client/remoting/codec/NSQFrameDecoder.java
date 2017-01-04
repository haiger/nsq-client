package com.github.haiger.nsq.client.remoting.codec;

import com.github.haiger.nsq.client.exception.NSQException;
import com.github.haiger.nsq.client.protocol.Response;
import com.github.haiger.nsq.client.protocol.ResponseType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:35:09
 */
public class NSQFrameDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger log = LoggerFactory.getLogger(NSQFrameDecoder.class);

    public NSQFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
            int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf byteBuf) throws NSQException {
        try {
            int size = byteBuf.capacity();
            int type = byteBuf.readInt();

            ResponseType respType = ResponseType.fromCode(type);

            if (respType == null) {
                throw new NSQException("Unknown frame type:" + respType);
            }

            byte[] data = new byte[size - 4];
            byteBuf.readBytes(data);

            return new Response(respType, data);
        } catch (Exception e) {
            log.error("NSQ message decode error:", e);
            return new NSQException("message decode error", e);
        }
    }
}
