package de.whs.studmap.client.core.web;

import android.webkit.JavascriptInterface;

public interface JavaScriptInterface {
	
	@JavascriptInterface
	public void punkt(String nodeId);
	
	@JavascriptInterface
	public void onFinish();
	
	@JavascriptInterface
	public void onNavigationCompleted();
}
