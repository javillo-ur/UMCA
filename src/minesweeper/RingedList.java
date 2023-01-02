package minesweeper;

import java.io.Serializable;

public class RingedList<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int length = 0;
	
	private RingedListBase<T> base;
	private RingedListBase<T> previous;
	
	public RingedList() {}
	
	public T getTile() {
		return base.getTile();
	}
	
	public void addTile(T tile) {
		if(base == null)
			base = new RingedListBase<T>(tile);
		else if(length > 1) {
			base.addLinkNext(tile);
		} else {
			previous = new RingedListBase<T>(tile);
			previous.setNext(base);
			base.setNext(previous);
		}
		length++;
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
		if(previous != null) {
			RingedListBase<T> next = base.getNext();
			previous.setNext(next);
			base = next;
		} else {
			base = null;
		}
		length--;
	}
	
	public int getLength() {
		return length;
	}
}
