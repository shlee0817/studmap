package de.whs.studmap.navigator;

import de.whs.studmap.fragments.PreferencesFragment;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class PreferencesActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PreferencesFragment()).commit();
		
		getActionBar().setDisplayHomeAsUpEnabled(true);		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}
