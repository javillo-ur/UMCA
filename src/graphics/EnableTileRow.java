package graphics;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import javax.swing.JButton;

import minesweeper.Tile;

public class EnableTileRow implements Runnable {
	private int row;
	private Game game;
	private JButton[][] buttons;
	private int width;
	private CyclicBarrier cb;
	
	public EnableTileRow(Game game, JButton[][] buttons, int rowNum, int width, CyclicBarrier cb) {
		this.game = game;
		this.row = rowNum;
		this.buttons = buttons;
		this.width = width;
		this.cb = cb;
	}
	
	@Override
	public void run() {
		for(int i = 0; i < width; i++) {
			Tile tile = game.get(i, row);
			JButton button = buttons[i][row];
			if(tile.isDisplayed()) {
				button.setEnabled(false);
				if(tile.isHot()) {
					button.setText("B");
				} else button.setText(tile.getNeighbourBombs() + "");
			} else button.setEnabled(true);
		}
		try {
			cb.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
	}

}
