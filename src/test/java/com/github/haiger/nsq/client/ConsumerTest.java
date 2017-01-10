package com.github.haiger.nsq.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.github.haiger.nsq.client.exception.NSQException;

/**
 * @author haiger
 * @since 2017年1月10日 下午4:10:13
 */
public class ConsumerTest {
    private static String LOOKUP_HOST = "localhost";
    private static int LOOKUP_PORT = 4161;
    
    @Test
    public void testConsumer() throws NSQException, InterruptedException {
        AtomicInteger msgCounter = new AtomicInteger(0);
        String topic = "testTopic";
        String channel = "testChannel";
        Consumer consumer = new Consumer(LOOKUP_HOST, LOOKUP_PORT, topic, channel);
        consumer.setConsumerhandler(new ConsumerHandler() {
            
            @Override
            public void handle(byte[] msg) throws Exception {
                msgCounter.incrementAndGet();
            }
        });
        consumer.connect();
        
        Producer producer = new Producer(LOOKUP_HOST, LOOKUP_PORT);
        producer.connect();
        
        int pubCounter = 10;
        String message = "test message";
        for (int i = 0; i < pubCounter; i++) {
            producer.put(topic, message + i);
        }
        producer.close();
        
        Thread.sleep(pubCounter * 10 * 1000);
        
        Assert.assertTrue(msgCounter.get() == pubCounter);
        consumer.close();
    }
}
