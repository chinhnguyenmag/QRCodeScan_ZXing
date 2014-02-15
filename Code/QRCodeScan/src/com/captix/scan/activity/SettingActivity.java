package com.captix.scan.activity;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.captix.scan.R;
import com.captix.scan.customview.DialogChangeProfile;
import com.captix.scan.customview.DialogChangeShortcut;
import com.captix.scan.customview.DialogChangeShortcut.ProcessDialogShortcus;
import com.captix.scan.customview.DialogChangeProfile.ProcessDialogProfile;
import com.captix.scan.customview.DialogConfirm;
import com.captix.scan.customview.DialogConfirm.ProcessDialogConfirm;
import com.captix.scan.customview.DialogPickTime;
import com.captix.scan.customview.DialogPickTime.ProcessDialogPickTime;
import com.captix.scan.customview.SlidingMenuCustom;
import com.captix.scan.listener.MenuSlidingClickListener;
import com.captix.scan.model.AppPreferences;
import com.captix.scan.utils.SocialUtil;
import com.captix.scan.utils.StringExtraUtils;
import com.captix.scan.utils.Utils;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

/**
 * @author vule
 * @description: This class use to setting application
 * 
 */
public class SettingActivity extends BaseActivity implements
		MenuSlidingClickListener, OnClickListener {

	private SlidingMenuCustom mMenu;
	private ToggleButton mSwitchViewSound;
	private ToggleButton mSwitchViewOpenUrl;
	private AppPreferences mAppPreferences;
	private TextView mTvTime;
	private TextView mTvTitle;
	private TextView mTvUrlProfile;
	private RelativeLayout mRlShareFacebook;
	private RelativeLayout mRlShareSMS;
	private RelativeLayout mRlShareMail;
	private RelativeLayout mRlShareTwitter;
	private Facebook mFacebook = new Facebook(SocialUtil.FACEBOOK_APPID);
	private SharedPreferences mSharedPreferences;
	private long lastPressedTime;
	private static final int PERIOD = 2000;
	private DialogChangeProfile mDlChangeUrl;
	private DialogChangeShortcut mDlChangeShortcus;
	private TextView mTvUrlShortcut;
	private ImageButton mIbShortcus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();

			StrictMode.setThreadPolicy(policy);
		}
		try {
			mTvTitle = (TextView) findViewById(R.id.header_tv_title);
			mTvTitle.setText(R.string.header_title_setting);

			mTvUrlProfile = (TextView) findViewById(R.id.setting_tv_urlprofile);

			mAppPreferences = new AppPreferences(this);
			String[] urlProfile = removeHttp(mAppPreferences.getProfileUrl())
					.split("/");
			if (mAppPreferences.getProfileUrl().equals("-1")) {
				mTvUrlProfile.setText("");
			} else {
				if (urlProfile.length == 0) {
					removeHttp(mAppPreferences.getProfileUrl());
				} else {
					mTvUrlProfile.setText(urlProfile[0]);
				}
			}

			mMenu = new SlidingMenuCustom(this, this);
			mMenu.setTouchModeAboveMargin();
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			int width = displaymetrics.widthPixels;
			int display_mode = getResources().getConfiguration().orientation;
			if (display_mode == 1) {
				mMenu.setBehindOff(width / 2 + width / 5);
			} else {
				mMenu.setBehindOff(width / 2 + width / 4);
			}
			mSwitchViewSound = (ToggleButton) findViewById(R.id.activity_settings_sv_sound);
			mSwitchViewOpenUrl = (ToggleButton) findViewById(R.id.activity_settings_sv_open_url);
			mTvTime = (TextView) findViewById(R.id.activity_setting_tv_time);
			mRlShareFacebook = (RelativeLayout) findViewById(R.id.setting_ll_social_facebook);
			mRlShareSMS = (RelativeLayout) findViewById(R.id.setting_ll_social_message);
			mRlShareMail = (RelativeLayout) findViewById(R.id.setting_ll_social_mail);
			mRlShareTwitter = (RelativeLayout) findViewById(R.id.setting_ll_social_twitter);
			mTvUrlShortcut = (TextView) findViewById(R.id.activity_setting_tv_link);
			mIbShortcus = (ImageButton) findViewById(R.id.header_ib_shortcus);
			mIbShortcus.setVisibility(View.GONE);
			mRlShareSMS.setOnClickListener(this);
			mRlShareMail.setOnClickListener(this);
			mRlShareTwitter.setOnClickListener(this);
			mRlShareFacebook.setOnClickListener(this);

			String[] urlShortcut = removeHttp(mAppPreferences.getShortcusUrl())
					.split("/");
			if (mAppPreferences.getShortcusUrl().equals("-1")) {
				mTvUrlShortcut.setText("none");
			} else {
				if (urlShortcut.length == 0) {
					removeHttp(mAppPreferences.getShortcusUrl());
				} else {
					mTvUrlShortcut.setText(urlShortcut[0]);
				}
			}

			mSwitchViewSound.setChecked(mAppPreferences.isSound());
			mSwitchViewOpenUrl
					.setChecked(mAppPreferences.getAskBeforeOpening());
			mSwitchViewSound
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton arg0,
								boolean isOn) {
							mAppPreferences.setSound(isOn);
						}
					});

			mSwitchViewOpenUrl
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton arg0,
								boolean isOn) {
							mAppPreferences.setAskBeforeOpening(isOn);
						}
					});

			if (mAppPreferences.getCloseUrlTime() == -1) {
				mTvTime.setText("Never");
			} else {
				mTvTime.setText(mAppPreferences.getCloseUrlTime() + " seconds");
			}

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
		try {
			startActivity(new Intent(this, AboutActivity.class));
			finish();
			overridePendingTransition(0, 0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSettingClickListener() {

	}

	public void onClick_AutoClose(View v) {
		try {
			DialogPickTime d = new DialogPickTime(this,
					new ProcessDialogPickTime() {

						@Override
						public void click_Ok(int value) {
							mAppPreferences.setCloseUrl(value);
							if (mAppPreferences.getCloseUrlTime() == -1) {
								mTvTime.setText("Never");
							} else {
								mTvTime.setText(mAppPreferences
										.getCloseUrlTime() + " seconds");
							}
						}

						@Override
						public void click_Cancel() {

						}
					}, mAppPreferences.getCloseUrlTime());
			d.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
			case R.id.setting_ll_social_message: {
				sendSMS();
				break;
			}

			case R.id.setting_ll_social_mail: {
				sendMail();

				break;
			}

			case R.id.setting_ll_social_twitter: {
				if (Utils.isNetworkConnected(this)) {
					Intent intent = new Intent(SettingActivity.this,
							TwitterLoginActivity.class);
					startActivity(intent);
				} else {
					showToastMessage(getString(R.string.mess_error_network));
				}
				break;
			}

			case R.id.setting_ll_social_facebook: {
				if (Utils.isNetworkConnected(this)) {
					loginToFacebook();
				} else {
					showToastMessage(getString(R.string.mess_error_network));
				}
				break;
			}

			default:
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onClick_UrlProfile(View v) {
		try {

			mDlChangeUrl = new DialogChangeProfile(this,
					mAppPreferences.getProfileUrl(),
					new ProcessDialogProfile() {

						@Override
						public void click_Ok(String url) {
							try {
								String oldUrl = mAppPreferences.getProfileUrl();
								mAppPreferences.setProfileUrl(url);
								String[] urlProfile = removeHttp(
										mAppPreferences.getProfileUrl()).split(
										"/");
								if (mAppPreferences.getProfileUrl()
										.equals("-1")) {
									mTvUrlProfile.setText("");
								} else {
									if (urlProfile.length == 0) {
										removeHttp(mAppPreferences
												.getProfileUrl());
									} else {
										mTvUrlProfile.setText(urlProfile[0]);
									}
								}

								if (!oldUrl.equalsIgnoreCase(url)) {
									showToastMessage(getString(R.string.mess_update_urlprofile_successfull));
								}

							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						@Override
						public void click_Cancel() {

						}
					});
			mDlChangeUrl.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onClick_UrlShortcus(View v) {
		try {

			mDlChangeShortcus = new DialogChangeShortcut(this,
					mAppPreferences.getShortcusUrl(),
					new ProcessDialogShortcus() {

						@Override
						public void click_Ok(String url) {
							try {
								String oldUrl = mAppPreferences
										.getShortcusUrl();
								mAppPreferences.setShortcutUrl(url);
								String[] urlProfile = removeHttp(
										mAppPreferences.getShortcusUrl())
										.split("/");
								if (mAppPreferences.getShortcusUrl().equals(
										"-1")) {
									mTvUrlShortcut.setText("none");
								} else {
									if (urlProfile.length == 0) {
										removeHttp(mAppPreferences
												.getShortcusUrl());
									} else {
										mTvUrlShortcut.setText(urlProfile[0]);
									}
								}

								if (!oldUrl.equalsIgnoreCase(url)) {
									showToastMessage(getString(R.string.mess_update_urlshortcut_successfull));
								}

							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						@Override
						public void click_Cancel() {
						}
					});
			mDlChangeShortcus.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method use to login facebook
	 */
	public void loginToFacebook() {

		try {

			PackageInfo info = getPackageManager().getPackageInfo(
					this.getPackageName(), PackageManager.GET_SIGNATURES);

			for (Signature signature : info.signatures) {

				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("====Hash Key===",
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}

		} catch (NameNotFoundException e) {

			e.printStackTrace();

		} catch (NoSuchAlgorithmException ex) {

			ex.printStackTrace();

		}
		mSharedPreferences = getPreferences(MODE_PRIVATE);
		String access_token = mSharedPreferences
				.getString("access_token", null);
		long expires = mSharedPreferences.getLong("access_expires", 0);
		if (access_token != null) {
			mFacebook.setAccessToken(access_token);

			DialogConfirm dialog = new DialogConfirm(SettingActivity.this,
					android.R.drawable.ic_dialog_alert, "Post to wall",
					SettingActivity.this
							.getString(R.string.activity_settting_confirm),
					true, new ProcessDialogConfirm() {

						@Override
						public void click_Ok() {
							postOnWall();
						}

						@Override
						public void click_Cancel() {

						}
					});
			dialog.show();
		}

		if (expires != 0) {
			mFacebook.setAccessExpires(expires);
		}

		if (!mFacebook.isSessionValid()) {
			mFacebook.authorize(this,
					new String[] { "email", "publish_stream" },
					new DialogListener() {

						@Override
						public void onFacebookError(FacebookError e) {
						}

						@Override
						public void onError(DialogError e) {
						}

						@Override
						public void onComplete(Bundle values) {
							SharedPreferences.Editor editor = mSharedPreferences
									.edit();
							editor.putString("access_token",
									mFacebook.getAccessToken());
							editor.putLong("access_expires",
									mFacebook.getAccessExpires());
							editor.commit();
							// postToWall();
							DialogConfirm dialog = new DialogConfirm(
									SettingActivity.this,
									android.R.drawable.ic_dialog_alert,
									"Post to wall",
									SettingActivity.this
											.getString(R.string.activity_settting_confirm),
									true, new ProcessDialogConfirm() {

										@Override
										public void click_Ok() {
											postOnWall();
										}

										@Override
										public void click_Cancel() {

										}
									});
							dialog.show();
						}

						@Override
						public void onCancel() {
						}
					});
		}
	}

	/**
	 * This method use to get access token from facebook
	 */
	public void getAccessToken() {
		try {
			String access_token = mFacebook.getAccessToken();
			Toast.makeText(getApplicationContext(),
					"Access Token: " + access_token, Toast.LENGTH_LONG).show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method use to post to wall on facebook
	 */
	public void postToWall() {
		try {
			Bundle parameters = new Bundle();

			// Uri captixUrl = Uri
			// .parse("http://www7.44doors.com/dl.aspx?cid=2444&pv_url=http://cptr.it/captixscan?2444_rm_id=100.3781911.7");

			// parameters
			// .putString(
			// "link",
			// "http://www7.44doors.com/dl.aspx?cid=2444&pv_url=http://cptr.it/captixscan?2444_rm_id=100.3781911.7");
			//
			// parameters.putString("caption",
			// getString(R.string.content_to_share_social_media));
			//
			// mFacebook.dialog(this, "feed", parameters, new DialogListener() {
			//
			// @Override
			// public void onFacebookError(FacebookError e) {
			// e.printStackTrace();
			// }
			//
			// @Override
			// public void onError(DialogError e) {
			// e.printStackTrace();
			// }
			//
			// @Override
			// public void onComplete(Bundle values) {
			// Toast.makeText(SettingActivity.this,
			// getString(R.string.mess_post_success),
			// Toast.LENGTH_SHORT).show();
			// }
			//
			// @Override
			// public void onCancel() {
			// Toast.makeText(SettingActivity.this, "Canceled",
			// Toast.LENGTH_SHORT).show();
			// }
			// });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method use to call intent sms system to send
	 */
	protected void sendSMS() {
		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent
				.putExtra(
						"sms_body",
						getString(R.string.content_to_share_social_media)
								+ " http://www7.44doors.com/dl.aspx?cid=2444&pv_url=http://cptr.it/captixscan?2444_rm_id=100.3781911.7");
		smsIntent.setType("vnd.android-dir/mms-sms");

		try {
			startActivity(smsIntent);
			// finish();
		} catch (Exception e) {
			Toast.makeText(this, "Please insert your simcard.",
					Toast.LENGTH_SHORT).show();
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			mFacebook.authorizeCallback(requestCode, resultCode, data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method use to call intent email system to send
	 */
	protected void sendMail() {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(Intent.EXTRA_SUBJECT,
				getString(R.string.email_title_share));
		Uri captixUrl = Uri
				.parse("http://www7.44doors.com/dl.aspx?cid=2444&pv_url=http://cptr.it/captixscan?2444_rm_id=100.3781911.7");
		emailIntent.putExtra(
				android.content.Intent.EXTRA_TEXT,
				Html.fromHtml(getString(R.string.content_to_share_social_media)
						+ "<a href=\"" + captixUrl
						+ "\"> cptr.it/captixscan</a>"));
		try {
			startActivity(Intent.createChooser(emailIntent,
					"Choose an Email client:"));
		} catch (Exception ex) {
			Toast.makeText(this, "There are no email clients installed.",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void postOnWall() {
		try {
			// String message = Html
			// .fromHtml(getString(R.string.content_to_share_social_media)
			// +
			// " http://www7.44doors.com/dl.aspx?cid=2444&pv_url=http://cptr.it/captixscan?2444_rm_id=100.3781911.7")
			// + "";
			// String response = mFacebook.request("me");
			// Bundle parameters = new Bundle();
			// parameters.putString("message", message);

			String message = Html
					.fromHtml(getString(R.string.content_to_share_social_media))
					+ "";
			String response = mFacebook.request("me");
			Bundle parameters = new Bundle();
			parameters.putString("message", message);
			parameters.putString("link", "http://44doors.com/");

			response = mFacebook.request("me/feed", parameters, "POST");
			Toast.makeText(SettingActivity.this,
					"Message has been posted on your wall.", Toast.LENGTH_SHORT)
					.show();
			if (response == null || response.equals("")
					|| response.equals("false")) {
				Log.v("Error", "Blank response");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	/**
	 * @param s
	 * @return String not have http://.
	 * 
	 */
	public String removeHttp(String s) {
		URL myURL;
		try {
			myURL = new URL(s);
			s = myURL.getHost();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return s;
	}

	public void onClick_Shortcus(View v) {
		if (mAppPreferences.getShortcusUrl().equals("-1") || mAppPreferences.getShortcusUrl().equals("")) {
			Toast.makeText(
					this,
					getString(R.string.mess_not_exist_shortcut),
					Toast.LENGTH_SHORT).show();
		} else {
			Intent dataIntent = new Intent(SettingActivity.this,
					BrowserActivity.class);
			dataIntent.putExtra(StringExtraUtils.KEY_SCAN_RESULT,
					mAppPreferences.getShortcusUrl().trim());
			startActivity(dataIntent);
		}
	}
}
