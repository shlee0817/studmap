package de.whs.fia.studmap.collector.fragments;

import de.whs.fia.studmap.collector.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FloorplanFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = (View) inflater.inflate(
				R.layout.fragment_flooplan, container, false);
		
		return rootView;
	}
}
