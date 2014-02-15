package com.captix.scan.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.captix.scan.R;

public class SplashActivity extends Activity {
	private final int STOPSPLASH = 0;
	// time in milliseconds
	private final long SPLASHTIME = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		Message msg = new Message();
		msg.what = STOPSPLASH;
		try {
			splashHandler.sendMessageDelayed(msg, SPLASHTIME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handler for splash screen
	 */
	private Handler splashHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {

				startActivity(new Intent(SplashActivity.this,
						ScanActivity.class));
				finish();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}
