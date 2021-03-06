package com.example.migration;

import java.util.TimerTask;

import android.app.Activity;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.FrameLayout;

public class RedrawTask extends TimerTask{
	private float[] speed = {0.0f, 0.0f};
	private float[] speedMultiplier = {2.0f, 2.3f};
	private float[] speedAdjust;
	private float[] speedCap = {7.0f, 10.0f};
	private int[] displaySize;
	private long pastTime, currentTime;
	private short refreshTime;
	private boolean firstSpeedRead = false;
	private boolean paused = false;
	
	private CustomActivity parent;
	private FrameLayout mainView;
	private BallView ballView;
	private Handler RedrawHandler = new Handler();
	private Vibrator vibrator;
	
	private boolean testWallActivated = true;
	
	public RedrawTask(short refreshTime, Display display){
		super();
		this.refreshTime = refreshTime;
		this.currentTime = SystemClock.uptimeMillis();
		
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		displaySize = new int[2];
        displaySize[0] = outMetrics.widthPixels;
        displaySize[1] = outMetrics.heightPixels;
	}
	
	public void changeContext(CustomActivity parent, FrameLayout mainView){
		this.parent = parent;
		this.ballView = new BallView((Activity)parent, 200, 200, 30);
		this.mainView = mainView;
		
		ballView.setKeepScreenOn(true);
		this.mainView.addView(ballView);
		
		vibrator = (Vibrator) parent.getVibrator();
		paused = false;
	}
	
	public void pause(){
		RedrawHandler.post(new Runnable(){ public void run(){ mainView.removeView(ballView); }});
		paused = true;
		firstSpeedRead = false;
	}
	
	public void updateSpeed(float[] speed){
		// Calibrate the zero-point from the initial positioning of the phone
		if(!firstSpeedRead){
			speedAdjust = new float[2];
			speedAdjust[0] = speed[0];
			speedAdjust[1] = speed[1];
			firstSpeedRead = true;
			return;
		}
		
		// Speed limit implementation
		//if(speed[0] > speedCap[0]) speed[0] = speedCap[0];
		//if(speed[1] > speedCap[1]) speed[1] = speedCap[1];
		
		// Speed is the value read from the accelerometer and is adjusted.
		this.speed[0] = (speed[0] - speedAdjust[0]) * speedMultiplier[0];
		this.speed[1] = (speed[1] - speedAdjust[1]) * speedMultiplier[1];
	}
	
	public void run(){
		if(paused) return;
		
		// Was it really 10 milliseconds since the last frame?
		pastTime = currentTime;
		currentTime = SystemClock.uptimeMillis();
		float ratio = (currentTime - pastTime) / refreshTime;
		
		// Move the ball
		ballView.x += speed[0] * ratio;
		ballView.y += speed[1] * ratio;
		
		// Wall-hit-detection in x-direction
		if(ballView.x < ballView.radius) ballView.x = ballView.radius;
		else if(ballView.x > displaySize[0]-ballView.radius) ballView.x = displaySize[0]-ballView.radius;
		
		// Wall-hit-detection in y-direction
		if(ballView.y < ballView.radius) ballView.y = ballView.radius;
		else if(ballView.y > displaySize[1]-2*ballView.radius) ballView.y = displaySize[1]-2*ballView.radius;
		
		
		// AFTER THIS POINT THE ACTIVITY SPECIFIC CODING BEGINS
		
		// Detecting Hits
		if(ballView.y == displaySize[1]-2*ballView.radius && testWallActivated){
			vibrator.vibrate(25);
			testWallActivated = false;
			
			RedrawHandler.post(new Runnable(){ public void run(){ mainView.removeView(ballView); }});
			RedrawHandler.post(new Runnable(){ public void run(){ parent.changeActivity(); }});
		}
		if(ballView.y < displaySize[1]-10*ballView.radius && !testWallActivated){
			testWallActivated = true;
		}
		
		// To do:
		// - Sticky Walls? When on a wall the ball won't roll along the wall.
		// - It's easier to tilt the device to the left than to the right and up than down.
		//   Implement different speed multipliers (or have a ratio multiplication).
		//   Draw a circle with your hand (by rotating the device) and observe how alike a circle the movement of the ball is.
		RedrawHandler.post(new Runnable(){ public void run() {	ballView.invalidate(); }});
	}
	
}