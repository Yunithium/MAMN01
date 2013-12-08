package com.example.migration;

import android.app.Application;

public class RollingStone extends Application{
	private static RollingStone instance;
	private String testString;
	private RedrawTask redrawTask;
	
	public static RollingStone getInstance(){
		if(instance==null)
			instance = new RollingStone();
		
		return instance;
	}
	
	private RollingStone(){
		testString = "empty string bro, sorry...";
	}
	
	
	
	public void setRedrawTask(RedrawTask redrawTask){
		this.redrawTask = redrawTask;
	}
	public RedrawTask getRedrawTask(){
		return redrawTask;
	}
	
	
	
	public void setString(String s){
		testString = s;
	}
	
	public String getString(){
		return testString;
	}
}