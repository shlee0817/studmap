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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.whs.studmap.client.core.snippets.ErrorHandler;
import de.whs.studmap.client.core.snippets.UserInfo;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.listener.OnGenericTaskListener;
import de.whs.studmap.client.listener.OnRegisterDialogListener;
import de.whs.studmap.client.tasks.UserRegisterTask;
import de.whs.studmap.navigator.R;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class RegisterDialogFragment extends DialogFragment implements
		OnGenericTaskListener<Void> {

	private OnRegisterDialogListener mCallback;
	private ErrorHandler mErrorHandler = null;
	private ProgressDialog pDialog = null;

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mPassword1;
	private String mPassword2;

	// UI references
	private EditText mUsernameView;
	private EditText mPasswordView1;
	private EditText mPasswordView2;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mErrorHandler = new ErrorHandler(getActivity());
		pDialog = UserInfo.StudmapProgressDialog(getActivity());

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.fragment_register_dialog,
				null);

		builder.setView(rootView).setPositiveButton(
				R.string.action_register_register, null);

		initFormFields(rootView);

		final AlertDialog mDialog = builder.create();
		mDialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				Button b = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						attemptRegister();
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
			mCallback = (OnRegisterDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnRegisterDialogListener");
		}
	}

	private void initFormFields(View rootView) {
		// Set up the login form.
		mUsernameView = (EditText) rootView
				.findViewById(R.id.register_username);
		mUsernameView.setText(mUsername);

		mPasswordView1 = (EditText) rootView
				.findViewById(R.id.register_password1);
		mPasswordView2 = (EditText) rootView
				.findViewById(R.id.register_password2);
		mPasswordView2
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.register || id == EditorInfo.IME_NULL) {
							attemptRegister();
							return true;
						}
						return false;
					}
				});
	}

	public void attemptRegister() {

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView1.setError(null);
		mPasswordView2.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword1 = mPasswordView1.getText().toString();
		mPassword2 = mPasswordView2.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid passwords.
		if (TextUtils.isEmpty(mPassword1)) {
			mPasswordView1.setError(getString(R.string.error_field_required));
			focusView = mPasswordView1;
			cancel = true;
		} else if (mPassword1.length() < 4) {
			mPasswordView1.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView1;
			cancel = true;
		}

		if (!TextUtils.equals(mPassword1, mPassword2)) {
			mPasswordView2.setError(getString(R.string.error_password_match));
			focusView = mPasswordView2;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(mUsername)) {
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
			pDialog.show();
			UserRegisterTask mAuthTask = new UserRegisterTask(this, mUsername,
					mPassword1);
			mAuthTask.execute((Void) null);
		}
	}

	@Override
	public void onError(int responseError) {	
		switch (responseError) {
		case ResponseError.UserNameDuplicate:
			mUsernameView
					.setError(getString(R.string.error_username_duplicate));
			mUsernameView.requestFocus();
			break;

		case ResponseError.UserNameInvalid:
			mUsernameView.setError(getString(R.string.error_invalid_username));
			mUsernameView.requestFocus();
			break;
			
		case ResponseError.PasswordInvalid:
			mPasswordView1.setError(getString(R.string.error_invalid_password));
			mPasswordView1.requestFocus();
			break;
			
		default:
			mErrorHandler.handle(responseError);
		}
		pDialog.dismiss();
	}

	@Override
	public void onCanceled() {
		pDialog.dismiss();
		mErrorHandler.handle(ResponseError.TaskCancelled);
	}

	@Override
	public void onSuccess(Void arg0) {
		pDialog.dismiss();
		mCallback.onRegister();
		dismiss();
	}
}
