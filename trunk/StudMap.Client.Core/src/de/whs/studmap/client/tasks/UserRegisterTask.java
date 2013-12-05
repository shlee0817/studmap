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

public class UserRegisterTask extends AsyncTask<Void, Void, Integer> {

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
	protected Integer doInBackground(Void... params) {

		try {
			Service.register(mUsername, mPassword);
			return ResponseError.None;
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_REGISTER_DIALOG,
					"UserRegisterTask - WebServiceException");
			JSONObject jObject = e.getJsonObject();

			try {
				Log.d(Constants.LOG_TAG_REGISTER_DIALOG,
						"UserRegisterTask - WebServiceException");
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				return errorCode;
				
			} catch (JSONException ignore) {
				Log.e(Constants.LOG_TAG_REGISTER_DIALOG,
						"UserRegisterTask - Parsing the WebServiceException failed!");
				return ResponseError.UnknownError;
			}
		} catch (ConnectException e) {
			Log.e(Constants.LOG_TAG_REGISTER_DIALOG,
					"UserRegisterTask - ConnectException");
			return ResponseError.ConnectionError;
		}
	}

	@Override
	protected void onPostExecute(final Integer result) {

		 if (result == ResponseError.None) 
			 mCallback.onSuccess((Void)null);
		 else
			 mCallback.onError(result);
	}

	@Override
	protected void onCancelled() {
		
		mCallback.onCanceled();
	}

}
