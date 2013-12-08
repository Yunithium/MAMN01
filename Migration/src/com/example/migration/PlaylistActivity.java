package com.example.migration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Window;
import android.widget.FrameLayout;

public class PlaylistActivity extends Activity implements CustomActivity{
	private FrameLayout mainView;
	private RollingStone rollingStone;
	private RedrawTask redrawTask;
	private boolean running = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.playlist_activity);
		//setupActionBar();
		
		rollingStone = RollingStone.getInstance();
		redrawTask = rollingStone.getRedrawTask();
		mainView = (android.widget.FrameLayout) findViewById(R.id.painting_place_playback);
	}
	
	public void changeActivity(){
		finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}
	
	public Vibrator getVibrator() {
		return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	@Override
	public void onResume() {
		if (!running) {
			redrawTask.changeContext(this, mainView);
			running = true;
		}
		super.onResume();
	}
	
	@Override
	public void onPause() {
		if (running) {
			redrawTask.pause();
			running = false;
		}
		super.onPause();
	}
}
