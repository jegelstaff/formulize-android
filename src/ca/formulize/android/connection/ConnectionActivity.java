package ca.formulize.android.connection;

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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import ca.formulize.android.R;
import ca.formulize.android.data.ConnectionInfo;
import ca.formulize.android.data.FormulizeDBContract.ConnectionEntry;
import ca.formulize.android.data.FormulizeDBHelper;
import ca.formulize.android.menu.ApplicationListActivity;

/**
 * Represents the connection list screen where users can choose from a list of
 * connections they have created in {@link AddConnectionActivity} to connect to
 * it.
 * 
 * @author timch326
 * 
 */
public class ConnectionActivity extends FragmentActivity {

	// UI References
	private ListView connectionList;
	private FormulizeDBHelper dbHelper;
	private SimpleCursorAdapter connectionAdapter;
	private ProgressDialog progressDialog;

	private ConnectionInfo selectedConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		connectionList = new ListView(this);
		setContentView(connectionList);

		registerForContextMenu(connectionList);

		// Instantiate Connection List
		dbHelper = new FormulizeDBHelper(this);
		Cursor connectionCursor = dbHelper.getConnectionList(-1);
		String[] selectDBColumns = {
				ConnectionEntry.COLUMN_NAME_CONNECTION_NAME,
				ConnectionEntry.COLUMN_NAME_USERNAME };
		int[] mappedViews = { android.R.id.text1, android.R.id.text2 };
		connectionAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, connectionCursor,
				selectDBColumns, mappedViews,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		connectionList.setAdapter(connectionAdapter);

		// Set up list item click listeners
		OnItemClickListener mConnectionClickedListener = new OnConnectionClickListener();
		connectionList.setOnItemClickListener(mConnectionClickedListener);
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
			return true;
		default:
			return super.onContextItemSelected(item);
		}
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

			// If no user name is in the connection info, prompt for login
			if (selectedConnection.getUsername() == null
					|| selectedConnection.getUsername().equals("")) {
				UserLoginDialogFragment loginDialog = new UserLoginDialogFragment();
				Bundle args = new Bundle();
				args.putParcelable(
						UserLoginDialogFragment.EXTRA_CONNECITON_INFO,
						selectedConnection);
				loginDialog.setArguments(args);
				loginDialog.show(ConnectionActivity.this.getSupportFragmentManager(), "login");
			} else {
				// FUserSession session = FUserSession.getInstance();

				// Start Async Login, go to Application List if successful
				// session.createConnection(ConnectionActivity.this,
				// selectedConnection);
				progressDialog = new ProgressDialog(ConnectionActivity.this);
				progressDialog.setMessage("Logging in");
				progressDialog.show();

				Runnable loginTask = new UserLoginAsyncTask(selectedConnection,
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

			String result = msg.getData().getString(
					UserLoginAsyncTask.EXTRA_LOGIN_RESPONSE_MSG);

			// Bad server Connection
			if (result == null)
				Log.d("Formulize", "Connection Failed");

			// Ask for credentials again if they were incorrect
			else if (result == UserLoginAsyncTask.LOGIN_UNSUCESSFUL_MSG) {
				UserLoginDialogFragment loginDialog = new UserLoginDialogFragment();
				Bundle args = new Bundle();
				args.putParcelable(
						UserLoginDialogFragment.EXTRA_CONNECITON_INFO,
						selectedConnection);
				args.putBoolean(UserLoginDialogFragment.EXTRA_IS_REATTEMPT,
						true);
				loginDialog.setArguments(args);
				loginDialog.show(activity.getSupportFragmentManager(), "login");
			} else if (result == UserLoginAsyncTask.LOGIN_SUCESSFUL_MSG) {
				Log.d("Formulize", result);
				FUserSession.getInstance()
						.setConnectionInfo(selectedConnection);

				// Go to application list once logged in
				Intent viewApplicationsIntent = new Intent(activity,
						ApplicationListActivity.class);
				activity.startActivity(viewApplicationsIntent);
			}
		}
	};

}
