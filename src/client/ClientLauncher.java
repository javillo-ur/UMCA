package client;

import java.io.IOException;
import java.net.Socket;

import javax.swing.JOptionPane;

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
					JOptionPane.showMessageDialog(null, "Has ganado");
					break;
				case Lose:
					JOptionPane.showMessageDialog(null, "Has perdido");
					break;
				case Error:
					JOptionPane.showMessageDialog(null, "La partida ha acabado de forma inesperada");
					break;
				case Cancelled:
					JOptionPane.showMessageDialog(null, "Se ha cancelado la ejecución");
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