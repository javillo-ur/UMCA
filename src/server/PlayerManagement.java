package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import model.Party;
import model.Player;

public class PlayerManagement implements Runnable{
	Socket s;
	Player player;
	
	public PlayerManagement(Socket s) {
		this.s = s;
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
		try{
			ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
			ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
			player = new Player(dis.readLine(), s.getInetAddress());
			boolean flag = true;
			while(!Thread.interrupted() && !s.isClosed() && flag) {
				int option = dis.readInt();
				switch(option) {
				case 0:
					getPartidas(dos);
				break;
				case 1:
					crearPartida(dos);
				break;
				case 2:
					unirsePartida(dos, dis);
				break;
				default:
					flag = false;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getPartidas(ObjectOutputStream dos) throws IOException {
		dos.writeObject(Server.parties);
		dos.flush();
	}
	
	public void crearPartida(ObjectOutputStream dos) throws IOException {
		Party party;
		synchronized(Server.parties){
			party = new Party(Server.parties.size(), player);
			Server.parties.add(party);
		}
		dos.writeObject(party);
		dos.flush();
	}
	
	public synchronized void unirsePartida(ObjectOutputStream dos, ObjectInputStream dis) 
			throws IOException, NullPointerException {
		dos.writeBoolean(Server.parties.get(dis.readInt()).add(player));
		dos.flush();
	}
}
