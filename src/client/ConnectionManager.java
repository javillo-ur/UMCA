package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import model.Message;

public class ConnectionManager extends Thread {
	@SuppressWarnings("unused")
	private Socket s;
	private BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
	private ExecutorService es;
	private MessageHub parent;
	private int port;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	private Future<String> otherName;
	private String otherNameBuffer = null;
	
	@SuppressWarnings("unused")
	private String ownName;
	@SuppressWarnings("unused")
	private boolean isOwner;
	
	public ConnectionManager(Socket s, ExecutorService es, int index, String ownName, boolean isOwner, MessageHub parent) throws IOException {
		this.s = s;
		this.es = es;
		this.port = index;
		this.ownName = ownName;
		this.isOwner = isOwner;
		this.parent = parent;
		oos = new ObjectOutputStream(s.getOutputStream());
		ois = new ObjectInputStream(s.getInputStream());
		otherName = es.submit(new Callable<String>() {
			@SuppressWarnings("deprecation")
			@Override
			public String call() throws Exception {
				if(!isOwner) {
					oos.writeBytes(ownName + "\r\n");
					oos.flush();
					return ois.readLine();
				} else {
					String ret = ois.readLine();
					oos.writeBytes(ownName + "\r\n");
					oos.flush();
					return ret;
				}
			}
		});
	}
	
	public int getIndex() {
		return port;
	}
	
	public void run() {
		try {
			otherName.get();
			es.submit(new Runnable() {
				@Override
				public void run() {
					while(!Thread.interrupted()) {
						try {
							oos.writeObject(queue.take());
							oos.flush();
							oos.reset();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							es.shutdown();
						}
					}
				}
			});
			
			es.submit(new Runnable() {
				@Override
				public void run() {
					while(!Thread.interrupted()) {
						try {
							parent.receiveMessage(new Message<Object>(ois.readObject(), port));
						} catch(IOException e) {
							parent.close();
							es.shutdown();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}
	}

	public void send(Object message) {
		queue.add(message);
	}
	
	public String getOtherName() throws InterruptedException, ExecutionException {
		if(otherNameBuffer == null)
			otherNameBuffer = otherName.get();
		return otherNameBuffer;
	}
}
