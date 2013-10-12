package de.whs.fia.studmap.collector.fragments;

import java.util.List;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import de.whs.fia.studmap.collector.R;
import de.whs.fia.studmap.collector.data.APsDataSource;
import de.whs.fia.studmap.collector.data.ScansDataSource;
import de.whs.fia.studmap.collector.models.AP;
import de.whs.fia.studmap.collector.models.Scan;

public class WlanPositioningFragment extends Fragment {

	private WifiReceiver wifiReceiver;
	private WifiManager wifiManager;
	private List<ScanResult> wifiList;

	private ProgressDialog pDialog;
	private ListView ap;

	private ScanResult selectedAP;
	
	private ScansDataSource scansDatasource;
	private APsDataSource apsDatasource;
	
	public WlanPositioningFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_wlan_positioning,
				container, false);
		
		scansDatasource = new ScansDataSource(getActivity());
		apsDatasource = new APsDataSource(getActivity());
		
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

		Button scan = (Button) rootView.findViewById(R.id.wlanPositioningScan);
		scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				pDialog.show();
				wifiManager.startScan();
			}
		});

		ap = (ListView) rootView.findViewById(R.id.wlanPositioningWifiList);
		ap.setChoiceMode(1);
		ap.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				ap.setItemChecked(arg2, true);
				
				selectedAP = (ScanResult)arg0.getItemAtPosition(arg2);
			}
		});

		Button position = (Button) rootView
				.findViewById(R.id.wlanPositioningFindMe);
		position.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if(wifiList == null || wifiList.size() == 0){
					Toast.makeText(
							getActivity(),
							"Zur Positionierung werden APs in der Nähe benötigt.",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (selectedAP == null) {

					Toast.makeText(
							getActivity(),
							"Zur Positionierung wird ein AP als Referenz benötigt.",
							Toast.LENGTH_SHORT).show();
					return;
				}

				List<Scan> scans = scansDatasource.getScans();
				
				scansDatasource.open();
				apsDatasource.open();				
				for(Scan scan : scans){
					
					for(AP ap : apsDatasource.getAPsToScan(scan.getId())){
					
						scan.addAP(ap);
					}
				}				
				scansDatasource.close();
				apsDatasource.close();
				
				// TODO: Positionierungsalg. entwerfen
				
				//TODO: Ausgabe des Ergebnis in das Textfeld
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

			wifiList = wifiManager.getScanResults();

			pDialog.dismiss();

			final ArrayAdapter<ScanResult> adapter = new ArrayAdapter<ScanResult>(
					getActivity(), android.R.layout.simple_list_item_checked,
					wifiList);

			ap.setAdapter(adapter);
		}
	}
}
