package com.github.haiger.nsq.client.remoting.connector;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.haiger.nsq.client.exception.NSQException;
import com.github.haiger.nsq.client.lookup.NSQNode;
import com.github.haiger.nsq.client.remoting.ConnectorMonitor;
import com.github.haiger.nsq.client.remoting.NSQConnector;
import com.github.haiger.nsq.client.remoting.listener.ConnectorListener;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:37:08
 */
public class CustomerConnector {
	private static final Logger log = LoggerFactory.getLogger(CustomerConnector.class);
	private String host;// lookup host
	private int port;// lookup port
	private String topic;
	private String channel;
	private static final int readyCount = 10;
	private ConnectorListener subListener;
	private ConcurrentHashMap</*ip:port*/String, NSQConnector> connectorMap;
	
	public CustomerConnector(String host, int port, String topic, String channel) {
		this.host = host;
		this.port = port;
		this.topic = topic;
		this.channel = channel;
		this.connectorMap = new ConcurrentHashMap<String, NSQConnector>();
	}
	
	public ConcurrentHashMap<String, NSQConnector> getConnectorMap() {
        return connectorMap;
    }
	
	public void connect() {
	    if (subListener == null) {
	        log.warn("ConnectorListener must be seted.");
	        return;
	    }
	    
        List<NSQNode> nsqNodes = ConnectorUtils.lookupTopic(host, port, topic);
        if (null == nsqNodes || nsqNodes.isEmpty()) {
			log.error("customer start fail !! no nsqd addr found at lookupd {}:{} with topic: {}", host, port, topic);
			return;
		}
        
        for (NSQNode node : nsqNodes) {
        	NSQConnector connector = null;
            try {
                connector = new NSQConnector(node.getHost(), node.getPort(), subListener, readyCount);
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

	public void setSubListener(ConnectorListener listener) {
		this.subListener = listener;
	}
	
	public ConnectorListener getSubListener() {
	    return subListener;
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
	
	public boolean removeConnector(NSQConnector connector) {
        if (connector == null) return true;
        connector.close();
        return connectorMap.remove(ConnectorUtils.getConnectorKey(connector), connector);
    }
    
    public void addConnector(NSQConnector connector) {
        connectorMap.put(ConnectorUtils.getConnectorKey(connector), connector);
    }
	
	public void close() {
		for (NSQConnector connector : connectorMap.values()) {
			connector.close();
		}
	}
}
