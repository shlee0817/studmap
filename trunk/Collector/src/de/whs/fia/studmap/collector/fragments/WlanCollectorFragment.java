package de.whs.fia.studmap.collector.fragments;

import java.util.ArrayList;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import de.whs.fia.studmap.collector.R;
import de.whs.fia.studmap.collector.data.APsDataSource;
import de.whs.fia.studmap.collector.data.ScansDataSource;
import de.whs.fia.studmap.collector.models.AP;
import de.whs.fia.studmap.collector.models.Scan;

/**
 * Enth�lt alle Funktionen, die in der Ansicht ben�tigt werden um einen
 * Wlan-Fingerprint zu erstellen.
 * 
 * @author Thomas
 * 
 */
public class WlanCollectorFragment extends Fragment {

	private ListView ap;
	private EditText nodeIdField;
	
	private WifiReceiver wifiReceiver;
	private WifiManager wifiManager;
	private List<ScanResult> wifiList;
	
	private List<Scan> scans = new ArrayList<Scan>();

	private ProgressDialog pDialog;
	
	private ScansDataSource scansDatasource;
	private APsDataSource apsDatasource;
	
	
	public WlanCollectorFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_wlan_collector,
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

		
		pDialog = new ProgressDialog(getActivity());
		pDialog.setMessage("Scanning...");
		pDialog.setCancelable(false);
		
		
		Button scan = (Button) rootView.findViewById(R.id.wlanCollector_Scan);
		scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				pDialog.show();
				wifiManager.startScan();
			}
		});

		ap = (ListView) rootView.findViewById(R.id.wlanCollector_APList);
		
		Button save = (Button) rootView.findViewById(R.id.wlanCollector_Save);
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				scansDatasource.open();
				apsDatasource.open();
				for(Scan scan : scans){
					
					Scan createdScan = scansDatasource.createScan(scan.getNodeId());
					
					for(AP ap : scan.getAPs()){
						
						apsDatasource.createAP(ap.getBSSID(), ap.getRSS(), createdScan.getId());
					}
				}
				scansDatasource.close();
				apsDatasource.close();
				
				nodeIdField.getText().clear();
				ap.setAdapter(null);
			}
		});
		
		nodeIdField = (EditText)rootView.findViewById(R.id.wlanCollector_PointId);

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

			Scan s = new Scan();
			
			int nodeId = 0;
			try{
				String nodeIdFieldStr = nodeIdField.getText().toString();
				nodeId = Integer.parseInt(nodeIdFieldStr);	
			}catch(Exception ex){
				
			}
			
			s.setNodeId(nodeId);
			
			wifiList = wifiManager.getScanResults();

			for(ScanResult r : wifiList){
				
				AP ap = new AP();
				ap.setBSSID(r.BSSID);
				ap.setRSS(r.level);
				s.addAP(ap);
			}
			
			scans.add(s);
			
			pDialog.dismiss();
			
			final ArrayAdapter<AP> adapter = new ArrayAdapter<AP>(getActivity(),
					android.R.layout.simple_list_item_1, s.getAPs());

			ap.setAdapter(adapter);
		}
	}
}
