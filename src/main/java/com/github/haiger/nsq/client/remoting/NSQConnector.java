package com.github.haiger.nsq.client.remoting;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.haiger.nsq.client.Connector;
import com.github.haiger.nsq.client.ConsumerHandler;
import com.github.haiger.nsq.client.exception.NSQException;
import com.github.haiger.nsq.client.protocol.Message;
import com.github.haiger.nsq.client.protocol.NSQConfig;
import com.github.haiger.nsq.client.protocol.Request;
import com.github.haiger.nsq.client.protocol.RequestBuilder;
import com.github.haiger.nsq.client.protocol.Response;
import com.github.haiger.nsq.client.protocol.ResponseType;
import com.github.haiger.nsq.client.remoting.codec.NSQFrameDecoder;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:29:29
 */
public class NSQConnector implements Connector {
    private static final Logger log = LoggerFactory.getLogger(NSQConnector.class);
    private String host; // nsqd host
    private int port; // nsqd tcp port
    private Channel channel;
    private EventLoopGroup workerGroup;
    private ConsumerHandler consumerHandler;
    private LinkedBlockingQueue<Request> requests = new LinkedBlockingQueue<Request>(1);
    private LinkedBlockingQueue<Response> responses = new LinkedBlockingQueue<Response>(1);
    private final NSQChannelHandler handler = new NSQChannelHandler();;
    private static final int DEFAULT_WAIT = 10;
    private static final int DEFAULT_REQ_TIMEOUT = 0;
    private static final int DEFAULT_REQ_TIMES = 10;
    private int defaultRdyCount;
    private final AtomicLong rdyCount = new AtomicLong();
    public static final AttributeKey<NSQConnector> CONNECTOR = AttributeKey.valueOf("connector");

