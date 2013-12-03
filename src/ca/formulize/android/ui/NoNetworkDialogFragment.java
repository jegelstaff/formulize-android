package ca.formulize.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import ca.formulize.android.R;

/**
 * A alert to notify users that they do not have a network connection to use Formulize.
 * @author timch326
 *
 */
public class NoNetworkDialogFragment extends DialogFragment {

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_title_no_network)
			   .setMessage(R.string.dialog_message_no_network)
			   .setPositiveButton(android.R.string.ok, 
					   new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
			   .setCancelable(false);
		
		return builder.create();
	}
}
