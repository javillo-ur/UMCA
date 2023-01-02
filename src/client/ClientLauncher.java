package client;

import java.io.IOException;
import java.net.Socket;

import graphics.ClientGame;
import graphics.ResultWindow;
import server.Server;

public class ClientLauncher {
	public static void main(String[] args) {
		try (Socket s = new Socket(Server.serverAddress, Server.serverPort)){
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
}