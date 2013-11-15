package ca.formulize.android.connection;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
 * Represents the screen that allows users to create new connections to a
 * Formulize server. Connections are saved once they are submitted and
 * validated.
 * 
 * @author timch326
 * 
 */
public class AddConnectionActivity extends FragmentActivity {

	// Values to populate form with for editing connections
	public final static String EXTRA_CONNECTION_URL = "ca.formulize.android.extra.connectionURL";
	public final static String EXTRA_CONNECTION_NAME = "ca.formulize.android.extra.connectionName";
	public final static String EXTRA_USERNAME = "ca.formulize.android.extra.username";
	public final static String EXTRA_PASSWORD = "ca.formulize.android.extra.password";

	// Values for connection information
	private String connectionURL;
	private String connectionName;
	private String username;
	private String password;

	// UI References
	private EditText connectionURLView;
	private EditText connectionNameView;
	private CheckBox saveLoginCredentialsView;
	private TextView loginDetails;
	private EditText usernameView;
	private EditText passwordView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_connection);
		// Show the Up button in the action bar.
		setupActionBar();

		// Set up connection form
		// TODO: Set up connection values if they exist to allow edits
		connectionURLView = (EditText) findViewById(R.id.connection_url);
		connectionNameView = (EditText) findViewById(R.id.connection_name);
		saveLoginCredentialsView = (CheckBox) findViewById(R.id.save_login_credentials_box);
		loginDetails = (TextView) findViewById(R.id.login_details);
		usernameView = (EditText) findViewById(R.id.username);
		passwordView = (EditText) findViewById(R.id.password);

		saveLoginCredentialsView
				.setOnCheckedChangeListener(new onCheckBoxClickedListener());
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
			username = usernameView.getText().toString();
			password = passwordView.getText().toString();

			if (isValidInput()) {

				ConnectionInfo connectionInfo = new ConnectionInfo(
						connectionURL, connectionName, username, password);

				addConnection(connectionInfo);

				// Return to the connection list
				Toast connectionToast = Toast.makeText(this,
						"Connection Added", Toast.LENGTH_SHORT);
				connectionToast.show();

				Intent connectionListIntent = new Intent(
						AddConnectionActivity.this, ConnectionActivity.class);
				startActivity(connectionListIntent);

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
			connectionNameView.setError("Enter a connection name");
			isValid = false;
		}
		if (saveLoginCredentialsView.isChecked()) {
			if ("".equals(username)) {
				usernameView.setError("Enter an username");
				isValid = false;
			}
			if ("".equals(password)) {
				passwordView.setError("Enter a password");
				isValid = false;
			}
		}

		// Append "http://" to URL if necessary
		if (!connectionURL.startsWith("http://") && !connectionURL.startsWith("https://")) {
			connectionURL = "http://" + connectionURL;
		}
		if (!connectionURL.endsWith("/")) {
			connectionURL += "/";
		}
		// Validate HTTP URL
		if (!connectionURL.matches(Patterns.WEB_URL.toString())) {
			connectionURLView.setError("Enter a valid URL");
			isValid = false;
		}
		return isValid;
	}

	boolean isValidConnection(ConnectionInfo connection) {
		// TODO: Implement the actual validation
		return true;
	}

	private void addConnection(ConnectionInfo connection) {
		FormulizeDBHelper dbHelper = new FormulizeDBHelper(this);
		dbHelper.insertConnectionInfo(connection);
	}
}
