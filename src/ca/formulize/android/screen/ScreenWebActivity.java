package ca.formulize.android.screen;

import java.net.CookieHandler;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import ca.formulize.android.R;
import ca.formulize.android.connection.FUserSession;
import ca.formulize.android.connection.LogoutAsyncTask;
import ca.formulize.android.connection.LogoutDialogFragment;
import ca.formulize.android.data.ConnectionInfo;

/**
 * Displays a Formulize screen given its screen ID. It assumes that there exists
 * proper session cookies given in the Cookie Manager within HttpURLConnection.
 * It transfers these session cookies to the Android WebView so it could display
 * the screen.
 * 
 * @author timch326
 * 
 */
public class ScreenWebActivity extends FragmentActivity {
	public static final String EXTRA_SID = "ca.formulize.android.extras.sid";

	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Show the Up button in the action bar.
		setupActionBar();

		webView = new WebView(this);
		setContentView(webView);

		if (savedInstanceState != null) {
			Log.d("Formulize", "Restoring Webview");
			webView.restoreState(savedInstanceState);
		} else {

			// Parameters to access a screen
			Intent screenIntent = getIntent();
			String sid = screenIntent.getStringExtra(EXTRA_SID);
			FUserSession userSession = FUserSession.getInstance();
			String urlString = userSession.getConnectionInfo()
					.getConnectionURL();

			// Create URI Connection
			URI baseURI = null;
			try {
				baseURI = new URI(urlString);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

			// Pass session cookies from HttpUrlConnection to the WebView
			android.webkit.CookieSyncManager.createInstance(this);
			android.webkit.CookieManager cookieManager = CookieManager
					.getInstance();
			java.net.CookieStore rawCookieStore = ((java.net.CookieManager) CookieHandler
					.getDefault()).getCookieStore();

			// Copy cookies from HttpURLConnection to WebView
			List<HttpCookie> cookies = rawCookieStore.get(baseURI);
			String url = baseURI.toString();
			for (HttpCookie cookie : cookies) {
				String setCookie = new StringBuilder(cookie.toString())
						.append("; domain=").append(cookie.getDomain())
						.append("; path=").append(cookie.getPath()).toString();
				cookieManager.setCookie(url, setCookie);
			}

			// Load screen page
			webView.setWebViewClient(new FScreenWebViewClient());
			webView.setWebChromeClient(new WebChromeClient());
			webView.getSettings().setJavaScriptEnabled(true);
			String fFormURL = userSession.getConnectionInfo()
					.getConnectionURL() + "modules/formulize/index.php?" + sid;

			webView.loadUrl(fFormURL);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		FUserSession.getInstance().startKeepAliveSession();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		FUserSession.getInstance().endKeepAliveSession();
	}

	protected void onSaveInstanceState(Bundle savedInstanceState) {
		webView.saveState(savedInstanceState);
		Log.d("Formulize", "Saving Webview");

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
			new LogoutDialogFragment().show(this.getSupportFragmentManager(), "logout");;
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class FScreenWebViewClient extends WebViewClient {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.d("Formulize", "Loading " + url);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			String hostname = Uri.parse(url).getHost();
			if (!hostname.contains("localhost")) {
				return false;
			}
			Log.d("Formulize", "Prevented " + hostname + " from loading.");

			// Replace localhost with Android local machine's "localhost" IP
			view.loadUrl(url.replace("localhost", "10.0.2.2"));

			return true;
		}
	}

}
