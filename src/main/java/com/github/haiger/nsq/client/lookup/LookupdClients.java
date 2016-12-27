package com.github.haiger.nsq.client.lookup;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:21:38
 */
public class LookupdClients {
	private static final Logger log = LoggerFactory.getLogger(LookupdClients.class);
	
	public static List<NSQNode> lookup(String host, int port, String topic) {
		if (null == host || null == topic || port < 0) {
			log.error("invalid input of host/topic");
			return Collections.emptyList();
		}
		
		return new Lookup(host, port).query(topic);
	}
	
	public static List<NSQNode> nodes(String host, int port) {
		if (null == host || port < 0) {
			log.error("invalid input of host/port");
			return Collections.emptyList();
		}
		
		return new Nodes(host, port).query();
	}
}
