package com.captix.scan.social.facebook;

import com.captix.scan.model.FaceBookAccount;

public interface FacebookListener {
	void facebookLoginSuccess(FaceBookAccount facebookAccount);

	void facebookLoginError();

	void facebookLoginFail();
}
