package client;

import java.io.Serializable;

public class Message implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private Object message;
	private int port;
	
	public Message(Object message, int port) {
		this.message = message;
		this.port = port;
	}

	public Object getMessage() {
		return message;
	}
	
	public int getPort() {
		return port;
	}
}
