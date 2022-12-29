package minesweeper;

public class RingedListBase{
	private RingedListBase next;
	private Tile tile;
	
	public RingedListBase(Tile tile) {
		next = this;
		this.tile = tile;
	}

	public Tile getTile() {
		return tile;
	}
	
	public void setTile(Tile tile) {
		this.tile = tile;
	}
	
	public void addLinkNext(Tile tile) {
		RingedListBase next = new RingedListBase(tile);
		next.next = this.next;
		this.next = next;
	}

	public RingedListBase move(int pos) {
		RingedListBase it = this;
		for(int i = 0; i < pos - 1; i++)
			it = it.next;
		return it;
	}

	public Tile getTile(int pos) {
		RingedListBase it = this;
		for(int i = 0; i < pos; i++)
			it = it.next;
		return it.tile;
	}

	public RingedListBase getNext() {
		return next;
	}
}
