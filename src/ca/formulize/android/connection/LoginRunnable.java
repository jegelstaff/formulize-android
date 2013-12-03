package ca.formulize.android.connection;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import ca.formulize.android.data.ConnectionInfo;
import ca.formulize.android.util.ConnectionUtil;

/**
 * Asynchronous routine that logs user into a Formulize account given a valid
 * {@link ConnectionInfo}. A successful login would store user session cookies
 * in {@link java.net.CookieManager}. A {@link Handler} needs to given to allow
 * the application to respond to the messages this object sends.
 * 
 * @author timch326
 */
public class LoginRunnable implements Runnable {

	// The Bundle key that this runnable uses to send messages to handlers
	public final static String EXTRA_LOGIN_RESPONSE_MSG = "ca.formulize.android.extras.LoginResponseMsg";

	private ConnectionInfo connectionInfo;
	private Handler handler;

	/*
	 * Messages to be handled by a Handler
	 */

	// User logged into their connection successfully
	public final static int LOGIN_SUCESSFUL_MSG = 0;
	// Unable to login, login credentials are probably incorrect
	public final static int LOGIN_UNSUCESSFUL_MSG = 1;
	// Bad network connection, or invalid Formulize connection
	public final static int LOGIN_ERROR_MSG = -1;

	public LoginRunnable(ConnectionInfo connectionInfo, Handler handler) {
		super();

		this.connectionInfo = connectionInfo;
		this.handler = handler;
	}

	@Override
	public void run() {
		try {

			if (isUserLoggedIn(connectionInfo)) {
				returnResponse(LOGIN_SUCESSFUL_MSG);
			} else {
				attemptLogin(connectionInfo);
				if (isUserLoggedIn(connectionInfo))
					returnResponse(LOGIN_SUCESSFUL_MSG);
				else
					returnResponse(LOGIN_UNSUCESSFUL_MSG);
			}

		} catch (MalformedURLException e) {
			returnResponse(LOGIN_ERROR_MSG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			returnResponse(LOGIN_ERROR_MSG);
		}
	}

	/**
	 * Attempts to login into a Formulize server with the given ConnectionInfo.
	 * Use {@link isUserLoggedIn()} method to check if the attempt was
	 * successful.
	 * 
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private void attemptLogin(ConnectionInfo connectionInfo)
			throws IOException, MalformedURLException {
		HttpURLConnection urlConnection;
		int responseCode;

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
	}

	/**
	 * Checks if the user currently has a session, if a session already exists,
	 * the session is renewed. If the given {@link ConnectionInfo} is not a
	 * Formulize URL, then a {@link MalformedURLException} is thrown.
	 * 
	 * @return whether a session exists or not, True if there is a session.
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private Boolean isUserLoggedIn(ConnectionInfo connectionInfo)
			throws IOException, MalformedURLException {
		HttpURLConnection urlConnection;

		// Check if the connection is a valid Formulize connection
		urlConnection = (HttpURLConnection) new URL(
				connectionInfo.getConnectionURL() + "isUserLoggedIn.php")
				.openConnection();

		InputStream in = new BufferedInputStream(urlConnection.getInputStream());
		String isUserLoggedIn = ConnectionUtil.readInputToString(
				new InputStreamReader(in)).trim();

		Log.d("Formulize", isUserLoggedIn);

		if (isUserLoggedIn.equals("1")) {
			return true;
		} else if (isUserLoggedIn.equals("0")) {
			return false;
		} else {
			throw new MalformedURLException();
		}
	}

	private void returnResponse(int result) {
		Message msgObj = handler.obtainMessage();
		Bundle b = new Bundle();
		b.putInt(EXTRA_LOGIN_RESPONSE_MSG, result);
		msgObj.setData(b);
		handler.sendMessage(msgObj);
	}
}
