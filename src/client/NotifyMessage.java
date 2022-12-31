package client;

import graphics.ClientGame;
import model.Message;

public class NotifyMessage implements Runnable {
	private Message<?> message;
	private ClientGame window;
	
	public NotifyMessage(Message<?> message, ClientGame window) {
		this.window = window;
		this.message = message;
	}
	
	@Override
	public void run() {
		try {
			window.receiveMessage(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
