package client;

import java.io.IOException;
import java.net.Socket;
import graphics.ClientGame;
import server.Server;

public class ClientLauncher {
	public static void main(String[] args) {
		Socket s = null;
		try{
			s = new Socket(Server.serverAddress, Server.serverPort);
			ClientGame cg = new ClientGame(s);
			Thread game = new Thread(cg);
			game.start();
			game.join();
			System.out.println(cg.getResult());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if(s != null && !s.isClosed()) {
				try {
					s.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}