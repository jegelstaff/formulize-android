package ca.formulize.android.ui;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import ca.formulize.android.R;
import ca.formulize.android.connection.FUserSession;
import ca.formulize.android.connection.LoginRunnable;
import ca.formulize.android.data.ConnectionInfo;
import ca.formulize.android.data.FormulizeDBContract.ConnectionEntry;
import ca.formulize.android.data.FormulizeDBHelper;
import ca.formulize.android.util.ConnectionUtil;

/**
 * Represents the connection list screen where users can choose from a list of
 * connections they have created in {@link AddConnectionActivity} to connect to
 * Formulize servers.
 * 
 * @author timch326
 * 
 */
public class ConnectionActivity extends FragmentActivity {

	// UI References
	private ListView connectionList;
	private ProgressDialog progressDialog;
	private Button addConnectionButton;

	private FormulizeDBHelper dbHelper;
	private SimpleCursorAdapter connectionAdapter;
	private ConnectionInfo selectedConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Try getting a list of connections
		dbHelper = new FormulizeDBHelper(this);
		Cursor connectionCursor = dbHelper.getConnectionList(-1);

		if (connectionCursor.getCount() <= 0) {
			// Prompt user to add new connections
			showAddConnectionText();
		} else {
			// Show available connections with connectionList
			String[] selectDBColumns = {
					ConnectionEntry.COLUMN_NAME_CONNECTION_NAME,
					ConnectionEntry.COLUMN_NAME_USERNAME };
			int[] mappedViews = { android.R.id.text1, android.R.id.text2 };
			connectionAdapter = new SimpleCursorAdapter(this,
					android.R.layout.simple_list_item_2, connectionCursor,
					selectDBColumns, mappedViews,
					CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

			connectionList = new ListView(this);
			setContentView(connectionList);
			registerForContextMenu(connectionList);
			connectionList.setAdapter(connectionAdapter);

			OnItemClickListener mConnectionClickedListener = new OnConnectionClickListener();
			connectionList.setOnItemClickListener(mConnectionClickedListener);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Set up cookie manager, removing any user sessions
		CookieHandler.setDefault(new CookieManager(null,
				CookiePolicy.ACCEPT_ALL));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.connection, menu);
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
		case R.id.addConnection:
			Intent addConnectionIntent = new Intent(ConnectionActivity.this,
					AddConnectionActivity.class);
			startActivity(addConnectionIntent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.connection_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.edit:
			addOrEditConnection(info.id);
			return true;
		case R.id.delete:
			FormulizeDBHelper dbHelper = new FormulizeDBHelper(this);
			dbHelper.deleteConnection(info.id);
			Cursor connectionCursor = dbHelper.getConnectionList(-1);
			connectionAdapter.swapCursor(connectionCursor);
			connectionAdapter.notifyDataSetChanged();
			if (connectionCursor.getCount() <= 0) {
				showAddConnectionText();
			}
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/**
	 * Replaces the usual connection list with a prompt to go to
	 * {@link AddConnectionActivity}.
	 */
	private void showAddConnectionText() {
		setContentView(R.layout.activity_connection);
		addConnectionButton = (Button) findViewById(R.id.addConnectionButton);
		addConnectionButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent addConnectionIntent = new Intent(
						ConnectionActivity.this, AddConnectionActivity.class);
				startActivity(addConnectionIntent);
			}
		});
	}

	private class OnConnectionClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {

			// Get selected connection info from database
			Cursor cursor = (Cursor) connectionAdapter.getItem(position);
			String connectionURL = cursor
					.getString(cursor
							.getColumnIndex(ConnectionEntry.COLUMN_NAME_CONNECTION_URL));
			String connectionName = cursor
					.getString(cursor
							.getColumnIndex(ConnectionEntry.COLUMN_NAME_CONNECTION_NAME));
			String username = cursor.getString(cursor
					.getColumnIndex(ConnectionEntry.COLUMN_NAME_USERNAME));
			String password = cursor.getString(cursor
					.getColumnIndex(ConnectionEntry.COLUMN_NAME_PASSWORD));

			selectedConnection = new ConnectionInfo(connectionURL,
					connectionName, username, password);

			Log.d("Formulize", "Connection Selected");

			// Do not make any connections if there is no Internet
			if (!ConnectionUtil.isOnline(ConnectionActivity.this)) {
				new NoNetworkDialogFragment().show(
						ConnectionActivity.this.getSupportFragmentManager(),
						"no network");
				return;
			}

			// If no user name is in the connection info, prompt for login
			if (selectedConnection.getUsername() == null
					|| selectedConnection.getUsername().equals("")) {
				LoginDialogFragment loginDialog = new LoginDialogFragment();
				Bundle args = new Bundle();
				args.putParcelable(LoginDialogFragment.EXTRA_CONNECITON_INFO,
						selectedConnection);
				loginDialog.setArguments(args);
				loginDialog.show(
						ConnectionActivity.this.getSupportFragmentManager(),
						"login");
			} else {

				// Start Async Login, go to Application List if successful
				progressDialog = new ProgressDialog(ConnectionActivity.this);
				progressDialog.setMessage(getString(R.string.progress_login));
				progressDialog.show();

				Runnable loginTask = new LoginRunnable(selectedConnection,
						new LoginHandler(ConnectionActivity.this,
								selectedConnection, progressDialog));
				Thread loginThread = new Thread(loginTask);

				loginThread.start();
			}
		}
	}

	/**
	 * Sends an intent to AddConnectionActivity to edit or add a connection.
	 * 
	 * @param connectionID
	 *            specifies which connection to edit, if null, it means a new
	 *            connection is to be created
	 */
	private void addOrEditConnection(long connectionID) {
		Intent addConnectionIntent = new Intent(ConnectionActivity.this,
				AddConnectionActivity.class);
		addConnectionIntent.putExtra(AddConnectionActivity.EXTRA_CONNECTION_ID,
				connectionID);
		startActivity(addConnectionIntent);
	}

	// Define the Handler that receives messages from the thread and update the
	// progress
	public static class LoginHandler extends Handler {

		private final ConnectionInfo selectedConnection;
		private final FragmentActivity activity;
		private final ProgressDialog progressDialog;

		public LoginHandler(FragmentActivity activity,
				ConnectionInfo selectedConnection, ProgressDialog progressDialog) {
			super();
			this.selectedConnection = selectedConnection;
			this.progressDialog = progressDialog;
			this.activity = activity;

		}

		public void handleMessage(Message msg) {

			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}

			int result = msg.getData().getInt(
					LoginRunnable.EXTRA_LOGIN_RESPONSE_MSG);

			switch (result) {
			case LoginRunnable.LOGIN_SUCESSFUL_MSG:
				FUserSession.getInstance()
						.setConnectionInfo(selectedConnection);

				// Go to application list once logged in
				Intent viewApplicationsIntent = new Intent(activity,
						ApplicationListActivity.class);
				activity.startActivity(viewApplicationsIntent);
				break;
			case LoginRunnable.LOGIN_UNSUCESSFUL_MSG:
				LoginDialogFragment loginDialog = new LoginDialogFragment();
				Bundle args = new Bundle();
				args.putParcelable(LoginDialogFragment.EXTRA_CONNECITON_INFO,
						selectedConnection);
				args.putBoolean(LoginDialogFragment.EXTRA_IS_REATTEMPT, true);
				loginDialog.setArguments(args);
				loginDialog.show(activity.getSupportFragmentManager(), "login");
				break;
			default:
				Toast connectionToast = Toast.makeText(activity,
						R.string.toast_connection_failed, Toast.LENGTH_SHORT);
				connectionToast.show();
			}
		}
	};

}
