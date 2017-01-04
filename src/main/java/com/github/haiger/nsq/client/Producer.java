package com.github.haiger.nsq.client;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.haiger.nsq.client.exception.NSQException;
import com.github.haiger.nsq.client.lookup.NSQNode;
import com.github.haiger.nsq.client.protocol.Request;
import com.github.haiger.nsq.client.protocol.RequestBuilder;
import com.github.haiger.nsq.client.protocol.Response;
import com.github.haiger.nsq.client.remoting.NSQConnector;
import com.github.haiger.nsq.client.util.ConnectorUtils;

/**
 * @author haiger
 * @since 2017年1月5日 上午6:39:24
 */
public class Producer {
    private static final Logger log = LoggerFactory.getLogger(Producer.class);
    private String host; // lookupd ip
    private int port; // lookupd port
    private ConcurrentHashMap</* ip:port */String, Connector> connectorMap;
    private AtomicLong index;
    private static final int DEFAULT_RETRY = 3;

    public Producer(String host, int port) {
        this.host = host;
        this.port = port;
        this.connectorMap = new ConcurrentHashMap<String, Connector>();
        this.index = new AtomicLong(0);
    }

    public ConcurrentHashMap<String, Connector> getConnectorMap() {
        return connectorMap;
    }

    public void connect() {
        List<NSQNode> nodes = ConnectorUtils.lookupNode(host, port);
        if (null == nodes || nodes.isEmpty()) {
            log.error("producer start fail !! could not find any nsqd from lookupd {}:{}", host, port);
            return;
        }

        for (NSQNode nsqNode : nodes) {
            if (ConnectorUtils.isExcluded(nsqNode))
                continue;

            Connector connector = null;
            try {
                connector = new NSQConnector(nsqNode.getHost(), nsqNode.getPort(), null, 0);
                connectorMap.put(ConnectorUtils.getConnectorKey(nsqNode), connector);
            } catch (NSQException e) {
                log.error("Producer: connector to {} goes wrong at:{}", ConnectorUtils.getConnectorKey(nsqNode), e);
            }
        }
        
        ConnectorMonitor.getInstance().setLookup(host, port);
        ConnectorMonitor.getInstance().registerProducer(this);
    }

    public boolean put(String topic, String msg) throws NSQException, InterruptedException {
        return put(topic, msg.getBytes());
    }

    public boolean put(String topic, byte[] msgData) throws NSQException, InterruptedException {
        Connector connector = getConnector();

        if (connector == null)
            throw new NSQException("No active connector to be used.");

        Request request = RequestBuilder.buildPub(topic, msgData);
        Response response = connector.writeAndWait(request);
        if (response.isOK()) {
            return true;
        }
        throw new NSQException(response.decodeString());
    }

    private Connector getConnector() {
        Connector connector = nextConnector();
        if (connector == null) return null;
        int retry = 0;
        while (!connector.isConnected()) {
            if (retry >= DEFAULT_RETRY) {
                connector = null;
                break;
            }
            removeConnector(connector);
            connector = nextConnector();
            retry++;
        }

        return connector;
    }

    private Connector nextConnector() {
        Connector[] connectors = new NSQConnector[connectorMap.size()];
        connectorMap.values().toArray(connectors);
        if (connectors.length < 1) return null;
        Long nextIndex = Math.abs(index.incrementAndGet() % connectors.length);
        return connectors[nextIndex.intValue()];
    }
    
    public boolean removeConnector(Connector connector) {
        if (connector == null) return true;
        log.info("Producer: removeConnector({})", ConnectorUtils.getConnectorKey(connector));
        connector.close();
        return connectorMap.remove(ConnectorUtils.getConnectorKey(connector), connector);
    }
    
    public void addConnector(Connector connector) {
        log.info("Producer: addConnector({})", ConnectorUtils.getConnectorKey(connector));
        connectorMap.put(ConnectorUtils.getConnectorKey(connector), connector);
    }

    public void close() {
        for (Connector connector : connectorMap.values()) {
            connector.close();
        }
    }
}
