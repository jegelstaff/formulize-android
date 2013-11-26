package ca.formulize.android.menu;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import ca.formulize.android.data.FormulizeLink;
import ca.formulize.android.screen.ScreenWebActivity;

/**
 * Displays the available screens of a chosen application. The activity expects
 * to given an array of FormulizeLink in the intent is receives, it would
 * display this array of links in a ListView. When the user selects an item on
 * the list, ScreenWebActivity would be opened, with the sid of the
 * corresponding item within the intent.
 * 
 * @author timch326
 * 
 */
public class ScreenListActivity extends FragmentActivity {

	public static final String EXTRA_SCREENS_AVAILABLE = "ca.formulize.android.extras.currentApplication";
	public static final String EXTRA_APP_ID = "ca.formulize.android.extras.applicationID";

	private FormulizeApplication currentApplication;
	private ArrayAdapter<FormulizeLink> linksAdapter;
	private ListView linksListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		setupActionBar();

		// Get selected application
		Intent screenListIntent = getIntent();
		currentApplication = screenListIntent
				.getParcelableExtra(EXTRA_SCREENS_AVAILABLE);
		setTitle(currentApplication.getName());

		// Initialize list of available screens for the application
		FormulizeLink[] links = currentApplication.getLinks();
		linksAdapter = new ArrayAdapter<FormulizeLink>(this,
				android.R.layout.simple_list_item_1, links);

		// Set up the list view's adapter and click listener
		linksListView = new ListView(this);
		linksListView.setAdapter(linksAdapter);
		linksListView.setOnItemClickListener(new ScreenListClickListener());
		setContentView(linksListView);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		savedInstanceState.putParcelable(EXTRA_SCREENS_AVAILABLE,
				currentApplication);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		currentApplication = savedInstanceState
				.getParcelable(EXTRA_SCREENS_AVAILABLE);
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
		getMenuInflater().inflate(R.menu.application_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// NavUtils.navigateUpFromSameTask(this);
			onBackPressed();
			return true;
		case R.id.logout:
			ConnectionInfo connectionInfo = FUserSession.getInstance()
					.getConnectionInfo();
			new LogoutAsyncTask(this).execute(connectionInfo);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class ScreenListClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long screenID) {

			// Open selected link with the screen list activity
			FormulizeLink selectedLink = (FormulizeLink) parent.getAdapter()
					.getItem(position);
			Intent screenList = new Intent(ScreenListActivity.this,
					ScreenWebActivity.class);

			// Check if the selected screen is an external link, open browser if
			// that's the case
			String screen = selectedLink.getScreen();
			if (!screen.startsWith("fid") && !screen.startsWith("sid")) {
				String url = selectedLink.getUrl();
				if (!url.startsWith("http://") && !url.startsWith("https://"))
					url = "http://" + url;
				Intent browserIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(url));
				startActivity(browserIntent);
			} else {
				screenList.putExtra(ScreenWebActivity.EXTRA_SID,
						selectedLink.getScreen());
				startActivity(screenList);
			}

		}
	}

}
