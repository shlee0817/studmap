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

public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

	private String mUsername;
	private String mPassword;
	private OnGenericTaskListener<Void> mCallback;

	public UserRegisterTask(OnGenericTaskListener<Void> listener,
			String userName, String password) {

		this.mUsername = userName;
		this.mPassword = password;
		this.mCallback = listener;
	}

	@Override
	protected Boolean doInBackground(Void... params) {

		try {
			boolean result = Service.register(mUsername, mPassword);
			if (result)
				return result;
			else
				throw new ConnectException();
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_REGISTER_ACTIVITY,
					"UserRegisterTask - WebServiceException");
			JSONObject jObject = e.getJsonObject();

			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				mCallback.onError(errorCode);
				
			} catch (JSONException ignore) {
				Log.d(Constants.LOG_TAG_REGISTER_ACTIVITY,
						"UserRegisterTask - Parsing the WebServiceException failed!");
			}
		} catch (ConnectException e) {
			Log.d(Constants.LOG_TAG_REGISTER_ACTIVITY,
					"UserRegisterTask - ConnectException");
			mCallback.onError(ResponseError.UnknownError);
		}
		return false;
	}

	@Override
	protected void onPostExecute(final Boolean success) {

		 if (success) {
			 mCallback.onSuccess(null);
		 }
		 else{
			 mCallback.onError(ResponseError.UnknownError);
		 }
	}

	@Override
	protected void onCancelled() {
		
		mCallback.onCanceled();
	}

}
