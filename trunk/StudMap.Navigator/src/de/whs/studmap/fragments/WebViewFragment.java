package de.whs.studmap.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.whs.studmap.client.core.data.Node;
import de.whs.studmap.client.core.web.JavaScriptService;
import de.whs.studmap.navigator.R;

/**
 * Fragment that appears in the "MainFragment", shows the Map/Image
 */
@SuppressLint("SetJavaScriptEnabled")
public class WebViewFragment extends Fragment {

	private WebView mMapWebView;
	private JavaScriptService mJScriptService;

	private final String mUrl = "http://193.175.199.115/StudMapClient"; //$NON-NLS-1$

	private int mMapId;
	private int mFloorId;

	private RelativeLayout mNavigationInfo;
	private ImageButton mCloseNavigation;
	private TextView mNodeText;
	private TextView mFromText;
	private TextView mToText;

	public WebViewFragment() {
		// Empty constructor required for fragment subclasses
	}

	public static WebViewFragment newInstance(int mapId) {

		Bundle args = new Bundle();
		args.putInt("MapId", mapId); //$NON-NLS-1$
		WebViewFragment f = new WebViewFragment();
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_webview, container,
				false);

		mMapWebView = (WebView) rootView.findViewById(R.id.map_web_view);

		mMapWebView.setVerticalScrollBarEnabled(false);
		mMapWebView.setHorizontalScrollBarEnabled(false);

		mMapWebView.setWebViewClient(new WebViewClient());
		mMapWebView.getSettings().setJavaScriptEnabled(true);
		mMapWebView.addJavascriptInterface(getActivity(), "jsinterface"); //$NON-NLS-1$
		
		mJScriptService = new JavaScriptService(getActivity());
		mJScriptService.addWebView(mMapWebView);

		if (getArguments() != null && getArguments().containsKey("MapId")) //$NON-NLS-1$
			this.mMapId = getArguments().getInt("MapId"); //$NON-NLS-1$

		loadNavigationInfoOverlay(rootView);

		return rootView;
	}
	
	private void loadNavigationInfoOverlay(View rootView) {

		mFromText = (TextView) rootView.findViewById(R.id.NavigationFrom);
		mToText = (TextView) rootView.findViewById(R.id.NavigationTo);
		mNodeText = (TextView) rootView.findViewById(R.id.NodeInfo);

		mNavigationInfo = (RelativeLayout) rootView
				.findViewById(R.id.NavigationInfoLayout);

		mNavigationInfo.setVisibility(View.GONE);

		mCloseNavigation = (ImageButton) rootView
				.findViewById(R.id.NavigationInfoCloseButton);
		mCloseNavigation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mNavigationInfo.setVisibility(View.GONE);
				mNodeText.setText("");
				mFromText.setText("");
				mToText.setText("");
				resetMap();
			}
		});
	}

	public void setFloorId(int floorId) {
		this.mFloorId = floorId;
		loadFloor();
		mMapWebView.requestFocus();
	}

	public void resetMap() {

		mJScriptService.resetMap();
		mMapWebView.requestFocus();
	}

	public void sendTarget(Node node) {

		mJScriptService.sendTarget(node.getNodeID());
		mMapWebView.requestFocus();

		mNavigationInfo.setVisibility(View.VISIBLE);
		mToText.setVisibility(View.GONE);
		mFromText.setVisibility(View.GONE);
		mNodeText.setVisibility(View.VISIBLE);

		mNodeText.setText(node.getDisplayName());
	}

	public void sendStart(Node node) {
		
		mJScriptService.sendStart(node.getNodeID());
		mMapWebView.requestFocus();

		mNavigationInfo.setVisibility(View.VISIBLE);
		mToText.setVisibility(View.VISIBLE);
		mFromText.setVisibility(View.VISIBLE);
		mNodeText.setVisibility(View.GONE);

		mFromText.setText(Messages.getString("WebViewFragment.From")
				+ node.getDisplayName());
	}

	public void sendDestination(Node node) {
		mJScriptService.sendDestination(node.getNodeID());
		mMapWebView.requestFocus();

		mNavigationInfo.setVisibility(View.VISIBLE);
		mToText.setVisibility(View.VISIBLE);
		mFromText.setVisibility(View.VISIBLE);
		mNodeText.setVisibility(View.GONE);

		mToText.setText(Messages.getString("WebViewFragment.To")
				+ node.getDisplayName());
	}

	public void reloadMap() {

		mMapWebView.reload();
	}

	private void loadFloor() {

		if (mMapId > 0 && mFloorId > 0) {

			mMapWebView.loadUrl(mUrl + "/?mapId=" + mMapId + "&floorID="
					+ mFloorId);
			mMapWebView.requestFocus();
		}
	}
}