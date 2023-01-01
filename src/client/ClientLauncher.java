package client;

import java.awt.Frame;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JOptionPane;

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
			Frame frame = new Frame();
			switch(cg.getResult()) {
			case Win:
				JOptionPane.showMessageDialog(frame, "Has ganado");
				break;
			case Lose:
				JOptionPane.showMessageDialog(frame, "Has perdido");
				break;
			case Error:
				JOptionPane.showMessageDialog(frame, "La partida ha acabado de forma inesperada");
				break;
			}
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