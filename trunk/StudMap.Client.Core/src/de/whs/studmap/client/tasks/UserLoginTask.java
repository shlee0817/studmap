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

public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

	private String mUserName;
	private String mPassword;

	private OnGenericTaskListener<Void> mCallback;

	public UserLoginTask(OnGenericTaskListener<Void> loginListener,
			String userName, String password) {
		this.mUserName = userName;
		this.mPassword = password;
		this.mCallback = loginListener;
	}

	@Override
	protected Integer doInBackground(Void... params) {

		try {
			Service.login(mUserName, mPassword);
			return ResponseError.None;
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_LOGIN_DIALOG,
					"UserLoginTask - WebServiceException");
			JSONObject jObject = e.getJsonObject();

			try {
				Log.d(Constants.LOG_TAG_LOGIN_DIALOG,
						"UserLoginTask - WebserviceException");
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				return errorCode;
			} catch (JSONException ignore) {
				Log.e(Constants.LOG_TAG_LOGIN_DIALOG,
						"UserLoginTask - Parsing the WebServiceException failed!");
				return ResponseError.UnknownError;
			}
		} catch (ConnectException e) {
			Log.e(Constants.LOG_TAG_LOGIN_DIALOG,
					"UserLoginTask - ConnectException");
			 return ResponseError.ConnectionError;
		}
	}

	@Override
	protected void onPostExecute(final Integer result) {

		if (ResponseError.None == result )
			mCallback.onSuccess((Void) null);
		else
			mCallback.onError(result);
	}

	@Override
	protected void onCancelled() {

		mCallback.onCanceled();
	}
}
