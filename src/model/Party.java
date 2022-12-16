package model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import server.Server;

public class Party implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private int id;
	private List<Player> players = new LinkedList<Player>();
	Player owner;
	Status status = Status.Waiting;
	
	public Party(int id, Player owner) {
		getPlayers().add(owner);
		this.owner = owner;
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		String ret = "Sala de " + getPlayers().get(0).getName() + "\r\n";
		ret += "Estado: " + StatusRep.statusToString(getStatus()) + "\r\n";
		ret += "Jugadores: " + "\r\n";
		for(Player player : getPlayers())
			ret += player.toString() + "\r\n";
		return ret;
	}
	
	public String name() {
		return "Sala de " + getPlayers().get(0).getName();
	}
	
	public void startGame() {
		status = Status.InGame;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public Status getStatus() {
		return status;
	}

	public boolean add(Player player) {
		players.add(player);
		return true;
	}
}
