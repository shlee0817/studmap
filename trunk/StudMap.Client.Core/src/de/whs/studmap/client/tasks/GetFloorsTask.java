package de.whs.studmap.client.tasks;

import java.net.ConnectException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.Floor;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import de.whs.studmap.client.listener.OnGetFloorsTaskListener;

public class GetFloorsTask extends AsyncTask<Void, Void, Integer> {

	private int mapId;
	private OnGetFloorsTaskListener mCallback;
	private List<Floor> mFloors;

	public GetFloorsTask(OnGetFloorsTaskListener listener, int mapId) {

		this.mapId = mapId;
		this.mCallback = listener;
	}

	@Override
	protected Integer doInBackground(Void... params) {

		try {
			mFloors = Service.getFloorsForMap(mapId);
			if(mFloors == null && mFloors.size() <= 0)
				return ResponseError.UnknownError;
			return ResponseError.None;
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"GetFloorsTask - WebserviceException");
			JSONObject jObject = e.getJsonObject();
			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				return  errorCode;
			} catch (JSONException ignore) {
				Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
						"GetNodeForQrCodeTask - Parsing the WebServiceException failed!");
				return ResponseError.UnknownError;
			}
		} catch (ConnectException e) {
			Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
					"GetFloorsTask - ConnectException");
			return ResponseError.ConnectionError;
		}
	}

	@Override
	protected void onPostExecute(final Integer result) {

		if (result == ResponseError.None) 
			mCallback.onGetFloorsSuccess(mFloors);
		else
			mCallback.onGetFloorsError(result);
	}

	@Override
	protected void onCancelled() {

		mCallback.onGetFloorsCanceled();
	}
}
