package com.captix.scan.utils;

import android.content.Context;
import android.content.Intent;

public class Constants {

	// Flurry
	public static final String FLURRY_KEY = "M7WPM285B2C9W88MFNJC";
	// TestFlight
	public static final String TESTFLIGHT_TOKEN = "93a840ab-5afd-4e1c-8ef2-138c254a0a7b";

	public static final int LOGIN_VIA_DEFAULT = 0;
	public static final int LOGIN_VIA_FACEBOOK = 1;
	public static final int LOGIN_VIA_TWITTER = 2;
	public static final int LOGIN_VIA_GOOGLE = 3;
	public static final int LOGIN_VIA_INSTAGRAM = 4;

	public static final String CONNECT_ERROR = "failed";
	public static final String CONNECT_SUCCESS = "success";
	public static final String MESSAGE_NO_ACCESSTOKEN = "No access token passed";
	public static final String MESSAGE_INVALID_TOKEN = "Invalid Auth key";

	// provider -> facebook , twitter , google_oauth2
	public static final String PROVIDER_FACEBOOK = "facebook";
	public static final String PROVIDER_TWITTER = "twitter";
	public static final String PROVIDER_GOOGLE = "google_oauth2";

	public static final int CATEGORY_FROM_RESTAURANT = 1;
	public static final int CATEGORY_FROM_SEARCH_MENU_ITEM = 2;
	public static final int CATEGORY_FROM_FAVORITE = 3;

	public static boolean isChangeOrder = false;

	public static final String DISPLAY_MESSAGE_ACTION = "DISPLAY_MESSAGE";
	public static final String KEY_MESSAGE_ACTION = "receiver_message";

	public static final String TAG = "TwitterLogin";

	// TWITTER OAUTH====================================================
	public static final String PREFERENCE_NAME = "twitter_oauth";
	public static final String PREF_KEY_SECRET = "oauth_token_secret";
	public static final String PREF_KEY_TOKEN = "oauth_token";

	public static final String TWITTER_CALLBACK_URL = "x-oauthflow-twitter://twitterlogin";

	public static final String IEXTRA_AUTH_URL = "auth_url";
	public static final String IEXTRA_OAUTH_VERIFIER = "oauth_verifier";
	public static final String IEXTRA_OAUTH_TOKEN = "oauth_token";
	// =================================================================
	public static final String VALIDATE_URL_PROFILE = "XXXXX";

	public static void displayMessage(Context context, String message) {

		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(KEY_MESSAGE_ACTION, message);
		context.sendBroadcast(intent);
	}
}
