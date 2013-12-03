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

public class GetRoomsTask extends AsyncTask<Void, Void, Boolean> {

	private int mMapId;
	private OnGetRoomsTaskListener mCallback;
	private List<Node> mNodes;

	public GetRoomsTask(OnGetRoomsTaskListener listener, int mapId) {

		this.mCallback = listener;
		this.mMapId = mapId;
	}

	@Override
	protected Boolean doInBackground(Void... params) {

		try {
			mNodes = Service.getRoomsForMap(mMapId);
			if (mNodes == null || mNodes.size() == 0)
				return false;
			return true;
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"GetDataTask - WebServiceException");

			JSONObject jObject = e.getJsonObject();
			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				mCallback.onGetRoomsError(errorCode);
			} catch (JSONException ignore) {
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
						"GetDataTask - Parsing the WebServiceException failed!");
			}
		} catch (ConnectException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"GetDataTask - ConnectException");
			mCallback.onGetRoomsError(ResponseError.UnknownError);
		}
		return false;
	}

	@Override
	protected void onPostExecute(final Boolean success) {

		if (success) {
			mCallback.onGetRoomsSuccess(mNodes);
		} else {
			mCallback.onGetRoomsError(ResponseError.UnknownError);
		}
	}

	@Override
	protected void onCancelled() {

		mCallback.onGetRoomsCanceled();
	}
}
