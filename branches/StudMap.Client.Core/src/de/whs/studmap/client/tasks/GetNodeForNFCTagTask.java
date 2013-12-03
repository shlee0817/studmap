package de.whs.studmap.client.tasks;

import java.net.ConnectException;

import de.whs.studmap.client.core.data.Node;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import android.os.AsyncTask;

public class GetNodeForNFCTagTask extends AsyncTask<Void, Void, Node> {

	private int mapId;
	private String nfcTag;

	public GetNodeForNFCTagTask(int mapId, String nfcTag){
		this.mapId = mapId;
		this.nfcTag = nfcTag;
	}
	
	@Override
	protected Node doInBackground(Void... params) {

		try {
			Node n = Service.getNodeForNFCTag(mapId, nfcTag);
			return n;
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WebServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
