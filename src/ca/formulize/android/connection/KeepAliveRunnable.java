package ca.formulize.android.connection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Handler;
import android.util.Log;
import ca.formulize.android.data.ConnectionInfo;


public class KeepAliveRunnable implements Runnable
{
	private ConnectionInfo connectionInfo;
	private Handler handler;
	
	private int count = 0;

	public KeepAliveRunnable(ConnectionInfo connectionInfo, Handler handler) {
		super();

		this.connectionInfo = connectionInfo;
		this.handler = handler;
	}
	
	@Override
	public void run() {
		HttpURLConnection urlConnection = null;

		try {
			// Check if the connection is a valid Formulize connection
			urlConnection = (HttpURLConnection) new URL(
					connectionInfo.getConnectionURL() + "isUserLoggedIn.php")
					.openConnection();

			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			String isUserLoggedIn = readInputToString(new InputStreamReader(in)).trim();
			if (!isUserLoggedIn.equals("1") && !isUserLoggedIn.equals("0") ) {
				throw new MalformedURLException();
			}
			Log.d("Formulize", isUserLoggedIn);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		Log.d("Formulize", "Keep Alive: " + count++);	
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
