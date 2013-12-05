package de.whs.studmap.client.tasks;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.PoI;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import de.whs.studmap.client.listener.OnGenericTaskListener;

public class GetPoITask extends AsyncTask<Void, Void, Integer> implements
		Constants {

	private int mapId;
	private List<PoI> mPoIs = new ArrayList<PoI>();
	private OnGenericTaskListener<List<PoI>> mCallback;

	public GetPoITask(OnGenericTaskListener<List<PoI>> listener, int mapId) {
		this.mapId = mapId;
		this.mCallback = listener;
	}

	@Override
	protected Integer doInBackground(Void... params) {

		try {
			List<PoI> pois = Service.getPoIsForMap(mapId);
			if (pois == null)
				return ResponseError.UnknownError;

			mPoIs.addAll(pois);
			return ResponseError.None;
		} catch (WebServiceException e) {
			Log.d(LOG_TAG_POI__ACTIVITY, "GetDataTask - WebServiceException");
			JSONObject jObject = e.getJsonObject();

			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				return errorCode;
			} catch (JSONException ignore) {
				Log.e(LOG_TAG_POI__ACTIVITY,
						"GetDataTask - Parsing the WebServiceException failed!");
				return ResponseError.UnknownError;
			}
		} catch (ConnectException e) {
			Log.e(LOG_TAG_POI__ACTIVITY, "GetDataTask - ConnectException");
			 return ResponseError.ConnectionError;
		}
	}

	@Override
	protected void onPostExecute(final Integer result) {

		if (result == ResponseError.None) 
			mCallback.onSuccess(mPoIs);
		else
			mCallback.onError(result);
	}

	@Override
	protected void onCancelled() {

		mCallback.onCanceled();
	}

}
