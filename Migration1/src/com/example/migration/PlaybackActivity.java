package com.example.migration;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Window;
import android.widget.FrameLayout;

public class PlaybackActivity extends Activity implements CustomActivity{
	private FrameLayout mainView;
	private RollingStone rollingStone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_playback);
		//setupActionBar();
		
		rollingStone = RollingStone.getInstance();
		mainView = (android.widget.FrameLayout) findViewById(R.id.painting_place_playback);
	}
	
	@Override
    public void onResume(){
		RedrawTask temp = rollingStone.getRedrawTask();
		temp.changeContext(this, mainView);
		super.onResume();
	}
	
	public void changeActivity(){
		finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}
	
	public Vibrator getVibrator() {
		return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	/*
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/
}
