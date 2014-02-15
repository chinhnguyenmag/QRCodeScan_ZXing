package com.captix.scan.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.captix.scan.utils.Utils;

/**
 * 
 * @author ChinhNguyen
 * 
 */
public class Server {
	public static int mStatus = 200;

	/**
	 * Download data form url using GET method
	 * 
	 * @param url
	 * @param string
	 * @return
	 */
	public static InputStream requestPost(String url, StringEntity stringEntity) {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();

		HttpPost httppost = new HttpPost(url);

		try {
			httppost.setEntity(stringEntity);
			httppost.setHeader("Content-type", "application/json");
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
			HttpConnectionParams.setSoTimeout(httpParameters, 10000);

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			return response.getEntity().getContent();

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Download data form url using GET method
	 * 
	 * @param urlString
	 * @return
	 * @throws IOException
	 */
	public static InputStream requestGet(String urlString) throws IOException {
		HttpURLConnection conn = buildHttpUrlConnection(urlString);
		conn.connect();
		InputStream stream;
		if (conn.getResponseCode() != 200) {
			mStatus = conn.getResponseCode();
			stream = conn.getErrorStream();
		} else {
			mStatus = 200;
			stream = conn.getInputStream();
		}

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
		if (!Utils.isFroyoOrHigher()) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	public static String inputStreamToString(InputStream is) {
		try {
			String line = "";
			StringBuilder total = new StringBuilder();

			// Wrap a BufferedReader around the InputStream
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					"UTF8"));
			// BufferedReader rd = new BufferedReader(new InputStreamReader(is,
			// "uft-8"));
			// Read response until the end

			while ((line = rd.readLine()) != null) {
				total.append(line);
			}
			try {
				return URLDecoder.decode(total.toString(), "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return total.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";

	}
}
