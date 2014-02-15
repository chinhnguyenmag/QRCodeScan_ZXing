package com.captix.scan.social.twitter;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public class TwittSharing {

	public TwitterHandler mTwitter;
	public Activity mActivity;
	private String mMessage;
	private File mImageFile;
	private String mImageUrl = "";

	private static TwittSharing instance;

	public static TwittSharing getInstance() {
		if (instance == null) {
			instance = new TwittSharing();
		}
		return instance;
	}

	public TwittSharing(Activity act, TwitterDialogListener listerner,
			String consumer_key, String consumer_secret) {
		this.mActivity = act;
		mTwitter = new TwitterHandler(mActivity, consumer_key, consumer_secret);
		mTwitter.setListener(listerner);

		if (!mTwitter.hasAccessToken()) {
			mTwitter.authorize();
		}
		instance = this;
	}

	public TwittSharing(Activity act, String consumer_key,
			String consumer_secret) {
		this.mActivity = act;
		mTwitter = new TwitterHandler(mActivity, consumer_key, consumer_secret);
		mTwitter.setListener(mTwLoginDialogListener);

		if (!mTwitter.hasAccessToken()) {
			mTwitter.authorize();
		}
		instance = this;
	}

	public TwittSharing() {
	}

	public TwitterHandler getTwitter() {
		return mTwitter;
	}

	public void setTwitter(TwitterHandler mTwitter) {
		this.mTwitter = mTwitter;
	}

	public void shareToTwitterFile(String msg, File Image_url) {
		this.mMessage = msg;
		this.mImageFile = Image_url;
		mTwitter.setListener(mTwLoginDialogListener);

		if (mTwitter.hasAccessToken()) {
			// this will post data in asyn background thread
			showTwittDialog();
		} else {
			mTwitter.authorize();
		}
	}

	public void shareToTwitterUrl(String msg, String Image_url) {
		this.mMessage = msg;
		this.mImageUrl = Image_url;
		mTwitter.setListener(mTwLoginDialogListener);

		if (mTwitter.hasAccessToken()) {
			// this will post data in asyn background thread
			showTwittDialog();
		} else {
			mTwitter.authorize();
		}
	}

	private void showTwittDialog() {
		new PostTwittTask().execute();
	}

	private final TwitterDialogListener mTwLoginDialogListener = new TwitterDialogListener() {

		@Override
		public void onError(String value) {
			showToast("Login Failed");
			mTwitter.resetAccessToken();
		}

		@Override
		public void onComplete(String value) {
			// showTwittDialog();
		}
	};

	void showToast(final String msg) {
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();

			}
		});

	}

	class PostTwittTask extends AsyncTask<Void, Void, String> {
		ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(mActivity);
			pDialog.setMessage("Processing...");
			pDialog.setCancelable(false);
			pDialog.show();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... twitt) {
			try {
				// mTwitter.updateStatus(twitt[0]);
				// File imgFile = new File("/sdcard/bluetooth/Baby.jpg");

				if (mImageFile != null) {
					SharePicTextTitter(mImageFile, mMessage,
							TwitterHandler.twitterObj);
				} else if (mImageUrl.length() > 0) {
					SharePicTextTitter(mImageUrl, mMessage,
							TwitterHandler.twitterObj);
				} else {
					SharePicTextTitter(mImageFile, mMessage,
							TwitterHandler.twitterObj);
				}

				return "success";

			} catch (Exception e) {
				if (e.getMessage().toString().contains("duplicate")) {
					return "Posting Failed because of Duplicate message...";
				}
				e.printStackTrace();
				return "failed";
			}

		}

		@Override
		protected void onPostExecute(String result) {
			pDialog.dismiss();

			if (null != result && result.equals("success")) {
				showToast("Your post has been shared");

			} else {
				showToast(result);
			}

			super.onPostExecute(result);
		}
	}

	public void SharePicTextTitter(File image_path, String message,
			Twitter twitter) throws Exception {
		try {
			StatusUpdate st = new StatusUpdate(message);
			if (image_path != null) {
				st.setMedia(image_path);
			}
			twitter.updateStatus(st);

		} catch (TwitterException e) {
			throw e;
		}
	}

	public void SharePicTextTitter(String image_path, String message,
			Twitter twitter) throws Exception {
		try {
			StatusUpdate st = new StatusUpdate(message);
			if (image_path != null) {
				st.setMedia("ImageShare", getBitmap(image_path));
			}
			st.getStatus();
			twitter.updateStatus(st);

		} catch (TwitterException e) {
			throw e;
		}
	}

	public void Authorize_UserDetail() {

	}

	private InputStream getBitmap(String url) {

		// from web
		try {
			String urlimage = url.replace(" ", "%20");
			URL imageUrl = new URL(urlimage);
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			return is;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
