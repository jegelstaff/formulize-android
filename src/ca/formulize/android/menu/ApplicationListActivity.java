package ca.formulize.android.menu;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ca.formulize.android.R;
import ca.formulize.android.connection.FUserSession;
import ca.formulize.android.data.ConnectionInfo;
import ca.formulize.android.data.FormulizeApplication;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApplicationListActivity extends Activity {

	public static final String APPLICATION = "ca.formulize.android.menu.application";

	// Temporary hard coded variables for prototyping
	public static final String TEST_JSON = "[{\"appid\":\"1\",\"name\":\"Application1\",\"description\":false,\"links\":[{\"menu_id\":\"3\",\"appid\":\"1\",\"screen\":\"fid=2\",\"rank\":\"1\",\"url\":\"\",\"link_text\":\"testFormLink\",\"name\":null,\"text\":\"testFormLink\"},{\"menu_id\":\"4\",\"appid\":\"1\",\"screen\":\"sid=1\",\"rank\":\"2\",\"url\":\"\",\"link_text\":\"Fruity Form\",\"name\":null,\"text\":\"Fruity Form\"},{\"menu_id\":\"5\",\"appid\":\"1\",\"screen\":\"sid=5\",\"rank\":\"4\",\"url\":\"\",\"link_text\":\"Mobile Form\",\"name\":null,\"text\":\"Mobile Form\"}]},{\"appid\":\"2\",\"name\":\"The Second Application\",\"description\":false,\"links\":[{\"menu_id\":\"9\",\"appid\":\"2\",\"screen\":\"sid=7\",\"rank\":\"1\",\"url\":\"\",\"link_text\":\"For Students\",\"name\":null,\"text\":\"For Students\"},{\"menu_id\":\"11\",\"appid\":\"2\",\"screen\":\"sid=9\",\"rank\":\"3\",\"url\":\"\",\"link_text\":\"Anyone can see this form\",\"name\":null,\"text\":\"Anyone can see this form\"}]}]";
	public static final String TEST_LINK = "ca.formulize.android.menu.testLink";

	private FormulizeApplication[] applications;
	private ArrayAdapter<FormulizeApplication> applicationListAdapter;
	private ListView applicationListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_application_list);

		// Parse JSON with GSON Library
		Gson gson = new GsonBuilder().setFieldNamingPolicy(
				FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
		applications = gson.fromJson(TEST_JSON, FormulizeApplication[].class);
		applicationListAdapter = new ArrayAdapter<FormulizeApplication>(this,
				android.R.layout.simple_list_item_1, applications);

		// Set ListView adapter and click listener
		applicationListView = (ListView) findViewById(R.id.applicationList);
		applicationListView.setAdapter(applicationListAdapter);
		applicationListView
				.setOnItemClickListener(new ApplicationListClickListener());
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
			screenListIntent.putExtra(TEST_LINK,
					selectedApplication.getLinks()[0]);
			screenListIntent.putExtra(APPLICATION, selectedApplication);
			startActivity(screenListIntent);

		}
	}
}
