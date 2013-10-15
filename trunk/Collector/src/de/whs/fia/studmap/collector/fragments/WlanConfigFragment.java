package de.whs.fia.studmap.collector.fragments;

import java.util.List;

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
import android.widget.Button;
import de.whs.fia.studmap.collector.R;
import de.whs.fia.studmap.collector.data.APsDataSource;
import de.whs.fia.studmap.collector.data.ScansDataSource;

public class WlanConfigFragment extends Fragment {

	private WifiReceiver wifiReceiver;
	private WifiManager wifiManager;
	private List<ScanResult> wifiList;
	
	private ScansDataSource scansDatasource;
	private APsDataSource apsDatasource;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_wlan_configuration,
				container, false);

		scansDatasource = new ScansDataSource(getActivity());
		apsDatasource = new APsDataSource(getActivity());
		
		wifiManager = (WifiManager) this.getActivity().getSystemService(
				Context.WIFI_SERVICE);
		
		if(!wifiManager.isWifiEnabled()){
			wifiManager.setWifiEnabled(true);
		}
		
		wifiReceiver = new WifiReceiver();
		getActivity().registerReceiver(wifiReceiver,
				new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		
		Button configurate = (Button)getActivity().findViewById(R.id.wlanConfig_BtnConfigurate);
		/*configurate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				wifiManager.startScan();
			}
		});
		*/
		
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
			
			//TODO: Werte vergleichen mit Datenbankwerten
			//TODO: Scan für den Root-Node (Foyer) wird zurück gegeben (Webserver)			
		}
	}
}
