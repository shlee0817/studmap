package de.whs.studmap.client.listener;

import de.whs.studmap.client.core.data.Node;

public interface OnGetNodeForNFCTagTaskListener {
	
	public void onGetNodeForNFCTagSuccess(Node node);

	public void onGetNodeForNFCTagError(int responseError);

	public void onGetNodeForNFCTagCanceled();
}
