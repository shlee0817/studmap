package de.whs.fia.studmap.collector.fragments;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import de.whs.fia.studmap.collector.R;
import de.whs.studmap.client.core.web.JavaScriptInterface;
import de.whs.studmap.client.core.web.JavaScriptService;

public class FloorplanFragment extends Fragment {

	private int floorId;
	private WebView webView;
	private View rootView;
	private JavaScriptService jsService;

	public FloorplanFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		this.floorId = getArguments().getInt("floorId");
		rootView = (View) inflater.inflate(R.layout.fragment_flooplan,
				container, false);

		Toast.makeText(rootView.getContext(), "Lädt Stockwerk: "
				+ floorId, 1).show();

		initializeWebView();

		return rootView;
	}

	private void initializeWebView() {
		webView = (WebView) rootView.findViewById(R.id.map_web_view);
		webView.setWebViewClient(new WebViewClient());

		JavaScriptInterface jsInterface = (JavaScriptInterface) new JavaScriptInterfaceImpl(
				rootView.getContext());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(jsInterface, "jsinterface");

		jsService = new JavaScriptService(getActivity());
		jsService.addWebView(webView);

		webView.loadUrl("http://193.175.199.115/StudMapClient/?floorID="
				+ floorId);
		webView.requestFocus();
	}

	public void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			// mNfcTag = bytesToHexString(tag.getId());
		}
	}
}
