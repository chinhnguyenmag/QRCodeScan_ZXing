package com.captix.scan.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.captix.scan.R;

public class MainActivity extends Activity {
	SlidingMenu menu = null;
	Button mBtScanner;
	Button mBtHistory;
	Button mBtSetting;
	Button mBtAbout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initMenu();
	}

	public void onClick_Menu(View view) {
		try {
			if (menu == null) {
				initMenu();
			}
			menu.setMode(SlidingMenu.LEFT);
			menu.toggle();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initMenu() {
		try {
			menu = new SlidingMenu(this);
			menu.setMode(SlidingMenu.LEFT);
			menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			menu.setShadowWidthRes(R.dimen.shadow_width);
			menu.setShadowDrawable(R.anim.shadow);
			menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
			menu.setFadeDegree(0.35f);
			menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
			menu.setMenu(R.layout.activity_sliding);
			menu.setSlidingEnabled(true);
			View view = menu.getRootView();
			mBtAbout = (Button) view
					.findViewById(R.id.activity_sliding_bt_about);
			mBtHistory = (Button) view
					.findViewById(R.id.activity_sliding_bt_history);
			mBtScanner = (Button) view
					.findViewById(R.id.activity_sliding_bt_scanner);
			mBtSetting = (Button) view
					.findViewById(R.id.activity_sliding_bt_setting);

			mBtAbout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					menu.toggle();
					startActivity(new Intent(MainActivity.this,
							HistoryActivity.class));
					finish();
				}
			});

			mBtHistory.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

				}
			});

			mBtScanner.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

				}
			});

			mBtSetting.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
