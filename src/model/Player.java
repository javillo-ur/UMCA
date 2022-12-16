package model;

import java.io.Serializable;
import java.net.InetAddress;

public class Player implements Serializable{
	private static final long serialVersionUID = 1L;
	private String name;
	InetAddress add;
	
	public Player(String name, InetAddress inetAddress) {
		this.name = name;
		this.add = inetAddress;
	}

	@Override
	public String toString() {
		return "Jugador " + getName() + " (" + add.getHostAddress() + ")";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
