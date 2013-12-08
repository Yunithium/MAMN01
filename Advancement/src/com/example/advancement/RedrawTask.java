package com.example.advancement;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import com.example.advancement.R;

import android.app.Activity;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class RedrawTask extends TimerTask{
	private float[] speed = {0.0f, 0.0f};
	private float[] speedMultiplier = {2.0f, 2.3f};
	private float[] speedAdjust;
	private int[] displaySize;
	private long pastTime, currentTime;
	private short refreshTime;
	private boolean paused = false;
	private int activeActivity;
	public boolean firstSpeedRead;
	
	public CustomActivity parent;
	private FrameLayout mainView;
	private BallView ballView;
	private Handler RedrawHandler = new Handler();
	private Vibrator vibrator;
	
	private WallButton bottomButton, leftButton, rightButton, quitButton, playlistButton;	// Playback buttons
	
	public RedrawTask(short refreshTime, Display display){
		super();
		this.refreshTime = refreshTime;
		this.currentTime = SystemClock.uptimeMillis();
		this.firstSpeedRead = false;
		
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		displaySize = new int[2];
        displaySize[0] = outMetrics.widthPixels;
        displaySize[1] = outMetrics.heightPixels;
	}
	
	public void changeContext(CustomActivity parent, FrameLayout mainView){
		if(this.parent == parent && !paused)
			return;
		
		this.parent = parent;
		this.ballView = new BallView((Activity)parent, displaySize[0]/2, displaySize[1]/2, 30);
		this.mainView = mainView;
		
		ballView.setKeepScreenOn(true);
		this.mainView.addView(ballView);
		
		vibrator = (Vibrator) parent.getVibrator();
		paused = false;
		
		if(parent instanceof PlaybackActivity) activeActivity = 0;
		else if(parent instanceof PlaylistActivity) activeActivity = 1;
		
		if(activeActivity == 0){
			((PlaybackActivity)parent).activateButton(R.id.bar1);
			((PlaybackActivity)parent).activateButton(R.id.bar2);
			
	        bottomButton = new WallButton(0, displaySize[0], WallButton.ALIGNED_BOTTOM, 30, displaySize);
	        leftButton = new WallButton(333, 300, WallButton.ALIGNED_LEFT, 30, displaySize);
	        rightButton = new WallButton(333, 300, WallButton.ALIGNED_RIGHT, 30, displaySize);
	        
	        quitButton = new WallButton(0, 100, WallButton.ALIGNED_TOP, 30, displaySize);
	        playlistButton = new WallButton(displaySize[1]-100, displaySize[1], WallButton.ALIGNED_TOP, 30, displaySize);
		}
	}
	
	public void pause(){
		RedrawHandler.post(new Runnable(){ public void run(){ mainView.removeView(ballView); }});
		paused = true;
		firstSpeedRead = false;
	}
	
	public void updateSpeed(float[] speed){
		// Calibrate the zero-point from the initial positioning of the phone
		if(!firstSpeedRead && !paused){
			speedAdjust = new float[2];
			speedAdjust[0] = speed[0];
			speedAdjust[1] = speed[1];
			firstSpeedRead = true;
			return;
		}
		
		// Speed is the value read from the accelerometer and is adjusted.
		this.speed[0] = (speed[0] - speedAdjust[0]) * speedMultiplier[0];
		this.speed[1] = (speed[1] - speedAdjust[1]) * speedMultiplier[1];
	}
	
	public void updatePos(float[] position){
		ballView.x = position[0];
		ballView.y = position[1];
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

		// Making sure the ball is within the outermost confinement
		if(ballView.x < ballView.radius){ ballView.x = ballView.radius; }
		else if(ballView.x > displaySize[0]-ballView.radius){ ballView.x = displaySize[0]-ballView.radius; }
		
		if(ballView.y < ballView.radius){ ballView.y = ballView.radius; }
		else if(ballView.y > displaySize[1]-ballView.radius){ ballView.y = displaySize[1]-ballView.radius; }
		
		
		// AFTER THIS POINT THE ACTIVITY SPECIFIC CODING BEGINS
		if(activeActivity == 0) animatePlaybackActivity();
		else if(activeActivity == 1) animatePlaylistActivity();
		
		// REDRAW THE BALLVIEW
		RedrawHandler.post(new Runnable(){ public void run() {	ballView.invalidate(); }});
	}
	
	private void animatePlaybackActivity(){
		if(quitButton.contact(ballView.x, ballView.y)){
			ballView.y = ballView.radius + quitButton.thickness;
			if(quitButton.active){
				quitButton.active = false;
				vibrator.vibrate(25);
				
				paused = true;
				RedrawHandler.post(new Runnable(){ public void run(){ mainView.removeView(ballView); }});
				RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).quitApplication(); }});
				//RedrawHandler.post(new Runnable(){ public void run(){ parent.changeActivity(); }});
			}
		}
		
		if(bottomButton.contact(ballView.x, ballView.y)){
			ballView.y = displaySize[1] - 2*ballView.radius - bottomButton.thickness;
			if(bottomButton.active){
				bottomButton.active = false;
				vibrator.vibrate(25);
				RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).playPause(); }});
			}
		}
		
		if(leftButton.contact(ballView.x, ballView.y)){
			ballView.x = ballView.radius + leftButton.thickness;
			if(leftButton.active){
				leftButton.active = false;
				vibrator.vibrate(25);
				RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).previousTrack(); }});
			}
		}
		
		if(rightButton.contact(ballView.x, ballView.y)){
			ballView.x = displaySize[0] - ballView.radius - leftButton.thickness;
			if(rightButton.active){
				rightButton.active = false;
				vibrator.vibrate(25);
				RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).nextTrack(); }});
			}
		}
		
		
		// Checking for button reviving, only some buttons change when decontacted
		quitButton.decontact(ballView.x, ballView.y);
		bottomButton.decontact(ballView.x, ballView.y);
		if(leftButton.decontact(ballView.x, ballView.y))
			RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).activateButton(R.id.bar1); }});
		if(rightButton.decontact(ballView.x, ballView.y))
			RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).activateButton(R.id.bar2); }});
	}
	
	private void animatePlaylistActivity(){
		// This function is called when the PLAYLIST context is active
	}
}