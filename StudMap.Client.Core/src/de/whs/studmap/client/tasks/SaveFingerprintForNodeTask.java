package de.whs.studmap.client.tasks;

import java.net.ConnectException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.Fingerprint;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;

public class SaveFingerprintForNodeTask extends AsyncTask<Void, Void, Boolean> {

	private int nodeId;
	private Fingerprint fingerprint;
	
	public SaveFingerprintForNodeTask(int nodeId, Fingerprint fingerprint){
		this.nodeId = nodeId;
		this.fingerprint = fingerprint;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		
		try {
			boolean result = Service.SaveFingerprintForNode(nodeId, fingerprint); 
			return result;
		} catch (ConnectException e) {
			Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
					"SaveFingerprintForNodeTask - ConnectException");
		} catch (WebServiceException e) {
			Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
					"SaveFingerprintForNodeTask - WebServiceException");
			JSONObject jObject = e.getJsonObject();
			try {
				int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
						"SaveFingerprintForNodeTask - " + errorCode);
			
			} catch (JSONException ignore) {
				Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
						"SaveFingerprintForNodeTask - Parsing the WebServiceException failed!");
			}
		}
		return false;
	}
}
