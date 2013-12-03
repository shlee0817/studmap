package de.whs.studmap.client.listener;

public interface OnGenericTaskListener <TObject> {

	public void onSuccess(TObject object);
	
	public void onError(int responseError);
	
	public void onCanceled();
}
