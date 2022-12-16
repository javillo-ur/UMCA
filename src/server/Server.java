package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.Party;
import model.Player;

public class Server {
	public static List<Party> parties = Collections.synchronizedList(new LinkedList<Party>());
	
	public static void main(String[] args) throws UnknownHostException {
		parties.add(new Party(0, new Player("alguien", InetAddress.getByName("www.google.com"))));
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