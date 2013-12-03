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

public class GetNodeInfoTask extends AsyncTask<Void, Void, Void> {

	private Node mNode;
	private OnGenericTaskListener<Node> mCallback;
	private int mNodeId;

	public GetNodeInfoTask(OnGenericTaskListener<Node> listener, int nodeId) {
		this.mCallback = listener;
		this.mNodeId = nodeId;
	}

	@Override
	protected Void doInBackground(Void... params) {

		try {
			mNode = Service.getNodeInformationForNode(mNodeId);
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_POSITION_ACTIVITY,
					"GetNodeInfoTask - WebServiceException");
			JSONObject jObject = e.getJsonObject();

			try {
				
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				mCallback.onError(errorCode);
				
			} catch (JSONException ignore) {
				Log.d(Constants.LOG_TAG_POSITION_ACTIVITY,
						"GetNodeInfoTask - Parsing the WebServiceException failed!");
			}
		} catch (ConnectException e) {
			Log.d(Constants.LOG_TAG_POSITION_ACTIVITY,
					"GetNodeInfoTask - ConnectException");

			mCallback.onError(ResponseError.UnknownError);
		}
		return null;
	}

	@Override
	protected void onPostExecute(final Void param){

		mCallback.onSuccess(mNode);
	}

	//
	// @Override
	// protected void onPostExecute(final Node node) {
	//
	// // mAsynTask = null;
	// // showProgress(false);
	// //
	// // if (node != null) {
	// // StringBuilder sb = new StringBuilder();
	// // sb.append(node.getDisplayName());
	// // sb.append("\n");
	// // sb.append(getString(R.string.navigationTitle));
	// // mPositionQuestion.setText(sb.toString());
	// // } else {
	// // if (bShowDialog)
	// // UserInfo.dialog(mContext, mUsername,
	// // getString(R.string.error_connection));
	// // }
	// }

	@Override
	protected void onCancelled() {

		mCallback.onCanceled();
	}
}
