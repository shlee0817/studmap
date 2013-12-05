package de.whs.studmap.client.tasks;

import java.net.ConnectException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.Map;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import de.whs.studmap.client.listener.OnGenericTaskListener;

public class GetMapsTask extends AsyncTask<Void, Void, Integer> {

	private OnGenericTaskListener<List<Map>> mCallback;
	private List<Map> mMaps;

	public GetMapsTask(OnGenericTaskListener<List<Map>> listener) {

		this.mCallback = listener;
	}

	@Override
	protected Integer doInBackground(Void... params) {

		try {
			mMaps = Service.getMaps();
			if (mMaps == null || mMaps.size() == 0){
				return ResponseError.UnknownError;
			}
			return ResponseError.None;
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"GetMapsTask - WebServiceException");

			JSONObject jObject = e.getJsonObject();
			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				return errorCode;
			} catch (JSONException ignore) {
				Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
						"GetMapsTask - Parsing the WebServiceException failed!");
				return ResponseError.UnknownError;
			}
		} catch (ConnectException e) {
			Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
					"GetMapsTask - ConnectException");
			return ResponseError.ConnectionError;
		}
	}

	@Override
	protected void onPostExecute(final Integer result) {

		if (result == ResponseError.None) 
			mCallback.onSuccess(mMaps);
		else
			mCallback.onError(result);
	}

	@Override
	protected void onCancelled() {

		mCallback.onCanceled();
	}
}