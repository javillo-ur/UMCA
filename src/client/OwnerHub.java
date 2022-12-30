package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class OwnerHub extends MessageHub{
	private ServerSocket ss;
	private List<ConnectionManager> conns = new LinkedList<ConnectionManager>();
	private List<String> players = new LinkedList<String>();
	private int connection = 0;
	
	public OwnerHub(ExecutorService es, ClientGame clientGame, ServerSocket ss, String ownName) {
		super(es, clientGame, ownName);
		this.ss = ss;
		players.add(ownName);
	}

	@Override
	public void run() {
		es.submit(new Runnable() {
			@Override
			public void run() {
				while(!Thread.interrupted()) {
					try {
						Socket client = ss.accept();
						ConnectionManager cm = new ConnectionManager(client, es, connection++, ownName, true, hub);
						synchronized(conns) {
							conns.add(cm);
							cm.start();
							players.add(cm.getOtherName());
							receiveMessage(new Message(players, -1));
						}
					} catch(IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		es.submit(new Runnable() {
			@Override
			public void run() {
				while(!Thread.interrupted()) {
					try {
						if(!queue.isEmpty()) {
							Object message = queue.take();
							synchronized(conns) {
								for(ConnectionManager cm : conns)
									cm.send(message);
							}
						}
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	@Override
	public void receiveMessage(Message readMessage) {
		super.receiveMessage(readMessage);
		for(ConnectionManager cm : conns) {
			if(cm.getIndex() != readMessage.getPort())
				cm.send(readMessage.getMessage());
		}
	}
}
