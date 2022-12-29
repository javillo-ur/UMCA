package minesweeper;

public class RingedList {
	private RingedListBase base;
	
	public RingedList() {}
	
	public Tile getTile() {
		return base.getTile();
	}
	
	public void addTile(Tile tile) {
		if(base == null)
			base = new RingedListBase(tile);
		else base.addLinkNext(tile);
	}
	
	public void move(int pos) {
		base = base.move(pos);
	}
	
	public Tile getTile(int pos) {
		return base.getTile(pos);
	}

	public RingedListBase getBase() {
		return base;
	}
}
