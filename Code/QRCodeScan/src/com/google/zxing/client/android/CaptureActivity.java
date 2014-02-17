/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.captix.scan.R;
import com.captix.scan.activity.AboutActivity;
import com.captix.scan.activity.BrowserActivity;
import com.captix.scan.activity.HistoryActivity;
import com.captix.scan.activity.SettingActivity;
import com.captix.scan.control.DatabaseHandler;
import com.captix.scan.customview.DialogConfirm;
import com.captix.scan.customview.DialogConfirm.ProcessDialogConfirm;
import com.captix.scan.customview.SlidingMenuCustom;
import com.captix.scan.listener.MenuSlidingClickListener;
import com.captix.scan.model.AppPreferences;
import com.captix.scan.model.QRCode;
import com.captix.scan.utils.Constants;
import com.captix.scan.utils.StringExtraUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.history.HistoryItem;
import com.google.zxing.client.android.history.HistoryManager;
import com.google.zxing.client.android.result.ResultButtonListener;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.result.ResultHandlerFactory;
import com.google.zxing.client.android.result.supplement.SupplementalInfoRetriever;
import com.google.zxing.client.android.share.ShareActivity;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements
		SurfaceHolder.Callback, MenuSlidingClickListener {

	private static final String TAG = CaptureActivity.class.getSimpleName();

	private static final long DEFAULT_INTENT_RESULT_DURATION_MS = 1500L;
	private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;

	private static final String PACKAGE_NAME = "com.google.zxing.client.android";
	private static final String PRODUCT_SEARCH_URL_PREFIX = "http://www.google";
	private static final String PRODUCT_SEARCH_URL_SUFFIX = "/m/products/scan";
	private static final String[] ZXING_URLS = {
			"http://zxing.appspot.com/scan", "zxing://scan/" };

	public static final int HISTORY_REQUEST_CODE = 0x0000bacc;

	private static final Set<ResultMetadataType> DISPLAYABLE_METADATA_TYPES = EnumSet
			.of(ResultMetadataType.ISSUE_NUMBER,
					ResultMetadataType.SUGGESTED_PRICE,
					ResultMetadataType.ERROR_CORRECTION_LEVEL,
					ResultMetadataType.POSSIBLE_COUNTRY);

	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private Result savedResultToShow;
	private ViewfinderView viewfinderView;
	private Result lastResult;
	private boolean hasSurface;
	private boolean copyToClipboard;
	private IntentSource source;
	private String sourceUrl;
	private ScanFromWebPageManager scanFromWebPageManager;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;
	private HistoryManager historyManager;
	private InactivityTimer inactivityTimer;
	private BeepManager beepManager;
	private AmbientLightManager ambientLightManager;

	/* Bao add code here */
	// For Sliding Menu
	private SlidingMenuCustom mMenu;
	// Save scanned QRCode into local database by SQLite
	private DatabaseHandler mDataHandler;
	private AlertDialog.Builder alertDialogBuilder;
	private AlertDialog alertDialog;
	private DialogConfirm dialogConfirm;
	private AppPreferences mAppPreferences;
	private long lastPressedTime;
	private static final int PERIOD = 2000;
	private AudioManager mAudio;
	private int mDefaultVolume = 0;

	ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.capture);
		mDataHandler = new DatabaseHandler(this);
		mAudio = (AudioManager) getSystemService(this.AUDIO_SERVICE);
		mAppPreferences = new AppPreferences(this);
		if (mAppPreferences.getProfileUrl().equals("")) {
			mAppPreferences.setProfileUrl("cptr.it/?var=XXXXX&id=test");
		}
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
		hasSurface = false;
		historyManager = new HistoryManager(this);
		historyManager.trimHistory();
		inactivityTimer = new InactivityTimer(this);
		beepManager = new BeepManager(this);
		ambientLightManager = new AmbientLightManager(this);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		// Show warning dialog for Invalid URL
		createInvalidURLDialog(CaptureActivity.this);
		// showHelpOnFirstLaunch();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// CameraManager must be initialized here, not in onCreate(). This is
		// necessary because we don't
		// want to open the camera driver and measure the screen size if we're
		// going to show the help on
		// first launch. That led to bugs where the scanning rectangle was the
		// wrong size and partially
		// off screen.
		cameraManager = new CameraManager(this);

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		handler = null;
		lastResult = null;

		resetStatusView();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		beepManager.updatePrefs();
		ambientLightManager.start(cameraManager);

		inactivityTimer.onResume();

		source = IntentSource.NONE;
		decodeFormats = null;
		characterSet = null;

		source = IntentSource.NATIVE_APP_INTENT;

		// Set Decode format for Scanner
		// decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
		decodeFormats = EnumSet.of(BarcodeFormat.QR_CODE, BarcodeFormat.UPC_E,
				BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
				BarcodeFormat.RSS_14, BarcodeFormat.RSS_EXPANDED,
				BarcodeFormat.CODE_39, BarcodeFormat.CODE_93,
				BarcodeFormat.CODE_128, BarcodeFormat.ITF,
				BarcodeFormat.CODABAR);
	}

	private static boolean isZXingURL(String dataString) {
		if (dataString == null) {
			return false;
		}
		for (String url : ZXING_URLS) {
			if (dataString.startsWith(url)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		if (mAppPreferences != null) {
			mAppPreferences = null;
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.capture, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		switch (item.getItemId()) {
		case R.id.menu_share:
			intent.setClassName(this, ShareActivity.class.getName());
			startActivity(intent);
			break;
		case R.id.menu_history:
			intent.setClassName(this, HistoryActivity.class.getName());
			startActivityForResult(intent, HISTORY_REQUEST_CODE);
			break;
		case R.id.menu_settings:
			intent.setClassName(this, PreferencesActivity.class.getName());
			startActivity(intent);
			break;
		case R.id.menu_help:
			intent.setClassName(this, HelpActivity.class.getName());
			startActivity(intent);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			if (requestCode == HISTORY_REQUEST_CODE) {
				int itemNumber = intent.getIntExtra(
						Intents.History.ITEM_NUMBER, -1);
				if (itemNumber >= 0) {
					HistoryItem historyItem = historyManager
							.buildHistoryItem(itemNumber);
					decodeOrStoreSavedBitmap(null, historyItem.getResult());
				}
			}
		}
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		// Bitmap isn't used yet -- will be used soon
		if (handler == null) {
			savedResultToShow = result;
		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler,
						R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG,
					"*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// if (this.getResources().getConfiguration().orientation ==
		// Configuration.ORIENTATION_PORTRAIT) {
		// cameraManager.setDisplayOrientation(90);
		// }
		// if (this.getResources().getConfiguration().orientation ==
		// Configuration.ORIENTATION_LANDSCAPE) {
		// // Change orientation when rotating camera
		// int angle;// This is camera orientation
		// Display display = this.getWindowManager().getDefaultDisplay();
		// switch (display.getRotation()) {// This is display orientation
		// case Surface.ROTATION_0:
		// // for Tablet
		// angle = 0;
		// break;
		// case Surface.ROTATION_90:
		// // for Phone
		// angle = 0;
		// break;
		// case Surface.ROTATION_180:
		// // for Tablet
		// angle = 180;
		// break;
		// case Surface.ROTATION_270:
		// // for Phone
		// angle = 180;
		// break;
		// default:
		// angle = 90;
		// break;
		// }
		// cameraManager.setDisplayOrientation(angle);
		// }

	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param scaleFactor
	 *            amount by which thumbnail was scaled
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();
		lastResult = rawResult;
		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(
				this, rawResult);

		boolean fromLiveScan = barcode != null;
		if (fromLiveScan) {
			historyManager.addHistoryItem(rawResult, resultHandler);
			// Then not from history, so beep/vibrate and we have an image to
			// draw on
			// beepManager.playBeepSoundAndVibrate();
			// drawResultPoints(barcode, scaleFactor, rawResult);
		}

		switch (source) {
		case NATIVE_APP_INTENT:
		case PRODUCT_SEARCH_LINK:
			handleDecodeExternally(rawResult, resultHandler, barcode);
			break;
		case ZXING_LINK:
			if (scanFromWebPageManager == null
					|| !scanFromWebPageManager.isScanFromWebPage()) {
				handleDecodeInternally(rawResult, resultHandler, barcode);
			} else {
				handleDecodeExternally(rawResult, resultHandler, barcode);
			}
			break;
		case NONE:
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			if (fromLiveScan
					&& prefs.getBoolean(PreferencesActivity.KEY_BULK_MODE,
							false)) {
				String message = getResources().getString(
						R.string.msg_bulk_mode_scanned)
						+ " (" + rawResult.getText() + ')';
				Toast.makeText(getApplicationContext(), message,
						Toast.LENGTH_SHORT).show();
				// Wait a moment or else it will scan the same barcode
				// continuously about 3 times
				restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
			} else {
				handleDecodeInternally(rawResult, resultHandler, barcode);
			}
			break;
		}
	}

	/**
	 * Superimpose a line for 1D or dots for 2D to highlight the key features of
	 * the barcode.
	 * 
	 * @param barcode
	 *            A bitmap of the captured image.
	 * @param scaleFactor
	 *            amount by which thumbnail was scaled
	 * @param rawResult
	 *            The decoded results which contains the points to draw.
	 */
	private void drawResultPoints(Bitmap barcode, float scaleFactor,
			Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.result_points));
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
			} else if (points.length == 4
					&& (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult
							.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
				// Hacky special case -- draw two lines, for the barcode and
				// metadata
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
				drawLine(canvas, paint, points[2], points[3], scaleFactor);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					canvas.drawPoint(scaleFactor * point.getX(), scaleFactor
							* point.getY(), paint);
				}
			}
		}
	}

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a,
			ResultPoint b, float scaleFactor) {
		if (a != null && b != null) {
			canvas.drawLine(scaleFactor * a.getX(), scaleFactor * a.getY(),
					scaleFactor * b.getX(), scaleFactor * b.getY(), paint);
		}
	}

	// Put up our own UI for how to handle the decoded contents.
	private void handleDecodeInternally(Result rawResult,
			ResultHandler resultHandler, Bitmap barcode) {
		viewfinderView.setVisibility(View.GONE);

		ImageView barcodeImageView = (ImageView) findViewById(R.id.barcode_image_view);
		if (barcode == null) {
			barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(
					getResources(), R.drawable.ic_launcher));
		} else {
			barcodeImageView.setImageBitmap(barcode);
		}

		TextView formatTextView = (TextView) findViewById(R.id.format_text_view);
		formatTextView.setText(rawResult.getBarcodeFormat().toString());

		TextView typeTextView = (TextView) findViewById(R.id.type_text_view);
		typeTextView.setText(resultHandler.getType().toString());

		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.SHORT);
		String formattedTime = formatter.format(new Date(rawResult
				.getTimestamp()));
		TextView timeTextView = (TextView) findViewById(R.id.time_text_view);
		timeTextView.setText(formattedTime);

		TextView metaTextView = (TextView) findViewById(R.id.meta_text_view);
		View metaTextViewLabel = findViewById(R.id.meta_text_view_label);
		metaTextView.setVisibility(View.GONE);
		metaTextViewLabel.setVisibility(View.GONE);
		Map<ResultMetadataType, Object> metadata = rawResult
				.getResultMetadata();
		if (metadata != null) {
			StringBuilder metadataText = new StringBuilder(20);
			for (Map.Entry<ResultMetadataType, Object> entry : metadata
					.entrySet()) {
				if (DISPLAYABLE_METADATA_TYPES.contains(entry.getKey())) {
					metadataText.append(entry.getValue()).append('\n');
				}
			}
			if (metadataText.length() > 0) {
				metadataText.setLength(metadataText.length() - 1);
				metaTextView.setText(metadataText);
				metaTextView.setVisibility(View.VISIBLE);
				metaTextViewLabel.setVisibility(View.VISIBLE);
			}
		}

		TextView contentsTextView = (TextView) findViewById(R.id.contents_text_view);
		CharSequence displayContents = resultHandler.getDisplayContents();
		contentsTextView.setText(displayContents);
		// Crudely scale betweeen 22 and 32 -- bigger font for shorter text
		int scaledSize = Math.max(22, 32 - displayContents.length() / 4);
		contentsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);

		TextView supplementTextView = (TextView) findViewById(R.id.contents_supplement_text_view);
		supplementTextView.setText("");
		supplementTextView.setOnClickListener(null);
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				PreferencesActivity.KEY_SUPPLEMENTAL, true)) {
			SupplementalInfoRetriever.maybeInvokeRetrieval(supplementTextView,
					resultHandler.getResult(), historyManager, this);
		}

		int buttonCount = resultHandler.getButtonCount();
		ViewGroup buttonView = (ViewGroup) findViewById(R.id.result_button_view);
		buttonView.requestFocus();
		for (int x = 0; x < ResultHandler.MAX_BUTTON_COUNT; x++) {
			TextView button = (TextView) buttonView.getChildAt(x);
			if (x < buttonCount) {
				button.setVisibility(View.VISIBLE);
				button.setText(resultHandler.getButtonText(x));
				button.setOnClickListener(new ResultButtonListener(
						resultHandler, x));
			} else {
				button.setVisibility(View.GONE);
			}
		}

		if (copyToClipboard && !resultHandler.areContentsSecure()) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			if (displayContents != null) {
				try {
					clipboard.setText(displayContents);
				} catch (NullPointerException npe) {
					// Some kind of bug inside the clipboard implementation, not
					// due to null input
					Log.w(TAG, "Clipboard bug", npe);
				}
			}
		}
	}

	// Briefly show the contents of the barcode, then handle the result outside
	// Barcode Scanner.
	private void handleDecodeExternally(Result rawResult,
			ResultHandler resultHandler, Bitmap barcode) {

		// if (barcode != null) {
		// viewfinderView.drawResultBitmap(barcode);
		// }

		long resultDurationMS;
		if (getIntent() == null) {
			resultDurationMS = DEFAULT_INTENT_RESULT_DURATION_MS;
		} else {
			resultDurationMS = getIntent().getLongExtra(
					Intents.Scan.RESULT_DISPLAY_DURATION_MS,
					DEFAULT_INTENT_RESULT_DURATION_MS);
		}

		if (resultDurationMS > 0) {
			String rawResultString = String.valueOf(rawResult);
			if (rawResultString.length() > 32) {
				rawResultString = rawResultString.substring(0, 32) + " ...";
			}
		}

		if (copyToClipboard && !resultHandler.areContentsSecure()) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			CharSequence text = resultHandler.getDisplayContents();
			if (text != null) {
				try {
					clipboard.setText(text);
				} catch (NullPointerException npe) {
					// Some kind of bug inside the clipboard implementation, not
					// due to null input
					Log.w(TAG, "Clipboard bug", npe);
				}
			}
		}

		if (source == IntentSource.NATIVE_APP_INTENT) {

			// Hand back whatever action they requested - this can be changed to
			// Intents.Scan.ACTION when
			// the deprecated intent is retired.
			Intent intent = new Intent(getIntent().getAction());
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			/* Bao add code here */
			// RESULT GOT FROM SCANNING
			intent.putExtra(Intents.Scan.RESULT, rawResult.toString());

			// Stop Preview
			cameraManager.stopPreview();
			// Check whether to play sound or not
			if (mAppPreferences.isSound()
					&& mAudio.getStreamVolume(AudioManager.STREAM_RING) != 0) {
				beepManager.playBeepSoundAndVibrate();
			}
			// Create dialog confirm to avoid opening twice
			createDialogConfirmBrowsing(rawResult.toString());
			if (mAppPreferences.getProfileUrl().equalsIgnoreCase("-1")) {
				// There is no URL profile format
				continueScan(rawResult.toString());
			} else {
				if (checkInvalidURL(rawResult.toString())) {
					// Continue scan following fixed URL format
					continueScan(rawResult.toString());
				} else if (!alertDialog.isShowing()) {
					alertDialog.show();
				}

			}
			// Start Preview
			cameraManager.startPreview();
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG,
					"initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						decodeHints, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		resetStatusView();
	}

	private void resetStatusView() {
		viewfinderView.setVisibility(View.VISIBLE);
		lastResult = null;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
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
									cameraManager.startPreview();
									restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
								}
							}); // create alert dialog
			alertDialog = alertDialogBuilder.create();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* ASK BEFORE OPENING BROWSER DIALOG */
	public void createDialogConfirmBrowsing(final String scanResult) {
		dialogConfirm = new DialogConfirm(CaptureActivity.this,
				android.R.drawable.ic_dialog_alert,
				CaptureActivity.this
						.getString(R.string.activity_scan_open_browser_title),
				CaptureActivity.this
						.getString(R.string.activity_scan_open_url_confirm),
				true, new ProcessDialogConfirm() {

					@Override
					public void click_Ok() {

						Intent dataIntent = new Intent(CaptureActivity.this,
								BrowserActivity.class);
						dataIntent.putExtra(StringExtraUtils.KEY_SCAN_RESULT,
								scanResult);
						startActivity(dataIntent);

					}

					@Override
					public void click_Cancel() {
						// Start scanning again
						cameraManager.startPreview();
						restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
					}
				});
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

	public void continueScan(final String scanResult) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.US);

		String date = formatter.format(new Date());
		mDataHandler.addQRCode(new QRCode(date, scanResult));

		// Get the QR Code after scanning and put it to
		// Browser
		// for
		// searching on WebSite

		if (!mAppPreferences.getAskBeforeOpening()) {
			Intent dataIntent = new Intent(CaptureActivity.this,
					BrowserActivity.class);
			dataIntent.putExtra(StringExtraUtils.KEY_SCAN_RESULT, scanResult);
			startActivity(dataIntent);
		} else {
			if (!dialogConfirm.isShowing()) {
				dialogConfirm.show();
			}
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
			startActivity(new Intent(CaptureActivity.this,
					HistoryActivity.class));
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
			startActivity(new Intent(CaptureActivity.this,
					SettingActivity.class));
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
			Toast.makeText(this, getString(R.string.mess_not_exist_shortcut),
					Toast.LENGTH_SHORT).show();
		} else {
			Intent dataIntent = new Intent(CaptureActivity.this,
					BrowserActivity.class);
			dataIntent.putExtra(StringExtraUtils.KEY_SCAN_RESULT,
					mAppPreferences.getShortcusUrl().trim());
			startActivity(dataIntent);
		}
	}
}
