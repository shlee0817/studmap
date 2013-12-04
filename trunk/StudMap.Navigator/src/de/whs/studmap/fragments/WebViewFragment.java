package de.whs.studmap.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import de.whs.studmap.client.core.web.JavaScriptService;
import de.whs.studmap.navigator.R;

/**
 * Fragment that appears in the "MainFragment", shows the Map/Image
 */
@SuppressLint("SetJavaScriptEnabled")
public class WebViewFragment extends Fragment {

	private WebView mMapWebView;
	private JavaScriptService mJScriptService;
	
	private final String mUrl = "http://193.175.199.115/StudMapClient/Home/Floorplan";
	
	private int mMapId;
	private int mFloorId;
	
	
	public WebViewFragment() {
		// Empty constructor required for fragment subclasses
	}

	public static WebViewFragment newInstance(int mapId) {

		Bundle args = new Bundle();
		args.putInt("MapId", mapId);
		WebViewFragment f = new WebViewFragment();
		f.setArguments(args);
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		
		mMapWebView = (WebView) rootView.findViewById(R.id.map_web_view);
		mMapWebView.setWebViewClient(new WebViewClient());
		mMapWebView.getSettings().setJavaScriptEnabled(true);
		mMapWebView.addJavascriptInterface(getActivity(), "jsinterface");
		
		mJScriptService = new JavaScriptService(getActivity());
		mJScriptService.addWebView(mMapWebView);
		
		if(getArguments() != null && getArguments().containsKey("MapId"))
			this.mMapId = getArguments().getInt("MapId");
		
		if(this.mFloorId > 0){

			loadFloor();
		}
		

		return rootView;
	}
	
	public void setFloorId(int floorId) {
		this.mFloorId = floorId;
		loadFloor();
	}
	
	public void resetMap(){
		
		mJScriptService.resetMap();
	}
	
	public void sendTarget(int nodeId) {
		
		mJScriptService.sendTarget(nodeId);
	}
	
	public void sendStart(int nodeId) {
		mJScriptService.sendStart(nodeId);
	}
	
	public void sendDestination(int nodeId) {
		mJScriptService.sendDestination(nodeId);
	}
	
	private void loadFloor() {
		mMapWebView.loadUrl(mUrl + "/?mapId=" + mMapId + "&floorID="
				+ mFloorId);
		mMapWebView.requestFocus();
	}
}