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

public class GetNodeForQrCodeTask extends AsyncTask<Void, Void, Boolean> {

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
	protected Boolean doInBackground(Void... params) {

		try {
			mNode = Service.getNodeForQRCode(mMapId, mQRCode);
			if (mNode != null)
				return true;
			return false;
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"GetNodeForQrCodeTask - WebServiceException");

			JSONObject jObject = e.getJsonObject();
			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				mCallback.onGetNodeForQrCodeError(errorCode);
			} catch (JSONException ignore) {
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
						"GetNodeForQrCodeTask - Parsing the WebServiceException failed!");
			}
		} catch (ConnectException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"GetDataTask - ConnectException");
			mCallback.onGetNodeForQrCodeError(ResponseError.UnknownError);
		}
		return false;
	}

	@Override
	protected void onPostExecute(final Boolean success) {

		if (success) {
			mCallback.onGetNodeForQrCodeSuccess(mNode);
		} else {
			mCallback.onGetNodeForQrCodeError(ResponseError.UnknownError);
		}
	}

	@Override
	protected void onCancelled() {
		mCallback.onGetNodeForQrCodeCanceled();
	}

}
