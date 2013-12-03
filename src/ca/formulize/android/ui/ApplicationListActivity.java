package ca.formulize.android.ui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import ca.formulize.android.R;
import ca.formulize.android.connection.FUserSession;
import ca.formulize.android.connection.LogoutAsyncTask;
import ca.formulize.android.data.ConnectionInfo;
import ca.formulize.android.data.FormulizeApplication;
import ca.formulize.android.util.ConnectionUtil;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Displays the available applications that a user can access from with their
 * Formulize account. It accesses app_list.php on the Formulize server to
 * retrieve the list of available applications and screens.
 * 
 * @author timch326
 * 
 */
public class ApplicationListActivity extends FragmentActivity {

	public static final String EXTRA_APPLICATIONS = "ca.formulize.android.extras.applications";

	public FormulizeApplication[] applications;
	private ArrayAdapter<FormulizeApplication> applicationListAdapter;
	private ListView applicationListView;
	private ConnectionInfo connectionInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		applicationListView = new ListView(this);
		applicationListView
				.setOnItemClickListener(new ApplicationListClickListener());

		setContentView(applicationListView);
		connectionInfo = FUserSession.getInstance().getConnectionInfo();

		// Get available applications from current user
		if (savedInstanceState != null) {
			applications = (FormulizeApplication[]) savedInstanceState
					.getParcelableArray(EXTRA_APPLICATIONS);
			loadApplicationList(applications);

		} else {
			new MenuListRequestTask(this).execute(connectionInfo);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		FUserSession.getInstance().startKeepAliveSession(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		FUserSession.getInstance().endKeepAliveSession(this);
	}

	/**
	 * Loads the list of available applications. It also if there are any
	 * applications available to the user. If there aren't any, the user would
	 * be notified through a DialogFragment and be returned to the connection
	 * activity.
	 */
	private void loadApplicationList(FormulizeApplication[] applications) {
		applicationListAdapter = new ArrayAdapter<FormulizeApplication>(this,
				android.R.layout.simple_list_item_1, applications);

		// Set ListView adapter and click listener
		applicationListView.setAdapter(applicationListAdapter);

		// If no applications were loaded, inform the user and logout
		if (applications.length <= 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_title_no_apps)
					.setMessage(R.string.dialog_message_no_apps)
					.setCancelable(false)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// Log the user out as this dialog closes
									new LogoutAsyncTask(
											ApplicationListActivity.this)
											.execute(connectionInfo);
								}
							});
			builder.show();
		} else {
			Log.d("Formulize", "Found " + applications.length + " applications");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArray(EXTRA_APPLICATIONS, applications);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.application_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			return true;
		case R.id.logout:
			new LogoutDialogFragment().show(this.getSupportFragmentManager(), "logout");;
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// Pressing back in this activities implies the user is attempting to logout
		new LogoutDialogFragment().show(this.getSupportFragmentManager(), "logout");;
	}

	private class ApplicationListClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long applicationID) {

			// Go to Application Screen List Activity with selected application
			FormulizeApplication selectedApplication = (FormulizeApplication) parent
					.getAdapter().getItem(position);
			Intent screenListIntent = new Intent(ApplicationListActivity.this,
					ScreenListActivity.class);
			screenListIntent
					.putExtra(ScreenListActivity.EXTRA_APP_ID, position);
			screenListIntent.putExtra(
					ScreenListActivity.EXTRA_SCREENS_AVAILABLE,
					selectedApplication);
			startActivity(screenListIntent);

		}
	}

	/**
	 * This AsyncTask retrieves the list of menu entries available to the user.
	 * NOTE: If the user is not logged in, menu links available to anonymous
	 * users will be retrieved. TODO: Stop the asynchronous task when the
	 * connection is cancelled (back button is pressed)
	 * 
	 * @author timch326
	 * 
	 */
	private class MenuListRequestTask extends
			AsyncTask<ConnectionInfo, String, String> {

		private ProgressDialog progressDialog;
		private FragmentActivity activity; // Application context
		private ConnectionInfo connectionInfo; // Login Info

		public MenuListRequestTask(FragmentActivity activity) {
			this.activity = activity;
			progressDialog = new ProgressDialog(activity);
		}

		protected void onPreExecute() {
			this.progressDialog.setMessage(activity
					.getString(R.string.progress_get_apps));
			progressDialog.show();
		}

		@Override
		protected String doInBackground(ConnectionInfo... info) {

			// Http Connection Variables
			HttpURLConnection urlConnection = null;
			String response = null;
			int responseCode = 0;

			connectionInfo = info[0];

			try {
				// Create connection to server and set request parameters
				urlConnection = (HttpURLConnection) new URL(
						connectionInfo.getConnectionURL() + "app_list.php")
						.openConnection();

				// Check Http Status Code
				urlConnection.connect();
				responseCode = urlConnection.getResponseCode();

				InputStream in = new BufferedInputStream(
						urlConnection.getInputStream());
				response = ConnectionUtil.readInputToString(new InputStreamReader(in));

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {

				// Print Error Response if response code is not 200
				if (responseCode != 200 && responseCode != 0) {
					InputStreamReader in = new InputStreamReader(
							urlConnection.getErrorStream());
					ConnectionUtil.readInputToString(in);
				}
				e.printStackTrace();
				return null;
			}
			return response;
		}

		protected void onPostExecute(String result) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			super.onPostExecute(result);

			// No applications found
			if (result == null) {
				Log.d("Formulize", "No Applciations!");

			}
			// Add all applications fetched from the server
			else {
				Gson gson = new GsonBuilder().setFieldNamingPolicy(
						FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
				applications = gson.fromJson(result,
						FormulizeApplication[].class);

				loadApplicationList(applications);
			}
		}
	}
}
