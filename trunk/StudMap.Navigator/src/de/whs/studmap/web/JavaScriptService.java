package de.whs.studmap.web;

import android.app.Activity;
import android.util.Log;
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
	
	//JavaScript Funktionsaufruf: [WebView.loadURL("javaScript:Funktiosnname(param0, param1,...)");]
		
	public void sendTarget(int nodeID){
		runOnUiThread("javascript:highlightPoint(" + nodeID + ", yellow)");				
	}
	
	public void sendStart(final int nodeID){
		runOnUiThread("javascript:setStartPoint(" + nodeID + ")");
	}
	
	public void sendDestination(int nodeID){
		runOnUiThread("javascript:setEndPoint(" + nodeID + ")");
	}
	
	public void resetMap(){
		runOnUiThread("javascript:resetMap()");
	}
	
	private void runOnUiThread(final String url){
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Log.d("TEST",  "hierher");
				mapView.loadUrl(url);						
			}
		});
	}

}
