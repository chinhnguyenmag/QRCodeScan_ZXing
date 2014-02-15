package com.captix.scan.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.captix.scan.R;
import com.captix.scan.control.DatabaseHandler;
import com.captix.scan.customview.CameraPreviewNew;
import com.captix.scan.customview.DialogConfirm;
import com.captix.scan.customview.DialogConfirm.ProcessDialogConfirm;
import com.captix.scan.customview.SlidingMenuCustom;
import com.captix.scan.listener.MenuSlidingClickListener;
import com.captix.scan.model.AppPreferences;
import com.captix.scan.model.QRCode;
import com.captix.scan.utils.Constants;
import com.captix.scan.utils.StringExtraUtils;
import com.captix.scan.utils.ZBarConstants;

public class ScanActivity extends Activity implements Camera.PreviewCallback,
		ZBarConstants, MenuSlidingClickListener {

	// private CameraPreview mPreview;
	private CameraPreviewNew mPreview;
	public Camera mCamera;
	private ImageScanner mScanner;
	private Handler mAutoFocusHandler;
	private boolean mPreviewing = true;
	private FrameLayout mFrameCamera;
	// Application Preference
	// For Sliding Menu
	private SlidingMenuCustom mMenu;
	// Save scanned QRCode into local database by SQLite
	private DatabaseHandler mDataHandler;
	private TextView mTvTitle;
	private AlertDialog.Builder alertDialogBuilder;
	private AlertDialog alertDialog;
	private DialogConfirm dialogConfirm;
	private long lastPressedTime;
	private static final int PERIOD = 2000;
	private AppPreferences mAppPreferences;
	private AudioManager mAudio;
	private int mDefaultVolume=0;

	static {
		System.loadLibrary("iconv");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		mAudio = (AudioManager) getSystemService(this.AUDIO_SERVICE);
		mAppPreferences = new AppPreferences(this);
		if (mAppPreferences.getProfileUrl().equals("")) {
			mAppPreferences.setProfileUrl("cptr.it/?var=XXXXX&id=test");
		}
		try {
			mTvTitle = (TextView) findViewById(R.id.header_tv_title);
			mTvTitle.setText(R.string.header_title_scan);
			mMenu = new SlidingMenuCustom(this, this);
			// Configure orientation for displaying Sliding Menu and Camera
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			int width = displaymetrics.widthPixels;
			int display_mode = getResources().getConfiguration().orientation;
			if (display_mode == 1) {
				mMenu.setBehindOff(width / 2 + width / 5);
			} else {
				mMenu.setBehindOff(width / 2 + width / 4);
			}
			mFrameCamera = (FrameLayout) findViewById(R.id.activity_scan_camera);
			mDataHandler = new DatabaseHandler(this);

			if (!isCameraAvailable()) {
				// Cancel request if there is no rear-facing camera.
				cancelRequest();
				return;
			}

			mAutoFocusHandler = new Handler();
			// Create and configure the ImageScanner;
			setupScanner();
			// Show warning dialog for Invalid URL
			createInvalidURLDialog(ScanActivity.this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setupScanner() {
		try {
			mScanner = new ImageScanner();
			mScanner.setConfig(0, Config.X_DENSITY, 3);
			mScanner.setConfig(0, Config.Y_DENSITY, 3);

			// int[] symbols = new int[] { Symbol.QRCODE };
			// if (symbols != null) {
			// mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
			// for (int symbol : symbols) {
			// mScanner.setConfig(symbol, Config.ENABLE, 1);
			// }
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
		}
		return c;
	}

	@Override
	protected void onResume() {
		try {
			super.onResume();
			// Open the default i.e. the first rear facing camera.
			mCamera = Camera.open();
			if (mCamera == null) {
				// Cancel request if mCamera is null.
				cancelRequest();
				return;
			}
			/* create layout for SurfaceView here */
			mPreview = new CameraPreviewNew(this, this, autoFocusCB);
			mFrameCamera.addView(mPreview);

			mPreview.setCamera(mCamera);

			mPreviewing = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			// Because the Camera object is a shared resource, it's very
			// important to release it when the activity is paused.
			if (mCamera != null) {
				mPreview.setCamera(null);
				mCamera.cancelAutoFocus();
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				// this line make difference
				mPreview.getHolder().removeCallback(mPreview);
				mCamera.release();
				mCamera = null;

				// remove layout Camera
				mFrameCamera.removeView(mPreview);
				mPreview = null;

				mPreviewing = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			if (mAppPreferences != null) {
				mAppPreferences = null;
			}
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
				mCamera = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isCameraAvailable() {
		PackageManager pm = getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	public void cancelRequest() {
		try {
			Toast.makeText(
					ScanActivity.this,
					ScanActivity.this
							.getString(R.string.activity_scan_camera_unavailable),
					Toast.LENGTH_LONG).show();
			// Intent dataIntent = new Intent();
			// dataIntent.putExtra(ERROR_INFO, "Camera unavailable");
			// setResult(Activity.RESULT_CANCELED, dataIntent);
			// finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onPreviewFrame(byte[] data, Camera camera) {
		try {
			Camera.Parameters parameters = camera.getParameters();
			Camera.Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = mScanner.scanImage(barcode);

			if (result != 0) {
				// Turn off Camera Preview
				mCamera.stopPreview();
				mPreviewing = false;
				SymbolSet syms = mScanner.getResults();
				for (Symbol sym : syms) {
					final String symData = sym.getData();
					if (!TextUtils.isEmpty(symData)) {
						// Check whether result has 16 digits or not because
						// this is wrong result
						if (!symData.matches("[0-9]{16}")) {
							// Check whether to play sound or not
							if (mAppPreferences.isSound()
									&& mAudio
											.getStreamVolume(AudioManager.STREAM_RING) != 0) {
								playSound();
							}
							// Stop scanning
							mCamera.cancelAutoFocus();
							mCamera.setPreviewCallback(null);
							// Create dialog confirm to avoid opening twice
							createDialogConfirmBrowsing(symData);
							if (mAppPreferences.getProfileUrl()
									.equalsIgnoreCase("-1")) {
								// There is no URL profile format
								continueScan(symData);
							} else {
								if (checkInvalidURL(symData)) {
									// Continue scan following fixed URL format
									continueScan(symData);
								} else if (!alertDialog.isShowing()) {
									alertDialog.show();
								}

							}
						}
					}
				}
				// Turn on Camera Preview
				mCamera.startPreview();
				mPreviewing = true;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean checkInvalidURL(String result) {
		try {

			String resultOld = result.toUpperCase();
			String result1 = resultOld.toUpperCase();
			String result2 = "";
			String urlProfile1 = mAppPreferences.getProfileUrl().toUpperCase();
			String urlProfile2 = "";

			result1 = result1.replace("HTTPS://", "");
			result1 = result1.replace("HTTP://", "");
			result1 = result1.replace("WWW.", "");
			result1 = result1.replace("FTP://", "");

			if (result1.indexOf("/") != -1) {
				String[] domain = result1.split("/");
				result1 = domain[0];
				if (domain.length > 1) {
					result2 = domain[1];
				}
			}

			urlProfile1 = urlProfile1.replace("HTTPS://", "");
			urlProfile1 = urlProfile1.replace("HTTP://", "");
			urlProfile1 = urlProfile1.replace("WWW.", "");
			urlProfile1 = urlProfile1.replace("FTP://", "");

			if (urlProfile1.indexOf("/") != -1) {
				String[] domain = urlProfile1.split("/");
				urlProfile1 = domain[0];
				if (domain.length > 1) {
					urlProfile2 = domain[1];
				}
			}

			if (urlProfile2.length() > 0) {
				if (urlProfile2.startsWith(Constants.VALIDATE_URL_PROFILE
						.toUpperCase())) {
					return result1.toUpperCase().equalsIgnoreCase(
							urlProfile1.toUpperCase());
				}

				if (result2.length() > 0) {
					if (resultOld.contains(Constants.VALIDATE_URL_PROFILE
							.toUpperCase())) {
						String contain1 = resultOld.toUpperCase().substring(
								0,
								resultOld
										.indexOf(Constants.VALIDATE_URL_PROFILE
												.toUpperCase()));

						String contain2 = resultOld
								.toUpperCase()
								.substring(
										resultOld
												.indexOf(Constants.VALIDATE_URL_PROFILE
														.toUpperCase())
												+ Constants.VALIDATE_URL_PROFILE
														.length(),
										resultOld.length());

						if (contain2.length() == 0) {
							if (mAppPreferences.getProfileUrl().toUpperCase()
									.contains(contain1.toUpperCase())) {
								return true;
							}
						} else if (mAppPreferences.getProfileUrl()
								.toUpperCase().contains(contain1.toUpperCase())
								&& mAppPreferences.getProfileUrl()
										.toUpperCase()
										.contains(contain2.toUpperCase())) {
							return true;
						}
					} else {
						return result1.toUpperCase().equalsIgnoreCase(
								urlProfile1.toUpperCase());
					}
				} else {
					return result1.toUpperCase().equalsIgnoreCase(
							urlProfile1.toUpperCase());
				}
			} else
				return result1.toUpperCase().equalsIgnoreCase(
						urlProfile1.toUpperCase());
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public void continueScan(final String symData) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.US);

		String date = formatter.format(new Date());
		mDataHandler.addQRCode(new QRCode(date, symData));

		// Get the QR Code after scanning and put it to
		// Browser
		// for
		// searching on WebSite

		if (!mAppPreferences.getAskBeforeOpening()) {
			Intent dataIntent = new Intent(ScanActivity.this,
					BrowserActivity.class);
			dataIntent.putExtra(StringExtraUtils.KEY_SCAN_RESULT, symData);
			startActivity(dataIntent);
		} else {
			if (!dialogConfirm.isShowing()) {
				dialogConfirm.show();
			}
		}
	}

	/* INVALID URL DIALOG */

	public void createInvalidURLDialog(Context context) {
		try {
			alertDialogBuilder = new AlertDialog.Builder(context);

			// set title
			alertDialogBuilder.setTitle(context.getResources().getString(
					R.string.activity_scan_invalid_url_title));

			// set dialog message
			alertDialogBuilder
					.setMessage(
							context.getResources().getString(
									R.string.activity_scan_invalid_url_message))
					.setCancelable(false)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									startPreviewAgain();
								}
							}); // create alert dialog
			alertDialog = alertDialogBuilder.create();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* ASK BEFORE OPENING BROWSER DIALOG */
	public void createDialogConfirmBrowsing(final String symData) {
		dialogConfirm = new DialogConfirm(ScanActivity.this,
				android.R.drawable.ic_dialog_alert,
				ScanActivity.this
						.getString(R.string.activity_scan_open_browser_title),
				ScanActivity.this
						.getString(R.string.activity_scan_open_url_confirm),
				true, new ProcessDialogConfirm() {

					@Override
					public void click_Ok() {

						Intent dataIntent = new Intent(ScanActivity.this,
								BrowserActivity.class);
						dataIntent.putExtra(StringExtraUtils.KEY_SCAN_RESULT,
								symData);
						startActivity(dataIntent);

					}

					@Override
					public void click_Cancel() {
						// Start scanning again
						mCamera.setPreviewCallback(ScanActivity.this);
					}
				});
	}

	public void startPreviewAgain() {
		try {
			// Turn on Camera Preview
			mCamera.startPreview();
			mPreviewing = true;
			// Start scanning again
			mCamera.setPreviewCallback(ScanActivity.this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (mCamera != null && mPreviewing) {
				mCamera.autoFocus(autoFocusCB);
			}
		}
	};

	// Mimic continuous auto-focusing
	Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			mAutoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

	private void playSound() {
		try {
			MediaPlayer mp = MediaPlayer.create(getBaseContext(),
					R.raw.camera_shutter);
			mDefaultVolume=mAudio.getStreamVolume(AudioManager.STREAM_MUSIC);
			mAudio.setStreamVolume(AudioManager.STREAM_MUSIC, mAudio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
			mp.setVolume(1.0f, 1.0f);
			mp.start();

			mp.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.release();
					mAudio.setStreamVolume(AudioManager.STREAM_MUSIC, mDefaultVolume, 0);
				}
			});
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
			mMenu.toggle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onHistoryClickListener() {
		try {
			startActivity(new Intent(ScanActivity.this, HistoryActivity.class));
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
		try {
			startActivity(new Intent(ScanActivity.this, SettingActivity.class));
			finish();
			overridePendingTransition(0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		if (mAppPreferences.getShortcusUrl().equals("-1")) {
			Toast.makeText(
					this,
					getString(R.string.mess_not_exist_shortcut),
					Toast.LENGTH_SHORT).show();
		} else {
			Intent dataIntent = new Intent(ScanActivity.this,
					BrowserActivity.class);
			dataIntent.putExtra(StringExtraUtils.KEY_SCAN_RESULT,
					mAppPreferences.getShortcusUrl().trim());
			startActivity(dataIntent);
		}
	}
}
