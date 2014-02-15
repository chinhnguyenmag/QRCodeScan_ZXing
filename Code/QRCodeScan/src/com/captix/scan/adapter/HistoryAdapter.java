package com.captix.scan.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.captix.scan.R;
import com.captix.scan.activity.HistoryActivity;
import com.captix.scan.customview.DialogConfirm;
import com.captix.scan.customview.DialogConfirm.ProcessDialogConfirm;
import com.captix.scan.listener.GetWidthListener;
import com.captix.scan.model.HistoryItem;
import com.captix.scan.model.HistorySectionItem;
import com.captix.scan.model.Item;

public class HistoryAdapter extends ArrayAdapter<Item> {

	private Context context;
	private List<Item> items;
	private LayoutInflater vi;
	private HistoryAdapter_Process mProcess;
	private GetWidthListener mListener;
	View v;

	public HistoryAdapter(Context context, List<Item> items,
			HistoryAdapter_Process process, GetWidthListener listener) {
		super(context, 0, items);
		this.context = context;
		this.items = new ArrayList<Item>();
		this.items.addAll(items);
		this.mProcess = process;
		this.mListener = listener;
		vi = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		try {
			v = convertView;
			final ViewHolder holder;
			final Item i = items.get(position);
			if (i != null) {
				if (i.isSection()) {
					HistorySectionItem si = (HistorySectionItem) i;
					// inflate view
					v = vi.inflate(R.layout.list_item_section, null);

					v.setOnClickListener(null);
					v.setOnLongClickListener(null);
					v.setLongClickable(false);

					final TextView sectionView = (TextView) v
							.findViewById(R.id.list_item_entry_tv_header);
					sectionView.setText(si.getTitle().toUpperCase());
				} else {
					holder = new ViewHolder();
					HistoryItem ei = (HistoryItem) i;
					v = vi.inflate(R.layout.list_item_entry, null);
					holder.mBtnDelete = (ImageButton) v
							.findViewById(R.id.list_item_entry_btn_delete);
					holder.mBtnSMS = (ImageButton) v
							.findViewById(R.id.list_item_entry_btn_sms);
					holder.mBtnEmail = (ImageButton) v
							.findViewById(R.id.list_item_entry_btn_email);
					holder.mBtnTwitter = (ImageButton) v

					.findViewById(R.id.list_item_entry_btn_twitter);
					holder.mBtnFacebook = (ImageButton) v
							.findViewById(R.id.list_item_entry_btn_facebook);
					holder.mBtnEvernote = (ImageButton) v
							.findViewById(R.id.list_item_entry_btn_evernote);
					holder.mTvContent = (TextView) v
							.findViewById(R.id.list_item_entry_title);
					holder.mRlSocial = (RelativeLayout) v
							.findViewById(R.id.list_item_entry_social);

					ViewTreeObserver vto = holder.mBtnDelete
							.getViewTreeObserver();
					vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

						@SuppressLint("NewApi")
						@Override
						public void onGlobalLayout() {
							mListener.getWidthBtDelete(holder.mBtnDelete
									.getWidth());
							if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
								holder.mBtnDelete.getViewTreeObserver()
										.removeOnGlobalLayoutListener(this);
							} else {
								holder.mBtnDelete.getViewTreeObserver()
										.removeGlobalOnLayoutListener(this);
							}
						}

					});

					ViewTreeObserver vto1 = v.getViewTreeObserver();
					vto1.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

						@SuppressLint("NewApi")
						@Override
						public void onGlobalLayout() {
							mListener.getWidthTotal(v.getWidth());
							if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
								holder.mBtnDelete.getViewTreeObserver()
										.removeOnGlobalLayoutListener(this);
							} else {
								holder.mBtnDelete.getViewTreeObserver()
										.removeGlobalOnLayoutListener(this);
							}
						}

					});

					ViewTreeObserver vto3 = holder.mRlSocial
							.getViewTreeObserver();
					vto3.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

						@SuppressLint("NewApi")
						@Override
						public void onGlobalLayout() {
							mListener.getWidthSocial(holder.mRlSocial
									.getWidth());
							if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
								holder.mBtnDelete.getViewTreeObserver()
										.removeOnGlobalLayoutListener(this);
							} else {
								holder.mBtnDelete.getViewTreeObserver()
										.removeGlobalOnLayoutListener(this);
							}
						}

					});

					if (holder.mTvContent != null)
						holder.mTvContent.setText(ei.getTitle());

					// Process click events
					holder.mBtnDelete.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							DialogConfirm dialog = new DialogConfirm(
									context,
									android.R.drawable.ic_dialog_alert,
									context.getString(R.string.activity_history_delete_history_title),
									context.getString(R.string.activity_history_delete_history_confirm),
									true, new ProcessDialogConfirm() {

										@Override
										public void click_Ok() {
											int position_delete = position
													- calculateNumberSectionBefore(position);
											items.remove(position);
											if (items.size() == 1) {
												items.clear();
											}
											mProcess.delete_item(position_delete, items);
										}

										@Override
										public void click_Cancel() {

										}
									});
							dialog.show();
							
						}
					});
					holder.mBtnEvernote
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									mProcess.click_evernote(position
											- calculateNumberSectionBefore(position));

								}
							});

					holder.mBtnSMS.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							mProcess.click_sms(position
									- calculateNumberSectionBefore(position));
						}
					});

					holder.mBtnEmail.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							mProcess.click_email(position
									- calculateNumberSectionBefore(position));
						}
					});

					holder.mBtnTwitter
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									mProcess.click_twitter(position
											- calculateNumberSectionBefore(position));
								}
							});

					holder.mBtnFacebook
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									mProcess.click_facebook(position
											- calculateNumberSectionBefore(position));
								}
							});
				}
				((SwipeListView) parent).recycle(v, position);
			}
			return v;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public int calculateNumberSectionBefore(int current_position) {
		int numberSection = 0;
		for (int i = 0; i < current_position; i++) {
			if (items.get(i).isSection()) {
				numberSection++;
			}
		}
		return numberSection;
	}

	private class ViewHolder {
		RelativeLayout mRlSocial;
		ImageButton mBtnDelete;
		ImageButton mBtnSMS;
		ImageButton mBtnEmail;
		ImageButton mBtnTwitter;
		ImageButton mBtnFacebook;
		ImageButton mBtnEvernote;
		TextView mTvContent;
	}

	public abstract static class HistoryAdapter_Process {
		public abstract void delete_item(int position, List<Item> listItems);

		public abstract void click_evernote(int position);

		public abstract void click_facebook(int position);

		public abstract void click_twitter(int position);

		public abstract void click_sms(int position);

		public abstract void click_email(int position);
	}
}
