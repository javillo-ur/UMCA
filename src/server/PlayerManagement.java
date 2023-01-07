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
	
	ServerLogger logger;
	
	public PlayerManagement(Socket s, ExecutorService es, ServerLogger sl) {
		this.s = s;
		this.es = es;
		logger = sl;
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
		String name = null;
		try{
			ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
			
			//El cliente especifica si es un cliente nuevo (enviando false), o llega para notificar el comienzo
			//de una partida de la que es propietario (enviando verdadero)
			if(dis.readBoolean()) {
				String owner = dis.readLine();
				comenzarPartida(owner);
				s.close();
				return;
			}
			
			//El cliente envía un nombre, y se verifica y añade si no es repetido. Si es repetido, se le insta
			//a introducir un nuevo nombre
			boolean flag = true;
			while(flag) {
				name = dis.readLine();
				if(name == null)
					return;
				synchronized (Server.players) {
					if(!Server.players.contains(name)) {
						flag = false;
						Server.players.add(name);
						logger.addLog(name + " se ha unido");
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
			
			//El cliente tiene tres opciones: ver las salas existentes, crear una, o unirse a una. Si crea una
			//sala, proporcionará el número de puerto en el que escuchará a clientes que se quieran unir, y se
			//desconectará. Para notificar el inicio de la partida, creará una conexión nueva.
			//En caso de unirse a una sala nueva, el servidor proporcionará al cliente la dirección y puerto
			//del propietario de la partida
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
			if(name != null && Server.players.contains(name)) {
				Server.players.remove(name);
				logger.addLog(name + " se ha desconectado");
			}
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
		logger.addLog(player.getName() + " ha creado una sala");
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
				logger.addLog(player.getName() + " se ha unido a la sala de " + party.getOwner().getName());
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
						logger.addLog(player.getName() + " se ha desconectado");
					}
				}
				Server.parties.remove(owner);
				logger.addLog(owner + " ha iniciado la partida");
				logger.addLog(owner + " se ha desconectado");
			}
		}
	}
}
