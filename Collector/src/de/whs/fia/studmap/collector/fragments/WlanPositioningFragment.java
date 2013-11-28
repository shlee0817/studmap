package de.whs.fia.studmap.collector.fragments;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.whs.fia.studmap.collector.R;
import de.whs.studmap.client.core.data.LocationAPScan;
import de.whs.studmap.client.core.data.LocationRequest;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;

public class WlanPositioningFragment extends Fragment {

	private WifiReceiver wifiReceiver;
	private WifiManager wifiManager;

	private ProgressDialog pDialog;

	private View rootView;

	public WlanPositioningFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_wlan_positioning,
				container, false);

		wifiManager = (WifiManager) this.getActivity().getSystemService(
				Context.WIFI_SERVICE);

		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}

		wifiReceiver = new WifiReceiver();
		getActivity().registerReceiver(wifiReceiver,
				new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		pDialog = new ProgressDialog(getActivity());
		pDialog.setMessage("Scanning...");
		pDialog.setCancelable(false);

		Button position = (Button) rootView
				.findViewById(R.id.wlanPositioningFindMe);
		position.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				pDialog.show();
				wifiManager.startScan();
			}
		});

		return rootView;
	}

	public void onPause() {
		getActivity().unregisterReceiver(wifiReceiver);
		super.onPause();
	}

	public void onResume() {
		getActivity().registerReceiver(wifiReceiver,
				new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	class WifiReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {

			LocationRequest locRequest = new LocationRequest();

			List<ScanResult> wifiList = wifiManager.getScanResults();

			// TODO: MapId auslesen
			locRequest.setMapId(3);
			locRequest.setNodeCount(10);

			for (ScanResult res : wifiList) {
				LocationAPScan scan = new LocationAPScan();
				scan.setMAC(res.BSSID.replaceAll(":", ""));
				scan.setRSS(res.level);
				locRequest.addScan(scan);
			}

			GetNodeForFingerprintTask task = new GetNodeForFingerprintTask(
					locRequest);
			task.execute((Void) null);
			JSONObject json = null;
			try {
				json = task.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			TextView pos = (TextView) rootView
					.findViewById(R.id.wlanPositioningPosition);
			if (json != null) {
				try {
					pos.setText(json.get("List").toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			locRequest = null;
			pDialog.dismiss();
		}
	}

	class GetNodeForFingerprintTask extends AsyncTask<Void, Void, JSONObject> {

		private LocationRequest fingerprint;

		public GetNodeForFingerprintTask(LocationRequest fingerprint) {
			this.fingerprint = fingerprint;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {

			try {
				JSONObject ret = Service.GetNodeForFingerprint(fingerprint);
				return ret;
			} catch (WebServiceException e) {
				e.printStackTrace();
			} catch (ConnectException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
