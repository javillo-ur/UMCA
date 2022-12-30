package client;

import java.net.ServerSocket;

import model.Party;

public class PartyListener {
	private Party party;
	private ServerSocket ss;
	private boolean isOwner;
	private int ownerPort;
	private String playerName;
	
	public PartyListener(Party party, int ownerPort, String playerName) {
		this.party = party;
		this.ownerPort = ownerPort;
		this.isOwner = false;
		this.playerName = playerName;
	}
	
	public PartyListener(Party party, ServerSocket ss, String playerName) {
		this.party = party;
		this.ss = ss;
		this.isOwner = true;
		this.playerName = playerName;
	}
	
	public ServerSocket getServerSocket() {
		return ss;
	}
	
	public Party getParty() {
		return party;
	}
	
	public void setParty(Party party) {
		this.party = party;
	}
	
	public boolean isOwner() {
		return isOwner;
	}
	
	public int getOwnerPort() {
		return ownerPort;
	}

	public String getPlayerName() {
		return playerName;
	}
}