    public NSQConnector(String host, int port, ConsumerHandler consumerHandler, int rdyCount) throws NSQException {
        this.consumerHandler = consumerHandler;
        this.host = host;
        this.port = port;
        this.defaultRdyCount = rdyCount;
        this.rdyCount.set(defaultRdyCount);
        workerGroup = new NioEventLoopGroup();
        Bootstrap boot = new Bootstrap();
        boot.group(workerGroup);
        boot.channel(NioSocketChannel.class);
        boot.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new IdleStateHandler(60, 0, 0));
                ch.pipeline().addLast(new NSQFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                ch.pipeline().addLast(handler);
            }
        });
        boot.option(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = boot.connect(this.host, this.port);
        channel = future.awaitUninterruptibly().channel();
        if (!future.isSuccess()) {
            throw new NSQException("can't connect to server", future.cause());
        }
        log.info("NSQConnector start, address {}:{}", host, port);

        this.channel.attr(CONNECTOR).set(this);
        sendMagic();

        try {
            Response resp = writeAndWait(RequestBuilder.buildIdentify(new NSQConfig()));
            if (resp.isOK()) {
                log.info("identify response:" + resp.decodeString());
            } else {
                close();
                log.error("identify error response:" + resp.decodeString());
            }
        } catch (InterruptedException | NSQException e) {
            close();
            throw new NSQException("send indentify goes wrong.", e);
        }
    }

    public void dealMsg(Object msg) {
        Response resp = (Response)msg;
        switch (resp.getType()) {
        case RESPONSE:
            if (resp.isHeartbeat()) {
                nop();
            }
        case ERROR:
            log.info("response string:" + resp.decodeString());
            dealResponse(resp);
            break;
            
        case MESSAGE:
            Message message = resp.decodeMessage();
            byte[] data = message.getBody();
            byte[] messageId = message.getMessageId();

            if (consumerHandler != null) {
                try {
                    consumerHandler.handle(data);
                    if (rdyCount.decrementAndGet() <= 0) {
                        rdyCount.set(defaultRdyCount);
                        finishAndRdy(messageId, defaultRdyCount);
                    } else {
                        finish(messageId);
                    }
                } catch (Exception e) {
                    if (message.getAttempts() < DEFAULT_REQ_TIMES) {
                        log.warn("nsq message deal fail(will requeue) at:{}", e);
                        requeue(messageId);
                    } else {
                        log.warn("nsq message deal fail and requeue times gt 10, then be finished. this messageID:{}",
                                new String(messageId));
                        finish(messageId);
                    }
                    
                    if (rdyCount.get() <= 0) 
                        rdy(defaultRdyCount);
                }
            } else {
                finish(messageId);
                log.warn("no message listener, this message has be finished.");
            }
            break;
        default:
            log.warn("something else error:{}", msg);
            break;
        }
    }

    private void dealResponse(Response resp) {
        try {
            this.responses.offer(resp, DEFAULT_WAIT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("response offer error:{}", e);
        }
    }

    public Response writeAndWait(Request request) throws NSQException, InterruptedException {
        if (!this.requests.offer(request, DEFAULT_WAIT, TimeUnit.SECONDS)) {
            throw new NSQException("command request offer timeout");
        }

        this.responses.clear();
        ChannelFuture cft = write(request);

        if (!cft.await(DEFAULT_WAIT, TimeUnit.SECONDS)) {
            this.requests.poll();
            throw new NSQException("command writer timeout");
        }

        Response response = this.responses.poll(DEFAULT_WAIT, TimeUnit.SECONDS);
        if (response == null) {
            this.requests.poll();
            throw new NSQException("command response poll timeout");
        }

        this.requests.poll();
        return response;
    }

    private ChannelFuture write(Request request) {
        ByteBuf buf = channel.alloc().buffer().writeBytes(request.encode());
        return channel.writeAndFlush(buf);
    }

    private void sendMagic() {
        ByteBuf magic = channel.alloc().buffer().writeBytes(RequestBuilder.buildMagic().encode());
        channel.writeAndFlush(magic);
    }

    public ChannelFuture sub(String topic, String channel) {
        ByteBuf sub = this.channel.alloc().buffer().writeBytes(RequestBuilder.buildSub(topic, channel).encode());
        return this.channel.writeAndFlush(sub);
    }

    public ChannelFuture finish(byte[] msgId) {
        ByteBuf fin = channel.alloc().buffer().writeBytes(RequestBuilder.buildFin(msgId).encode());
        return channel.writeAndFlush(fin);
    }

    public ChannelFuture requeue(byte[] msgId) {
        ByteBuf req = channel.alloc().buffer().writeBytes(RequestBuilder.buildReq(msgId, DEFAULT_REQ_TIMEOUT).encode());
        return channel.writeAndFlush(req);
    }

    public ChannelFuture rdy(int count) {
        ByteBuf rdy = channel.alloc().buffer().writeBytes(RequestBuilder.buildRdy(count).encode());
        return channel.writeAndFlush(rdy);
    }

    public ChannelFuture finishAndRdy(byte[] msgId, final int count) {
        return finish(msgId).addListener(new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(Future<? super Void> future) throws Exception {
                rdy(count);
            };
        });
    }

    public ChannelFuture nop() {
        ByteBuf nop = channel.alloc().buffer().writeBytes(RequestBuilder.buildNop().encode());
        return channel.writeAndFlush(nop);
    }

    private void cleanClose() {
        try {
            Response resp = writeAndWait(RequestBuilder.buildCls());
            if (ResponseType.ERROR == resp.getType())
                log.warn("cleanClose {}:{} goes error at:{}", host, port, resp.decodeString());
        } catch (NSQException | InterruptedException e) {
            log.warn("cleanClose {}:{} goes error at:{}", host, port, e);
        }
    }

    public void close() {
        if (channel != null) {
            if (channel.isActive())
                cleanClose();
            channel.disconnect();
        }
        if (workerGroup != null)
            workerGroup.shutdownGracefully();
    }

    public boolean isConnected() {
        return channel == null ? false : channel.isActive() ? true : false;
    }
    
    public int getDefaultRdyCount() {
        return defaultRdyCount;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
