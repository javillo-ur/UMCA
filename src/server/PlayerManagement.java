package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Enumeration;
import java.util.LinkedList;
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
			ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
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
		Enumeration<Party> parties = Server.parties.elements();
		List<Party> ret = new LinkedList<Party>();
		while(parties.hasMoreElements())
			ret.add(parties.nextElement());
		dos.writeObject(ret);
		dos.flush();
	}
	
	public void crearPartida(ObjectOutputStream dos) throws IOException {
		Party party = new Party(player);
		Server.parties.put(player.getName(), party);
		dos.writeObject(party);
		dos.flush();
	}
	
	@SuppressWarnings("deprecation")
	public synchronized void unirsePartida(ObjectOutputStream dos, ObjectInputStream dis) 
		throws IOException, NullPointerException {
		dos.writeBoolean(Server.parties.get(dis.readLine()).add(player));
		dos.flush();
	}
}
