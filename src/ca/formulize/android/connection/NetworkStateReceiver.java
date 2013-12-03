package ca.formulize.android.connection;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Used to handle changes in network connection. If Internet connection is
 * available, the keep alive routine is restarted.
 * 
 * This receiver should only be enabled when the network connection needs to be re-established.
 * 
 * @author timch326
 * 
 */
public class NetworkStateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {

		if (intent.getExtras() != null) {
			final ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo netInfo = connectivityManager
					.getActiveNetworkInfo();

			if (netInfo != null && netInfo.isConnectedOrConnecting()) {
				FUserSession.getInstance().startKeepAliveSession(context);

				// We don't need to listen for connection to be online anymore
				// Disable this receiver
				ComponentName receiver = new ComponentName(context,
						NetworkStateReceiver.class);
				PackageManager pm = context.getPackageManager();
				pm.setComponentEnabledSetting(receiver,
						PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);

				Log.d("Formulize",
						"Connection Re-established, attempting login connection again.");
				
			} else if (intent.getBooleanExtra(
					ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
				Log.d("Formulize", "Connection Lost");
			}
		}
	}

}
