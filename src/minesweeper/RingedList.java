package minesweeper;

import java.io.Serializable;

public class RingedList<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private RingedListBase<T> base;
	private RingedListBase<T> previous;
	
	public RingedList() {}
	
	public T getTile() {
		return base.getTile();
	}
	
	public void addTile(T tile) {
		if(base == null)
			base = new RingedListBase<T>(tile);
		else {
			if(previous == null)
				previous = base;
			base.addLinkNext(tile);
		}
	}
	
	public void move(int pos) {
		base = base.move(pos);
		if(previous != null)
			previous = previous.move(pos);
	}
	
	public T getTile(int pos) {
		return base.getTile(pos);
	}

	public RingedListBase<T> getBase() {
		return base;
	}

	public void removeCurrent() {
		previous.setNext(base.getNext());
		base = base.getNext();
	}
}
