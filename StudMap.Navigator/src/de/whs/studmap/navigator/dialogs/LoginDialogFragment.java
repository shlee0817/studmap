package de.whs.studmap.navigator.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.whs.studmap.client.core.snippets.ErrorHandler;
import de.whs.studmap.client.core.snippets.UserInfo;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.listener.OnGenericTaskListener;
import de.whs.studmap.client.listener.OnLoginDialogListener;
import de.whs.studmap.client.tasks.UserLoginTask;
import de.whs.studmap.navigator.R;

public class LoginDialogFragment extends DialogFragment implements
		OnGenericTaskListener<Void> {

	private ErrorHandler mErrorHandler = null;
	private ProgressDialog pDialog = null;

	private String mUserName;
	private String mPassword;

	// UI references
	private EditText mUsernameView;
	private EditText mPasswordView;
	private OnLoginDialogListener mCallback;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mErrorHandler = new ErrorHandler(getActivity());
		pDialog = UserInfo.StudmapProgressDialog(getActivity());

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.fragment_login_dialog, null);

		builder.setView(rootView)
			.setPositiveButton(R.string.action_sign_in,	null)
			.setNeutralButton(R.string.action_register, null);
		

		initFormFields(rootView);

		final AlertDialog mDialog = builder.create();
		mDialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				Button positiveButton = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				Button neutralButton = mDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
				
				positiveButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
				
				neutralButton.setOnClickListener( new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						RegisterDialogFragment registerDialog = new RegisterDialogFragment();
						registerDialog.show(getFragmentManager(), "Register");
						dismiss();
					}
				});
			}
		});

		return mDialog;
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
		
		Bundle b = getArguments();
		if (b != null && b.containsKey("username")){
			mUserName = getArguments().getString("username");
			mPasswordView.requestFocus();
		}
		mUsernameView.setText(mUserName);
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
			pDialog.show();
			UserLoginTask mAuthTask = new UserLoginTask(this, mUserName,
					mPassword);
			mAuthTask.execute((Void) null);
		}
	}

	@Override
	public void onSuccess(Void object) {
		pDialog.dismiss();

		mCallback.onLogin(mUserName);
		dismiss();
	}

	@Override
	public void onCanceled() {
		pDialog.dismiss();
		mErrorHandler.handle(ResponseError.TaskCancelled);
	}

	@Override
	public void onError(int responseError) {
		switch (responseError) {
		case ResponseError.LoginInvalid:
			mPasswordView
					.setError(getString(R.string.error_incorrect_password));
			mUsernameView
					.setError(getString(R.string.error_incorrect_username));
			break;
		default:
			mErrorHandler.handle(responseError);
			break;
		}
		pDialog.dismiss();
	}

}
