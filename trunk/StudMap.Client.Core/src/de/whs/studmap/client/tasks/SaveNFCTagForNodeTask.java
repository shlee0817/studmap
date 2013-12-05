package de.whs.studmap.client.tasks;

import java.net.ConnectException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;

public class SaveNFCTagForNodeTask extends AsyncTask<Void, Void, Boolean> {

	private int nodeId;
	private String nfcTag;

	public SaveNFCTagForNodeTask(int nodeId, String nfcTag) {
		this.nodeId = nodeId;
		this.nfcTag = nfcTag;
	}

	@Override
	protected Boolean doInBackground(Void... params) {

		try {
			boolean result = Service.SaveNFCTagForNode(nodeId, nfcTag);
			return result;
		} catch (ConnectException e) {
			Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
					"SaveNFCTagForNodeTask - ConnectException");
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"SaveNFCTagForNodeTask - WebServiceException");
			JSONObject jObject = e.getJsonObject();
			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
						"SaveNFCTagForNodeTask - " + errorCode);
			
			} catch (JSONException ignore) {
				Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
						"SaveNFCTagForNodeTask - Parsing the WebServiceException failed!");
			}
		}
		return false;
	}
}
