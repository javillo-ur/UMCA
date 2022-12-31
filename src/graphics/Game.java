package graphics;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

import minesweeper.Board;
import minesweeper.RingedList;
import minesweeper.Tile;
import model.Message;

public class Game implements Serializable{
	private static final long serialVersionUID = 1L;

	private RingedList<Message<String>> turnNames;
	private Board board = new Board(16, 30, 99);
	private boolean firstClick = true;

	public Game(List<String> turnNames) {
		this.turnNames = new RingedList<Message<String>>();
		int i = 0;
		for(String name : turnNames)
			this.turnNames.addTile(new Message<String>(name, i++));
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
		if(firstClick)
			board.rectify(x, y);
		return board.click(x, y);
	}

	public Tile get(int x, int y) {
		return board.get(x, y);
	}

	public void removePlayer() {
		turnNames.removeCurrent();
	}
}
