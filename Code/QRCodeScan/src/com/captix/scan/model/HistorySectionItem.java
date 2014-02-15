package com.captix.scan.model;

public class HistorySectionItem implements Item{

	private final String title;
	
	public HistorySectionItem(String title) {
		this.title = title;
	}
	
	public String getTitle(){
		return title;
	}
	
	@Override
	public boolean isSection() {
		return true;
	}

}
