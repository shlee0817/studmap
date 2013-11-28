package de.whs.studmap.client.core.web;

import android.app.Activity;
import android.webkit.WebView;

public class JavaScriptService {

	private Activity activity;
	private WebView mapView;
	
	public JavaScriptService(Activity activity){
		this.activity = activity;
	}
	
	public void addWebView(WebView webview){
		this.mapView = webview;
	}
	
	//JavaScript Funktionsaufruf: [WebView.loadURL("javaScript:Funktiosnname("param0", "param1",...)");]
		
	public void sendTarget(Integer nodeID){
		runOnUiThread("javascript:zoomOut()");
		runOnUiThread("javascript:highlightPointR(\"" + nodeID + "\", \"purple\", \"4\")");				
	}
	
	public void sendStart(Integer nodeID){
		runOnUiThread("javascript:setStartPoint(\"" + nodeID + "\")");
	}
	
	public void sendDestination(Integer nodeID){
		runOnUiThread("javascript:setEndPoint(\"" + nodeID + "\")");
	}
	
	public void resetMap(){
		runOnUiThread("javascript:resetMap()");
	}
	
	private void runOnUiThread(final String url){
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mapView.loadUrl(url);						
			}
		});
	}

}
