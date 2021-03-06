package com.example.migration;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

public class RollingStone extends Application{
	private static RollingStone instance;
	public int[] trax;
	public int currentSong;
	private ArrayList<String> playlist;
	private RedrawTask redrawTask;
	
	public static RollingStone getInstance(){
		if(instance==null)
			instance = new RollingStone();
		
		return instance;
	}
	
	private RollingStone(){
		trax = new int[2];
		trax[0] = R.raw.pling;
		trax[1] = R.raw.plong;
		currentSong = 0;
		
		// Temporary code
		
	}
	
	public void setPlaylist(ArrayList<String> playlist){
		this.playlist = new ArrayList<String>(playlist);
	}
	
	public ArrayList<String> getPlaylist(){
		return playlist;
	}
	
	public void setRedrawTask(RedrawTask redrawTask){
		this.redrawTask = redrawTask;
	}
	
	public RedrawTask getRedrawTask(){
		return redrawTask;
	}
}