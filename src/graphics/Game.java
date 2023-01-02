package graphics;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

import minesweeper.Board;
import minesweeper.RingedList;
import minesweeper.Tile;
import model.Message;
import model.Turn;

public class Game implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private int rectification = -1;
	
	private int milliseconds = 10000;

	private RingedList<Message> turnNames;
	private Board board = new Board(16, 30, 99);
	private boolean firstClick = true;
	
	private int x;
	private int y;

	public Game(List<String> turnNames) {
		this.turnNames = new RingedList<Message>();
		int i = 0;
		for(String name : turnNames)
			this.turnNames.addTile(new Message(name, i++, 0));
	}

	public int getTurn() {
		return turnNames.getTile().getPort();
	}
	
	public void nextTurn() {
		turnNames.move(1);
	}
	
	public String getTurnName() {
		return (String) turnNames.getTile().getMessage();
	}
	
	public Tile click(int x, int y) throws InterruptedException, BrokenBarrierException {
		if(x == -1) {
			removePlayer();
			return null;
		}
		
		if(firstClick) {
			rectification = board.rectify(x, y, rectification);
			firstClick = false;
		}
		Tile tile = board.click(x, y);
		this.x = x;
		this.y = y;
		if(tile.isHot()) {
			removePlayer();
		}
		else
			nextTurn();
		return tile;
	}

	public Tile get(int x, int y) {
		return board.get(x, y);
	}

	public void removePlayer() {
		turnNames.removeCurrent();
	}
	
	public boolean isEnded() {
		return turnNames.getLength() < 2;
	}

	public int getNumPlayers() {
		return turnNames.getLength();
	}

	public boolean isInitialised() {
		return !firstClick;
	}

	public Turn lastTurnSummary() {
		return new Turn(x, y, rectification);
	}
	
	public void setRectification(int rectification) {
		this.rectification = rectification;
	}
	
	public int getMillis() {
		int ret = milliseconds;
		milliseconds = (int) Math.round(ret * 0.9);
		return ret;
	}
}
