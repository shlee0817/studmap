package de.whs.fia.studmap.collector.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import de.whs.fia.studmap.collector.R;

public class WlanCollectorFragment extends Fragment {

	public WlanCollectorFragment(){}
	
	private ListView ap;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wlan_collector, container, false);
        
        Button scan = (Button)rootView.findViewById(R.id.wlanCollector_Scan);
        scan.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
        
        ap = (ListView)rootView.findViewById(R.id.wlanCollector_APList);
        
        Button save = (Button)rootView.findViewById(R.id.wlanCollector_Save);
        save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
        
        return rootView;
    }
}
