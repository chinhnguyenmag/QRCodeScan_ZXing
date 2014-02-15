package com.captix.scan.activity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.captix.scan.R;
import com.captix.scan.adapter.HistoryAdapter;
import com.captix.scan.adapter.HistoryAdapter.HistoryAdapter_Process;
import com.captix.scan.control.DatabaseHandler;
import com.captix.scan.customview.DialogConfirm;
import com.captix.scan.customview.DialogConfirm.ProcessDialogConfirm;
import com.captix.scan.customview.SlidingMenuCustom;
import com.captix.scan.listener.GetWidthListener;
import com.captix.scan.listener.MenuSlidingClickListener;
import com.captix.scan.model.HistoryItem;
import com.captix.scan.model.HistorySectionItem;
import com.captix.scan.model.Item;
import com.captix.scan.model.QRCode;
import com.captix.scan.utils.SocialUtil;
import com.captix.scan.utils.StringExtraUtils;
import com.captix.scan.utils.Utils;
import com.evernote.client.android.EvernoteSession;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

public class HistoryActivity extends ParentActivity implements
		MenuSlidingClickListener, GetWidthListener {

	private HistoryAdapter mAdapter;
	private List<Item> items;
	private SlidingMenuCustom mMenu;
	private SwipeListView mSwipeListView;
	private DatabaseHandler mDataHandler;
	private List<QRCode> mListQRCodes;
	private SimpleDateFormat mFormatterSection;
	private DateFormat inputFormat;
	private int mSectionNumber = 0;
	private Facebook mFacebook = new Facebook(SocialUtil.FACEBOOK_APPID);
	private SharedPreferences mSharedPreferences;
	private String mEverNoteContent;

	// =============================================================
	private int swipeMode = SwipeListView.SWIPE_MODE_BOTH;
	private int swipeActionLeft = SwipeListView.SWIPE_ACTION_REVEAL;
	private int swipeActionRight = SwipeListView.SWIPE_ACTION_REVEAL;
	private TextView mTvTitle;
	private TextView mTvDelete;
	private int mWidthBtDelete;
	private int mWidthSocial;
	private int mWidthTotal;
	private long lastPressedTime;
	private ImageButton mIbShortcus;
	private static final int PERIOD = 2000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		mTvTitle = (TextView) findViewById(R.id.header_tv_title);
		mTvTitle.setText(R.string.header_title_history);
		mTvDelete = (TextView) findViewById(R.id.header_tv_right);
		mIbShortcus = (ImageButton) findViewById(R.id.header_ib_shortcus);
		mIbShortcus.setVisibility(View.GONE);
		// inflate layout for list view
		mSwipeListView = (SwipeListView) findViewById(R.id.activity_history_lv);
		View v = new View(this);
		v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 10));
		v.setBackgroundColor(getResources().getColor(
				R.color.color_divider_listview));
		mSwipeListView.addFooterView(new View(this), null, true);

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
		// Get QRCode from Database to inflate into list view
		mDataHandler = new DatabaseHandler(this);
		items = new ArrayList<Item>();
		mListQRCodes = new ArrayList<QRCode>();
		mListQRCodes.addAll(mDataHandler.getAllQRCodes());
		Collections.sort(mListQRCodes, new Comparator<QRCode>() {

			@Override
			public int compare(QRCode lhs, QRCode rhs) {
				SimpleDateFormat form = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss", Locale.US);

				Date d1 = null;
				Date d2 = null;
				try {
					d1 = form.parse(lhs.getDate());
					d2 = form.parse(rhs.getDate());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if (d1.compareTo(d2) > 0) {
					return -1;
				} else if (d1.compareTo(d2) < 0) {
					return 1;
				} else
					return 0;
			}
		});
		// add the first section and item into list view
		if (mListQRCodes.size() != 0) {
			mTvDelete.setVisibility(View.VISIBLE);
			// Align margin attributes for title
			RelativeLayout.LayoutParams marginParams = (RelativeLayout.LayoutParams) mTvTitle
					.getLayoutParams();
			marginParams.setMargins(20, 0, 0, 0);
			mTvTitle.setLayoutParams(marginParams);

			mFormatterSection = new SimpleDateFormat("EEEE, MMMM dd yyyy",
					Locale.US);
			inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				Date newDate = new Date();
				newDate = inputFormat.parse(mListQRCodes.get(0).getDate());
				items.add(new HistorySectionItem(mFormatterSection
						.format(newDate)));
			} catch (Exception e) {
				e.printStackTrace();
			}

			items.add(new HistoryItem(mListQRCodes.get(0).getUrl()));

			for (int i = 1; i < mListQRCodes.size(); i++) {
				if (!mListQRCodes
						.get(i)
						.getDate()
						.substring(0, 11)
						.equals(mListQRCodes.get(i - 1).getDate()
								.substring(0, 11))) {
					// section
					Date newDate = new Date();
					try {
						newDate = inputFormat.parse(mListQRCodes.get(i)
								.getDate());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					items.add(new HistorySectionItem(mFormatterSection
							.format(newDate)));
					items.add(new HistoryItem(mListQRCodes.get(i).getUrl()));
				} else {
					// item
					items.add(new HistoryItem(mListQRCodes.get(i).getUrl()));
				}
			}
		}

		mAdapter = new HistoryAdapter(this, items,
				new HistoryAdapter_Process() {

					@Override
					public void delete_item(final int position,
							final List<Item> listItems) {
						// delete from database
						mDataHandler.deleteQRCode(mListQRCodes.get(position));
						mListQRCodes.remove(position);

						items.clear();
						items.addAll(listItems);
						// mSwipeListView.setAdapter(mAdapter);
						mAdapter.notifyDataSetChanged();

						// Disable Delete All Button
						if (mListQRCodes.size() == 0) {
							mTvDelete.setVisibility(View.GONE);
							// Align margin attributes for title
							RelativeLayout.LayoutParams marginParams = (RelativeLayout.LayoutParams) mTvTitle
									.getLayoutParams();
							marginParams.setMargins(0, 0, 20, 0);
							mTvTitle.setLayoutParams(marginParams);
						}

					}

					@Override
					public void click_evernote(int position) {
						if (Utils.isNetworkConnected(HistoryActivity.this)) {
							mEverNoteContent = mListQRCodes.get(position)
									.getUrl();
							addEverNote(null);
						} else {
							Toast.makeText(HistoryActivity.this,
									getString(R.string.mess_error_network),
									Toast.LENGTH_LONG).show();
						}

					}

					@Override
					public void click_facebook(int position) {
						if (Utils.isNetworkConnected(HistoryActivity.this)) {
							loginToFacebook(mListQRCodes.get(position).getUrl()
									.trim());
						} else {
							Toast.makeText(HistoryActivity.this,
									getString(R.string.mess_error_network),
									Toast.LENGTH_LONG).show();
						}

					}

					@Override
					public void click_twitter(int position) {
						if (Utils.isNetworkConnected(HistoryActivity.this)) {
							Intent intent = new Intent(HistoryActivity.this,
									TwitterLoginActivity.class);
							intent.putExtra(
									StringExtraUtils.KEY_INTENT_TWITTER,
									mListQRCodes.get(position).getUrl().trim());
							startActivity(intent);
						} else {
							Toast.makeText(HistoryActivity.this,
									getString(R.string.mess_error_network),
									Toast.LENGTH_LONG).show();
						}

					}

					@Override
					public void click_sms(int position) {
						sendSMS(mListQRCodes.get(position).getUrl().trim());
					}

					@Override
					public void click_email(int position) {
						sendMail(mListQRCodes.get(position).getUrl().trim());
					}

				}, this);

		mSwipeListView
				.setSwipeListViewListener(new BaseSwipeListViewListener() {

					@Override
					public void onStartOpen(int position, int action,
							boolean right) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onStartClose(int position, boolean right) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onOpened(int position, boolean toRight) {
					}

					@Override
					public void onMove(int position, float x) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onListChanged() {
						// TODO Auto-generated method stub

					}

					@Override
					public void onLastListItem() {
						// TODO Auto-generated method stub

					}

					@Override
					public void onFirstListItem() {
						// TODO Auto-generated method stub

					}

					@Override
					public void onDismiss(int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							items.remove(position);
						}
						mAdapter.notifyDataSetChanged();
					}

					@Override
					public void onClosed(int position, boolean fromRight) {

					}

					@Override
					public void onClickFrontView(int position) {
						if (!items.get(position).isSection()) {
							// Add code to process
							HistoryItem item = (HistoryItem) items
									.get(position);
							Intent dataIntent = new Intent(
									HistoryActivity.this, BrowserActivity.class);
							dataIntent.putExtra(
									StringExtraUtils.KEY_SCAN_RESULT,
									item.getTitle());
							startActivity(dataIntent);
						}
					}

					@Override
					public void onClickBackView(int position) {
						mSwipeListView.closeAnimate(position);
					}

					@Override
					public void onChoiceStarted() {
						// TODO Auto-generated method stub

					}

					@Override
					public void onChoiceEnded() {
						// TODO Auto-generated method stub

					}

					@Override
					public void onChoiceChanged(int position, boolean selected) {
						// TODO Auto-generated method stub

					}

				});

		// Set Adapter for List View
		mSwipeListView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();

		// Disable Delete All Button
		if (mListQRCodes.size() == 0) {
			mTvDelete.setVisibility(View.GONE);
			// Align margin attributes for title
			RelativeLayout.LayoutParams marginParams = (RelativeLayout.LayoutParams) mTvTitle
					.getLayoutParams();
			marginParams.setMargins(0, 0, 20, 0);
			mTvTitle.setLayoutParams(marginParams);
		}
	}

	public void onClick_Menu(View view) {
		if (mMenu == null) {
			mMenu = new SlidingMenuCustom(this, this);
		}
		mMenu.toggle();
	}

	@Override
	public void onScannerClickListener() {
		startActivity(new Intent(this, ScanActivity.class));
		finish();
		overridePendingTransition(0, 0);
	}

	@Override
	public void onHistoryClickListener() {
		mMenu.toggle();

	}

	@Override
	public void onAboutClickListener() {
		startActivity(new Intent(this, AboutActivity.class));
		finish();
		overridePendingTransition(0, 0);
	}

	@Override
	public void onSettingClickListener() {
		startActivity(new Intent(this, SettingActivity.class));
		overridePendingTransition(0, 0);
		finish();
		overridePendingTransition(0, 0);
	}

	public void onClick_DeleteAll(View v) {
		if (mListQRCodes != null && mListQRCodes.size() > 0) {
			DialogConfirm dialog = new DialogConfirm(
					HistoryActivity.this,
					android.R.drawable.ic_dialog_alert,
					HistoryActivity.this
							.getString(R.string.activity_history_delete_all_history_title),
					HistoryActivity.this
							.getString(R.string.activity_history_delete_all_history_confirm),
					true, new ProcessDialogConfirm() {

						@Override
						public void click_Ok() {

							// delete all QR Code from Database
							for (QRCode code : mListQRCodes) {
								mDataHandler.deleteQRCode(code);
							}
							items.clear();
							mAdapter.notifyDataSetChanged();
							mSwipeListView.setAdapter(mAdapter);
							// Let Delete All Button GONE
							mTvDelete.setVisibility(View.GONE);
							// Align margin attributes for title
							RelativeLayout.LayoutParams marginParams = (RelativeLayout.LayoutParams) mTvTitle
									.getLayoutParams();
							marginParams.setMargins(0, 0, 20, 0);
							mTvTitle.setLayoutParams(marginParams);
						}

						@Override
						public void click_Cancel() {

						}
					});
			dialog.show();
		} else {
			Toast.makeText(this, "There are no history to delete.",
					Toast.LENGTH_SHORT).show();
		}

	}

	private void reload() {
		mSwipeListView.setSwipeMode(swipeMode);
		mSwipeListView.setSwipeActionLeft(swipeActionLeft);
		mSwipeListView.setSwipeActionRight(swipeActionRight);
		mSwipeListView.setOffsetLeft(mWidthTotal - mWidthBtDelete);
		mSwipeListView.setOffsetRight(mWidthTotal - mWidthSocial);
	}

	/**
	 * @param v
	 * @Description Processing login and adding a new EverNote
	 */
	public void addEverNote(View v) {
		mEvernoteSession.authenticate(HistoryActivity.this);
	}

	/**
	 * Called when the control returns from an activity that we launched.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mFacebook.authorizeCallback(requestCode, resultCode, data);
		switch (requestCode) {
		// Add a new EverNote when OAuth activity returns result
		case EvernoteSession.REQUEST_CODE_OAUTH:
			if (resultCode == Activity.RESULT_OK) {
				Intent intent = new Intent(HistoryActivity.this,
						CreateEverNote.class);
				intent.putExtra(StringExtraUtils.KEY_HISTORY_ITEM,
						mEverNoteContent);
				startActivity(intent);
			}
			break;
		}
	}

	public void loginToFacebook(final String link) {

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
			postToWall(link);
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

							postToWall(link);
						}

						@Override
						public void onCancel() {
						}
					});
		}
	}

	public void getAccessToken() {
		String access_token = mFacebook.getAccessToken();
		Toast.makeText(getApplicationContext(),
				"Access Token: " + access_token, Toast.LENGTH_LONG).show();
	}

	public void postToWall(String link) {
		Bundle parameters = new Bundle();
		parameters.putString("link", link);
		mFacebook.dialog(this, "feed", parameters, new DialogListener() {

			@Override
			public void onFacebookError(FacebookError e) {
			}

			@Override
			public void onError(DialogError e) {
			}

			@Override
			public void onComplete(Bundle values) {
				Toast.makeText(HistoryActivity.this,
						"Commment has been posted in your wall!",
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCancel() {
			}
		});
	}

	protected void sendSMS(String link) {
		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.putExtra("sms_body", link);
		smsIntent.setType("vnd.android-dir/mms-sms");

		try {
			startActivity(smsIntent);
			// finish();
		} catch (Exception e) {
			Toast.makeText(this, "Please insert your simcard.",
					Toast.LENGTH_SHORT).show();
		}
	}

	protected void sendMail(String link) {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(Intent.EXTRA_SUBJECT,
				getString(R.string.email_title_share));
		emailIntent.putExtra(Intent.EXTRA_TEXT, link);
		try {
			startActivity(Intent.createChooser(emailIntent,
					"Choose an Email client:"));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "There are no email clients installed.",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void getWidthBtDelete(int widthBtDelete) {
		mWidthBtDelete = widthBtDelete;
		reload();
	}

	@Override
	public void getWidthSocial(int widthSocial) {
		mWidthSocial = widthSocial;
	}

	@Override
	public void getWidthTotal(int widthTotal) {
		mWidthTotal = widthTotal;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			switch (event.getAction()) {
			case KeyEvent.ACTION_DOWN:
				if (event.getDownTime() - lastPressedTime < PERIOD) {
					finish();
				} else {
					Toast.makeText(getApplicationContext(),
							getString(R.string.press_exit), Toast.LENGTH_SHORT)
							.show();
					lastPressedTime = event.getEventTime();
				}
				return true;
			}
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
		Toast.makeText(this, "Shortcus !", Toast.LENGTH_SHORT).show();
	}
}
