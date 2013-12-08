package com.example.migration;

import java.util.Timer;
import com.example.migration.R;
import com.example.migration.util.SystemUiHider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ViewFlipper;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class PlaybackActivity extends Activity implements CustomActivity {
	private FrameLayout mainView;
	private RollingStone rollingStone;
	private Timer timer;
	private RedrawTask redrawTask;
	private short refreshTime = 10;
	private boolean running = false;

	private ViewFlipper viewFlipper;
	private ImageView imageView;
	private MediaPlayer mediaPlayer;
	private int currentImage;
	private int currentImageView;
	private int[] images;
	private int[] imageViews;

	private SeekBar timeLine;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.playback_activity);

		// Send the context and the view the ball is to be rendered on to the
		// rendertask.
		mainView = (android.widget.FrameLayout) findViewById(R.id.painting_place);
		redrawTask = new RedrawTask(refreshTime, getWindowManager()
				.getDefaultDisplay());

		setUpViewFlipper();

		rollingStone = RollingStone.getInstance();
		rollingStone.setRedrawTask(redrawTask);

		timer = new Timer();
		timer.schedule(redrawTask, 100, refreshTime);

		// MEDIAPLAYER
		mediaPlayer = MediaPlayer.create(this, R.raw.pling);
		timeLine = (SeekBar) findViewById(R.id.seekbartimeline);
		// END MEDIAPLAYER

		((SensorManager) getSystemService(Context.SENSOR_SERVICE))
				.registerListener(new SensorEventListener() {
					@Override
					public void onSensorChanged(SensorEvent event) {
						event.values[0] = -event.values[0];
						redrawTask.updateSpeed(event.values);
						// MEDIAPLAYER
						int mediaDuration = mediaPlayer.getDuration();
						int mediaPosition = mediaPlayer.getCurrentPosition();
						timeLine.setMax(mediaDuration);
						timeLine.setProgress(mediaPosition);
						// END MEDIAPLAYER
					}

					@Override
					public void onAccuracyChanged(Sensor sensor, int accuracy) {
					} // ignore this event
				}, ((SensorManager) getSystemService(Context.SENSOR_SERVICE))
						.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0),
						SensorManager.SENSOR_DELAY_GAME);

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

	public void previousTrack() {
		Button button = (android.widget.Button) findViewById(R.id.bar1);
		button.setBackgroundColor(Color.GREEN);

		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.push_right_in));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.push_right_out));

		currentImage = (currentImage - 1 + images.length) % images.length;
		currentImageView = (currentImageView + 1) % imageViews.length;

		imageView = (ImageView) findViewById(imageViews[currentImageView]);
		imageView.setImageResource(images[currentImage]);

		// MEDIAPLAYER
		mediaPlayer.release();
		rollingStone.currentSong = (rollingStone.currentSong + (rollingStone.playlist.length - 1)) % rollingStone.playlist.length;
		mediaPlayer = MediaPlayer.create(this, rollingStone.playlist[rollingStone.currentSong]);
		mediaPlayer.start();
		// END MEDIAPLAYER

		viewFlipper.showPrevious();
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

		// MEDIAPLAYER
		mediaPlayer.release();
		rollingStone.currentSong = (rollingStone.currentSong + 1) % rollingStone.playlist.length;
		mediaPlayer = MediaPlayer.create(this, rollingStone.playlist[rollingStone.currentSong]);
		mediaPlayer.start();
		// END MEDIAPLAYER

		viewFlipper.showNext();
	}

	public void playPause() {
		try {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			} else {
				mediaPlayer.start();
			}
		} catch (Exception e) {
			Toast.makeText(this, "Can't play", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
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

		// Only two ImageView objects are used.
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

	public void onRestart() {
		super.onRestart();
	}
	public void onStart() {
		super.onStart();
	}

	public void onResume() {
		if (!running) {
			redrawTask.changeContext(this, mainView);
			running = true;
		}
		super.onResume();
	}

	public void onPause() {
		if (running) {
			redrawTask.pause();
			running = false;
		}
		super.onPause();
	}

	public void onStop() {
		super.onStop();
	}
	public void onDestroy() {
		super.onDestroy();
	}

	public void testToast(String s) {
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}
}
