package ca.formulize.android.connection;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import ca.formulize.android.R;
import ca.formulize.android.data.ConnectionInfo;
import ca.formulize.android.util.ConnectionUtil;

/**
 * This AsyncTask logs the user out of their Formulize account if they are
 * currently logged in. It is assumed that when a logout is successful, two
 * cookies: "autologin_uname" and "autologin_pass", would be in the Set-Cookie
 * header in the response.
 * 
 * A successful or failed logout is currently handled the same way - the
 * application returns to the Connection List.
 * 
 * @author timch326
 * 
 */
public class LogoutAsyncTask extends
		AsyncTask<ConnectionInfo, Integer, Integer> {

	public int LOGOUT_SUCCESSFUL_MSG = 0;
	public int LOGOUT_FAILED_MSG = -1;

	private ProgressDialog progressDialog;
	private FragmentActivity activity; // Application context
	private ConnectionInfo connectionInfo; // Login Info

	public LogoutAsyncTask(FragmentActivity activity) {
		this.activity = activity;
		progressDialog = new ProgressDialog(activity);
	}

	protected void onPreExecute() {
		this.progressDialog.setMessage(activity.getString(R.string.progress_logout));
		progressDialog.show();
	}

	@Override
	protected Integer doInBackground(ConnectionInfo... info) {

		HttpURLConnection urlConnection = null;
		int response = LOGOUT_FAILED_MSG;
		int responseCode = 0;
		connectionInfo = info[0];

		try {
			// Create connection to server and set request parameters
			urlConnection = (HttpURLConnection) new URL(
					connectionInfo.getConnectionURL() + "user.php"
							+ "?op=logout").openConnection();
			urlConnection.setReadTimeout(10000);
			urlConnection.setConnectTimeout(15000);
			urlConnection.setInstanceFollowRedirects(false);

			Log.d("Formulize", connectionInfo.getConnectionURL());

			// Check Http Status Code
			urlConnection.connect();
			responseCode = urlConnection.getResponseCode();

			// Check For Cookies
			List<String> cookies = urlConnection.getHeaderFields().get(
					"Set-Cookie");
			Log.d("Formulize", "Cookies:" + cookies.toString());

			// If there are 2 or more cookies received, login was successful
			if (cookies.size() >= 2) {
				response = LOGOUT_SUCCESSFUL_MSG;
			} else {
				response = LOGOUT_FAILED_MSG;
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return LOGOUT_FAILED_MSG;
		} catch (IOException e) {

			// Print Error Response if response code is not 200
			if (responseCode != 200 && responseCode != 0) {
				InputStreamReader in = new InputStreamReader(
						urlConnection.getErrorStream());
				ConnectionUtil.readInputToString(in);

			}
			e.printStackTrace();
			return LOGOUT_FAILED_MSG;
		}

		return response;
	}

	protected void onPostExecute(Integer result) {
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		super.onPostExecute(result);
		
		FUserSession.getInstance().endKeepAliveSession(activity);
		Intent connectionIntent = new Intent(activity, ConnectionActivity.class);
		activity.startActivity(connectionIntent);
	}

}
