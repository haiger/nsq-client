package com.github.haiger.nsq.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.haiger.nsq.client.lookup.NSQNode;
import com.github.haiger.nsq.client.remoting.NSQConnector;
import com.github.haiger.nsq.client.util.ConnectorUtils;
import com.github.haiger.nsq.client.exception.NSQException;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:25:41
 */
public class ConnectorMonitor {
	private static final Logger log = LoggerFactory.getLogger(ConnectorMonitor.class);
	private static final int DEFAULT_RECONNECT_PERIOD = 60 * 1000;
	private HashSet<Producer> producers = new HashSet<Producer>();
    private HashSet<Consumer> consumers = new HashSet<Consumer>();
    private String lookupHost;
    private int lookupPort;
    
    private ConnectorMonitor(){
        monitor();
    }
    
    private static class ConnectorMonitorHolder {
        static final ConnectorMonitor instance = new ConnectorMonitor();
    }
    
    public static ConnectorMonitor getInstance(){
        return ConnectorMonitorHolder.instance;
    }
	
	private void monitor() {
	    Thread monitor = new Thread(new Runnable() {
            
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(DEFAULT_RECONNECT_PERIOD);
                    } catch (InterruptedException e) {
                        log.warn("NSQ connector monitor sleep goes wrong at:{}", e);
                    }
                    try {
                        dealProducer();
                        dealCustomer();
                    } catch (Exception e) {
                        log.error("Monitor deal goes wrong at:{}", e);
                    }
                }
            }
        });
	    monitor.setName("ConnectorMonitorThread");
	    monitor.setDaemon(true);
	    monitor.start();
	}
	
	private void dealProducer() {
	    List<NSQNode> nodes = ConnectorUtils.lookupNode(lookupHost, lookupPort);
	    
	    for (Producer producer : producers) {
	        ConcurrentHashMap<String, Connector> connectorMap = producer.getConnectorMap();
	        List<NSQNode> oldNodes = new ArrayList<NSQNode>();
	        for (Connector connector : connectorMap.values()) {
	            if (!connector.isConnected())
	                producer.removeConnector(connector);
	            else 
	                oldNodes.add(new NSQNode(connector.getHost(), connector.getPort()));
	        }
	        
	        for (NSQNode node : nodes) {
	            if (!oldNodes.contains(node) && !ConnectorUtils.isExcluded(node)) {
	                Connector connector = null;
                    try {
                        connector = new NSQConnector(node.getHost(), node.getPort(), null, 0);
                        producer.addConnector(connector);
                    } catch (NSQException e) {
                        log.warn("Producer monitor: connector to ({}:{}) failed.", node.getHost(), node.getPort());
                    }
	            }
	        }
	    }
	}
	
	private void dealCustomer() {
	    for (Consumer consumer : consumers) {
	        List<NSQNode> nodes = ConnectorUtils.lookupTopic(lookupHost, lookupPort, consumer.getTopic());
	        ConcurrentHashMap<String, Connector> connectorMap = consumer.getConnectorMap();
	        List<NSQNode> oldNodes = new ArrayList<NSQNode>();
	        
	        for (Connector connector : connectorMap.values()) {
	            if (!connector.isConnected())
	                consumer.removeConnector(connector);
                else 
                    oldNodes.add(new NSQNode(connector.getHost(), connector.getPort()));
	        }
	        
	        for (NSQNode node : nodes) {
                if (!oldNodes.contains(node)) {
                    Connector connector = null;
                    try {
                        connector = new NSQConnector(node.getHost(), node.getPort(), consumer.getSubListener(), consumer.getReadyCount());
                        connector.sub(consumer.getTopic(), consumer.getChannel());
                        connector.rdy(consumer.getReadyCount());
                        connectorMap.put(ConnectorUtils.getConnectorKey(node), connector);
                    } catch (NSQException e) {
                        log.error("Customer: connector to {} goes wrong at:{}", ConnectorUtils.getConnectorKey(node), e);
                    }
                }
            }
	    }
	}
	
	public void registerProducer(Producer producer) {
		if (null == producer) {
			return;
		}
		producers.add(producer);
	}

	public void registerConsumer(Consumer connector) {
		if (null == connector) {
			return;
		}
		consumers.add(connector);
	}
	
	public void setLookup(String lookupHost, int lookupPort) {
	    this.lookupHost = lookupHost;
	    this.lookupPort = lookupPort;
	}
}