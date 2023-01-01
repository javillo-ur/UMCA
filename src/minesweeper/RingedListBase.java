package minesweeper;

import java.io.Serializable;

public class RingedListBase<T> implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private RingedListBase<T> next;
	private T tile;
	
	public RingedListBase(T tile) {
		next = this;
		this.tile = tile;
	}

	public T getTile() {
		return tile;
	}
	
	public void setTile(T tile) {
		this.tile = tile;
	}
	
	public void addLinkNext(T tile) {
		RingedListBase<T> next = new RingedListBase<T>(tile);
		next.next = this.next;
		this.next = next;
	}

	public RingedListBase<T> move(int pos) {
		RingedListBase<T> it = this;
		for(int i = 0; i < pos; i++)
			it = it.next;
		return it;
	}

	public T getTile(int pos) {
		RingedListBase<T> it = this;
		for(int i = 0; i < pos; i++)
			it = it.next;
		return it.tile;
	}

	public RingedListBase<T> getNext() {
		return next;
	}

	public void setNext(RingedListBase<T> next2) {
		this.next = next2;
	}
}
