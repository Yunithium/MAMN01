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
	public boolean paused = false;
	private int activeActivity;
	private int specialTopAdjustment = 75;
	
	public CustomActivity parent;
	private FrameLayout mainView;
	private BallView ballView;
	private Handler RedrawHandler = new Handler();
	private Vibrator vibrator;
	
	private WallButton playPauseButton, previousButton, nextButton, quitButton, playlistButton;	// Playback buttons
	
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
			((PlaybackActivity)parent).activateButton(R.id.quit);
			((PlaybackActivity)parent).activateButton(R.id.playlist);
			((PlaybackActivity)parent).activateButton(R.id.play_pause);
			((PlaybackActivity)parent).activateButton(R.id.previous);
			((PlaybackActivity)parent).activateButton(R.id.next);
			
			quitButton = new WallButton(0, 75, WallButton.ALIGNED_TOP, 30, displaySize);
	        playlistButton = new WallButton(displaySize[0]-75, displaySize[0], WallButton.ALIGNED_TOP, 30, displaySize);
			
	        playPauseButton = new WallButton(75, 320, WallButton.ALIGNED_BOTTOM, 30, displaySize);
	        previousButton = new WallButton(333, 300, WallButton.ALIGNED_LEFT, 30, displaySize);
	        nextButton = new WallButton(333, 300, WallButton.ALIGNED_RIGHT, 30, displaySize);
		}else if(activeActivity == 1){
			
		}
	}
	
	public void pause(){
		RedrawHandler.post(new Runnable(){ public void run(){ mainView.removeView(ballView); }});
		paused = true;
	}
	
	public void updateSpeed(float[] speed, boolean updateZero){
		// Calibrate the zero-point from the initial positioning of the phone
		if(updateZero){
			speedAdjust = new float[2];
			speedAdjust[0] = speed[0];
			speedAdjust[1] = speed[1];
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
	
	public void resetPos(){
		ballView.x = displaySize[0]/2;
		ballView.y = displaySize[1]/2;
	}

	public void run(){
		if(paused){ return; }
		
		// Was it really 10 milliseconds since the last frame?
		pastTime = currentTime;
		currentTime = SystemClock.uptimeMillis();
		float ratio = (currentTime - pastTime) / refreshTime;
		
		// Move the ball
		float factor = 0.6f;
		if(speed[0] > 0) ballView.x += 1.5f * speed[0] * ratio * factor;
		if(speed[0] < 0) ballView.x += 1.3f * speed[0] * ratio * factor;
		
		if(speed[1] > 0) ballView.y += 3.1f * speed[1] * ratio * factor;
		if(speed[1] < 0) ballView.y += 1.6f * speed[1] * ratio * factor;

		// Making sure the ball is within the outermost confinement
		if(ballView.x < ballView.radius){ ballView.x = ballView.radius; }
		else if(ballView.x > displaySize[0]-ballView.radius){ ballView.x = displaySize[0]-ballView.radius; }
		
		if(ballView.y < ballView.radius + specialTopAdjustment){ ballView.y = ballView.radius + specialTopAdjustment; } // specialTopAdjustment
		else if(ballView.y > displaySize[1]-2*ballView.radius-8){ ballView.y = displaySize[1]-2*ballView.radius-8; }
		
		
		// AFTER THIS POINT THE ACTIVITY SPECIFIC CODING BEGINS
		if(activeActivity == 0) animatePlaybackActivity();
		else if(activeActivity == 1) animatePlaylistActivity();
		
		// REDRAW THE BALLVIEW and SCROLL A LITTLE
		RedrawHandler.post(new Runnable(){ public void run() {	ballView.invalidate(); }});
	}
	
	private void animatePlaybackActivity(){
		if(quitButton.contact(ballView.x, ballView.y - specialTopAdjustment)){
			ballView.y = ballView.radius + quitButton.thickness + specialTopAdjustment;
			if(quitButton.active){
				quitButton.active = false;
				vibrator.vibrate(25);
				
				paused = true;
				RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).minimizeApplication(); }});
				RedrawHandler.post(new Runnable(){ public void run(){ mainView.removeView(ballView); }});
			}
		}
		
		if(playlistButton.contact(ballView.x, ballView.y - specialTopAdjustment)){
			ballView.y = ballView.radius + playlistButton.thickness + specialTopAdjustment;
			if(playlistButton.active){
				playlistButton.active = false;
				vibrator.vibrate(25);
				
				paused = true;
				RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).changeActivity(); }});
				RedrawHandler.post(new Runnable(){ public void run(){ mainView.removeView(ballView); }});
			}
		}
		
		if(playPauseButton.contact(ballView.x, ballView.y)){
			ballView.y = displaySize[1] - 2*ballView.radius-7 - playPauseButton.thickness;
			if(playPauseButton.active){
				playPauseButton.active = false;
				vibrator.vibrate(25);
				RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).playPause(); }});
			}
		}
		
		if(previousButton.contact(ballView.x, ballView.y)){
			ballView.x = ballView.radius + previousButton.thickness;
			if(previousButton.active){
				previousButton.active = false;
				vibrator.vibrate(25);
				RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).previousTrack(); }});
			}
		}
		
		if(nextButton.contact(ballView.x, ballView.y)){
			ballView.x = displaySize[0] - ballView.radius - previousButton.thickness;
			if(nextButton.active){
				nextButton.active = false;
				vibrator.vibrate(25);
				RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).nextTrack(); }});
			}
		}
		
		
		// Checking for button reviving, only some buttons change when decontacted
		if(quitButton.decontact(ballView.x, ballView.y-75))
			RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).activateButton(R.id.quit); }});
		if(playPauseButton.decontact(ballView.x, ballView.y))
			RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).activateButton(R.id.play_pause); }});
		if(previousButton.decontact(ballView.x, ballView.y))
			RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).activateButton(R.id.previous); }});
		if(nextButton.decontact(ballView.x, ballView.y))
			RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).activateButton(R.id.next); }});
		
		RedrawHandler.post(new Runnable(){ public void run(){ ((PlaybackActivity)parent).scrollAlittle(); }});
	}
	
	private void animatePlaylistActivity(){
		// This function is called when the PLAYLIST context is active
		//RedrawHandler.post(new Runnable(){ public void run(){ ((PlaylistActivity)parent).makeToast("Running playlist activity"); }});
	}
}