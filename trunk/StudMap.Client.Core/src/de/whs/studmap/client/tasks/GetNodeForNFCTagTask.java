package de.whs.studmap.client.tasks;

import java.net.ConnectException;

import org.json.JSONException;
import org.json.JSONObject;

import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.Node;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import de.whs.studmap.client.listener.OnGetNodeForNFCTagTaskListener;
import android.os.AsyncTask;
import android.util.Log;

public class GetNodeForNFCTagTask extends AsyncTask<Void, Void, Boolean> {

	private int mapId;
	private String nfcTag;
	private Node mNode;
	private OnGetNodeForNFCTagTaskListener mCallback;

	public GetNodeForNFCTagTask(OnGetNodeForNFCTagTaskListener listener, int mapId, String nfcTag){
		this.mapId = mapId;
		this.nfcTag = nfcTag;
		this.mCallback = listener;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {

		try {
			mNode = Service.getNodeForNFCTag(mapId, nfcTag);
			if(mNode == null)
				return false;
			return true;
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WebServiceException e) {
			
			JSONObject jObject = e.getJsonObject();
			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				mCallback.onGetNodeForNFCTagError(errorCode);
			} catch (JSONException ignore) {
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
						"GetNodeForQrCodeTask - Parsing the WebServiceException failed!");
			}
		}
		return false;
	}
	
	@Override
	protected void onPostExecute(final Boolean success) {

		if (success) {
			mCallback.onGetNodeForNFCTagSuccess(mNode);
		} else {
			mCallback.onGetNodeForNFCTagError(ResponseError.UnknownError);
		}
	}

	@Override
	protected void onCancelled() {
		mCallback.onGetNodeForNFCTagCanceled();
	}
}
