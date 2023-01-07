package client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import graphics.ClientGame;
import graphics.ResultWindow;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import model.ClientConfig;

public class ClientLauncher {
	private static String serverAddress = "localhost";
	private static int serverPort = 5000;
	
	private final static int boardHeight = 16;
	private final static int boardWidth = 30;
	private static int bombNumber = 99;
	
	public static void main(String[] args) {
		readConfig();
		
		try (Socket s = new Socket(serverAddress, serverPort)){
			ClientGame cg = new ClientGame(s);
			Thread game = new Thread(cg);
			game.setDaemon(true);
			game.start();
			game.join();
			Result result = cg.getResult();
			cg.dispose();
			
			ResultWindow rw = new ResultWindow(result);
			rw.setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void readConfig() {
		File config = new File("clientConfig.xml");
		if(config.exists()) {
			try {
				Unmarshaller um = JAXBContext.newInstance(ClientConfig.class).createUnmarshaller();
				ClientConfig cnfg = (ClientConfig) um.unmarshal(config);
				serverAddress = cnfg.getUrl();
				serverPort = cnfg.getPort();
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Marshaller m = JAXBContext.newInstance(ClientConfig.class).createMarshaller();
				ClientConfig cnfg = new ClientConfig();
				cnfg.setUrl("localhost");
				cnfg.setPort(5000);
				m.marshal(cnfg, config);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
	}

	public static int getBoardHeight() {
		return boardHeight;
	}

	public static int getBoardWidth() {
		return boardWidth;
	}

	public static int getBombNumber() {
		return bombNumber ;
	}

	public static int getServerPort() {
		return serverPort;
	}

	public static String getServerAddress() {
		return serverAddress;
	}
}