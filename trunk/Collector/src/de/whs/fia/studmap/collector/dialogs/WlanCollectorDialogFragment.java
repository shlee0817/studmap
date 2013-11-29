package de.whs.fia.studmap.collector.dialogs;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import de.whs.fia.studmap.collector.MainActivity;
import de.whs.fia.studmap.collector.R;
import de.whs.fia.studmap.collector.data.APsDataSource;
import de.whs.fia.studmap.collector.data.ScansDataSource;
import de.whs.fia.studmap.collector.models.AP;
import de.whs.fia.studmap.collector.models.Scan;
import de.whs.fia.studmap.collector.tasks.SaveFingerprintForNodeTask;
import de.whs.studmap.client.core.data.AccessPoint;
import de.whs.studmap.client.core.data.AccessPointScan;
import de.whs.studmap.client.core.data.Fingerprint;

/**
 * Enthält alle Funktionen, die in der Ansicht benötigt werden um einen
 * Wlan-Fingerprint zu erstellen.
 * 
 * @author Thomas
 * 
 */
public class WlanCollectorDialogFragment extends DialogFragment {

	private ListView ap;
	private EditText nodeIdField;
	
	private WifiReceiver wifiReceiver;
	private WifiManager wifiManager;
	private List<ScanResult> wifiList;
	
	private ScansDataSource scansDatasource;
	private APsDataSource apsDatasource;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.fragment_wlan_collector, null);

		builder.setView(rootView).setPositiveButton(
				R.string.wlanCollector_SaveTxt,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						scansDatasource.open();
						apsDatasource.open();
						List<Scan> createdScan = scansDatasource.getScans();
						
						for(Scan s : createdScan)
						{
							Fingerprint f = new Fingerprint();
							f.setNodeId(s.getNodeId());

							List<AccessPointScan> accessPointScans = new ArrayList<AccessPointScan>();					
							for(AP a : apsDatasource.getAPsToScan(s.getId())){
								AccessPoint accessPoint = new AccessPoint();
								accessPoint.setId(a.getId());
								accessPoint.setMAC(a.getBSSID());
								AccessPointScan accessPointScan = new AccessPointScan();
								accessPointScan.setAccessPoint(accessPoint);
								accessPointScan.setReceivedSignalStrength(a.getRSS());
								accessPointScans.add(accessPointScan);
							}
							f.setAccessPointScans(accessPointScans);
							
							new SaveFingerprintForNodeTask(s.getNodeId(), f).execute((Void)null);
						}
						
						scansDatasource.clear();
						apsDatasource.clear();
						
						scansDatasource.close();
						apsDatasource.close();
						
						nodeIdField.getText().clear();
						ap.setAdapter(null);
					}
				});

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
		
		
		Button scan = (Button) rootView.findViewById(R.id.wlanCollector_Scan);
		scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				((MainActivity)getActivity()).openProgressDialog("Scanning");
				wifiManager.startScan();
			}
		});

		ap = (ListView) rootView.findViewById(R.id.wlanCollector_APList);
		
		nodeIdField = (EditText)rootView.findViewById(R.id.wlanCollector_PointId);

		if(getArguments().containsKey("NodeId")){
			nodeIdField.setText(getArguments().getString("NodeId"));
		}
		
		return builder.create();
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
				ap.setBSSID(r.BSSID.replaceAll(":", ""));
				ap.setRSS(r.level);
				s.addAP(ap);
			}
			
			scansDatasource.open();
			apsDatasource.open();
			Scan createdScan = scansDatasource.createScan(s.getNodeId());
			
			for(AP ap : s.getAPs()){
				
				apsDatasource.createAP(ap.getBSSID(), ap.getRSS(), createdScan.getId());
			}
			scansDatasource.close();
			apsDatasource.close();

			((MainActivity)getActivity()).closeProgressDialog();
			
			final ArrayAdapter<AP> adapter = new ArrayAdapter<AP>(getActivity(),
					android.R.layout.simple_list_item_1, s.getAPs());

			ap.setAdapter(adapter);
		}
	}
}
