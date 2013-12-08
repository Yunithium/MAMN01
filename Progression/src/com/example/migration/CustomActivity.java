package com.example.migration;

import android.content.Intent;
import android.os.Vibrator;

public interface CustomActivity {
	public void changeActivity();
	public Vibrator getVibrator();
	public void sendOrderedBroadcast(Intent downIntent, String s);
}
