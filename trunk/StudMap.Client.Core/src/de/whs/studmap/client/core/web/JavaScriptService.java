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
		
	public void sendTarget(Integer nodeId){
		runOnUiThread("javascript:client.zoomToNode(" + nodeId + ")");
		runOnUiThread("javascript:client.highlightPoint(\"" + nodeId + "\", \"purple\", \"4\")");				
	}
	
	public void sendStart(Integer nodeId){
		runOnUiThread("javascript:client.setStartPoint(\"" + nodeId + "\")");
	}
	
	public void sendDestination(Integer nodeId){
		runOnUiThread("javascript:client.setEndPoint(\"" + nodeId + "\")");
	}
	
	public void resetMap(){
		runOnUiThread("javascript:client.resetMap()");
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
