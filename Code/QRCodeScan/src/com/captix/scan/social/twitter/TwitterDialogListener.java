package com.captix.scan.social.twitter;

public interface TwitterDialogListener {
	public void onComplete(String value);

	public void onError(String value);
}
