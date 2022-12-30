package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.Party;

public class Server {
	public static ConcurrentHashMap<String, Party> parties = new ConcurrentHashMap<String, Party>();
	public static List<String> players = new LinkedList<String>();
	public static final String serverAddress = "localhost";
	public static final int serverPort = 5000;
	
	public static void main(String[] args) throws UnknownHostException {
		players.add("");
		try(ServerSocket ss = new ServerSocket(serverPort)){
			ExecutorService es = Executors.newCachedThreadPool();
			while(!Thread.interrupted()) {
				try {
					Socket s = ss.accept();
					es.submit(new PlayerManagement(s, es));
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}