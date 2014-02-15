package com.captix.scan.customview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.captix.scan.R;

public class DialogPostToWall extends Dialog implements
		android.view.View.OnClickListener {

	private Button mBtCancel;
	private Button mBtPost;
	private EditText mEtContent;
	private Activity mActivity;
	private ProcessDialogPostToWallTwitter mProcessDialogPostToWallTwitter;

	public DialogPostToWall(Context context,
			ProcessDialogPostToWallTwitter processDialogPostToWallTwitter) {
		super(context);
		mActivity = (Activity) context;
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		setContentView(R.layout.dialog_twitter_post_t_wall);
		mBtCancel = (Button) findViewById(R.id.post_to_wall_bt_cancel);
		mBtPost = (Button) findViewById(R.id.post_to_wall_bt_post);
		mEtContent = (EditText) findViewById(R.id.post_to_wall_et_content);
		mBtCancel.setOnClickListener(this);
		mBtPost.setOnClickListener(this);
		mProcessDialogPostToWallTwitter = processDialogPostToWallTwitter;
		mEtContent.setText("Follow this link http://QRcode.com");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.post_to_wall_bt_cancel: {
			mProcessDialogPostToWallTwitter.click_Cancel();
			dismiss();
			break;
		}

		case R.id.post_to_wall_bt_post: {
			if (getContent().length() > 0) {
				mProcessDialogPostToWallTwitter.click_Post(getContent());
				dismiss();
			} else {
				Toast.makeText(mActivity, "Please input your content to post.",
						Toast.LENGTH_SHORT).show();
			}
			break;
		}

		default:
			break;
		}
	}
	
	/**
	 * @Description: Turn off virtual keyboard when touch on outside of text box
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		View v = getCurrentFocus();
		boolean ret = super.dispatchTouchEvent(event);

		if (v instanceof EditText) {
			View w = getCurrentFocus();
			int scrcoords[] = new int[2];
			w.getLocationOnScreen(scrcoords);
			float x = event.getRawX() + w.getLeft() - scrcoords[0];
			float y = event.getRawY() + w.getTop() - scrcoords[1];

			if (event.getAction() == MotionEvent.ACTION_UP
					&& (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w
							.getBottom())) {
				InputMethodManager imm = (InputMethodManager) getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindow().getCurrentFocus()
						.getWindowToken(), 0);
			}
		}
		return ret;
	}

	public String getContent() {
		return mEtContent.getText().toString().trim();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	/**
	 * @author Thanh Vu
	 * 
	 */
	public static abstract class ProcessDialogPostToWallTwitter {
		public abstract void click_Post(String mMessage);

		public abstract void click_Cancel();
	}

}
