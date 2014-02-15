package com.captix.scan.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;

public class Utils {
	public static String TAG = "Utils";
	public static final int DEFAULT_BUFFER_SIZE = 8192;
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String ZIPCODE_PATTERN = "^\\d{5}?$";
	private static final String USERNAME_PATTERN = "[^\\s]{3,30}";
	private static final String PASSWORD_PATTERN = "[^\\s]{5,50}";
	private static final String NAME_PATTERN = "[^$^()?*+{.\\]\\[\\s]{1,30}";
	private static Pattern pattern;
	private static Matcher matcher;

	public static boolean isJellyBeanOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

	public static boolean isICSOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	public static boolean isHoneycombOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static boolean isGingerbreadOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	public static boolean isFroyoOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	/**
	 * Check SdCard
	 * 
	 * @return
	 */
	public static boolean isExtStorageAvailable() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	/**
	 * Check internet
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			return activeNetworkInfo.isConnected();
		}
		return false;
	}

	/**
	 * Check wifi
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo != null) {
			return wifiNetworkInfo.isConnected();
		}
		return false;
	}

	/**
	 * Check on/off gps
	 * 
	 * @return
	 */
	public static boolean checkAvailableGps(Context context) {
		LocationManager manager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	/**
	 * Download data url
	 * 
	 * @param urlString
	 * @return
	 * @throws IOException
	 */
	public static InputStream downloadUrl(String urlString) throws IOException {
		HttpURLConnection conn = buildHttpUrlConnection(urlString);
		conn.connect();

		InputStream stream = conn.getInputStream();
		return stream;
	}

	/**
	 * Returns an {@link HttpURLConnection} using sensible default settings for
	 * mobile and taking care of buggy behavior prior to Froyo.
	 */
	public static HttpURLConnection buildHttpUrlConnection(String urlString)
			throws MalformedURLException, IOException {
		Utils.disableConnectionReuseIfNecessary();

		URL url = new URL(urlString);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setDoInput(true);
		conn.setRequestMethod("GET");
		return conn;
	}

	/**
	 * Prior to Android 2.2 (Froyo), {@link HttpURLConnection} had some
	 * frustrating bugs. In particular, calling close() on a readable
	 * InputStream could poison the connection pool. Work around this by
	 * disabling connection pooling.
	 */
	public static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (!isFroyoOrHigher()) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	/**
	 * Exchange rating from integer number(0 to 12) of SeekBar value to suitable
	 * value on Server (13 to 1)
	 * 
	 * @param value
	 *            Rating value on SeekBar
	 * @return
	 */
	public static int exchangeRatePost(int value) {
		int valuePost = 0;
		switch (value) {
		case 0:
			valuePost = 13;
			break;
		case 1:
			valuePost = 12;
			break;
		case 2:
			valuePost = 11;
			break;
		case 3:
			valuePost = 10;
			break;
		case 4:
			valuePost = 9;
			break;
		case 5:
			valuePost = 8;
			break;
		case 6:
			valuePost = 7;
			break;
		case 7:
			valuePost = 6;
			break;
		case 8:
			valuePost = 5;
			break;
		case 9:
			valuePost = 4;
			break;
		case 10:
			valuePost = 3;
			break;
		case 11:
			valuePost = 2;
			break;
		case 12:
			valuePost = 1;
			break;
		default:
			break;
		}
		return valuePost;
	}

	/**
	 * Exchange rating from double number(1.00 to 13.00) to character
	 * (A+,A,A-,B+,B,B-,C+,C,C-,D+,D,D-,F)
	 * 
	 * @param value
	 * @return
	 */
	public static String exchangeRateGetExtra(float value) {
		String valueString = "";
		if ((value <= 1.5) && (value > 0))
			valueString = "A+";
		else if (value <= 2.5)
			valueString = "A";
		else if (value <= 3.5)
			valueString = "A-";
		else if (value <= 4.5)
			valueString = "B+";
		else if (value <= 5.5)
			valueString = "B";
		else if (value <= 6.5)
			valueString = "B-";
		else if (value <= 7.5)
			valueString = "C+";
		else if (value <= 8.5)
			valueString = "C";
		else if (value <= 9.5)
			valueString = "C-";
		else if (value <= 10.5)
			valueString = "D+";
		else if (value <= 11.5)
			valueString = "D";
		else if (value <= 12.5)
			valueString = "D-";
		else
			valueString = "F";

		return valueString;
	}

	/**
	 * 
	 * @param rating
	 *            ranging from 1 to 13
	 * @return number for showing on SeekBar from 0 to 12
	 */
	public static int exchangeRatingForSeekBar(int rating) {
		int values = 0;
		switch (rating) {
		case 1:
			values = 12;
			break;
		case 2:
			values = 11;
			break;
		case 3:
			values = 10;
			break;
		case 4:
			values = 9;
			break;
		case 5:
			values = 8;
			break;
		case 6:
			values = 7;
			break;
		case 7:
			values = 6;
			break;
		case 8:
			values = 5;
			break;
		case 9:
			values = 4;
			break;
		case 10:
			values = 3;
			break;
		case 11:
			values = 2;
			break;
		case 12:
			values = 1;
			break;
		case 13:
			values = 0;
			break;
		default:
			break;
		}
		return values;
	}

	/**
	 * @author Vu le
	 * @param decimal
	 *            is number you want to convert
	 * @param value
	 *            is number decimal after "." Fx: ' "0.00" ' -> if you input
	 *            1,it will show 1.00
	 * @return number formated
	 */
	public static String formatNumber(float decimal, String value) {
		String format = "";
		try {
			DecimalFormat df = new DecimalFormat(value);
			format = df.format(decimal);
		} catch (Exception e) {
		}
		return format.replace(",", ".");
	}

	public static String formatDecimal(Float decimal, String value) {
		String format = "";
		try {
			DecimalFormat df = new DecimalFormat(value);
			format = df.format(decimal);
			float number = Float.parseFloat(format) * 100;
			if ((number % 100) == 0) {
				format = (int) Float.parseFloat(format) + "";
			}

		} catch (Exception e) {
		}
		return format;
	}

	public static int formatPointNumbers(Float decimal) {
		int number = decimal.intValue();
		return number;
	}

	/**
	 * This function use to format time by format hh:mm am/pm
	 * 
	 * @param time
	 * @return string time formated
	 */
	public static String formatTime(String time) {
		StringTokenizer st2 = new StringTokenizer(time, ".");
		StringBuilder sb = new StringBuilder();
		int hh = 0, mm = 0, ss = 0;
		while (st2.hasMoreElements()) {
			hh = Integer.parseInt(st2.nextElement().toString());
			mm = Integer.parseInt(st2.nextElement().toString());
			ss = Integer.parseInt(st2.nextElement().toString());
			sb.append(hh);
			sb.append(":" + mm);
			sb.append(":" + ss);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
		Time mTime = new Time(hh, mm, ss);

		return sdf.format(mTime);
	}

	/**
	 * Check an email is valid or not
	 * 
	 * @param email
	 *            the email need to check
	 * @return {@code true} if valid, {@code false} if invalid
	 */
	public static boolean isValidEmail(String email) {
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(email);
		return matcher.matches();
	}

	/**
	 * Validate username with regular expression
	 * 
	 * @param username
	 *            username for validation
	 * @return true valid username, false invalid username
	 */
	public static boolean isValidUsername(final String username) {
		pattern = Pattern.compile(USERNAME_PATTERN);
		matcher = pattern.matcher(username);
		return matcher.matches();
	}

	/**
	 * Validate zipcode with regular expression
	 * 
	 * @param username
	 *            zipcode for validation
	 * @return true valid zipcode, false invalid zipcode
	 */
	public static boolean isValidZip(final String zip) {
		pattern = Pattern.compile(ZIPCODE_PATTERN);
		matcher = pattern.matcher(zip);
		return matcher.matches();
	}

	/**
	 * Validate password with regular expression
	 * 
	 * @param password
	 *            for validation
	 * @return true valid password, false invalid password
	 */
	public static boolean isValidPassword(final String password) {
		pattern = Pattern.compile(PASSWORD_PATTERN);
		matcher = pattern.matcher(password);
		return matcher.matches();
	}

	public static boolean isValidName(final String name) {
		pattern = Pattern.compile(NAME_PATTERN);
		matcher = pattern.matcher(name);
		return matcher.matches();
	}

	public static String exTime(int hours) {
		if (hours >= 0 && hours < 12) {
			return "am";
		} else {
			return "pm";
		}
	}

	/**
	 * 
	 * @param list
	 *            of String
	 * @return String of all list item cooperated
	 */
	public static String listStringToString(List<String> list) {
		String result = "";
		for (int i = 0; i < list.size(); i++) {
			if (i == list.size() - 1)
				result += list.get(i);
			else
				result += list.get(i) + ",";
		}
		return result;

	}

	/**
	 * 
	 * @param list
	 *            of Integer
	 * @return String of all list item cooperated
	 */
	public static String listIntToString(List<Integer> list) {
		String result = "";
		for (int i = 0; i < list.size(); i++) {
			if (i == list.size() - 1)
				result += list.get(i);
			else
				result += list.get(i) + ",";
		}
		return result;

	}

	public static String getDateFormat() {
		DateFormat df = DateFormat
				.getDateInstance(DateFormat.MEDIUM, Locale.US);
		String formattedDate = df.format(new Date());
		return formattedDate;
	}

	/**
	 * @author baonguyen
	 * @param s
	 *            : Input string
	 * @return the list of string elements
	 */
	public static List<String> parseStringIntoList(String s) {
		List<String> list = new ArrayList<String>();
		if (!s.equalsIgnoreCase("")) {
			String[] s1 = s.split(",");
			for (int i = 0; i < s1.length; i++) {
				list.add(s1[i]);
			}
		}
		return list;
	}

	/**
	 * @author baonguyen
	 * @param mListStrings
	 * @return the list of Integer elements
	 */
	public static List<Integer> parseListStringIntoListInteger(
			List<String> mListStrings) {
		List<Integer> mListInteger = new ArrayList<Integer>();
		for (String s : mListStrings) {
			mListInteger.add(Integer.parseInt(s));
		}
		return mListInteger;
	}

	/* COMING SOON DIALOG */
	/*
	 * public static void showComingSoonDialog(Context context) {
	 * AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
	 * context);
	 * 
	 * // set title
	 * alertDialogBuilder.setTitle(context.getResources().getString(
	 * R.string.comming_soon));
	 * 
	 * // set dialog message alertDialogBuilder .setMessage(
	 * context.getResources().getString(R.string.common_error))
	 * .setCancelable(false) .setPositiveButton("Ok", new
	 * DialogInterface.OnClickListener() { public void onClick(DialogInterface
	 * dialog, int id) { dialog.cancel(); } }); // create alert dialog
	 * AlertDialog alertDialog = alertDialogBuilder.create();
	 * 
	 * // show it alertDialog.show(); }
	 */
	public static String formatPhoneNumber(String number) {
		number = number.substring(number.length() - 10);
		return String.valueOf(number).replaceFirst("(\\d{3})(\\d{3})(\\d+)",
				"($1) $2-$3");
	}

	/**
	 * Generate the UUID
	 * 
	 * @return the UUID
	 */
	public static String genUUID() {
		String result = null;
		try {
			UUID mUUID = UUID.randomUUID();
			result = new String(mUUID.toString());
			result = result.replaceAll("-", "");
			mUUID = null;
		} catch (Exception e) {
			e.printStackTrace();
		}// end catch
		return result;
	}

	public static String checkFormatPhoneNumber(String phoneNumber) {
		String result = "";
		try {
			for (int i = 0; i < result.length(); i++) {

			}
		} catch (Exception e) {
			e.printStackTrace();
		}// end catch
		return result;
	}

	public static float formatPriceOrder(float decimal) {
		String format = "";
		try {
			DecimalFormat df = new DecimalFormat("0.00");
			format = df.format(decimal);
		} catch (Exception e) {
		}
		return Float.parseFloat(format.replace(",", "."));
	}

	public static String removeSpecial(String s) {
		return s.replaceAll("[\\]\\[\\s$^()?*+{.]", "");
	}

	/**
	 * 
	 * @param date_time
	 *            Ex: "11.21.2013 13:30:21"
	 * @return "01:30:21 PM"
	 */
	public static String parseTime(String date_time) {
		String time = date_time.substring(11, 19);
		int hour = Integer.parseInt(time.substring(0, 2));
		if (hour > 12) {
			String time1 = "0" + (hour - 12)
					+ time.substring(2, time.length() - 3) + " PM";
			return time1;
		} else {
			String time2 = time.substring(0, time.length() - 3) + " AM";
			return time2;
		}

	}

	public static int parseIndex(int value, int step) {
		if (value == -1) {
			return 19;
		}
		return (value / step)-1;
	}

	/**
	 * Gets html content from the assets folder.
	 */
	public static String getHtmlFromAsset(Context context, int html_file) {
		InputStream is;
		StringBuilder builder = new StringBuilder();
		String htmlString = "";
		try {
			is = context.getAssets().open(context.getString(html_file));
			if (is != null) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				htmlString = builder.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return htmlString;
	}

}