package model;

import java.io.Serializable;
import java.net.InetAddress;

public class Player implements Serializable{
	private static final long serialVersionUID = 1L;
	private String name;
	private InetAddress add;
	private int updatePort = -1;
	
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
	
	public int getUpdatePort() {
		return updatePort;
	}
	
	public void setUpdatePort(int updatePort) {
		this.updatePort = updatePort;
	}

	public String getAddress() {
		return add.getHostAddress();
	}
}
