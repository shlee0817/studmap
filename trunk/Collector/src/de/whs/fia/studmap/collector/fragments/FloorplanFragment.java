package de.whs.fia.studmap.collector.fragments;

import de.whs.fia.studmap.collector.R;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FloorplanFragment extends Fragment {

	private int floorId;
	
	public FloorplanFragment(int floorId){
		
		this.floorId = floorId;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = (View) inflater.inflate(
				R.layout.fragment_flooplan, container, false);
		
		return rootView;
	}
	
	public void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			//mNfcTag = bytesToHexString(tag.getId());
		}
	}
}
