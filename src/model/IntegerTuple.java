package model;

import java.io.Serializable;

public class IntegerTuple<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private T object;
	private int integer;
	
	public int getInteger() {
		return integer;
	}
	
	public T getObject() {
		return object;
	}
	
	public IntegerTuple(T object, int integer) {
		this.integer = integer;
		this.object = object;
	}
}
