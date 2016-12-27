package com.github.haiger.nsq.client.lookup;


/**
 * @author haiger
 * @since 2016年12月27日 下午2:23:48
 */
public class NSQNode {
	private String host;
	private int port;
	
	public NSQNode(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (null == obj) {
			return false;
		} else if (!(obj instanceof NSQNode)) {
			return false;
		} else {
			NSQNode node = (NSQNode) obj;
			if (((host == node.getHost()) ||
				(host != null && host.equals(node.getHost()))) &&
				port == node.getPort()) {
				return true;
			}
		}
		
		return false;
	}
	
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
}
