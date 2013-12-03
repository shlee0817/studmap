package de.whs.studmap.client.listener;

import de.whs.studmap.client.core.data.Node;

public interface OnGetNodeForQrCodeTaskListener {

	public void onGetNodeForQrCodeSuccess(Node node);

	public void onGetNodeForQrCodeError(int responseError);

	public void onGetNodeForQrCodeCanceled();
}
