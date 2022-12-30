package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import model.Party;
import model.Player;

public class PlayerManagement implements Runnable{
	Socket s;
	Player player;
	ExecutorService es;
	
	public PlayerManagement(Socket s, ExecutorService es) {
		this.s = s;
		this.es = es;
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
		try{
			ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
			if(dis.readBoolean()) {
				String owner = dis.readLine();
				comenzarPartida(owner);
				s.close();
				return;
			}
			String name = null;
			boolean flag = true;
			while(flag) {
				name = dis.readLine();
				if(name == null)
					return;
				synchronized (Server.players) {
					if(!Server.players.contains(name)) {
						flag = false;
						Server.players.add(name);
					} else {
						dos.writeBoolean(false);
						dos.flush();
					}
				}
			}
			player = new Player(name, s.getInetAddress());
			dos.writeBoolean(true);
			dos.flush();
			flag = true;
			while(!Thread.interrupted() && !s.isClosed() && flag) {
				int option = dis.readInt();
				switch(option) {
				case 0:
					getPartidas(dos);
				break;
				case 1:
					crearPartida(dos, dis);
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
	
	public void crearPartida(ObjectOutputStream dos, ObjectInputStream dis) throws IOException {
		player.setUpdatePort(dis.readInt());
		Party party = new Party(player);
		Server.parties.put(player.getName(), party);
		dos.writeObject(party);
		dos.flush();
	}
	
	@SuppressWarnings("deprecation")
	public void unirsePartida(ObjectOutputStream dos, ObjectInputStream dis) 
		throws IOException, NullPointerException {
		Party party = null;
		synchronized(Server.parties) {
			String owner = dis.readLine();
			party = Server.parties.get(owner);
			if(party == null) {
				dos.writeBoolean(false);
				dos.flush();
			}
			else {
				dos.writeBoolean(party.add(player));
				dos.writeInt(party.getOwner().getUpdatePort());
				dos.flush();
			}
		}
	}
	
	public void comenzarPartida(String owner) {
		synchronized(Server.parties) {
			Party partida = Server.parties.get(owner);
			if(partida != null) {
				synchronized(Server.players) {
					for(Player player : partida.getPlayers()) {
						Server.players.remove(player.getName());
					}
				}
				Server.parties.remove(owner);
			}
		}
	}
}
