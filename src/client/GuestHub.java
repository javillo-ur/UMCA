package client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;

public class GuestHub extends MessageHub{
	private String ownerIp;
	private int ownerPort;
	
	public GuestHub(ExecutorService es, ClientGame clientGame, String ownerIp, int ownerPort, String ownName) {
		super(es, clientGame, ownName);
		this.ownerIp = ownerIp;
		this.ownerPort = ownerPort;
	}
	
	@Override
	public void run() {
		Socket owner;
		try {
			owner = new Socket(ownerIp, ownerPort);
			ConnectionManager conn = new ConnectionManager(owner, es, 0, ownName, false, hub);
			conn.start();
			es.submit(new Runnable() {
				@Override
				public void run() {
					while(!Thread.interrupted()) {
						try {
							if(!queue.isEmpty())
								conn.send(queue.take());
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
