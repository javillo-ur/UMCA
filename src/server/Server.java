package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import model.Party;
import model.ServerConfig;

public class Server {
	public static ConcurrentHashMap<String, Party> parties = new ConcurrentHashMap<String, Party>();
	public static List<String> players = new LinkedList<String>();
	
	private static int serverPort = 5000;
	
	public static void main(String[] args) throws UnknownHostException {
		readConfig();
		
		ServerLogger sl = new ServerLogger(Thread.currentThread());
		sl.setVisible(true);
		
		players.add("");
		try(ServerSocket ss = new ServerSocket(serverPort)){
			ExecutorService es = Executors.newCachedThreadPool();
			while(!Thread.interrupted()) {
				try {
					Socket s = ss.accept();
					es.submit(new PlayerManagement(s, es, sl));
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void readConfig() {
		File config = new File("serverConfig.xml");
		if(config.exists()) {
			try {
				JAXBContext context = JAXBContext.newInstance(ServerConfig.class);
				Unmarshaller um = context.createUnmarshaller();
				ServerConfig sc = (ServerConfig) um.unmarshal(config);
				serverPort = sc.getPort();
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		} else {
			ServerConfig sc = new ServerConfig();
			sc.setPort(5000);
			try {
				Marshaller m = JAXBContext.newInstance(ServerConfig.class).createMarshaller();
				m.marshal(sc, config);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
	}
}