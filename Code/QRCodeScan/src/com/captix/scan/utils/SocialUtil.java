package com.captix.scan.utils;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;

import com.captix.scan.model.FaceBookAccount;
import com.captix.scan.social.facebook.Facebook;
import com.captix.scan.social.facebook.FacebookListener;
import com.captix.scan.social.facebook.SessionStore;
import com.captix.scan.social.facebook.Facebook.DialogListener;

public class SocialUtil {

	public static final String SHARED_PREFERENCES = "Account_Preferences";

	/* Instagram Constanst */
	public static final String INSTAGRAM_CLIENT_ID = "379d744556c743c090c8a2014779f59f";
	public static final String INSTAGRAM_CLIENT_SECRET = "fd6ec75e44054da1a5088ad2d72f2253";
	public static final String INSTAGRAM_CALLBACK_URL = "instagram://connect";

	/* Google Constanst */
	public static final String GOOGLE_CONSUMER_KEY = "1094114369185-bn7tlapn4danul3i4fkabhff9ppre7ke.apps.googleusercontent.com";
	public static final String GOOGLE_CONSUMER_SECRET = "CHgCfrGFrIo1nX6dv9WdblPE";

	public static final String GOOGLE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";
	public static final String GOOGLE_REQUEST_URL = "https://www.google.com/accounts/OAuthGetRequestToken";
	public static final String GOOGLE_ACCESS_URL = "https://www.google.com/accounts/OAuthGetAccessToken";
	public static final String GOOGLE_AUTHORIZE_URL = "https://www.google.com/accounts/OAuthAuthorizeToken";

	public static final String GOOGLE_GET_PROFILE = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json";

	public static final String ENCODING = "UTF-8";

	public static final String GOOGLE_OAUTH_CALLBACK_SCHEME = "oauth-example";
	public static final String GOOGLE_OAUTH_CALLBACK_HOST = "callback";
	public static final String GOOGLE_OAUTH_CALLBACK_URL = GOOGLE_OAUTH_CALLBACK_SCHEME
			+ "://" + GOOGLE_OAUTH_CALLBACK_HOST;
	public static final String GOOGLE_APP_NAME = "MyMenu";

	/* Twitter Constanst */
//	public static final String TWITTER_CONSUMER_KEY = "n5Uxq7r0n2imgb48UO5fBA";
//	public static final String TWITTER_CONSUMER_SECRET = "EDA35k4DwQrlR5C0tb8kebvTZUN2Ioxi2NSZCfpGo";

	public static final String TWITTER_AUTH_KEY = "twitter_auth_key";
	public static final String TWITTER_AUTH_SECRET_KEY = "twitter_auth_secret_key";
	public static final String TWITTER_USER_NAME = "twitter_user_name";
	public static final String TWITTER_IMAGE_URL = "twitter_image_url";
	public static final String TWITTER_UER_ID = "twitter_user_id";
	public static final String TWITTER_USER_PROFILE_URL = "twitter_user_profile_url";
	public static final String TWITTER_CALLBACK_URL = "twitterapp://connect";
	public static final String TWITTER_ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
	public static final String TWITTER_AUTHORZE_URL = "https://api.twitter.com/oauth/authorize";
	public static final String TWITTER_REQUEST_URL = "https://api.twitter.com/oauth/request_token";

	/* Facebook Constans */

	/**
	 * This is chinh's facebook appid
	 */
	//public static final String FACEBOOK_APPID = "1413450295564258";
	
	/**
	 * This is vu's facebook appid
	 */
	public static final String FACEBOOK_APPID = "588040127956217";
	

	public static final String FACEBOOK_SECRET_APPID = "d39c6c15302f553251f9949c40b0ea21";
	public static final String FACEBOOK_ACCESS_TOKEN = "facebook_access_token";
	public static final String FACEBOOK_EXPIRES = "facebook_expires_in";
	public static final String FACEBOOK_NAME = "facebook_name";
	public static final String FACEBOOK_USERNAME = "facebook_user_name";
	public static final String FACEBOOK_IMAGE_URL = "facebook_image_url";
	public static final String FACEBOOK_FIRST_NAME = "facebook_firstname";
	public static final String FACEBOOK_LAST_NAME = "facebook_lastname";
	public static final String FACEBOOK_EMAIL = "facebook_email";
	public static final String FACEBOOK_ACCOUNT_RESOURCE = "facebook_account_resource";
	public static final String FACEBOOK_USER_ID = "facebook_user_id";
	public static final String FACEBOOK_GENDER = "facebook_gender";

	public static String RESTSERVER_URL = "https://api.facebook.com/restserver.php";
	public static String GRAPH_BASE_URL = "https://graph.facebook.com/";

	public static final String[] FACEBOOK_PERMISSIONS = new String[] {
			"publish_stream", "read_stream", "offline_access", "email",
			"user_photos" };

