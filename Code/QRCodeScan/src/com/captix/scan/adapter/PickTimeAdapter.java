package com.captix.scan.adapter;

import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.content.Context;

import com.captix.scan.R;

/**
 * @author Hung Hoang
 * 
 */
public class PickTimeAdapter extends NumericWheelAdapter {
	// Items step value
	private int mStep;

	/**
	 * Constructor
	 */
	public PickTimeAdapter(Context context, int maxValue, int step) {
		super(context, 0, maxValue / step);
		this.mStep = step;
		setItemResource(R.layout.wheel_text_item);
		setItemTextResource(R.id.text);
	}

	@Override
	public CharSequence getItemText(int index) {
		if (index >= 0 && index < getItemsCount()) {
			int value = 5 + index * mStep;
			if (value == 95) {
				return "Never";
			}
			return Integer.toString(value) + "s";
		}
		return null;
	}

	public CharSequence getValueItem(int index) {
		return getItemText(index);
	}
}
