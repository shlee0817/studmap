package de.whs.studmap.web;

import android.webkit.WebView;

public class JavaScriptService {

	private WebView mapView;
	
	public JavaScriptService(WebView webView){
		this.mapView = webView;
	}
	
	//JavaScript Funktionsaufruf: [WebView.loadURL("javaScript:Funktiosnname(param0, param1,...)");]
	
	public void sendTarget(int nodeID){
		//mapView.loadUrl("javascript:");
	}
	
	public void sendStart(int nodeID){
		//mapView.loadUrl("javascript:");
	}
	
	public void sendDestination(int nodeID){
		//mapView.loadUrl("javascript:");
	}
	
	public void resetMap(int mapID){
		//mapView.loadUrl("javascript:");
	}
}
