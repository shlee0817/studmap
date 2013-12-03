package de.whs.studmap.client.tasks;

import java.net.ConnectException;

import de.whs.studmap.client.core.data.Fingerprint;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import android.os.AsyncTask;

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
		} catch (WebServiceException e) {
			e.printStackTrace();
		} catch (ConnectException e){
			e.printStackTrace();
		}
		return false;
	}
}
