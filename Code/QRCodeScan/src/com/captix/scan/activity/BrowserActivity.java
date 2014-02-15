package com.captix.scan.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.captix.scan.R;
import com.captix.scan.model.AppPreferences;
import com.captix.scan.utils.StringExtraUtils;

public class BrowserActivity extends BaseActivity {

	private String mScanResult;
	private WebView mWebView;
	private TextView mTvTitle;
	// Application Preference
	private AppPreferences mPreference;

	// Closing Web Page
	private int mCloseTime;
	private Timer mTimer;
	private ImageButton mIbShortcus;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_website);
		try {

			// Get the setting of time of opening URL
			mPreference = new AppPreferences(BrowserActivity.this);
			mCloseTime = mPreference.getCloseUrlTime();

			mTvTitle = (TextView) findViewById(R.id.header_website_tv_title);

			Bundle bundle = getIntent().getExtras();
			if (bundle != null) {
				mScanResult = bundle
						.getString(StringExtraUtils.KEY_SCAN_RESULT);
			}
			mWebView = (WebView) findViewById(R.id.activity_website_wv);
			if (mScanResult != null
					&& !mScanResult.toLowerCase().startsWith("http://")
					&& !mScanResult.toLowerCase().startsWith("https://")) {
				mScanResult = "http://" + mScanResult;
			}
			mWebView.loadUrl(mScanResult);

			mWebView.setWebViewClient(new WebViewClient() {

				public void onPageFinished(WebView view, String url) {
					mTvTitle.setText(view.getTitle());

					if (mCloseTime != -1) {
						// Timer for counting the amount of time to close
						// application
						mTimer = new Timer();
						TimerTask closeWebPage = new TimerTask() {

							@Override
							public void run() {
								finish();
							}
						};
						mTimer.schedule(closeWebPage, mCloseTime * 1000);
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onClick_Back(View v) {
		finish();
	}

	public void onClick_Share(View v) {
		try {
			Intent sharingIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					getString(R.string.email_title_share));
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					mScanResult);
			startActivity(Intent.createChooser(sharingIntent, "Share via"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void share(String nameApp, String imagePath, String message) {
		try {
			List<Intent> targetedShareIntents = new ArrayList<Intent>();
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			shareIntent.setType("image/*");
			List<ResolveInfo> resInfo = getPackageManager()
					.queryIntentActivities(shareIntent, 0);
			if (!resInfo.isEmpty()) {
				for (ResolveInfo info : resInfo) {
					Intent targetedShare = new Intent(
							android.content.Intent.ACTION_SEND);
					targetedShare.setType("image/*");
					if (info.activityInfo.packageName.toLowerCase().contains(
							nameApp)
							|| info.activityInfo.name.toLowerCase().contains(
									nameApp)) {
						targetedShare.putExtra(Intent.EXTRA_SUBJECT,
								"Sample Photo");
						targetedShare.putExtra(Intent.EXTRA_TEXT, message);
						targetedShare.putExtra(Intent.EXTRA_STREAM,
								Uri.fromFile(new File(imagePath)));
						targetedShare.setPackage(info.activityInfo.packageName);
						targetedShareIntents.add(targetedShare);
					}
				}
				Intent chooserIntent = Intent.createChooser(
						targetedShareIntents.remove(0), "Select app to share");
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
						targetedShareIntents.toArray(new Parcelable[] {}));
				startActivity(chooserIntent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
