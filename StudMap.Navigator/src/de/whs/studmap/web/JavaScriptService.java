package de.whs.studmap.web;

import android.webkit.WebView;

public class JavaScriptService {

	private WebView mapView;
	
	public JavaScriptService(WebView webView){
		this.mapView = webView;
	}
	
	//JavaScript Funktionsaufruf: [WebView.loadURL("javaScript:Funktiosnname(param0, param1,...)");]
	
	//TODO: mapView / webView Änderung dürfen nur im den WebViewThread gemacht werden, sonst Exception
	
	public void sendTarget(int nodeID){
		mapView.loadUrl("javascript:highlightPoint(" + nodeID + ", yellow)");
	}
	
	public void sendStart(int nodeID){
		mapView.loadUrl("javascript:setStartPunkt(" + nodeID + ")");
	}
	
	public void sendDestination(int nodeID){
		mapView.loadUrl("javascript:setEndPunkt(" + nodeID + ")");
	}
	
	public void resetMap(){
		mapView.loadUrl("javascript:resetMap()");
	}
}
