package minesweeper;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Board implements Serializable{
	private static final long serialVersionUID = 1L;
	private int height;
	private int width;
	
	private Tile[][] tiles;
	
	private RingedList<Tile> ring;
	
	public Board(int height, int width, int numBombs) {
		this.height = height;
		this.width = width;
		tiles = new Tile[width][height];
		ring = new RingedList<Tile>();
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
	
	public int rectify(int x, int y, int rectification) throws InterruptedException, BrokenBarrierException {
		if(rectification != -1) {
			ring.move(rectification);
			prepareTiles();
			return rectification;
		} else {
			int posClick = y * width + x;
			Random rand = new Random();
			int turn = rand.nextInt(height*width);
			while(ring.getTile(turn + posClick).isHot()) {
				turn = rand.nextInt();
			}
			ring.move(turn + posClick);
			prepareTiles();
			return turn + posClick;
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
			ExecutorService es = Executors.newFixedThreadPool(8);
			List<Callable<Void>> propagations = new LinkedList<Callable<Void>>();
			for(Tile neighbour : ret.getNeighbours()) {
				if(!neighbour.isDisplayed()) {
					propagations.add(new Callable<Void>() {
						@Override
						public Void call() {
							Thread.currentThread().setName("Expandir click");
							propagateClick(neighbour, es);
							return null;
						}
					});
				}
			}
			try {
				es.invokeAll(propagations);
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
