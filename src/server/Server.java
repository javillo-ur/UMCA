package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.Party;
import model.Player;

public class Server {
	public static ConcurrentHashMap<String, Party> parties = new ConcurrentHashMap<String, Party>();
	
	public static void main(String[] args) throws UnknownHostException {
		parties.put("alguien", new Party(new Player("alguien", InetAddress.getByName("www.google.com"))));
		try(ServerSocket ss = new ServerSocket(5000)){
			ExecutorService es = Executors.newFixedThreadPool(8);
			while(!Thread.interrupted()) {
				try {
					Socket s = ss.accept();
					es.submit(new PlayerManagement(s));
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}