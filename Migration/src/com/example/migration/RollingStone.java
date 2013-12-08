package com.example.migration;

import android.app.Application;

public class RollingStone extends Application{
	private static RollingStone instance;
	public int[] playlist;
	public int currentSong;
	private RedrawTask redrawTask;
	
	public static RollingStone getInstance(){
		if(instance==null)
			instance = new RollingStone();
		
		return instance;
	}
	
	private RollingStone(){
		playlist = new int[2];
		playlist[0] = R.raw.pling;
		playlist[1] = R.raw.plong;
		currentSong = 0;
	}
	
	public void setRedrawTask(RedrawTask redrawTask){
		this.redrawTask = redrawTask;
	}
	
	public RedrawTask getRedrawTask(){
		return redrawTask;
	}
}