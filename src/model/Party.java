package model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Party implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private List<Player> players = new LinkedList<Player>();
	Player owner;
	Status status = Status.Waiting;
	
	public Party(Player owner) {
		getPlayers().add(owner);
		this.owner = owner;
	}
	
	@Override
	public String toString() {
		return "Sala de " + owner.getName();
	}
	
	public String getSummary() {
		String ret = "Sala de " + getPlayers().get(0).getName() + "\r\n";
		ret += "Estado: " + StatusRep.statusToString(getStatus()) + "\r\n";
		ret += "Jugadores: " + "\r\n";
		for(Player player : getPlayers())
			ret += player.toString() + "\r\n";
		return ret;
	}
	
	public String getName() {
		return "Sala de " + owner.getName();
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

	public String getOwner() {
		return this.owner.getName();
	}
}
