package model;

import java.io.Serializable;

public class Message<T> implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private T message;
	private int port;
	
	public Message(T message, int port) {
		this.message = message;
		this.port = port;
	}

	public T getMessage() {
		return message;
	}
	
	public int getPort() {
		return port;
	}
}
