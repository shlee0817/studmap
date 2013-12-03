package de.whs.studmap.client.tasks;

import java.net.ConnectException;
import java.util.List;

import android.os.AsyncTask;
import de.whs.studmap.client.core.data.Floor;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import de.whs.studmap.client.listener.OnGetFloorsTaskListener;

public class GetFloorsTask extends AsyncTask<Void, Void, Boolean> {

	private int mapId;
	private OnGetFloorsTaskListener mCallback;
	private List<Floor> mFloors;

	public GetFloorsTask(OnGetFloorsTaskListener listener, int mapId) {

		this.mapId = mapId;
		this.mCallback = listener;
	}

	@Override
	protected Boolean doInBackground(Void... params) {

		try {
			mFloors = Service.getFloorsForMap(mapId);
			if(mFloors != null && mFloors.size() > 0)
				return true;
			return false;
		} catch (WebServiceException e) {
			e.printStackTrace();
		} catch (ConnectException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onPostExecute(final Boolean success) {

		if (success) {
			mCallback.onGetFloorsSuccess(mFloors);
		} else {
			mCallback.onGetFloorsError(ResponseError.UnknownError);
		}
	}

	@Override
	protected void onCancelled() {

		mCallback.onGetFloorsCanceled();
	}
}
