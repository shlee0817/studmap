package de.whs.studmap.client.tasks;

import java.net.ConnectException;

import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import android.os.AsyncTask;

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
		} catch (WebServiceException e) {
			e.printStackTrace();
		} catch (ConnectException e) {
			e.printStackTrace();
		}
		return false;
	}
}
