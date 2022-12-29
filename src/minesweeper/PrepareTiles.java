package minesweeper;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class PrepareTiles implements Runnable{
	private int y;
	private Tile[][] tiles;
	private int width;
	private int height;
	private CyclicBarrier cb;
	
	public PrepareTiles(Tile[][] tiles, int y, int width, int height, CyclicBarrier cb) {
		this.y = y;
		this.tiles = tiles;
		this.width = width;
		this.height = height;
		this.cb = cb;
	}
	
	@Override
	public void run() {
		if(y == 0) {
			caseUpperRow();
		}else if(y == height - 1) {
			caseLowerRow();
		}else {
			caseMiddleRow();
		}
		try {
			cb.await();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void caseUpperRow() {
		List<Tile> neighbours = new LinkedList<Tile>();
		neighbours.add(tiles[1][0]);
		neighbours.add(tiles[1][1]);
		neighbours.add(tiles[0][1]);
		tiles[0][0].setNeighbours(neighbours);
		for(int x = 1; x < width - 1; x++) {
			neighbours = new LinkedList<Tile>();
			neighbours.add(tiles[x-1][0]);
			neighbours.add(tiles[x-1][1]);
			neighbours.add(tiles[x][1]);
			neighbours.add(tiles[x+1][1]);
			neighbours.add(tiles[x+1][0]);
			tiles[x][0].setNeighbours(neighbours);
		}
		neighbours = new LinkedList<Tile>();
		neighbours.add(tiles[width-2][0]);
		neighbours.add(tiles[width-2][1]);
		neighbours.add(tiles[width-1][1]);
		tiles[width-1][0].setNeighbours(neighbours);
	}
	
	private void caseMiddleRow() {
		List<Tile> neighbours = new LinkedList<Tile>();
		neighbours.add(tiles[0][y-1]);
		neighbours.add(tiles[1][y-1]);
		neighbours.add(tiles[1][y]);
		neighbours.add(tiles[1][y+1]);
		neighbours.add(tiles[0][y+1]);
		tiles[0][y].setNeighbours(neighbours);
		for(int x = 1; x < width - 1; x++) {
			neighbours = new LinkedList<Tile>();
			neighbours.add(tiles[x-1][y-1]);
			neighbours.add(tiles[x][y-1]);
			neighbours.add(tiles[x+1][y-1]);
			neighbours.add(tiles[x+1][y]);
			neighbours.add(tiles[x+1][y+1]);
			neighbours.add(tiles[x][y+1]);
			neighbours.add(tiles[x-1][y+1]);
			neighbours.add(tiles[x-1][y]);
			tiles[x][y].setNeighbours(neighbours);
		}
		neighbours = new LinkedList<Tile>();
		neighbours.add(tiles[width-1][y-1]);
		neighbours.add(tiles[width-2][y-1]);
		neighbours.add(tiles[width-2][y]);
		neighbours.add(tiles[width-2][y+1]);
		neighbours.add(tiles[width-1][y+1]);
		tiles[width-1][y].setNeighbours(neighbours);
	}
	
	private void caseLowerRow() {
		List<Tile> neighbours = new LinkedList<Tile>();
		neighbours.add(tiles[0][height-2]);
		neighbours.add(tiles[1][height-2]);
		neighbours.add(tiles[1][height-1]);
		tiles[0][height-1].setNeighbours(neighbours);
		for(int x = 1; x < width - 1; x++) {
			neighbours = new LinkedList<Tile>();
			neighbours.add(tiles[x-1][height-2]);
			neighbours.add(tiles[x][height-2]);
			neighbours.add(tiles[x+1][height-2]);
			neighbours.add(tiles[x+1][height-1]);
			neighbours.add(tiles[x-1][height-1]);
			tiles[x][height-1].setNeighbours(neighbours);
		}
		neighbours = new LinkedList<Tile>();
		neighbours.add(tiles[width-1][height-2]);
		neighbours.add(tiles[width-2][height-2]);
		neighbours.add(tiles[width-2][height-1]);
		tiles[width-1][height-1].setNeighbours(neighbours);
	}
}
