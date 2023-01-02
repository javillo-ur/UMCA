package minesweeper;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Board implements Serializable{
	private static final long serialVersionUID = 1L;
	private int height;
	private int width;
	
	private int blankPos;
	
	private Tile[][] tiles;
	
	private RingedList<Tile> ring;
	
	public Board(int height, int width, int numBombs) {
		this.height = height;
		this.width = width;
		tiles = new Tile[width][height];
		ring = new RingedList<Tile>();
		for(int i = 0; i < height*width; i++)
			ring.addTile(new Tile());
		Random rand = new Random();
		blankPos = rand.nextInt(width * height);
		List<Integer> safeSpaces = new LinkedList<Integer>();
		for(int x = -1; x < 2; x++) {
			for(int y = -1; y < 2; y++) {
				safeSpaces.add(blankPos + (y * width) + x);
			}
		}
		for(int i = 0; i < numBombs; i++) {
			int nextPos = rand.nextInt(width * height);
			Tile bomb = ring.getTile(nextPos);
			while(safeSpaces.contains(nextPos) || bomb.isHot()) {
				nextPos = rand.nextInt(width * height);
				bomb = ring.getTile(nextPos);
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
	
	public int rectify(int x, int y, int rectification) throws InterruptedException, BrokenBarrierException {
		if(rectification != -1) {
			ring.move(rectification);
			prepareTiles();
			return rectification;
		} else {
			int posClick = y * width + x;
			int turn;
			if(posClick < blankPos) {
				turn = blankPos - posClick;
			} else {
				turn = ring.getLength() - posClick + blankPos;
			}
			ring.move(turn);
			prepareTiles();
			return turn;
		}
	}

	private void prepareTiles() throws InterruptedException, BrokenBarrierException {
		RingedListBase<Tile> base = ring.getBase();
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++) {
				tiles[x][y] = base.getTile();
				base = base.getNext();
			}
		ExecutorService es = Executors.newCachedThreadPool();
		CyclicBarrier cb = new CyclicBarrier(height + 1);
		for(int y = 0; y < height; y++)
			es.submit(new PrepareTiles(tiles, y, width, height, cb));
		cb.await();
		es.shutdown();
	}

	public Tile click(int x, int y) {
		Tile ret = get(x, y);
		ret.setDisplayed();
		if(ret.getNeighbourBombs() == 0) {
			ExecutorService es = Executors.newCachedThreadPool(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setDaemon(true);
					return thread;
				}
			});
			for(Tile neighbour : ret.getNeighbours()) {
				if(!neighbour.isDisplayed()) {
					es.submit(new Runnable() {
						@Override
						public void run() {
							Thread.currentThread().setName("Expandir click");
							propagateClick(neighbour, es);
						}
					});
				}
			}
			try {
				es.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	private void propagateClick(Tile tile, ExecutorService es) {
		tile.setDisplayed();
		if(tile.getNeighbourBombs() == 0) {
			for(Tile neighbour : tile.getNeighbours()) {
				if(!neighbour.isDisplayed()) {
					es.submit(new Runnable() {
						@Override
						public void run() {
							Thread.currentThread().setName("Expandir click");
							propagateClick(neighbour, es);
						}
					});
				}
			}
		}
	}
}
