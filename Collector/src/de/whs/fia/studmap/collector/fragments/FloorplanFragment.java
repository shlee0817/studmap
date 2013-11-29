package de.whs.fia.studmap.collector.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import de.whs.fia.studmap.collector.R;
import de.whs.fia.studmap.collector.dialogs.ChoicesDialogFragment;
import de.whs.fia.studmap.collector.dialogs.NFCReaderDialogFragment;
import de.whs.studmap.client.core.snippets.NFC;
import de.whs.studmap.client.core.web.JavaScriptInterface;
import de.whs.studmap.client.core.web.JavaScriptService;

public class FloorplanFragment extends Fragment implements JavaScriptInterface {

	private int mapId;
	private int floorId;
	private WebView webView;
	private JavaScriptService jsService;
	private View rootView;
	private ChoicesDialogFragment mChoicesDialog;

	public static FloorplanFragment newInstance(int mapId, int floorId) {

		FloorplanFragment f = new FloorplanFragment();
		Bundle args = new Bundle();
		args.putInt("mapId", floorId);
		args.putInt("floorId", floorId);

		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = (View) inflater.inflate(R.layout.fragment_flooplan,
				container, false);

		this.mapId = getArguments().getInt("mapId");
		this.floorId = getArguments().getInt("floorId");

		Toast.makeText(rootView.getContext(), "Lädt Stockwerk: " + floorId,
				Toast.LENGTH_SHORT).show();

		initializeWebView();

		return rootView;
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initializeWebView() {
		webView = (WebView) rootView.findViewById(R.id.map_web_view);
		webView.setWebViewClient(new WebViewClient());

		WebSettings settings = webView.getSettings();
		settings.setAppCacheMaxSize(1024 * 1024 * 8);
		settings.setJavaScriptEnabled(true);
		String appCachePath = getActivity().getApplicationContext()
				.getCacheDir().getAbsolutePath();
		settings.setAppCachePath(appCachePath);
		settings.setAllowFileAccess(true);
		settings.setAppCacheEnabled(true);

		webView.addJavascriptInterface(this, "jsinterface");

		jsService = new JavaScriptService(getActivity());
		jsService.addWebView(webView);

		webView.loadUrl("http://193.175.199.115/StudMapClient/Home/FloorPlan?mapId="
				+ mapId + "&floorID=" + floorId);
		webView.requestFocus();
	}

	public void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String nfcTag = NFC.BytesToHexString(tag.getId());

			if (mChoicesDialog != null && mChoicesDialog.nfcDialogIsOpen()) {

				mChoicesDialog.setNFCTagInDialog(nfcTag);

			} else {

				Bundle bundle = new Bundle();
				bundle.putString("TagUID", nfcTag);

				NFCReaderDialogFragment nfc = new NFCReaderDialogFragment();
				nfc.setArguments(bundle);
				nfc.show(getFragmentManager(), "NFC Dialog");
			}
		}
	}

	@Override
	@JavascriptInterface
	public void punkt(String nodeId) {

		Toast.makeText(getActivity(), "Punkt berührt: " + nodeId,
				Toast.LENGTH_SHORT).show();

		Bundle args = new Bundle();
		args.putString("NodeId", nodeId);

		if (mChoicesDialog == null)
			mChoicesDialog = new ChoicesDialogFragment();

		mChoicesDialog.setArguments(args);
		mChoicesDialog.show(getFragmentManager(), "Auswahl Dialog");
	}

	@Override
	@JavascriptInterface
	public void onFinish() {

		Log.e("", "JavaScriptInterface onFinish");
	}
}
