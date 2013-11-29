package ca.formulize.android.connection;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import ca.formulize.android.R;
import ca.formulize.android.data.ConnectionInfo;
import ca.formulize.android.data.FormulizeApplication;

/**
 * Represents the user's login information. Other classes may retrieve this to
 * get the login information on a user. Note: A user's login session token is
 * stored in the CookieManager in the HttpUrlConnection Library.
 * 
 * @author timch326
 * 
 */
public class FUserSession {
	private static final int KEEP_ALIVE_INTERVAL = 300;

	private static FUserSession instance;
	private ScheduledThreadPoolExecutor keepAliveExecutor;
	private ConnectionInfo connectionInfo;
	private String userToken;
	public FormulizeApplication[] applications;

	public static FUserSession getInstance() {
		if (instance == null) {
			instance = new FUserSession();
			return instance;
		} else
			return instance;
	}

	private FUserSession() {
	}

	public ConnectionInfo getConnectionInfo() {
		return this.connectionInfo;
	}

	public void setConnectionInfo(ConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	public String getUserToken() {
		return userToken;
	}

	public void startKeepAliveSession(FragmentActivity activity) {
		keepAliveExecutor = new ScheduledThreadPoolExecutor(1);
		keepAliveExecutor.scheduleAtFixedRate(new LoginRunnable(
				connectionInfo, new KeepAliveHandler(activity)), KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, TimeUnit.SECONDS);
	}

	public void endKeepAliveSession() {
		keepAliveExecutor.shutdown();
	}

	private static class KeepAliveHandler extends Handler {

		private final FragmentActivity activity;

		public KeepAliveHandler(FragmentActivity activity) {
			super();
			this.activity = activity;
		}

		public void handleMessage(Message msg) {

			int result = msg.getData().getInt(
					LoginRunnable.EXTRA_LOGIN_RESPONSE_MSG);

			switch (result) {
			case LoginRunnable.LOGIN_SUCESSFUL_MSG:
				break;
			default:
				// Keep alive failed, return to connection list
				Intent connectionIntent = new Intent(activity,
						ConnectionActivity.class);
				activity.startActivity(connectionIntent);

				Toast connectionToast = Toast.makeText(activity,
						R.string.toast_connection_failed, Toast.LENGTH_SHORT);
				connectionToast.show();
			}
		}
	};
}
