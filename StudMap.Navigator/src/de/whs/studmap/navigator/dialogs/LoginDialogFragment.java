package de.whs.studmap.navigator.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import de.whs.studmap.client.core.snippets.UserInfo;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.listener.OnGenericTaskListener;
import de.whs.studmap.client.listener.OnLoginDialogListener;
import de.whs.studmap.client.tasks.UserLoginTask;
import de.whs.studmap.navigator.R;

public class LoginDialogFragment extends DialogFragment implements
		OnGenericTaskListener<Void> {

	private String mUserName;
	private String mPassword;

	// UI references
	private EditText mUsernameView;
	private EditText mPasswordView;
	private OnLoginDialogListener mCallback;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.fragment_login_dialog, null);

		builder.setView(rootView).setPositiveButton(R.string.action_sign_in,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						attemptLogin();
					}
				});

		initFormFields(rootView);
		return builder.create();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnLoginDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnLoginDialogListener");
		}
	}

	private void initFormFields(View rootView) {
		// Set up the login form.
		mUsernameView = (EditText) rootView.findViewById(R.id.userName);
		mUsernameView.setText(mUserName);

		mPasswordView = (EditText) rootView.findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUserName = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(mUserName)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and execute a background task to
			// perform the user login attempt.
			// TODO showProgress(true);
			UserLoginTask mAuthTask = new UserLoginTask(this, mUserName,
					mPassword);
			mAuthTask.execute((Void) null);
		}
	}

	@Override
	public void onSuccess(Void object) {
		// TODO showProgress(true);

		mCallback.onLogin(mUserName);
		dismiss();
	}

	@Override
	public void onCanceled() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(int responseError) {
		switch (responseError) {
		case ResponseError.DatabaseError:

			UserInfo.dialog(getActivity(), mUserName,
					getString(R.string.error_connection));
			break;
		default:
			mPasswordView
					.setError(getString(R.string.error_incorrect_password));
			mPasswordView.requestFocus();
			break;
		}
	}

}
