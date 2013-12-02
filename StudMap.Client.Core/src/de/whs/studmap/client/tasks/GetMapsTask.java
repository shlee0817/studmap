package de.whs.studmap.client.tasks;

import java.net.ConnectException;
import java.util.List;


import de.whs.studmap.client.core.data.Map;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;

import android.os.AsyncTask;

public class GetMapsTask extends AsyncTask<Void, Void, List<Map>> {
	
	@Override
	protected List<Map> doInBackground(Void... params) {

		try {
			List<Map> result = Service.getMaps(); 
			return result;
		} catch (WebServiceException e) {
			e.printStackTrace();
		} catch (ConnectException e){
			e.printStackTrace();
		}		
		return null;
	}

}
