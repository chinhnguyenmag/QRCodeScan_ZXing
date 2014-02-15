package com.captix.scan.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AppPreferences {
	private final String APP_SHARED_PREFS = "CaptixScanConfiguration";
	private SharedPreferences mAppSharedPrefs;
	private Editor mPrefsEditor;

	public AppPreferences(Context context) {
		this.mAppSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS,
				Context.MODE_PRIVATE);
		this.mPrefsEditor = mAppSharedPrefs.edit();
	}

	/**
	 * @param serverUrl
	 * @return
	 */
	public boolean setServerURL(String serverUrl) {
		mPrefsEditor.putString("Server", serverUrl);
		mPrefsEditor.commit();
		return true;
	}

	/**
	 * @return server url.
	 */
	public String getServerURL() {
		String server = mAppSharedPrefs.getString("Server", "");
		return server;
	}

	/**
	 * @param isSound
	 * @return
	 */
	public boolean setSound(boolean isSound) {
		mPrefsEditor.putBoolean("Sound", isSound);
		mPrefsEditor.commit();
		return true;
	}

	/**
	 * @return open sound true/false
	 */
	public boolean isSound() {
		boolean isSound = mAppSharedPrefs.getBoolean("Sound", false);
		return isSound;
	}

	/**
	 * @param isOpen
	 * @return
	 */
	public boolean setAskBeforeOpening(boolean isOpen) {
		mPrefsEditor.putBoolean("OpenUrl", isOpen);
		mPrefsEditor.commit();
		return true;
	}

	/**
	 * @return auto open url true/false.
	 */
	public boolean getAskBeforeOpening() {
		boolean isOpen = mAppSharedPrefs.getBoolean("OpenUrl", true);
		return isOpen;
	}

	/**
	 * @param profileUrl
	 * @return
	 */
	public boolean setProfileUrl(String profileUrl) {
		mPrefsEditor.putString("ProfileUrl", profileUrl);
		mPrefsEditor.commit();
		return true;
	}

	/**
	 * @return url of profile.
	 */
	public String getProfileUrl() {
		String profileUrl = mAppSharedPrefs.getString("ProfileUrl", "");
		return profileUrl;
	}
	
	
	/**
	 * @param profileUrl
	 * @return
	 */
	public boolean setShortcutUrl(String shortcutUrl) {
		mPrefsEditor.putString("shortcutUrl", shortcutUrl);
		mPrefsEditor.commit();
		return true;
	}

	/**
	 * @return url of profile.
	 */
	public String getShortcusUrl() {
		String profileUrl = mAppSharedPrefs.getString("shortcutUrl", "");
		return profileUrl;
	}

	/**
	 * @param closeUrl
	 * @return
	 */
	public boolean setCloseUrl(int closeUrl) {
		mPrefsEditor.putInt("CloseUrl", closeUrl);
		mPrefsEditor.commit();
		return true;
	}

	/**
	 * 
	 * @return auto close url view after time
	 */
	public int getCloseUrlTime() {
		int closeUrl = mAppSharedPrefs.getInt("CloseUrl", -1);
		return closeUrl;
	}
}
