package com.captix.scan.database;

/**
 * This class contains the constant to use for mapping
 * 
 * @author Chinhnd
 */
public class DatabaseTable {
	public static String DATABASE_PATH = "/data/data/com.captix.scan/databases/";
	/** declare database name */
	public final static String DATABASE_NAME = "QRCodeScanDb.sqlite";
	/** db version */
	public static final int DB_VERSION = 1;
	/** declare Item table name */

	// Table Category
	public final static String DATABASE_CATEGORY_TABLE = "CategoryDetail";
	public final static String CATEGORY_ID = "id";
	public final static String CATEGORY_TITLE = "title";
	public final static String CATEGORY_LINK_IMAGE = "linkImage";
	public final static String CATEGORY_DESCRIPTION = "description";
	public final static String CATEGORY_ISVIDEO = "isVideo";

	// Table Payment
	public final static String DATABASE_PAYMENT_TABLE = "Payment";
	public final static String PAYMENT_ID = "id";
	public final static String PAYMENT_REGISTER = "isRegister";
	public final static String PAYMENT_COIN = "coins";
	public final static String PAYMENT_EXPIRATION = "expirationDate";
}
