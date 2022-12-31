package minesweeper;

import java.io.Serializable;
import java.util.List;

public class Tile implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private boolean hot = false;
	private List<Tile> neighbours;
	private boolean displayed = false;
	
	private int neighbourBombs = -1;
	
	private void computeNeighbourBombs() {
		neighbourBombs = 0;
		for(Tile neighbour : neighbours)
			if(neighbour.isHot())
				neighbourBombs++;
	}
	
	public int getNeighbourBombs() {
		if(neighbourBombs == -1)
			computeNeighbourBombs();
		return neighbourBombs;
	}
	
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

	public void setDisplayed() {
		displayed = true;
	}
	
	public boolean isDisplayed() {
		return displayed;
	}
}
