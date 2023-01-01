package client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import graphics.ClientGame;

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
	
	public void send(Object message) throws InterruptedException {
		queue.put(message);
	}

	public void receiveMessage(Object readObject, int port) throws InterruptedException {
		window.receiveMessage(readObject);
	}

	public void close() {
		window.dispose();
	}
}