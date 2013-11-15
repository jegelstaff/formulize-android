package ca.formulize.android.connection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import ca.formulize.android.R;
import ca.formulize.android.data.ConnectionInfo;

/**
 * The login dialogue that should appear when the user attempts to connect to a
 * server without user credentials.
 * 
 * @author timch326
 * 
 */
public class UserLoginDialogFragment extends DialogFragment {

	public static final String EXTRA_CONNECITON_INFO = "ca.formulize.android.extras.connectionInfo";
	public static final String EXTRA_IS_REATTEMPT = "ca.formulize.android.extras.isReattempt";

	// Connection Details
	private ConnectionInfo connectionInfo;
	private Boolean isReattempt;
	private String username;
	private String password;

	// UI References
	private TextView errorMessageView;
	private EditText usernameView;
	private EditText passwordView;

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_login, null);

		// Set UI References
		errorMessageView = (TextView) view.findViewById(R.id.errorMessage);
		usernameView = (EditText) view.findViewById(R.id.username);
		passwordView = (EditText) view.findViewById(R.id.password);

		// Retrieve arguments
		Bundle args = getArguments();
		connectionInfo = (ConnectionInfo) args
				.getParcelable(EXTRA_CONNECITON_INFO);
		isReattempt = args.getBoolean(EXTRA_IS_REATTEMPT, false);

		// Show and set error message if this is a login re-attempt
		if (isReattempt) {
			errorMessageView.setText(R.string.reattempt_message);
			errorMessageView.setVisibility(View.VISIBLE);
		}

		// Override the onclick of the positive button:
		// http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
		builder.setView(view)
				.setPositiveButton(android.R.string.ok, null)
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								UserLoginDialogFragment.this.getDialog()
										.cancel();

							}
						}).setTitle(R.string.sign_in_label);

		final AlertDialog alertDialog = builder.create();

		// Override standard dialog positive button behaviour
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Button positiveButton = alertDialog
						.getButton(AlertDialog.BUTTON_POSITIVE);
				positiveButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						username = usernameView.getText().toString();
						password = passwordView.getText().toString();
						connectionInfo.setUsername(username);
						connectionInfo.setPassword(password);

						if (isValidInput()) {
							FUserSession session = FUserSession.getInstance();
							session.createConnection(getActivity(),
									connectionInfo);
						}
					}
				});
			}
		});

		return alertDialog;
	}

	/**
	 * Checks the validity of the input the user has entered
	 * 
	 * @return the validity of the inputs
	 */
	private boolean isValidInput() {
		Boolean isValid = true;
		if ("".equals(username)) {
			usernameView.setError("Enter an username");
			isValid = false;
		}
		if ("".equals(password)) {
			passwordView.setError("Enter a password");
			isValid = false;
		}
		return isValid;
	}
}
