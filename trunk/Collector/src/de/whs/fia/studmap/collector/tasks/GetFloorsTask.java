package de.whs.fia.studmap.collector.tasks;

import java.net.ConnectException;
import java.util.List;

import android.os.AsyncTask;
import de.whs.studmap.client.core.data.Floor;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;

public class GetFloorsTask extends AsyncTask<Void, Void, List<Floor>> {

	private int mapId;
	
	public GetFloorsTask(int mapId){
		this.mapId = mapId;
	}
	
	@Override
	protected List<Floor> doInBackground(Void... params) {

		try {
			List<Floor> result = Service.getFloorsForMap(mapId); 
			return result;
		} catch (WebServiceException e) {
			e.printStackTrace();
		} catch (ConnectException e){
			e.printStackTrace();
		}	
		return null;
	}

}
