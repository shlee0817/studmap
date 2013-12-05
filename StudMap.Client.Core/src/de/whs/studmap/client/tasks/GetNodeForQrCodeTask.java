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
import de.whs.studmap.client.listener.OnGetNodeForQrCodeTaskListener;

public class GetNodeForQrCodeTask extends AsyncTask<Void, Void, Integer> {

	private OnGetNodeForQrCodeTaskListener mCallback;
	private int mMapId;
	private String mQRCode;
	private Node mNode;

	public GetNodeForQrCodeTask(OnGetNodeForQrCodeTaskListener listener,
			int mapId, String qrCode) {
		this.mMapId = mapId;
		this.mQRCode = qrCode;
		this.mCallback = listener;
	}

	@Override
	protected Integer doInBackground(Void... params) {

		try {
			mNode = Service.getNodeForQRCode(mMapId, mQRCode);
			if (mNode == null)
				return ResponseError.UnknownError;
			
			return ResponseError.None;
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"GetNodeForQrCodeTask - WebServiceException");

			JSONObject jObject = e.getJsonObject();
			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				return errorCode;
			} catch (JSONException ignore) {
				Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
						"GetNodeForQrCodeTask - Parsing the WebServiceException failed!");
				return ResponseError.UnknownError;
			}
		} catch (ConnectException e) {
			Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
					"GetDataTask - ConnectException");
			 return ResponseError.ConnectionError;
		}
	}

	protected void onPostExecute(final Integer result) {

		if (result == ResponseError.None)
			mCallback.onGetNodeForQrCodeSuccess(mNode);
		else
			mCallback.onGetNodeForQrCodeError(result);
	}

	@Override
	protected void onCancelled() {
		mCallback.onGetNodeForQrCodeCanceled();
	}

}
