package ca.formulize.android.connection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import ca.formulize.android.data.ConnectionInfo;

/**
 * An asynchronous routine that logs into a Formulize server given the
 * connection info and a callback function.
 * 
 * isUserLoggedIn.php is used to validate whether a connection is a valid Formulize connection. 
 * If the server does not respond to this link with a "1" or "0" then it is considered invalid.
 * Once it is considered valid, it would attempt to create a user session through user.php, 
 * if a user session is established, isUserLoggedIn.php returns a "1" when accessed.
 * 
 * @author timch326
 * 
 */
public class LoginRunnable implements Runnable {

	// The Bundle key that this runnable uses to send messages to handlers
	public final static String EXTRA_LOGIN_RESPONSE_MSG = "ca.formulize.android.extras.LoginResponseMsg";
	
	// Messages sent by this runnable
	public final static int LOGIN_SUCESSFUL_MSG = 0;	// User logged into their connection successfully
	public final static int LOGIN_UNSUCESSFUL_MSG = -2; // Unable to login, login credentials are probably incorrect
	public final static int LOGIN_ERROR_MSG = -1;		// Bad network connection, or invalid Formulize connection

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

			// Check if the connection is a valid Formulize connection
			urlConnection = (HttpURLConnection) new URL(
					connectionInfo.getConnectionURL() + "isUserLoggedIn.php")
					.openConnection();

			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			String isUserLoggedIn = readInputToString(new InputStreamReader(in)).trim();
			if (!isUserLoggedIn.equals("1") && !isUserLoggedIn.equals("0") ) {
				Log.d("Formulize", isUserLoggedIn);
				throw new MalformedURLException();
			}

			// Create connection to server and set request parameters
			urlConnection = (HttpURLConnection) new URL(
					connectionInfo.getConnectionURL() + "user.php")
					.openConnection();
			urlConnection.setDoOutput(true); // Triggers POST
			urlConnection.setInstanceFollowRedirects(false);

			String query = String.format("op=%s&pass=%s&uname=%s",
					URLEncoder.encode("login", "UTF-8"),
					URLEncoder.encode(connectionInfo.getPassword(), "UTF-8"),
					URLEncoder.encode(connectionInfo.getUsername(), "UTF-8"));

			OutputStream output = urlConnection.getOutputStream();
			output.write(query.getBytes("UTF-8"));

			responseCode = urlConnection.getResponseCode();
			if (responseCode != 200 && responseCode != 302) {
				throw new MalformedURLException();
			}

			/*
			 * Check if the user is successfully logged in. isUserLoggedIn.php
			 * indicates the user has a session with the session by return a "1"
			 * otherwise it should return a "0".
			 */
			urlConnection = (HttpURLConnection) new URL(
					connectionInfo.getConnectionURL() + "isUserLoggedIn.php")
					.openConnection();

			in = new BufferedInputStream(
					urlConnection.getInputStream());
			isUserLoggedIn = readInputToString(new InputStreamReader(in)).trim();
			if (isUserLoggedIn.equals("1")) {
				response = LOGIN_SUCESSFUL_MSG;
			} else if (isUserLoggedIn.equals("0")) {
				response = LOGIN_UNSUCESSFUL_MSG;
			} else {
				response = LOGIN_ERROR_MSG;
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