	/**
	 * =========================================================
	 * ===================== Facebook ============================
	 * =========================================================
	 */
	/**
	 * ===================== Login Facebook =======================
	 * 
	 * @param activity
	 * @param mFacebook
	 * @param dgListener
	 * @param delegate
	 */
	public static void loginFacebook(Activity activity, Facebook mFacebook,
			DialogListener dgListener, FacebookListener delegate) {
		SessionStore.restore(mFacebook, activity.getApplicationContext());
		if (mFacebook.isSessionValid()) {
			delegate.facebookLoginFail();
		} else {
			mFacebook.authorize(activity, SocialUtil.FACEBOOK_PERMISSIONS, -1,
					dgListener);
		}
	}

	/**
	 * =================== Get FaceBook Name ======================
	 */
	public static void getFacebookName(final Context context,
			final Facebook mFacebook, final FacebookListener delegate) {
		new Thread() {
			public void run() {
				FaceBookAccount fbacc = new FaceBookAccount();
				try {
					String me = mFacebook.request("me");
					JSONObject jsonObj = (JSONObject) new JSONTokener(me)
							.nextValue();

					if (jsonObj.has("name"))
						fbacc.mName = jsonObj.getString("name");

					if (jsonObj.has("first_name"))
						fbacc.mFirstName = jsonObj.getString("first_name");

					if (jsonObj.has("last_name"))
						fbacc.mLastName = jsonObj.getString("last_name");

					if (jsonObj.has("username"))
						fbacc.mUserName = jsonObj.getString("username");

					if (jsonObj.has("id"))
						fbacc.mUserId = jsonObj.getString("id");

					if (jsonObj.has("email"))
						fbacc.mEmail = jsonObj.getString("email");

					if (jsonObj.has("gender"))
						fbacc.mGender = jsonObj.getString("gender");

					fbacc.mAccessToken = mFacebook.getAccessToken();
					fbacc.mExpire = String
							.valueOf(mFacebook.getAccessExpires());
					fbacc.mAccountResource = me;
					fbacc.saveFacebookData(context);
					delegate.facebookLoginSuccess(fbacc);
				} catch (Exception ex) {
					fbacc.clearFacebookData(context);
					delegate.facebookLoginError();
				}
			}
		}.start();
	}

	public static abstract class FacebookDelegate {
		public abstract void loginFail();

		public abstract void success(FaceBookAccount content);

		public abstract void error(String content);
	}

	/**
	 * =================== Twitter ================================
	 * ============= Chuyen chuoi co dinh dang ========================
	 * 
	 * input : dsds @name #tag comment #rock dsdsds
	 * 
	 * output : dsds <a href = https://twitter/com/name><font
	 * color="\#1b39f4\">@name</font></a> <a
	 * href=https://twitter.com/search/?src=hash&q=%23tag><font
	 * color="\#1b39f4\">#tag</font></a> comment <a
	 * href=https://twitter.com/search/?src=hash&q=%23rock><font
	 * color="\#1b39f4\">#rock</font></a> dsdsds
	 * 
	 * 
	 * @param strContent
	 * @return
	 */
	public static String convertToTwitterFormat(String strContent) {
		if (strContent.contains("@") || strContent.contains("#")) {
			StringBuffer strBuf = new StringBuffer();
			char[] chs = strContent.toCharArray();
			int index = 0;
			int len = chs.length;
			int num = 0;
			while (index < len) {
				char c = chs[index];
				num = 1;
				if (c == '@') {
					// xử lý @
					String strSub = checkSubString(index, chs);
					num = strSub.length();
					if (strSub.endsWith(":"))
						strSub = strSub.substring(0, strSub.length() - 1);

					strBuf.append(addUser(strSub));

				} else if (c == '#') {
					// xu ly khi gap #
					String strSub = checkSubString(index, chs);
					num = strSub.length();
					if (strSub.endsWith(":"))
						strSub = strSub.substring(0, strSub.length() - 1);

					strBuf.append(addTag(strSub));
				} else {
					strBuf.append(c);
				}
				index += num;
			}
			return strBuf.toString();
		} else
			return strContent;
	}

	/**
	 * xử lý chuổi con khi gặp ký tự @ hoạc #
	 * 
	 * @param startIndex
	 * @param chs
	 * @return
	 */
	private static String checkSubString(int startIndex, char[] chs) {
		StringBuffer buf = new StringBuffer();
		for (int i = startIndex, l = chs.length; i < l; i++) {
			char c = chs[i];
			if (c != ' ') {
				buf.append(c);
			} else
				break;
		}

		return buf.toString();
	}

	/**
	 * add font
	 * 
	 * @param content
	 * @return
	 */
	private static String addFont(String content) {
		return "<font color=\"#1b39f4\">" + content + "</font>";
	}

	private static String addUser(String content) {
		return "<a href = \"https://twitter.com/" + content.replace("@", "")
				+ "\">" + addFont(content) + "</a>";
	}

	private static String addTag(String content) {
		return "<a href = \"https://twitter.com/search/?src=hash&q=%23"
				+ content.replace("#", "") + "\">" + addFont(content) + "</a>";
	}
}
