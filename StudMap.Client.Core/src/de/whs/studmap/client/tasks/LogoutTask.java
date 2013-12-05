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
import de.whs.studmap.client.listener.OnLogoutTaskListener;

public class LogoutTask extends AsyncTask<Void, Void, Integer> {

	private String mUserName;
	private OnLogoutTaskListener mCallback;

	public LogoutTask(OnLogoutTaskListener listener, String userName) {

		this.mCallback = listener;
		this.mUserName = userName;
	}

	@Override
	protected Integer doInBackground(Void... params) {

		try {
			Service.logout(mUserName);
			return ResponseError.None;
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"LogoutTask - WebServiceException");

			JSONObject jObject = e.getJsonObject();
			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				return errorCode;
			} catch (JSONException ignore) {
				Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
						"LogoutTask - Parsing the WebServiceException failed!");
				return ResponseError.UnknownError;
			}
		} catch (ConnectException e) {
			Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
					"LogoutTask - ConnectException");
			return ResponseError.ConnectionError;
		}
	}

	protected void onPostExecute(final Integer result) {

		if (result == ResponseError.None) 
			mCallback.onLogoutSuccess();
		else
			mCallback.onLogoutError(result);
	}

	@Override
	protected void onCancelled() {
		mCallback.onLogoutCanceled();
	}

}
