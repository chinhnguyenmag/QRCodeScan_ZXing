package com.captix.scan.model;

public class QRCode {
	
	//private variables
	int _id;
	String _date;
	String _url;
	
	// Empty constructor
	public QRCode(){
		
	}
	// constructor
	public QRCode(int id, String date, String url){
		this._id = id;
		this._date = date;
		this._url = url;
	}
	
	// constructor
	public QRCode(String date, String url){
		this._date = date;
		this._url = url;
	}
	// getting ID
	public int getID(){
		return this._id;
	}
	
	// setting id
	public void setID(int id){
		this._id = id;
	}
	
	// getting name
	public String getDate(){
		return this._date;
	}
	
	// setting name
	public void setDate(String date){
		this._date = date;
	}
	
	// getting phone number
	public String getUrl(){
		return this._url;
	}
	
	// setting phone number
	public void setUrl(String url){
		this._url = url;
	}
}
