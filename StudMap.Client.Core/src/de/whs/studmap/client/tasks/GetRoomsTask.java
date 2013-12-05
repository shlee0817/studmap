package de.whs.studmap.client.tasks;

import java.net.ConnectException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.Node;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import de.whs.studmap.client.listener.OnGetRoomsTaskListener;

public class GetRoomsTask extends AsyncTask<Void, Void, Integer> {

	private int mMapId;
	private OnGetRoomsTaskListener mCallback;
	private List<Node> mNodes;

	public GetRoomsTask(OnGetRoomsTaskListener listener, int mapId) {

		this.mCallback = listener;
		this.mMapId = mapId;
	}

	@Override
	protected Integer doInBackground(Void... params) {

		try {
			mNodes = Service.getRoomsForMap(mMapId);
			if (mNodes == null || mNodes.size() == 0)
				return ResponseError.UnknownError;
			return ResponseError.None;
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"GetRoomsTask - WebServiceException");

			JSONObject jObject = e.getJsonObject();
			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				return errorCode;
			} catch (JSONException ignore) {
				Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
						"GetRoomsTask - Parsing the WebServiceException failed!");
				return ResponseError.UnknownError;
			}
		} catch (ConnectException e) {
			Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
					"GetRoomsTask - ConnectException");
			 return ResponseError.ConnectionError;
		}
	}

	protected void onPostExecute(final Integer result) {

		if (result == ResponseError.None) 
			mCallback.onGetRoomsSuccess(mNodes);
		else
			mCallback.onGetRoomsError(result);
	}

	@Override
	protected void onCancelled() {

		mCallback.onGetRoomsCanceled();
	}
}
