package com.github.haiger.nsq.client;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.haiger.nsq.client.exception.NSQException;
import com.github.haiger.nsq.client.lookup.NSQNode;
import com.github.haiger.nsq.client.remoting.NSQConnector;
import com.github.haiger.nsq.client.util.ConnectorUtils;

/**
 * @author haiger
 * @since 2017年1月5日 上午6:40:54
 */
public class Consumer {
    private static final Logger log = LoggerFactory.getLogger(Consumer.class);
    private String host;// lookup host
    private int port;// lookup port
    private String topic;
    private String channel;
    private static final int readyCount = 10;
    private ConsumerHandler consumerHandler;
    private ConcurrentHashMap</*ip:port*/String, Connector> connectorMap;
    
    public Consumer(String host, int port, String topic, String channel) {
        this.host = host;
        this.port = port;
        this.topic = topic;
        this.channel = channel;
        this.connectorMap = new ConcurrentHashMap<String, Connector>();
    }
    
    public ConcurrentHashMap<String, Connector> getConnectorMap() {
        return connectorMap;
    }
    
    public void connect() {
        if (consumerHandler == null) {
            log.warn("ConnectorListener must be seted.");
            return;
        }
        
        List<NSQNode> nsqNodes = ConnectorUtils.lookupTopic(host, port, topic);
        if (null == nsqNodes || nsqNodes.isEmpty()) {
            log.error("customer start fail !! no nsqd addr found at lookupd {}:{} with topic: {}", host, port, topic);
            return;
        }
        
        for (NSQNode node : nsqNodes) {
            Connector connector = null;
            try {
                connector = new NSQConnector(node.getHost(), node.getPort(), consumerHandler, readyCount);
                connector.sub(topic, channel);
                connector.rdy(readyCount);
                connectorMap.put(ConnectorUtils.getConnectorKey(node), connector);
            } catch (NSQException e) {
                log.error("Customer: connector to {} goes wrong at:{}", ConnectorUtils.getConnectorKey(node), e);
            }
        }
        
        ConnectorMonitor.getInstance().setLookup(host, port);
        ConnectorMonitor.getInstance().registerConsumer(this);
    }

    public void setSubListener(ConsumerHandler consumerHandler) {
        this.consumerHandler = consumerHandler;
    }
    
    public ConsumerHandler getConsumerHandler() {
        return consumerHandler;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public String getChannel() {
        return channel;
    }
    
    public int getReadyCount() {
        return readyCount;
    }
    
    public boolean removeConnector(Connector connector) {
        if (connector == null) return true;
        connector.close();
        return connectorMap.remove(ConnectorUtils.getConnectorKey(connector), connector);
    }
    
    public void addConnector(Connector connector) {
        connectorMap.put(ConnectorUtils.getConnectorKey(connector), connector);
    }
    
    public void close() {
        for (Connector connector : connectorMap.values()) {
            connector.close();
        }
    }
}
