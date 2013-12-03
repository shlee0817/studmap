package de.whs.studmap.client.listener;

import java.util.List;

import de.whs.studmap.client.core.data.Floor;

public interface OnGetFloorsTaskListener {

	public void onGetFloorsSuccess(List<Floor> floors);
	
	public void onGetFloorsError(int responseError);
	
	public void onGetFloorsCanceled();
}

