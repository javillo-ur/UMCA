package minesweeper;

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Board implements Serializable{
	private static final long serialVersionUID = 1L;
	private int height;
	private int width;
	
	private Tile[][] tiles;
	
	private RingedList ring;
	
	public Board(int height, int width, int numBombs) {
		this.height = height;
		this.width = width;
		tiles = new Tile[height][width];
		ring = new RingedList();
		for(int i = 0; i < height*width; i++)
			ring.addTile(new Tile());
		for(int i = 0; i < numBombs; i++) {
			Random rand = new Random();
			Tile bomb = ring.getTile(rand.nextInt(width * height));
			while(bomb.isHot()) {
				bomb = ring.getTile(rand.nextInt(width * height));
			}
			bomb.setHot();
		}
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public Tile[][] getTiles(){
		return tiles;
	}
	
	public Tile get(int x, int y) {
		return tiles[x][y];
	}
	
	public void rectify(int x, int y) throws InterruptedException, BrokenBarrierException {
		int posClick = y * width + x;
		Random rand = new Random();
		int turn = rand.nextInt(height*width);
		while(ring.getTile(turn + posClick).isHot()) {
			turn = rand.nextInt();
		}
		ring.move(turn + posClick);
		prepareTiles();
	}

	private void prepareTiles() throws InterruptedException, BrokenBarrierException {
		RingedListBase base = ring.getBase();
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++) {
				tiles[x][y] = base.getTile();
				base = base.getNext();
			}
		ExecutorService es = Executors.newCachedThreadPool();
		CyclicBarrier cb = new CyclicBarrier(width + 1);
		for(int y = 0; y < height; y++)
			es.submit(new PrepareTiles(tiles, y, width, height, cb));
		cb.await();
	}
}
