package com.captix.scan.customview;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.captix.scan.R;

/**
 * 
 * @author Hung Hoang
 * 
 */
public class DialogChangeProfile extends BaseDialog implements OnClickListener {
	private Button mBtOk;
	private Button mBtCancel;
	private EditText mEtUrlProfile;
	private ProcessDialogProfile mProcess;
	private String mUrl = "";

	/**
	 * 
	 * 
	 * @param context
	 * @param arrMenu
	 */
	public DialogChangeProfile(Context context, String urlProfile,
			ProcessDialogProfile process) {
		super(context);
		this.mContext = context;
		this.mProcess = process;
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		/** Design the dialog in main.xml file */
		setContentView(R.layout.dialog_change_profile);
		mBtOk = (Button) findViewById(R.id.dialog_change_profile_bt_Ok);
		mBtCancel = (Button) findViewById(R.id.dialog_change_profile_bt_Cancel);
		mEtUrlProfile = (EditText) findViewById(R.id.dialog_change_profile_et_url);
		if (urlProfile.equals("-1")) {
			mEtUrlProfile.setText("");
		} else {
			mEtUrlProfile.setText(urlProfile);
		}
		mEtUrlProfile.requestFocus();
		mBtOk.setOnClickListener(this);
		mBtCancel.setOnClickListener(this);

	}

	public static abstract class ProcessDialogProfile {
		public abstract void click_Ok(String url);

		public abstract void click_Cancel();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dialog_change_profile_bt_Ok:
			if (validate()) {
				mProcess.click_Ok(mUrl);
			}
			dismiss();
			break;
		case R.id.dialog_change_profile_bt_Cancel:
			mProcess.click_Cancel();
			dismiss();
			break;
		default:
			break;
		}
	}

	public boolean validate() {
		String urlProfile = mEtUrlProfile.getText().toString().trim();
		if (urlProfile.length() == 0) {
			mUrl = "-1";
			return true;
		}

		if (urlProfile.length() <= 3) {
			Toast.makeText(mContext,
					mContext.getString(R.string.mess_update_urlprofile),
					Toast.LENGTH_LONG).show();
			return false;
		}

		String urlProfile2 = urlProfile;
		urlProfile2 = urlProfile2.replace("http://", "");
		urlProfile2 = urlProfile2.replace("https://", "");
		urlProfile2 = urlProfile2.replace("www.", "");
		urlProfile2 = urlProfile2.replace("ftp://", "");

		urlProfile2 = urlProfile2.replace("HTTP://", "");
		urlProfile2 = urlProfile2.replace("HTTPS://", "");
		urlProfile2 = urlProfile2.replace("WWW.", "");
		urlProfile2 = urlProfile2.replace("FTP://", "");

		if (urlProfile.indexOf("/") != -1) {
			String[] domain = urlProfile2.split("/");
			if (!domain[0].contains(" ") && domain[0].contains(".")) {
				mUrl = urlProfile;
				return true;
			}
		} else {
			if (!urlProfile2.contains(" ") && urlProfile2.contains(".")) {
				mUrl = urlProfile;
				return true;
			}
		}

		// Toast.makeText(
		// mContext,
		// "Invalid URL profile. Url format should be  cptr.it/?var={variable}&id=test.",
		// Toast.LENGTH_LONG).show();
		Toast.makeText(mContext,
				mContext.getString(R.string.mess_update_urlprofile),
				Toast.LENGTH_LONG).show();
		return false;
	}
}
