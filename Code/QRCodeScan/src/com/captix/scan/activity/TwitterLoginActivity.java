package com.captix.scan.activity;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.captix.scan.R;
import com.captix.scan.customview.DialogPostToWall;
import com.captix.scan.customview.DialogPostToWall.ProcessDialogPostToWallTwitter;
import com.captix.scan.utils.Constants;
import com.captix.scan.utils.StringExtraUtils;

public class TwitterLoginActivity extends Activity {

	public static final int TWITTER_LOGIN_RESULT_CODE_SUCCESS = 1;
	public static final int TWITTER_LOGIN_RESULT_CODE_FAILURE = 2;

	public static final String TWITTER_CONSUMER_KEY = "n5Uxq7r0n2imgb48UO5fBA";
	public static final String TWITTER_CONSUMER_SECRET = "EDA35k4DwQrlR5C0tb8kebvTZUN2Ioxi2NSZCfpGo";

	private WebView twitterLoginWebView;
	private ProgressDialog mProgressDialog;

	private static Twitter twitter;
	private static RequestToken requestToken;
	public static SharedPreferences sharedPrefs;
	private DialogPostToWall mDialogPostToWallTwitter;
	private String mLink;
	private Bundle mBundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter_login);

		mBundle = getIntent().getExtras();
		if (mBundle != null) {
			mLink = getIntent().getExtras().getString(
					StringExtraUtils.KEY_INTENT_TWITTER);
		}

		try {
			if (TWITTER_CONSUMER_KEY.trim().equals("")
					|| TWITTER_CONSUMER_SECRET.trim().equals("")) {
				Log.e(Constants.TAG,
						"ERROR: Consumer Key and Consumer Secret required!");
				TwitterLoginActivity.this
						.setResult(TWITTER_LOGIN_RESULT_CODE_FAILURE);
				TwitterLoginActivity.this.finish();
			}

			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("Please wait...");
			mProgressDialog.setCancelable(false);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.show();

			twitterLoginWebView = (WebView) findViewById(R.id.twitter_login_web_view);
			twitterLoginWebView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (url.contains(Constants.TWITTER_CALLBACK_URL)) {
						Uri uri = Uri.parse(url);
						TwitterLoginActivity.this.saveAccessTokenAndFinish(uri);
						// showTweetDialog();
						return true;
					}
					return false;
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					if (mProgressDialog != null)
						mProgressDialog.dismiss();
				}

				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);

					if (mProgressDialog != null)
						mProgressDialog.show();
				}
			});

			askOAuth();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			if (mProgressDialog != null)
				mProgressDialog.dismiss();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	private void saveAccessTokenAndFinish(final Uri uri) {
		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					String verifier = uri
							.getQueryParameter(Constants.IEXTRA_OAUTH_VERIFIER);
					try {
						sharedPrefs = getSharedPreferences(
								Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
						AccessToken accessToken = twitter.getOAuthAccessToken(
								requestToken, verifier);
						Editor e = sharedPrefs.edit();
						e.putString(Constants.PREF_KEY_TOKEN,
								accessToken.getToken());
						e.putString(Constants.PREF_KEY_SECRET,
								accessToken.getTokenSecret());
						e.commit();
						TwitterLoginActivity.this
								.setResult(TWITTER_LOGIN_RESULT_CODE_SUCCESS);
					} catch (Exception e) {
						e.printStackTrace();
						if (e.getMessage() != null)
							Log.e(Constants.TAG, e.getMessage());
						else
							Log.e(Constants.TAG,
									"ERROR: Twitter callback failed");
						TwitterLoginActivity.this
								.setResult(TWITTER_LOGIN_RESULT_CODE_FAILURE);
					}
				}
			}).start();

			Intent intent = new Intent(TwitterLoginActivity.this,
					ActivityPostToWall.class);
			if(mBundle != null) {
				intent.putExtra(StringExtraUtils.KEY_INTENT_TWITTER_LOGIN, mLink);
			}
			startActivity(intent);
			finish();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ====== TWITTER HELPER METHODS ======

	public static boolean isConnected(Context ctx) {
		try {
			SharedPreferences sharedPrefs = ctx.getSharedPreferences(
					Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
			return sharedPrefs.getString(Constants.PREF_KEY_TOKEN, null) != null;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void logOutOfTwitter(Context ctx) {
		try {
			SharedPreferences sharedPrefs = ctx.getSharedPreferences(
					Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
			Editor e = sharedPrefs.edit();
			e.putString(Constants.PREF_KEY_TOKEN, null);
			e.putString(Constants.PREF_KEY_SECRET, null);
			e.commit();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getAccessToken(Context ctx) {
		try {
			SharedPreferences sharedPrefs = ctx.getSharedPreferences(
					Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
			return sharedPrefs.getString(Constants.PREF_KEY_TOKEN, null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getAccessTokenSecret(Context ctx) {
		SharedPreferences sharedPrefs = ctx.getSharedPreferences(
				Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return sharedPrefs.getString(Constants.PREF_KEY_SECRET, null);
	}

	public void showTweetDialog() {
		try {
			mDialogPostToWallTwitter = new DialogPostToWall(
					TwitterLoginActivity.this,
					new ProcessDialogPostToWallTwitter() {

						@Override
						public void click_Post(String mMessage) {
							new postToWall().execute(mMessage);
						}

						@Override
						public void click_Cancel() {
							finish();
						}
					});
			mDialogPostToWallTwitter.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void askOAuth() {
		try {
			ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
			configurationBuilder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			configurationBuilder
					.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			Configuration configuration = configurationBuilder.build();
			twitter = new TwitterFactory(configuration).getInstance();

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						requestToken = twitter
								.getOAuthRequestToken(Constants.TWITTER_CALLBACK_URL);
					} catch (Exception e) {
						final String errorString = e.toString();
						TwitterLoginActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mProgressDialog.cancel();
								Toast.makeText(TwitterLoginActivity.this,
										errorString.toString(),
										Toast.LENGTH_SHORT).show();
								finish();
							}
						});
						e.printStackTrace();
						return;
					}

					TwitterLoginActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Log.d(Constants.TAG, "LOADING AUTH URL");
							twitterLoginWebView.loadUrl(requestToken
									.getAuthenticationURL());
						}
					});
				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Function to update status
	 * */
	class postToWall extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(TwitterLoginActivity.this);
			mProgressDialog.setMessage("Posting to wall...");
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {
			String status = args[0];
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

				// Access Token
				String access_token = sharedPrefs.getString(
						Constants.PREF_KEY_TOKEN, "");
				// Access Token Secret
				String access_token_secret = sharedPrefs.getString(
						Constants.PREF_KEY_SECRET, "");

				AccessToken accessToken = new AccessToken(access_token,
						access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build())
						.getInstance(accessToken);

				// Update status
				twitter4j.Status response = twitter.updateStatus(status);

				Log.d("Status", "> " + response.getText());
			} catch (TwitterException e) {
				// Error in updating status
				Log.d("Twitter Update Error", e.getMessage());
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {
			try {
				// dismiss the dialog after getting all products
				mProgressDialog.dismiss();
				// updating UI from Background Thread
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(),
								"Message has been posted successfully.",
								Toast.LENGTH_SHORT).show();
						finish();
					}
				});

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
