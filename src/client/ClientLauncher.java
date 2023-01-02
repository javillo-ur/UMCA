package client;

import java.io.IOException;
import java.net.Socket;

import graphics.ClientGame;
import server.Server;

public class ClientLauncher {
	public static void main(String[] args) {
		try (Socket s = new Socket(Server.serverAddress, Server.serverPort)){
			ClientGame cg = new ClientGame(s);
			Thread game = new Thread(cg);
			game.setDaemon(true);
			game.start();
			game.join();
			switch(cg.getResult()) {
				case Win:
					System.out.println("Has ganado");
					break;
				case Lose:
					System.out.println("Has perdido");
					break;
				case Error:
					System.out.println("La partida ha acabado de forma inesperada");
					break;
				case Cancelled:
					System.out.println("Se ha cancelado la ejecución");
					break;
				default:
					break;
			}
			cg.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}