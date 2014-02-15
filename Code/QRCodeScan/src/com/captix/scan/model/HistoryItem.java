package com.captix.scan.model;


public class HistoryItem implements Item{

	private String mTitle;

	public HistoryItem(String title) {
		this.mTitle = title;
	}
	
	
	public String getTitle() {
		return mTitle;
	}


	public void setTitle(String title) {
		this.mTitle = title;
	}


	@Override
	public boolean isSection() {
		return false;
	}

}
