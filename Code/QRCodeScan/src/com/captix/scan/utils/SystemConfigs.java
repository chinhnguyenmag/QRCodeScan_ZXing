package com.captix.scan.utils;

import android.util.DisplayMetrics;

/**
 * This class contain Screen config
 * 
 * @author DUC CHINH
 * 
 */
public class SystemConfigs {
	public boolean iIsCheckShowContact = false;

	private DisplayMetrics iDisplayMetrics = null;

	public void setDisplayMetrics(DisplayMetrics iDisplayMetrics) {
		this.iDisplayMetrics = iDisplayMetrics;
	}

	public DisplayMetrics getDisplayMetrics() {
		return iDisplayMetrics;
	}

	private static SystemConfigs iInstance = null;

	public static SystemConfigs Instance() {
		if (iInstance == null) {
			iInstance = new SystemConfigs();
		}

		return iInstance;
	}

}
