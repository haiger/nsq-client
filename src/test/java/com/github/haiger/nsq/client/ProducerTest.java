package com.github.haiger.nsq.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.github.haiger.nsq.client.exception.NSQException;

/**
 * @author haiger
 * @since 2017年1月10日 下午4:09:55
 */
public class ProducerTest {
    private static String LOOKUP_HOST = "localhost";
    private static int LOOKUP_PORT = 4161;
    
    public void pub(boolean isSuccess) throws NSQException, InterruptedException {
        Producer producer = new Producer(LOOKUP_HOST, LOOKUP_PORT);
        producer.connect();
        
        String topic = "testTopic";
        String message = "test message";
        producer.put(topic, message);
        producer.close();
        
        AtomicInteger msgCounter = new AtomicInteger(0);
        String channel = "testChannel";
        Consumer consumer = new Consumer(LOOKUP_HOST, LOOKUP_PORT, topic, channel);
        consumer.setConsumerhandler(new ConsumerHandler() {
            
            @Override
            public void handle(byte[] msg) throws Exception {
                if (isSuccess) {
                    msgCounter.incrementAndGet();
                } else {
                    throw new Exception("deal msg fail. should be requeue.");
                }
            }
        });
        consumer.connect();
        
        while (msgCounter.get() == 0) {
            Thread.sleep(10 * 1000);
        }
        
        Assert.assertTrue(msgCounter.get() == 1);
        consumer.close();
    }
    
    @Test
    public void testPubSuccess() throws NSQException, InterruptedException {
        pub(true);
    }
    
    @Test
    public void testPubFail() throws NSQException, InterruptedException {
        pub(false);
    }
}
