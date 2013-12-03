package ca.formulize.android.connection;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import ca.formulize.android.R;
import ca.formulize.android.data.ConnectionInfo;
import ca.formulize.android.ui.ConnectionActivity;
import ca.formulize.android.util.ConnectionUtil;

/**
 * Singleton representation of the user's login session. Other classes may
 * retrieve this to get the login information on a user. User name and password
 * of the user is stored here, even if "Remember me" is not checked by the user.
 * 
 * Note: A user's login session token is stored in the CookieManager in the
 * HttpUrlConnection Library.
 * 
 * @author timch326
 * 
 */
public class FUserSession {

	private static final int KEEP_ALIVE_INTERVAL = 10;

	private static FUserSession instance;
	private ScheduledThreadPoolExecutor keepAliveExecutor;
	private ConnectionInfo connectionInfo;

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

	/**
	 * Keeps a user session from being timed out when the application is
	 * running. This should be called only after a user has logged into a
	 * Formulize account, and a Connection Info is stored in this object.
	 * 
	 * @param context
	 *            The current context of the Android application
	 */
	public void startKeepAliveSession(Context context) {

		keepAliveExecutor = new ScheduledThreadPoolExecutor(1);
		keepAliveExecutor.scheduleAtFixedRate(new LoginRunnable(connectionInfo,
				new KeepAliveHandler(context)), 0, KEEP_ALIVE_INTERVAL,
				TimeUnit.MINUTES);
	}

	/**
	 * Stops the application from keeping the user session alive. Should be
	 * called when a user logs out, or when the application is suspended.
	 * 
	 * @param context
	 *            The current context of the Android application
	 */
	public void endKeepAliveSession(Context context) {
		keepAliveExecutor.shutdown();

		ComponentName receiver = new ComponentName(context,
				NetworkStateReceiver.class);
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
	}

	/**
	 * Formulize login handler that is used for keeping a user session from
	 * timing out. It handles lost network connection by waiting for the
	 * connection to be re-established, other errors will be handled by
	 * returning to {@link ConnectionActivity}.
	 * 
	 * @author timch326
	 * 
	 */
	private static class KeepAliveHandler extends Handler {

		private final Context context;

		public KeepAliveHandler(Context context) {
			super();
			this.context = context;
		}

		public void handleMessage(Message msg) {

			int result = msg.getData().getInt(
					LoginRunnable.EXTRA_LOGIN_RESPONSE_MSG);

			switch (result) {
			case LoginRunnable.LOGIN_SUCESSFUL_MSG:
				break;
			default:
				// Keep alive failed, return to connection list

				Toast connectionToast;

				if (!ConnectionUtil.isOnline(context)) {

					// Enable NetworkStateReceiver to listen for network changes
					ComponentName receiver = new ComponentName(context,
							NetworkStateReceiver.class);
					PackageManager pm = context.getPackageManager();
					pm.setComponentEnabledSetting(receiver,
							PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
							PackageManager.DONT_KILL_APP);

					connectionToast = Toast.makeText(context,
							R.string.toast_no_network_connection,
							Toast.LENGTH_SHORT);
				} else {
					// Error with the Formulize Connection, return to
					// ConnectionActivity
					connectionToast = Toast.makeText(context,
							R.string.toast_connection_failed,
							Toast.LENGTH_SHORT);

					Intent connectionIntent = new Intent(context,
							ConnectionActivity.class);
					context.startActivity(connectionIntent);
				}

				connectionToast.show();
			}
		}
	};
}
