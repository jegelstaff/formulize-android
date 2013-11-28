package ca.formulize.android.connection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import ca.formulize.android.R;
import ca.formulize.android.data.ConnectionInfo;

/**
 * A dialog confirmation for logging out the user.
 * @author timch326
 *
 */
public class LogoutDialogFragment extends DialogFragment {

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_title_logout)
			   .setMessage(R.string.dialog_message_logout)
			   .setNegativeButton(android.R.string.no, 
					   new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
			   .setPositiveButton(android.R.string.yes, 
					   new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ConnectionInfo connectionInfo = FUserSession.getInstance().getConnectionInfo();
							FragmentActivity activity = LogoutDialogFragment.this.getActivity();
							new LogoutAsyncTask(activity).execute(connectionInfo);							
						}
					});
		
		return builder.create();
	}
}
