package ca.formulize.android.connection;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ca.formulize.android.R;
import ca.formulize.android.data.ConnectionInfo;
import ca.formulize.android.data.FormulizeDBHelper;

/**
 * Represents the screen that allows users to create or edit connections to a
 * Formulize server. Connections are saved once they are submitted and
 * validated.
 * 
 * If no parameters are passed through the intent, the activity will assume a
 * connection is to be added by the user.
 * 
 * By passing an id of a connection info through the intent to this activity,
 * the activity will retrieve the contents of the connection with that id, and
 * allow the user to modify and update it.
 * 
 * @author timch326
 * 
 */
public class AddConnectionActivity extends FragmentActivity {

	// Extra parameter to allow connections to be edited
	public static final String EXTRA_CONNECTION_ID = "ca.formulize.android.extras.connectionID";

	// Values for connection information
	private String connectionURL;
	private String connectionName;
	private String username;
	private String password;
	private long selectedConnectionID;

	// UI References
	private EditText connectionURLView;
	private EditText connectionNameView;
	private CheckBox saveLoginCredentialsView;
	private TextView loginDetails;
	private EditText usernameView;
	private EditText passwordView;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_connection);
		// Show the Up button in the action bar.
		setupActionBar();

		// Set up connection form
		connectionURLView = (EditText) findViewById(R.id.connection_url);
		connectionNameView = (EditText) findViewById(R.id.connection_name);
		saveLoginCredentialsView = (CheckBox) findViewById(R.id.save_login_credentials_box);
		loginDetails = (TextView) findViewById(R.id.login_details);
		usernameView = (EditText) findViewById(R.id.username);
		passwordView = (EditText) findViewById(R.id.password);
		saveLoginCredentialsView
				.setOnCheckedChangeListener(new onCheckBoxClickedListener());

		// If a connection id is given from intent, the activity needs to edit a
		// connection
		selectedConnectionID = getIntent()
				.getLongExtra(EXTRA_CONNECTION_ID, -1);

		if (selectedConnectionID >= 0) {
			FormulizeDBHelper dbHelper = new FormulizeDBHelper(this);
			ConnectionInfo connection = dbHelper
					.getConnection(selectedConnectionID);
			connectionURLView.setText(connection.getConnectionURL());
			connectionNameView.setText(connection.getConnectionName());

			if (!connection.getUsername().equals("")) {
				saveLoginCredentialsView.setChecked(true);
				usernameView.setText(connection.getUsername());
				passwordView.setText(connection.getPassword());
			}
			setTitle(R.string.title_activity_edit_connection);
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_connection, menu);
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
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.saveConnection:
			connectionURLView.setError(null);
			connectionNameView.setError(null);
			usernameView.setError(null);
			passwordView.setError(null);

			connectionURL = connectionURLView.getText().toString();
			connectionName = connectionNameView.getText().toString();

			if (saveLoginCredentialsView.isChecked()) {
				username = usernameView.getText().toString();
				password = passwordView.getText().toString();
			} else {
				username = "";
				password = "";
			}

			if (isValidInput()) {
				ConnectionInfo connectionInfo = new ConnectionInfo(
						connectionURL, connectionName, username, password);
				validateFormulizeConnection(connectionInfo,
						selectedConnectionID);

			} else {
				return false;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Check box listener for
	 * 
	 * @author timch326
	 * 
	 */
	private class onCheckBoxClickedListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {

			switch (buttonView.getId()) {
			case R.id.save_login_credentials_box:
				if (isChecked) {
					loginDetails.setVisibility(View.VISIBLE);
					usernameView.setVisibility(View.VISIBLE);
					passwordView.setVisibility(View.VISIBLE);
				} else {
					loginDetails.setVisibility(View.GONE);
					usernameView.setVisibility(View.GONE);
					passwordView.setVisibility(View.GONE);
				}
			}
		}
	}

	/**
	 * Checks the validity of the input the user has entered. It also modifies
	 * connectionURL so some URLs (e.g. add http://)
	 * 
	 * @return the validity of the inputs
	 */
	boolean isValidInput() {

		boolean isValid = true;

		// Check for empty textboxes
		if ("".equals(connectionName)) {
			connectionNameView
					.setError(getString(R.string.error_connection_name));
			isValid = false;
		}
		if (saveLoginCredentialsView.isChecked()) {
			if ("".equals(username)) {
				usernameView.setError(getString(R.string.error_username));
				isValid = false;
			}
			if ("".equals(password)) {
				passwordView.setError(getString(R.string.error_password));
				isValid = false;
			}
		}

		// Append "http://" to URL if necessary
		if (!connectionURL.startsWith("http://")
				&& !connectionURL.startsWith("https://")) {
			connectionURL = "http://" + connectionURL;
		}
		if (!connectionURL.endsWith("/")) {
			connectionURL += "/";
		}
		// Validate HTTP URL
		if (!connectionURL.matches(Patterns.WEB_URL.toString())) {
			connectionURLView.setError(getString(R.string.error_url));
			isValid = false;
		}
		return isValid;
	}

	/**
	 * Checks if the connection leads to a valid Formulize server by trying to
	 * do a login. If the login did not return an error, then the connection is
	 * considered valid and it would be added/updated to the database.
	 * 
	 * @param connection
	 *            The connection information input by user
	 * @param connectionID
	 *            The associated connection ID, -1 if this is a new connection
	 */
	void validateFormulizeConnection(ConnectionInfo connection,
			long connectionID) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getString(R.string.progress_validation));
		progressDialog.show();

		Runnable loginTask = new LoginRunnable(connection,
				new ConnectionValidationHandler(this, connection,
						progressDialog, connectionID));
		Thread loginThread = new Thread(loginTask);

		loginThread.start();
	}

	/**
	 * Adds the specified connection to the database.
	 * 
	 * @param connection
	 *            The connection information input by user
	 * @param activity
	 *            The activity that the database helper and views would attach
	 *            onto
	 */
	private static void addConnection(ConnectionInfo connection,
			FragmentActivity activity) {
		FormulizeDBHelper dbHelper = new FormulizeDBHelper(activity);
		dbHelper.insertConnectionInfo(connection);
		Toast connectionToast = Toast.makeText(activity,
				R.string.toast_connection_added, Toast.LENGTH_SHORT);
		connectionToast.show();
	}

	/**
	 * Updates the specified connection in the database.
	 * 
	 * @param connection
	 *            The connection information input by user
	 * 
	 * @param connectionID
	 *            The associated connection ID that the connection has in the
	 *            database
	 * @param activity
	 *            The activity that the database helper and views would attach
	 *            onto
	 */
	private static void modifyConnection(ConnectionInfo connection,
			long connectionID, FragmentActivity activity) {
		FormulizeDBHelper dbHelper = new FormulizeDBHelper(activity);
		int result = dbHelper.updateConnectionInfo(connection, connectionID);
		Log.d("Formulize", "Updated connection" + result);

		Toast connectionToast = Toast.makeText(activity,
				R.string.toast_connection_updated, Toast.LENGTH_SHORT);
		connectionToast.show();
	}

	/**
	 * This Handler responds to messages sent by {@link LoginRunnable} to
	 * indicate whether the connection information provided by the user is a
	 * valid Formulize server connection. A connection is considered valid even
	 * if login credentials are incorrect.
	 * 
	 * @author timch326
	 * 
	 */
	private static class ConnectionValidationHandler extends Handler {

		private final ConnectionInfo selectedConnection;
		private final AddConnectionActivity activity;
		private final ProgressDialog progressDialog;
		private final long connectionID;

		public ConnectionValidationHandler(AddConnectionActivity activity,
				ConnectionInfo selectedConnection,
				ProgressDialog progressDialog, long connectionID) {
			super();
			this.selectedConnection = selectedConnection;
			this.progressDialog = progressDialog;
			this.activity = activity;
			this.connectionID = connectionID;
		}

		public void handleMessage(Message msg) {

			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}

			int result = msg.getData().getInt(
					LoginRunnable.EXTRA_LOGIN_RESPONSE_MSG);

			switch (result) {
			case LoginRunnable.LOGIN_SUCESSFUL_MSG:
			case LoginRunnable.LOGIN_UNSUCESSFUL_MSG:
				if (connectionID >= 0) {
					AddConnectionActivity.modifyConnection(selectedConnection,
							connectionID, activity);
				} else {
					AddConnectionActivity.addConnection(selectedConnection,
							activity);
				}
				Intent connectionListIntent = new Intent(activity,
						ConnectionActivity.class);
				activity.startActivity(connectionListIntent);
				break;
			default:
				activity.connectionURLView.setError(activity
						.getString(R.string.error_formulize_url));
			}
		}
	};
}
