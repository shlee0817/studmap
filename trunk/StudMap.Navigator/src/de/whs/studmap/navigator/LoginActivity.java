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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import de.whs.studmap.snippets.UserInfo;
import de.whs.studmap.web.ResponseError;
import de.whs.studmap.web.Service;
import de.whs.studmap.web.WebServiceException;

public class LoginActivity extends Activity {
	public final int REQUEST_ID_REGISTER = 200;
	public static final String EXTRA_USERNAME = "username";
		
	
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mUserName;
	private String mPassword;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.userName);
		mUsernameView.setText(mUserName);

		mPasswordView = (EditText) findViewById(R.id.password);
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

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.login_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ID_REGISTER){
			if (resultCode == RESULT_OK){
				boolean result = data.getBooleanExtra(RegisterActivity.EXTRA_SUCCESS,false);
				String username = data.getStringExtra(RegisterActivity.EXTRA_USERNAME);
				if (result){
					mUsernameView.setText(mUserName);
					mPasswordView.requestFocus();
					UserInfo.dialog(this, username, getString(R.string.register_successful));
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
		switch (item.getItemId()) {
		case R.id.action_register :
			startActivityForResult(new Intent(this, RegisterActivity.class),REQUEST_ID_REGISTER);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

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

		// Check for a valid user name.
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
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask(this);
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

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		private Context mContext = null;
		private boolean bShowDialog = false;
		private boolean bRequestFocus = false;
		
		public UserLoginTask(Context ctx){
			mContext = ctx;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				boolean result = Service.login(mUserName, mPassword); 
				return result;
			} catch (WebServiceException e) {
				JSONObject jObject = e.getJsonObject();
				
				try {
					int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
					
					switch (errorCode) {
					case ResponseError.DatabaseError:
						bShowDialog = true;
						break;
					default:
						mPasswordView.setError(getString(R.string.error_incorrect_password));
						bRequestFocus = true;
						break;
					}
				} catch (JSONException ignore) {}
			} catch (ConnectException e){
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
	            result.putExtra(EXTRA_USERNAME,mUserName);
	            setResult(Activity.RESULT_OK,result);
	            finish();
			}
			else{
				if (bRequestFocus)
					mPasswordView.requestFocus();
				if (bShowDialog)
					UserInfo.dialog(mContext,mUserName, getString(R.string.error_connection));
			}				
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
