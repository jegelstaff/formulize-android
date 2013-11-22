package ca.formulize.android.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import ca.formulize.android.data.ConnectionInfo;

/**
 * An asynchronous routine that logs into a Formulize server given the
 * connection info and a callback function.
 * 
 * @author timch326
 * 
 */
public class LoginRunnable implements Runnable {
	
	public final static String EXTRA_LOGIN_RESPONSE_MSG = "ca.formulize.android.connection.ConnectionActivity.extraLoginResponseMsg";
	public final static int LOGIN_SUCESSFUL_MSG = 0;
	public final static int LOGIN_UNSUCESSFUL_MSG = -2;
	public final static int LOGIN_ERROR_MSG = -1;

	private ConnectionInfo connectionInfo;
	private Handler handler;

	public LoginRunnable(ConnectionInfo connectionInfo, Handler handler) {
		super();

		this.connectionInfo = connectionInfo;
		this.handler = handler;
	}

	@Override
	public void run() {
		HttpURLConnection urlConnection = null;
		int response = -1;
		int responseCode = 0;

		try {

			// Set up cookie manager
			CookieHandler.setDefault(new CookieManager(null,
					CookiePolicy.ACCEPT_ALL));

			// Create connection to server and set request parameters
			urlConnection = (HttpURLConnection) new URL(
					connectionInfo.getConnectionURL() + "user.php")
					.openConnection();
			urlConnection.setReadTimeout(10000);
			urlConnection.setConnectTimeout(15000);
			urlConnection.setDoOutput(true); // Triggers POST
			urlConnection.setInstanceFollowRedirects(false);

			// Enter Post Parameters
			String query = String.format("op=%s&pass=%s&uname=%s",
					URLEncoder.encode("login", "UTF-8"),
					URLEncoder.encode(connectionInfo.getPassword(), "UTF-8"),
					URLEncoder.encode(connectionInfo.getUsername(), "UTF-8"));

			Log.d("Formulize", query);

			OutputStream output = urlConnection.getOutputStream();
			output.write(query.getBytes("UTF-8"));

			Log.d("Formulize", connectionInfo.getConnectionURL());

			// Check Http Status Code
			urlConnection.connect();
			responseCode = urlConnection.getResponseCode();

			// Check For Cookies
			List<String> cookies = urlConnection.getHeaderFields().get(
					"Set-Cookie");
			Log.d("Formulize", cookies.toString());

			// If there are 2 or more cookies received, login was successful
			if (cookies.size() >= 2) {
				response = LOGIN_SUCESSFUL_MSG;
			} else {
				response = LOGIN_UNSUCESSFUL_MSG;
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
			returnResponse(LOGIN_ERROR_MSG);
		} catch (IOException e) {

			// Print Error Response if response code is not 200
			if (responseCode != 200 && responseCode != 0) {
				InputStreamReader in = new InputStreamReader(
						urlConnection.getErrorStream());
				readInputToString(in);

			}
			e.printStackTrace();
			returnResponse(LOGIN_ERROR_MSG);
		}

		returnResponse(response);
	}

	private void returnResponse(int result) {
		Message msgObj = handler.obtainMessage();
		Bundle b = new Bundle();
		b.putInt(EXTRA_LOGIN_RESPONSE_MSG, result);
		msgObj.setData(b);
		handler.sendMessage(msgObj);
	}

	/**
	 * Helper function to convert an entire input stream into a String
	 * 
	 * @param in
	 * @return String representation of the input stream
	 */
	private String readInputToString(InputStreamReader in) {
		BufferedReader reader = new BufferedReader(in);
		StringBuilder stringBuilder = new StringBuilder();

		try {
			// Read server response
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stringBuilder.toString();

	}
}
