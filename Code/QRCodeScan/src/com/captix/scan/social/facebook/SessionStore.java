/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.captix.scan.social.facebook;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.captix.scan.model.FaceBookAccount;
import com.captix.scan.utils.SocialUtil;

public class SessionStore {

	public static void save(Facebook session, Context context) {
		FaceBookAccount fbacc = FaceBookAccount.getInstance(context);
		fbacc.saveFacebookData(context);
	}

	public static boolean restore(Facebook session, Context context) {
		FaceBookAccount fbacc = FaceBookAccount.getInstance(context);
		fbacc.loadFacebookData(context);
		session.setAccessToken(fbacc.mAccessToken);

		long expired = -1;
		try {
			expired = Long.valueOf(fbacc.mExpire);
		} catch (Exception e) {
			expired = 0;
		}
		session.setAccessExpires(expired);

		return session.isSessionValid();
	}

	public static boolean saveName(String name, Context context) {
		Editor editor = context.getSharedPreferences(
				SocialUtil.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
		editor.putString(SocialUtil.FACEBOOK_NAME, name);

		return editor.commit();
	}

	public static String getName(Context context) {
		SharedPreferences savedSession = context.getSharedPreferences(
				SocialUtil.SHARED_PREFERENCES, Context.MODE_PRIVATE);

		return savedSession.getString(SocialUtil.FACEBOOK_NAME, "Unknown");
	}

	// public static void clear(Context context) {
	// FaceBookAccount fbacc = FaceBookAccount.getInstance();
	// fbacc.ClearFacebookData(context);
	// }

}
