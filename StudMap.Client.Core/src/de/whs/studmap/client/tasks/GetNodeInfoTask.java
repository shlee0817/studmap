package de.whs.studmap.client.tasks;

import java.net.ConnectException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.Node;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import de.whs.studmap.client.listener.OnGenericTaskListener;

public class GetNodeInfoTask extends AsyncTask<Void, Void, Integer> {

	private Node mNode;
	private OnGenericTaskListener<Node> mCallback;
	private int mNodeId;

	public GetNodeInfoTask(OnGenericTaskListener<Node> listener, int nodeId) {
		this.mCallback = listener;
		this.mNodeId = nodeId;
	}

	@Override
	protected Integer doInBackground(Void... params) {

		try {
			mNode = Service.getNodeInformationForNode(mNodeId);
			return ResponseError.None;
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_POSITION_ACTIVITY,
					"GetNodeInfoTask - WebServiceException");
			JSONObject jObject = e.getJsonObject();

			try {				
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				return errorCode;
				
			} catch (JSONException ignore) {
				Log.e(Constants.LOG_TAG_POSITION_ACTIVITY,
						"GetNodeInfoTask - Parsing the WebServiceException failed!");
				return ResponseError.UnknownError;
			}
		} catch (ConnectException e) {
			Log.e(Constants.LOG_TAG_POSITION_ACTIVITY,
					"GetNodeInfoTask - ConnectException");

			return ResponseError.ConnectionError;
		}
	}

	@Override
	protected void onPostExecute(final Integer result){
		if (result == ResponseError.None)
			mCallback.onSuccess(mNode);			
		else
			mCallback.onError(result);
	}

	@Override
	protected void onCancelled() {

		mCallback.onCanceled();
	}
}
