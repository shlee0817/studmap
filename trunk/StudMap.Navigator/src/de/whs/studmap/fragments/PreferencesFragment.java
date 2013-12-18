package de.whs.studmap.fragments;

import java.util.List;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.widget.Toast;
import de.whs.studmap.client.core.data.Map;
import de.whs.studmap.client.listener.OnGenericTaskListener;
import de.whs.studmap.client.tasks.GetMapsTask;
import de.whs.studmap.navigator.R;

public class PreferencesFragment extends PreferenceFragment implements OnGenericTaskListener<List<Map>> {
	
	ListPreference list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 addPreferencesFromResource(R.xml.preferences);
		 
		 list = (ListPreference) getPreferenceScreen().findPreference(getString(R.string.pref_map_key));
	
		 GetMapsTask task = new GetMapsTask(this);
		 task.execute((Void)null);
	}

	@Override
	public void onSuccess(List<Map> maps) {
		
		if(maps.size() == 0)
			return;
		
		String[] values = new String[maps.size()];
		String[] entries = new String[maps.size()];
		
		for (int i = 0; i < maps.size(); i++) {
			values[i] = "" + maps.get(i).getId();
			entries[i] = maps.get(i).getName();
		}
		
		list.setEntries(entries);
		list.setEntryValues(values);
	}

	@Override
	public void onError(int responseError) {
		// TODO: Dennis Error Handling!
		Toast.makeText(getActivity(), "Load Maps failed", Toast.LENGTH_LONG).show();		
	}

	@Override
	public void onCanceled() {
		// nothing :(
		
	}	
}
