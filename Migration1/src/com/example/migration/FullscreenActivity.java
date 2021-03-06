package com.example.migration;

import java.util.Timer;
import com.example.migration.R;
import com.example.migration.util.SystemUiHider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements CustomActivity{
	private FrameLayout mainView;
	private RollingStone rollingStone;
	private Timer timer;
	private RedrawTask redrawTask;
	private short refreshTime = 10;
	private boolean running = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fullscreen);
		
		// Send context and the view the ball is to be rendered on to the rendertask.
		mainView = (android.widget.FrameLayout) findViewById(R.id.painting_place);
		redrawTask = new RedrawTask(refreshTime, getWindowManager().getDefaultDisplay());
		
		rollingStone = RollingStone.getInstance();
		rollingStone.setRedrawTask(redrawTask);
		
		timer = new Timer();
		timer.schedule(redrawTask, 100, refreshTime);
		
		
		((SensorManager)getSystemService(Context.SENSOR_SERVICE)).registerListener(
	    		new SensorEventListener() {    
	    			@Override  
	    			public void onSensorChanged(SensorEvent event) {
	    				event.values[0] = -event.values[0];
	    				redrawTask.updateSpeed(event.values);
	    			}
	        		@Override  
	        		public void onAccuracyChanged(Sensor sensor, int accuracy) {} //ignore this event
	        	},
	        	((SensorManager)getSystemService(Context.SENSOR_SERVICE))
	        	.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_GAME);
	}
	
	public void changeActivity(){
		Toast.makeText(this, "Wall hit!", Toast.LENGTH_SHORT).show();
		
		Intent intent = new Intent(this, PlaybackActivity.class);
		startActivity(intent);
    	overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}
	
	public Vibrator getVibrator(){
		return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	
	
	
	
	
	// Initiating - calling changeContext more than once creates stationary balls 
	@Override
    public void onRestart(){
		// This is called before the application is even running
		//if(!running){ redrawTask.changeContext(this, mainView); running = true; }
		super.onRestart();
	}@Override
    public void onStart(){
		if(!running){ redrawTask.changeContext(this, mainView); running = true; }
		super.onStart();
	}
	@Override
    public void onResume(){
		if(!running){ redrawTask.changeContext(this, mainView); running = true; }
		super.onResume();
	}
	
	// Ending
	@Override
    public void onPause(){
		if(running) { redrawTask.pause(); running = false; }
		super.onPause();
	}
	@Override
    public void onStop(){ // Called when the user press the home button
		if(running) { redrawTask.pause(); running = false; }
		super.onStop();
	}
	@Override
    public void onDestroy(){ // Called when the user press the back button
		if(running) { redrawTask.pause(); running = false; }
		super.onDestroy();
	}
}
