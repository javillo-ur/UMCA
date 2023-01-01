package model;

import java.io.Serializable;

public class Message implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private Object message;
	private int port;
	private int order;
	
	public Message(Object message, int port, int order) {
		this.message = message;
		this.port = port;
		this.order = order;
	}

	public Object getMessage() {
		return message;
	}
	
	public int getPort() {
		return port;
	}
	
	public int getOrder() {
		return order;
	}

	public void setPort(int port2) {
		this.port = port2;
	}
}
