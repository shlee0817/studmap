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

public class LogoutTask extends AsyncTask<Void, Void, Boolean> {

	private String mUserName;
	private OnLogoutTaskListener mCallback;

	public LogoutTask(OnLogoutTaskListener listener, String userName) {

		this.mCallback = listener;
		this.mUserName = userName;
	}

	@Override
	protected Boolean doInBackground(Void... params) {

		try {
			boolean success = Service.logout(mUserName);
			return success;
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"LogoutTask - WebServiceException");

			JSONObject jObject = e.getJsonObject();
			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				mCallback.onLogoutError(errorCode);
			} catch (JSONException ignore) {
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
						"LogoutTask - Parsing the WebServiceException failed!");
			}
		} catch (ConnectException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"LogoutTask - ConnectException");
			mCallback.onLogoutError(ResponseError.UnknownError);
		}
		return false;
	}

	@Override
	protected void onPostExecute(final Boolean success) {

		if (success) {
			mCallback.onLogoutSuccess();
		} else
			mCallback.onLogoutError(ResponseError.UnknownError);
	}

	@Override
	protected void onCancelled() {
		mCallback.onLogoutCanceled();
	}

}
