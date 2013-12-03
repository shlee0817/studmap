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

public class GetPoITask extends AsyncTask<Void, Void, Boolean> implements
		Constants {

	private int mapId;
	private List<PoI> mPoIs = new ArrayList<PoI>();
	private OnGenericTaskListener<List<PoI>> mCallback;

	public GetPoITask(OnGenericTaskListener<List<PoI>> listener, int mapId) {
		this.mapId = mapId;
		this.mCallback = listener;
	}

	@Override
	protected Boolean doInBackground(Void... params) {

		try {
			List<PoI> pois = Service.getPoIsForMap(mapId);
			if (pois != null)
				mPoIs.addAll(pois);
			return true;
		} catch (WebServiceException e) {

			Log.d(LOG_TAG_POI__ACTIVITY, "GetDataTask - WebServiceException");
			JSONObject jObject = e.getJsonObject();

			try {

				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				mCallback.onError(errorCode);
			} catch (JSONException ignore) {
				Log.d(LOG_TAG_POI__ACTIVITY,
						"GetDataTask - Parsing the WebServiceException failed!");
			}
		} catch (ConnectException e) {

			mCallback.onError(ResponseError.DatabaseError);
		}
		return false;
	}

	@Override
	protected void onPostExecute(final Boolean success) {

		if (success) {
			mCallback.onSuccess(mPoIs);
		} else {
			mCallback.onError(ResponseError.UnknownError);
		}
	}

	@Override
	protected void onCancelled() {

		mCallback.onCanceled();
	}

}
