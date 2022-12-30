package client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MessageHub extends Thread{
	protected ExecutorService es;
	protected ClientGame window;
	protected BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
	protected String ownName;
	protected MessageHub hub = this;
	
	public MessageHub(ExecutorService es, ClientGame clientGame, String ownName) {
		this.es = es;
		this.window = clientGame;
		this.ownName = ownName;
	}
	
	public void send(Object message) {
		queue.add(message);
	}

	public void receiveMessage(Message readObject) {
		window.receiveMessage(readObject);
	}
}