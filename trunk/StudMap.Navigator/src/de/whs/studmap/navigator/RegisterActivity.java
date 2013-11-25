package de.whs.studmap.navigator;

import java.net.ConnectException;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import de.whs.studmap.data.Constants;
import de.whs.studmap.snippets.UserInfo;
import de.whs.studmap.web.ResponseError;
import de.whs.studmap.web.Service;
import de.whs.studmap.web.WebServiceException;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class RegisterActivity extends Activity {
	public static final String EXTRA_SUCCESS = "registrationComplete";
	public static final String EXTRA_USERNAME = "username";
	
	private UserRegisterTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mPassword1;
	private String mPassword2;

	// UI references
	private EditText mUsernameView;
	private EditText mPasswordView1;
	private EditText mPasswordView2;
	private View mRegisterFormView;
	private View mRegisterStatusView;
	private TextView mRegisterStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);

		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.register_username);
		mUsernameView.setText(mUsername);

		mPasswordView1 = (EditText) findViewById(R.id.register_password1);
		mPasswordView2 = (EditText) findViewById(R.id.register_password2);
		mPasswordView2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

		mRegisterFormView = findViewById(R.id.register_form);
		mRegisterStatusView = findViewById(R.id.register_status);
		mRegisterStatusMessageView = (TextView) findViewById(R.id.register_status_message);

		findViewById(R.id.register_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptRegister();
					}
				});
	}

	public void attemptRegister() {
		if (mAuthTask != null) {
			return;
		}

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
		
		if (!TextUtils.equals(mPassword1, mPassword2)){
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
			mRegisterStatusMessageView.setText(R.string.register_progress_registering);
			showProgress(true);
			mAuthTask = new UserRegisterTask(this);
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mRegisterStatusView.setVisibility(View.VISIBLE);
			mRegisterStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegisterStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mRegisterFormView.setVisibility(View.VISIBLE);
			mRegisterFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegisterFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {
		private Context mContext = null;
		private boolean bShowDialog = false;
		private boolean	bRequestFocus = false;
		
		public UserRegisterTask(Context ctx){
			mContext = ctx;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				boolean result = Service.register(mUsername, mPassword1); 
				if (result)
					return result;
				else
					throw new ConnectException();
			} catch (WebServiceException e) {
				Log.d(Constants.LOG_TAG_REGISTER_ACTIVITY, "UserRegisterTask - WebServiceException");
				JSONObject jObject = e.getJsonObject();
				
				try {
					int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
					
					switch (errorCode) {
					case ResponseError.DatabaseError:
						bShowDialog = true;
						break;
					case ResponseError.UserNameDuplicate:
						mUsernameView.setError(getString(R.string.error_username_duplicate));
						bRequestFocus = true;
						break;
					}
				} catch (JSONException ignore) {
					Log.d(Constants.LOG_TAG_REGISTER_ACTIVITY, "UserRegisterTask - Parsing the WebServiceException failed!");
				}
			} catch (ConnectException e){
				Log.d(Constants.LOG_TAG_REGISTER_ACTIVITY, "UserRegisterTask - ConnectException");
				bShowDialog = true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				Intent result = new Intent();
	            result.putExtra(EXTRA_USERNAME,mUsername);
	            result.putExtra(EXTRA_SUCCESS, success);
	            setResult(Activity.RESULT_OK,result);
	            finish();
			}
			else{
				if (bRequestFocus)
					mUsernameView.requestFocus();
				if (bShowDialog)
					UserInfo.dialog(mContext,mUsername, getString(R.string.error_connection));
			}	
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
