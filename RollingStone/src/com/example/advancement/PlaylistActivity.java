package com.example.advancement;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;

import com.example.advancement.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlaylistActivity extends Activity implements CustomActivity{
	private FrameLayout mainView;
	private RollingStone rollingStone;
	private RedrawTask redrawTask;
	//private Timer timer;
	private boolean running = false;
	private GridAdapter gridAdapter;
	private GridView gridView;
	private int lastPosition;
	private int displaySize;
	private boolean isTuning;
	
	private static final String[] EXTENSIONS = { ".mp3", ".mid", ".wav", ".ogg", ".mp4" };
	private boolean asleep;
	private boolean awoken;
	private long delayTime;
	private Timer timer;
	private short refreshTime = 10;
	
	private boolean firstTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.playlist_activity);
		
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		displaySize = outMetrics.heightPixels;
		delayTime = SystemClock.uptimeMillis();
		//setupActionBar();
		
		asleep = false;
		awoken = false;
		
		firstTime = true;
		
		
	    gridView = (GridView) findViewById(R.id.gridview);
	    gridAdapter = new GridAdapter(this);
	    gridView.setAdapter(gridAdapter);
	    lastPosition = GridView.INVALID_POSITION;
		
		rollingStone = RollingStone.getInstance();
		
		timer = rollingStone.getTimer();
		//redrawTask = rollingStone.getRedrawTask();
		//timer = rollingStone.getTimer();
		mainView = (android.widget.FrameLayout) findViewById(R.id.painting_place_playback);
		
		
		((SensorManager) getSystemService(Context.SENSOR_SERVICE)).registerListener(new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent event) {
					
				if((SystemClock.uptimeMillis() - delayTime) > 500){
					event.values[0] = -event.values[0];
					if(!asleep){
						redrawTask.updateSpeed(event.values, awoken);
						
						if(awoken || firstTime){
							redrawTask.unPause();
							awoken = false;
							if(firstTime){
								firstTime = false;
							}
						}
						
//						if(firstTime){
//							redrawTask.unPause();
//							firstTime = false;
//						}
					}
				}
			}
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) { } // ignore this event
	}, ((SensorManager) getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_GAME);
	
		// ReMove ball on touch
				gridView.setOnTouchListener(new android.view.View.OnTouchListener() {
					public boolean onTouch(android.view.View v,
							android.view.MotionEvent e) {
						disableBall();
						return true;
					}
				});
	//gridView.setTranscriptMode(GridView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	//gridView.scrollTo(0, 0);
	}
	
	/* Removes the ball from the field. */
	public void disableBall(){
		asleep = true;
		redrawTask.pause();
		//((ImageButton) findViewById(R.id.tounge_ball)).setBackgroundResource(R.drawable.ic_launcher_2);
		((ImageButton) findViewById(R.id.tounge_ball)).setImageDrawable(getResources().getDrawable(R.drawable.red_tounge));
	}
	
	/* Enables the ball again. It will start from the center of the display and stay there for a short duration. */
	public void enableBall(View view){
		redrawTask.changeContext(this, mainView);
    	delayTime = SystemClock.uptimeMillis();
    	if(asleep){
			asleep = false;
			awoken = true;
		}
    	((ImageButton) findViewById(R.id.tounge_ball)).setImageDrawable(getResources().getDrawable(R.drawable.gray_tounge));
    }
	
	public void changeActivity(){
		finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}
	
	public void playTrack(float x, float y){
		rollingStone.currentSong = gridView.pointToPosition((int) x, (int) y + gridView.getScrollY() - 60 - 30);
		rollingStone.playListChanged = true;
		finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		
	}
	
	public void activateButton(int buttonID) {
		Button button = (android.widget.Button) findViewById(buttonID);
		button.setBackgroundColor(Color.WHITE);
	}
	
	public void deActivateButton(int buttonID) {
		Button button = (android.widget.Button) findViewById(buttonID);
		button.setBackgroundColor(Color.GREEN);
	}

	public void changeImage(float x, float y){
		int position = gridView.pointToPosition((int) x, (int) y + gridView.getScrollY() - 60 - 30);
		
		if(position != lastPosition && position != gridView.INVALID_POSITION){
			gridAdapter.doAchange(position);
			gridAdapter.notifyDataSetChanged();
		}
	}
	
	public void scrollGridUp(){
	if(gridView.getScrollY()==0){
			
		} else{
			gridView.scrollBy(0, -2);
		}
	}
	
	public void scrollGrid(){
		
		if(gridView.getScrollY() <= gridView.getBottom() - displaySize + 100 + 100){
			gridView.post(new Runnable() {
			    @SuppressLint("NewApi")
				@Override
			    public void run() {
			        gridView.scrollBy(0, 2);
			    }
			});
		}
		
		/*
		gridAdapter.notifyDataSetChanged();
		if(gridView.getScrollY() <= gridView.getBottom() - displaySize + 100)
			gridView.scrollBy(0, 2);
		*/
	}

	public Vibrator getVibrator() {
		return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	@Override
	public void onResume() {
		delayTime = SystemClock.uptimeMillis();
		if(asleep){
			rollingStone.setRedrawTask(new RedrawTask(refreshTime, getWindowManager().getDefaultDisplay()));
			timer.schedule(rollingStone.getRedrawTask(), 100, refreshTime);
		}
		redrawTask = rollingStone.getRedrawTask();
		redrawTask.changeContext(this, mainView);
		//redrawTask.unPause();
		
		asleep = false;
		super.onResume();
	}
	
	@Override
	public void onPause() {
		redrawTask.pause();
		asleep = true;
		redrawTask = null;
		super.onPause();
	}
	
	public void makeToast(String s) {
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}
}
