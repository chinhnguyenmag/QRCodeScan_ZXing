package com.captix.scan.activity;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.captix.scan.R;
import com.captix.scan.utils.Constants;
import com.captix.scan.utils.StringExtraUtils;

public class ActivityPostToWall extends Activity implements OnClickListener {

	private Button mBtCancel;
	private Button mBtPost;
	private EditText mEtContent;
	private ProgressDialog mProgressDialog;
	private String mLink;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_twitter_post_t_wall);
		try {
			mBtCancel = (Button) findViewById(R.id.post_to_wall_bt_cancel);
			mBtPost = (Button) findViewById(R.id.post_to_wall_bt_post);
			mEtContent = (EditText) findViewById(R.id.post_to_wall_et_content);
			mBtCancel.setOnClickListener(this);
			mBtPost.setOnClickListener(this);

			Bundle bundle = getIntent().getExtras();
			if (bundle != null) {
				mLink = getIntent().getExtras().getString(
						StringExtraUtils.KEY_INTENT_TWITTER_LOGIN);
				mEtContent.setText(mLink);
			} else {
				Uri captixUrl = Uri
						.parse("http://www7.44doors.com/dl.aspx?cid=2444&pv_url=http://cptr.it/captixscan?2444_rm_id=100.3781911.7");
				mEtContent
						.setText(Html
								.fromHtml(getString(R.string.content_to_share_social_media)
										+ "<a href=\""
										+ captixUrl
										+ "\"> cptr.it/captixscan</a>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.post_to_wall_bt_cancel: {
			finish();
			break;
		}

		case R.id.post_to_wall_bt_post: {
			try {
				if (mEtContent.getText().toString().trim().length() > 0) {
					new postToWall().execute(mEtContent.getText().toString()
							.trim());
				} else {
					Toast.makeText(this, "Please input your content to post.",
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			break;
		}

		default:
			break;
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
			mProgressDialog = new ProgressDialog(ActivityPostToWall.this);
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
				builder.setOAuthConsumerKey(TwitterLoginActivity.TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TwitterLoginActivity.TWITTER_CONSUMER_SECRET);

				// Access Token
				String access_token = TwitterLoginActivity.sharedPrefs
						.getString(Constants.PREF_KEY_TOKEN, "");
				// Access Token Secret
				String access_token_secret = TwitterLoginActivity.sharedPrefs
						.getString(Constants.PREF_KEY_SECRET, "");

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
				// TODO: handle exception
			}
		}

	}
}
