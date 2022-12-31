package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import graphics.ClientGame;
import model.Message;

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
							receiveMessage(new Message<List<String>>(players, -1));
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
						Object message = queue.take();
						synchronized(conns) {
							for(ConnectionManager cm : conns)
								cm.send(message);
						}
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	@Override
	public void receiveMessage(Message<?> readMessage) throws InterruptedException {
		super.receiveMessage(readMessage);
		for(ConnectionManager cm : conns) {
			if(cm.getIndex() != readMessage.getPort())
				cm.send(readMessage.getMessage());
		}
	}

	public void sendTurns() throws InterruptedException, ExecutionException {
		List<Integer> turns = new ArrayList<Integer>(conns.size() + 1);
		turns.add(-1);
		for(int i = 0; i < conns.size(); i++)
			turns.add(i);
		Collections.shuffle(turns);
		int i = 0;
		for(int turn : turns) {
			if(turn == -1)
				super.receiveMessage(new Message<Integer>(i++, -1));
			else conns.get(turn).send(i++);
		}
		List<String> turnNames = new ArrayList<String>(turns.size());
		for(i = 0; i < turns.size(); i++) {
			if(turns.get(i) == -1)
				turnNames.add(i, ownName);
			else turnNames.add(i, conns.get(turns.get(i)).getOtherName());
		}
		if(turns.get(0) == -1) {
			super.receiveMessage(new Message<List<String>>(turnNames, -1));
		}
		else {
			conns.get(turns.get(0)).send(turnNames);
		}
	}
}
