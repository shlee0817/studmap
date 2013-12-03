package de.whs.studmap.client.tasks;

import java.net.ConnectException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import de.whs.studmap.client.listener.OnGenericTaskListener;

public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

	private String mUserName;
	private String mPassword;

	private OnGenericTaskListener<Void> mLoginListener;

	public UserLoginTask(OnGenericTaskListener<Void> loginListener,
			String userName, String password) {
		this.mUserName = userName;
		this.mPassword = password;
		this.mLoginListener = loginListener;
	}

	@Override
	protected Boolean doInBackground(Void... params) {

		try {
			boolean result = Service.login(mUserName, mPassword);
			return result;
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_LOGIN_ACTIVITY,
					"UserLoginTask - WebServiceException");
			JSONObject jObject = e.getJsonObject();

			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);

				mLoginListener.onError(errorCode);
			} catch (JSONException ignore) {
				Log.d(Constants.LOG_TAG_LOGIN_ACTIVITY,
						"UserLoginTask - Parsing the WebServiceException failed!");
			}
		} catch (ConnectException e) {
			Log.d(Constants.LOG_TAG_LOGIN_ACTIVITY,
					"UserLoginTask - ConnectException");
			mLoginListener.onError(ResponseError.DatabaseError);
		}
		return false;
	}

	@Override
	protected void onPostExecute(final Boolean success) {

		if (success)
			mLoginListener.onSuccess((Void) null);
		else
			mLoginListener.onError(ResponseError.UnknownError);
	}

	@Override
	protected void onCancelled() {

		mLoginListener.onCanceled();
	}
}
