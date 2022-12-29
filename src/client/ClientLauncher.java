package client;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import model.Party;

public class ClientLauncher {
	public static void main(String[] args) {
		Socket s = null;
		try{
			s = new Socket("localhost", 5000);
			ExecutorService es = Executors.newCachedThreadPool(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = Executors.defaultThreadFactory().newThread(r);
					thread.setDaemon(true);
					return thread;
				}
			});
			Future<Party> partyTask = es.submit(new SelectParty(s, es));
			ClientGame cg = new ClientGame(partyTask.get());
			cg.setVisible(true);
			es.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
