package com.example.migration;

public class WallButton{
	public static final int ALIGNED_TOP = 0;
	public static final int ALIGNED_BOTTOM = 1;
	public static final int ALIGNED_LEFT = 2;
	public static final int ALIGNED_RIGHT = 3;
	
	public int start, length, alignation, thickness;
	public int radius, offset;
	public int[] displaySize;
	public boolean active;
	
	public WallButton(int start, int length, int alignation, int radius, int[] displaySize){
		this.start = start;
		this.length = length;
		this.alignation = alignation;
		this.radius = radius;
		
		this.displaySize = new int[2];
		this.displaySize[0] = displaySize[0];
		this.displaySize[1] = displaySize[1];
		
		thickness = 10;
		offset = 40;
		active = true;
	}
	
	public boolean contact(float x, float y){
		switch(alignation){
		case ALIGNED_TOP:		return y <= radius+thickness && x >= start && x <= start+length;
		case ALIGNED_BOTTOM:	return y >= displaySize[1]-2*radius-thickness && x >= start && x <= start+length;
		case ALIGNED_LEFT:		return x <= radius+thickness && y >= start && y <= start+length;
		default:				return x >= displaySize[0]-radius-thickness && y >= start && y <= start+length;
		}
	}
	
	public boolean decontact(float x, float y){
		boolean temp;
		switch(alignation){
		case ALIGNED_TOP:		temp = y >= radius+thickness+offset || x <= start-offset || x >= start+length+offset; break;
		case ALIGNED_BOTTOM:	temp = y <= displaySize[1]-2*radius-thickness-offset || x <= start-offset || x >= start+length+offset; break;
		case ALIGNED_LEFT:		temp = x >= radius+thickness+offset || y <= start-offset || y >= start+length+offset; break;
		default:				temp = x <= displaySize[0]-radius-thickness-offset || y <= start-offset || y >= start+length+offset;
		}
		
		if(temp && !active)
			active = true;
		
		return temp;
	}
}