package ca.formulize.android.menu;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import ca.formulize.android.R;
import ca.formulize.android.connection.FUserSession;
import ca.formulize.android.data.ConnectionInfo;
import ca.formulize.android.data.FormulizeApplication;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApplicationListActivity extends FragmentActivity {

	public static final String APPLICATIONS = "ca.formulize.android.menu.ApplicationListActivity.applications";

	// Temporary hard coded variables for prototyping
	public static final String TEST_JSON = "[{\"appid\":\"1\",\"name\":\"Application1\",\"description\":false,\"links\":[{\"menu_id\":\"3\",\"appid\":\"1\",\"screen\":\"fid=2\",\"rank\":\"1\",\"url\":\"\",\"link_text\":\"testFormLink\",\"name\":null,\"text\":\"testFormLink\"},{\"menu_id\":\"4\",\"appid\":\"1\",\"screen\":\"sid=1\",\"rank\":\"2\",\"url\":\"\",\"link_text\":\"Fruity Form\",\"name\":null,\"text\":\"Fruity Form\"},{\"menu_id\":\"5\",\"appid\":\"1\",\"screen\":\"sid=5\",\"rank\":\"4\",\"url\":\"\",\"link_text\":\"Mobile Form\",\"name\":null,\"text\":\"Mobile Form\"}]},{\"appid\":\"2\",\"name\":\"The Second Application\",\"description\":false,\"links\":[{\"menu_id\":\"9\",\"appid\":\"2\",\"screen\":\"sid=7\",\"rank\":\"1\",\"url\":\"\",\"link_text\":\"For Students\",\"name\":null,\"text\":\"For Students\"},{\"menu_id\":\"11\",\"appid\":\"2\",\"screen\":\"sid=9\",\"rank\":\"3\",\"url\":\"\",\"link_text\":\"Anyone can see this form\",\"name\":null,\"text\":\"Anyone can see this form\"}]}]";
	public static final String TEST_LINK = "ca.formulize.android.menu.testLink";

	public FormulizeApplication[] applications;
	private ArrayAdapter<FormulizeApplication> applicationListAdapter;
	private ListView applicationListView;
	private ConnectionInfo connectionInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_application_list);

		// Get available applications from current user
		connectionInfo = FUserSession.getInstance().getConnectionInfo();
		new MenuListRequestTask(this).execute(connectionInfo);
		
		
//		applicationListAdapter = new ArrayAdapter<FormulizeApplication>(this,
//				android.R.layout.simple_list_item_1, new FormulizeApplication[1]);
//
//		// Set ListView adapter and click listener
//		applicationListView = (ListView) findViewById(R.id.applicationList);
//		applicationListView.setAdapter(applicationListAdapter);
//		applicationListView
//				.setOnItemClickListener(new ApplicationListClickListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.application_list, menu);
		return true;
	}
	
	private class ApplicationListClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long applicationID) {

			// Go to Application Screen List Activity with selected application
			FormulizeApplication selectedApplication = (FormulizeApplication) parent
					.getAdapter().getItem(position);
			Intent screenListIntent = new Intent(ApplicationListActivity.this,
					ScreenListActivity.class);
			screenListIntent.putExtra(ScreenListActivity.CURRENT_APPLICATION, selectedApplication);
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
			this.progressDialog.setMessage("Getting Available Forms");
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
				response = readInputToString(new InputStreamReader(in));

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {

				// Print Error Response if response code is not 200
				if (responseCode != 200 && responseCode != 0) {
					InputStreamReader in = new InputStreamReader(
							urlConnection.getErrorStream());
					readInputToString(in);
				}
				e.printStackTrace();
				return null;
			}
			return response;
		}

		protected void onPostExecute(String result) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
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
				applications = gson.fromJson(result, FormulizeApplication[].class);
				
				applicationListAdapter = new ArrayAdapter<FormulizeApplication>(activity,
						android.R.layout.simple_list_item_1, applications);

				// Set ListView adapter and click listener
				applicationListView = (ListView) findViewById(R.id.applicationList);
				applicationListView.setAdapter(applicationListAdapter);
				applicationListView
						.setOnItemClickListener(new ApplicationListClickListener());
			}
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
}
