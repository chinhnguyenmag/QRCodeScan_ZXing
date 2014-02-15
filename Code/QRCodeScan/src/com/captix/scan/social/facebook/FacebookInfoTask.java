package com.captix.scan.social.facebook;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.AsyncTask;

import com.captix.scan.model.FaceBookAccount;
import com.captix.scan.server.Server;
import com.captix.scan.social.facebook.model.ImageProfileEntity;
import com.google.gson.Gson;

public class FacebookInfoTask extends AsyncTask<Void, Void, Boolean> {

	private final WeakReference<Facebook> mFacebook;
	private final WeakReference<Context> mContext;
	private final WeakReference<FacebookListener> mListener;
	private FaceBookAccount mFacebookAccout = null;

	public static void execute(Context context, Facebook facebook,
			FacebookListener listener) {
		new FacebookInfoTask(context, facebook, listener).execute();
	}

	private FacebookInfoTask(Context context, Facebook facebook,
			FacebookListener listener) {
		mContext = new WeakReference<Context>(context);
		mListener = new WeakReference<FacebookListener>(listener);
		mFacebook = new WeakReference<Facebook>(facebook);
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		Context context = mContext.get();
		Facebook facebook = mFacebook.get();

		boolean result = true;
		try {
			mFacebookAccout = new FaceBookAccount();
			try {
				String me = facebook.request("me");
				JSONObject jsonObj = (JSONObject) new JSONTokener(me)
						.nextValue();

				if (jsonObj.has("name"))
					mFacebookAccout.mName = jsonObj.getString("name");

				if (jsonObj.has("first_name"))
					mFacebookAccout.mFirstName = jsonObj
							.getString("first_name");

				if (jsonObj.has("last_name"))
					mFacebookAccout.mLastName = jsonObj.getString("last_name");

				if (jsonObj.has("username"))
					mFacebookAccout.mUserName = jsonObj.getString("username");

				if (jsonObj.has("id"))
					mFacebookAccout.mUserId = jsonObj.getString("id");

				if (jsonObj.has("email"))
					mFacebookAccout.mEmail = jsonObj.getString("email");

				// if (jsonObj.has("gender"))
				// mFacebookAccout.mGender = jsonObj.getString("gender");

				String url = "https://graph.facebook.com/"
						+ mFacebookAccout.mUserId
						+ "?fields=picture.type(large)";

				InputStream stream = Server.requestGet(url);
				Gson gson = new Gson();

				String json = Server.inputStreamToString(stream).toString();
				ImageProfileEntity response = gson.fromJson(json,
						ImageProfileEntity.class);

				mFacebookAccout.mPicture = response.mProfile.mData.mUrlAvatar;

				mFacebookAccout.mAccessToken = facebook.getAccessToken();
				mFacebookAccout.mExpire = String.valueOf(facebook
						.getAccessExpires());
				mFacebookAccout.mAccountResource = me;
				mFacebookAccout.saveFacebookData(context);
			} catch (Exception ex) {
				mFacebookAccout.clearFacebookData(context);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		FacebookListener listener = mListener.get();
		if (result) {
			listener.facebookLoginSuccess(mFacebookAccout);
		} else {
			listener.facebookLoginError();
		}
	}
}
