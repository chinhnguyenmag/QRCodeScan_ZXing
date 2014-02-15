package com.captix.scan.social.facebook;

import android.os.Bundle;

public interface FacebookDialogLoginListener {
	void onComplete(Bundle values);

	void onFacebookError(FacebookError error);

	void facebookLoginFail();
}
