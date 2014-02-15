package com.captix.scan.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.captix.scan.R;
import com.captix.scan.customview.SlidingMenuCustom;
import com.captix.scan.listener.MenuSlidingClickListener;
import com.captix.scan.model.AppPreferences;
import com.captix.scan.utils.StringExtraUtils;
import com.captix.scan.utils.Utils;

/**
 * @author vu le
 * 
 */
public class AboutActivity extends BaseActivity implements
		MenuSlidingClickListener {

	private SlidingMenuCustom mMenu;
	private TextView mTvTitle;
	WebView mWvContent;
	String mContent = "";
	private long lastPressedTime;
	private static final int PERIOD = 2000;
	private AppPreferences mAppPreferences;
	private ImageButton mIbShortcus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		try {

			mTvTitle = (TextView) findViewById(R.id.header_tv_title);
			mTvTitle.setText(R.string.header_title_about);
			mIbShortcus = (ImageButton) findViewById(R.id.header_ib_shortcus);
			mIbShortcus.setVisibility(View.GONE);
			mAppPreferences = new AppPreferences(this);

			mWvContent = (WebView) findViewById(R.id.about_wv_introduce);
			setTransparentBackground();
			mMenu = new SlidingMenuCustom(this, this);
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			int width = displaymetrics.widthPixels;
			int display_mode = getResources().getConfiguration().orientation;
			if (display_mode == 1) {
				mMenu.setBehindOff(width / 2 + width / 5);
			} else {
				mMenu.setBehindOff(width / 2 + width / 4);
			}
			new loadTask().execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onClick_Menu(View view) {
		try {
			if (mMenu == null) {
				mMenu = new SlidingMenuCustom(this, this);
			}
			mMenu.toggle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onScannerClickListener() {
		try {
			startActivity(new Intent(this, ScanActivity.class));
			finish();
			overridePendingTransition(0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onHistoryClickListener() {
		try {
			startActivity(new Intent(this, HistoryActivity.class));
			finish();
			overridePendingTransition(0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAboutClickListener() {
		// startActivity(new Intent(this,AboutActivity.class));
		// finish();
	}

	@Override
	public void onSettingClickListener() {
		try {
			startActivity(new Intent(this, SettingActivity.class));
			finish();
			overridePendingTransition(0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class loadTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			showProgress();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				mContent = Utils.getHtmlFromAsset(AboutActivity.this,
						R.string.content_html);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			try {
				mWvContent.loadDataWithBaseURL("file:///android_asset",
						mContent, "text/html", "UTF-8", null);
				dismissProgress();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void setTransparentBackground() {
		try {
			mWvContent.setBackgroundColor(0x00000000);
			mWvContent.getSettings().setLayoutAlgorithm(
					LayoutAlgorithm.SINGLE_COLUMN);
			mWvContent.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				switch (event.getAction()) {
				case KeyEvent.ACTION_DOWN:
					if (event.getDownTime() - lastPressedTime < PERIOD) {
						finish();
					} else {
						Toast.makeText(getApplicationContext(),
								getString(R.string.press_exit),
								Toast.LENGTH_SHORT).show();
						lastPressedTime = event.getEventTime();
					}
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int width = displaymetrics.widthPixels;
		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mMenu.setBehindOff(width / 2 + width / 4);
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			mMenu.setBehindOff(width / 2 + width / 5);
		}
	}

	public void onClick_Shortcus(View v) {
		if (mAppPreferences.getShortcusUrl().equals("-1")) {
			Toast.makeText(
					this,
					getString(R.string.mess_not_exist_shortcut),
					Toast.LENGTH_SHORT).show();
		} else {
			Intent dataIntent = new Intent(
					AboutActivity.this, BrowserActivity.class);
			dataIntent.putExtra(
					StringExtraUtils.KEY_SCAN_RESULT,
					mAppPreferences.getShortcusUrl().trim());
			startActivity(dataIntent);
		}
	}
}
