package com.example.advancement;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import com.example.advancement.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class PlaybackActivity extends Activity implements CustomActivity, OnCompletionListener {
	private FrameLayout mainView;
	private RollingStone rollingStone;
	private Timer timer;
	private RedrawTask redrawTask;
	private short refreshTime = 10;
	private boolean running = false;

	private ViewFlipper viewFlipper;
	private ImageView imageView;
	private int currentImage;
	private int currentImageView;
	private int[] images;
	private int[] imageViews;
	private SeekBar timeLine;
	
	private static final String[] EXTENSIONS = { ".mp3", ".mid", ".wav", ".ogg", ".mp4" };
	private ArrayList<String> trackNames;
	private ArrayList<String> trackArtworks;
	private File path;
	private File path2;
	private Music track;
	private boolean isTuning; //is user currently jamming out, if so automatically start playing the next track...?
	private int currentTrack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.playback_activity);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Send the context and the view the ball is to be rendered on to the rendertask.
		mainView = (android.widget.FrameLayout) findViewById(R.id.painting_place);
		redrawTask = new RedrawTask(refreshTime, getWindowManager().getDefaultDisplay());
		redrawTask.changeContext(this, mainView);

		setUpViewFlipper();

		rollingStone = RollingStone.getInstance();
		rollingStone.setRedrawTask(redrawTask);

		timer = new Timer();
		timer.schedule(redrawTask, 100, refreshTime);
		
		initializeMedia();
		timeLine = (SeekBar) findViewById(R.id.seekbartimeline);

		((SensorManager) getSystemService(Context.SENSOR_SERVICE)).registerListener(new SensorEventListener() {
				@Override
				public void onSensorChanged(SensorEvent event) {
					event.values[0] = -event.values[0];
					redrawTask.updateSpeed(event.values);
					
					//if(canReadTrackInfo){
					if(running){
						int mediaDuration = track.getDuration();
						int mediaPosition = track.getCurrentPosition();
						timeLine.setMax(mediaDuration);
						timeLine.setProgress(mediaPosition);
					}
				}
				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) { } // ignore this event
		}, ((SensorManager) getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_GAME);
		
		
		// Move ball on touch
		mainView.setOnTouchListener(new android.view.View.OnTouchListener() {
			public boolean onTouch(android.view.View v,
					android.view.MotionEvent e) {
				float position[] = { e.getX(), e.getY() };
				redrawTask.updatePos(position);
				return true;
			}
		});
	}

	public void changeActivity() {
		Toast.makeText(this, "Wall hit!", Toast.LENGTH_SHORT).show();

		Intent intent = new Intent(this, PlaylistActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}
	
	private void initializeMedia(){
    	trackNames = new ArrayList<String>();
    	trackArtworks = new ArrayList<String>();
    	currentTrack = 0;
    	isTuning = false;
    	
    	addTracks(getTracks());
    	loadTrack();
	}
	
	@SuppressLint("NewApi")
	private String[] getTracks(){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
    		path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    		path2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    		
    		TextView textView = (TextView) findViewById(R.id.test_output);
    		path = new File(path.toString() + "/" + "Ringtones");
    		
			String[] temp = path.list();
			
			for(String s : temp){
				textView.setText(textView.getText() + "\n" + s);
			}
			
			return temp;
		} else{
			Toast.makeText(getBaseContext(), "SD Card is either mounted elsewhere or is unusable", Toast.LENGTH_LONG).show();
		}
    	return null;
    }
	
	private void addTracks(String[] temp){
    	if(temp != null){
			for(int i = 0; i < temp.length; i++){
				if(trackChecker(temp[i])){
					trackNames.add(temp[i]);
					trackArtworks.add(temp[i].substring(0, temp[i].length()-4));
				}
			}
			Toast.makeText(getBaseContext(), "Loaded " + Integer.toString(trackNames.size()) + " Tracks", Toast.LENGTH_SHORT).show();
		}
    }
	
	private boolean trackChecker(String trackToTest){
    	for(int j = 0; j < EXTENSIONS.length; j++){
			if(trackToTest.contains(EXTENSIONS[j])){
				return true;
			}
		}
    	return false;
    }
	
	private void loadTrack(){
    	if(track != null){
    		track.dispose();
    	}
    	if(trackNames.size() > 0){
    		track = loadMusic();
    	}
    }
	
	private Music loadMusic(){
		try{
			FileInputStream fis = new FileInputStream(new File(path, trackNames.get(currentTrack)));
			FileDescriptor fileDescriptor = fis.getFD();
			return new Music(fileDescriptor);
		} catch(IOException e){
			e.printStackTrace();
			Toast.makeText(getBaseContext(), "Error Loading " + trackNames.get(currentTrack), Toast.LENGTH_LONG).show();
		}
		return null;
    }
	
	private void playTrack(){
    	if(isTuning && track != null){
			track.play();
			Toast.makeText(getBaseContext(), "Playing " + trackNames.get(currentTrack).substring(0, trackNames.get(currentTrack).length()-4), Toast.LENGTH_SHORT).show();
		}
    	track.setOnCompletionListener(this);
    }

	public void previousTrack() {
		Button button = (android.widget.Button) findViewById(R.id.bar1);
		button.setBackgroundColor(Color.GREEN);

		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));

		currentImage = (currentImage - 1 + images.length) % images.length;
		currentImageView = (currentImageView + 1) % imageViews.length;

		imageView = (ImageView) findViewById(imageViews[currentImageView]);
		imageView.setImageResource(images[currentImage]);
		viewFlipper.showPrevious();
		
		currentTrack--;
		if(currentTrack < 0)
			currentTrack = trackNames.size()-1;
		
		loadTrack();
		playTrack();
	}

	public void nextTrack() {
		Button button = (android.widget.Button) findViewById(R.id.bar2);
		button.setBackgroundColor(Color.GREEN);

		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));

		currentImage = (currentImage + 1) % images.length;
		currentImageView = (currentImageView + 1) % imageViews.length;

		imageView = (ImageView) findViewById(imageViews[currentImageView]);
		imageView.setImageResource(images[currentImage]);
		viewFlipper.showNext();
		
		currentTrack++;
		if(currentTrack > trackNames.size()-1)
			currentTrack = 0;
		
		loadTrack();
		playTrack();
	}

	public void playPause() {
		synchronized(this){
			if(isTuning){
				isTuning = false;
				track.pause();
			} else{
				isTuning = true;
				playTrack();
			}
		}
	}

	public void activateButton(int buttonID) {
		Button button = (android.widget.Button) findViewById(buttonID);
		button.setBackgroundColor(Color.WHITE);
	}

	private void setUpViewFlipper() {
		currentImage = 0;
		currentImageView = 0;

		images = new int[5];
		images[0] = R.drawable.rolling_stones;
		images[1] = R.drawable.stevie_ray_vaughan;
		images[2] = R.drawable.taylor_swift;
		images[3] = R.drawable.the_beatles;
		images[4] = R.drawable.the_knife;

		imageViews = new int[2];
		imageViews[0] = R.id.image_view_1;
		imageViews[1] = R.id.image_view_2;

		viewFlipper = (ViewFlipper) findViewById(R.id.flipper_of_views);

		imageView = (ImageView) findViewById(imageViews[currentImageView]);
		imageView.setImageResource(images[currentImage]);
	}

	public Vibrator getVibrator() {
		return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	public void onResume() {
		redrawTask.changeContext(this, mainView);
		if (!running) {
			running = true;
		}
		super.onResume();
	}
	public void onPause() {
		redrawTask.pause();
		super.onPause();
	}
	public void onDestroy(){
		redrawTask.pause();
		if (running) {
			running = false;
		}
		if (track != null) {
			if (track.isPlaying()) {
				track.pause();
				isTuning = false;
			}
			if (isFinishing()) {
				track.dispose();
				finish();
			}
		} else {
			if (isFinishing()) {
				finish();
			}
		}
		super.onDestroy();
	}

	public void makeToast(String s) {
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		nextTrack();
		makeToast("Music finished playing");
	}
	
	public void quitApplication(){
		redrawTask.pause();
		redrawTask.firstSpeedRead = false;
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}
}