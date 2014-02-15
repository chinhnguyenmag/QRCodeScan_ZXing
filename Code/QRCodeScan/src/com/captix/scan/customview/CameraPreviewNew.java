/**
 * @author baonguyen
 */
package com.captix.scan.customview;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** A basic Camera preview class */
public class CameraPreviewNew extends SurfaceView implements
		SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private SurfaceView mSurfaceView;
	private Camera mCamera;
	private Size mPreviewSize;
	private List<Size> mSupportedPreviewSizes;
	private PreviewCallback previewCallback;
	private AutoFocusCallback autoFocusCallback;
	private Activity mActivity;

	public CameraPreviewNew(Context context, PreviewCallback previewCb,
			AutoFocusCallback autoFocusCb) {
		super(context);
		mActivity = (Activity) context;
		previewCallback = previewCb;
		autoFocusCallback = autoFocusCb;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);

		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			requestLayout();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (Exception e) {
			Log.d("DBG", "Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Camera preview released in activity
		stop();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		/*
		 * If your preview can change or rotate, take care of those events here.
		 * Make sure to stop the preview before resizing or reformatting it.
		 */
		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		try {
			// Now that the size is known, set up the camera parameters and
			// begin the preview.
			Camera.Parameters parameters = mCamera.getParameters();
			if (mPreviewSize != null) {
				parameters.setPreviewSize(mPreviewSize.width,
						mPreviewSize.height);
			}
			if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				mCamera.setDisplayOrientation(90);
			}
			if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				// Change orientation when rotating camera
				int angle;// This is camera orientation
				Display display = mActivity.getWindowManager()
						.getDefaultDisplay();
				switch (display.getRotation()) {// This is display orientation
				case Surface.ROTATION_0:
					// for Tablet
					angle = 0;
					break;
				case Surface.ROTATION_90:
					// for Phone
					angle = 0;
					break;
				case Surface.ROTATION_180:
					// for Tablet
					angle = 180;
					break;
				case Surface.ROTATION_270:
					// for Phone
					angle = 180;
					break;
				default:
					angle = 90;
					break;
				}
				mCamera.setDisplayOrientation(angle);
			}
			mCamera.setParameters(parameters);
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(previewCallback);
			mCamera.startPreview();
			mCamera.autoFocus(autoFocusCallback);
		} catch (Exception e) {
			Log.d("DBG", "Error starting camera preview: " + e.getMessage());
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// We purposely disregard child measurements because act as a wrapper to
		// a SurfaceView that
		// centers the camera preview instead of stretching it.
		final int width = resolveSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		setMeasuredDimension(width, height);

		if (mSupportedPreviewSizes == null && mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters()
					.getSupportedPreviewSizes();
		}
		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width,
					height);
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void stop() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}
}
