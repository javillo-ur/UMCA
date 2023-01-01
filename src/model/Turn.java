package model;

import java.io.Serializable;

public class Turn implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int x;
	private int y;

	private int rectification;
	
	public Turn(int x, int y, int rectification) {
		this.x = x;
		this.y = y;
		this.rectification = rectification;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getRectification() {
		return rectification;
	}
}
