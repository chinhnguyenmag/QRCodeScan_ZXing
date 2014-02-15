package com.captix.scan.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.captix.scan.social.facebook.Facebook;
import com.captix.scan.utils.SocialUtil;

public class FaceBookAccount {
	public Facebook mFacebook;
	private static final String APP_SHARED_PREFS = "FacebookConfiguration";
	private SharedPreferences mFacebookSharedPrefs;
	private Editor mPrefsEditor;
	public String mFirstName = "";
	public String mLastName = "";
	public String mUserName = "";
	public String mGender = "";
	public String mName = "";
	public String mEmail = "";
	public String mUserId = "";
	public String mAccountResource = "";
	public String mAccessToken = "";
	public String mExpire = "";
	public String mPicture = "";

	private static FaceBookAccount instance;

	public static FaceBookAccount getInstance(Context context) {
		if (instance == null) {
			instance = new FaceBookAccount(context);
		}
		return instance;
	}

	public FaceBookAccount() {

	}

	public FaceBookAccount(Context context) {
		mFacebookSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS,
				Context.MODE_PRIVATE);

		mPrefsEditor = mFacebookSharedPrefs.edit();
	}

	public Facebook getFacebook() {
		return mFacebook;
	}

	public void setFacebook(Facebook mFacebook) {
		this.mFacebook = mFacebook;
	}

	public void loadFacebookData(Context context) {
		if (mFacebookSharedPrefs == null) {
			this.mFacebookSharedPrefs = context.getSharedPreferences(
					APP_SHARED_PREFS, Context.MODE_PRIVATE);
			this.mPrefsEditor = mFacebookSharedPrefs.edit();
		}

		this.mAccessToken = mFacebookSharedPrefs.getString(
				SocialUtil.FACEBOOK_ACCESS_TOKEN, "");
		this.mAccountResource = mFacebookSharedPrefs.getString(
				SocialUtil.FACEBOOK_ACCOUNT_RESOURCE, "");
		this.mEmail = mFacebookSharedPrefs.getString(SocialUtil.FACEBOOK_EMAIL,
				"");
		this.mFirstName = mFacebookSharedPrefs.getString(
				SocialUtil.FACEBOOK_FIRST_NAME, "");
		this.mGender = mFacebookSharedPrefs.getString(
				SocialUtil.FACEBOOK_GENDER, "");
		this.mLastName = mFacebookSharedPrefs.getString(
				SocialUtil.FACEBOOK_LAST_NAME, "");
		this.mName = mFacebookSharedPrefs.getString(SocialUtil.FACEBOOK_NAME,
				"");
		this.mUserId = mFacebookSharedPrefs.getString(
				SocialUtil.FACEBOOK_USER_ID, "");
		this.mUserName = mFacebookSharedPrefs.getString(
				SocialUtil.FACEBOOK_USERNAME, "");
		this.mExpire = mFacebookSharedPrefs.getString(
				SocialUtil.FACEBOOK_EXPIRES, "-1");

	}

	public void saveFacebookData(Context context) {
		if (mFacebookSharedPrefs == null) {
			this.mFacebookSharedPrefs = context.getSharedPreferences(
					APP_SHARED_PREFS, Context.MODE_PRIVATE);
			this.mPrefsEditor = mFacebookSharedPrefs.edit();
		}
		mPrefsEditor.putString(SocialUtil.FACEBOOK_ACCESS_TOKEN,
				this.mAccessToken);
		mPrefsEditor.putString(SocialUtil.FACEBOOK_ACCOUNT_RESOURCE,
				this.mAccountResource);
		mPrefsEditor.putString(SocialUtil.FACEBOOK_EMAIL, this.mEmail);
		mPrefsEditor.putString(SocialUtil.FACEBOOK_FIRST_NAME, this.mFirstName);
		mPrefsEditor.putString(SocialUtil.FACEBOOK_LAST_NAME, this.mLastName);
		mPrefsEditor.putString(SocialUtil.FACEBOOK_GENDER, this.mGender);
		mPrefsEditor.putString(SocialUtil.FACEBOOK_NAME, this.mName);
		mPrefsEditor.putString(SocialUtil.FACEBOOK_USER_ID, this.mUserId);
		mPrefsEditor.putString(SocialUtil.FACEBOOK_USERNAME, this.mUserName);
		mPrefsEditor.putString(SocialUtil.FACEBOOK_EXPIRES, this.mExpire);
		mPrefsEditor.commit();
	}

	public void clearFacebookData(Context context) {
		mPrefsEditor.clear();
		mPrefsEditor.commit();
	}

	public boolean checkHasToken(Context context) {
		SharedPreferences savedSession = context.getSharedPreferences(
				SocialUtil.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		String token = savedSession.getString(SocialUtil.FACEBOOK_ACCESS_TOKEN,
				"");
		return !token.equals("");
	}
}
