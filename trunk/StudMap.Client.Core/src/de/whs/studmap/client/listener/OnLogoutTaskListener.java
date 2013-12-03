package de.whs.studmap.client.listener;

public interface OnLogoutTaskListener {

	public void onLogoutSuccess();

	public void onLogoutError(int responseError);

	public void onLogoutCanceled();
}
