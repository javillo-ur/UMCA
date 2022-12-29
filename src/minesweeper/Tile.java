package minesweeper;

import java.io.Serializable;
import java.util.List;

public class Tile implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private boolean hot = false;
	private List<Tile> neighbours;
	
	public boolean isHot() {
		return hot;
	}

	public void setHot() {
		this.hot = true;
	}
	
	public List<Tile> getNeighbours(){
		return neighbours;
	}
	
	public void setNeighbours(List<Tile> neighbours) {
		this.neighbours = neighbours;
	}
	
	public int getNumber() {
		int ret = 0;
		for(Tile tile : neighbours)
			if(tile.isHot())
				ret++;
		return ret;
	}
}
