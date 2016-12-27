package com.github.haiger.nsq.client.remoting.connector;

import java.util.List;

import com.github.haiger.nsq.client.lookup.NSQNode;
import com.github.haiger.nsq.client.lookup.LookupdClients;
import com.github.haiger.nsq.client.remoting.NSQConnector;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:26:37
 */
public class ConnectorUtils {
    private static final String[] excludedNodes = {};
    
    public static boolean isExcluded(NSQNode node) {
        for (String ip : excludedNodes) {
            if (ip.trim().equalsIgnoreCase(node.getHost().trim())) {
                return true;
            }
        }
        return false;
    }

    public static String getConnectorKey(NSQNode node) {
        StringBuffer sb = new StringBuffer();
        sb.append(node.getHost()).append(":").append(node.getPort());
        return sb.toString();
    }

    public static String getConnectorKey(NSQConnector connector) {
        StringBuffer sb = new StringBuffer();
        sb.append(connector.getHost()).append(":").append(connector.getPort());
        return sb.toString();
    }
    
    public static List<NSQNode> lookupTopic(String lookupHost, int lookupPort, String topic) {
        return LookupdClients.lookup(lookupHost, lookupPort, topic);
    }
    
    public static List<NSQNode> lookupNode(String lookupHost, int lookupPort) {
        return LookupdClients.nodes(lookupHost, lookupPort);
    }
}
