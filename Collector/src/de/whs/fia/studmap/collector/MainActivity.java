package de.whs.fia.studmap.collector;

import de.whs.fia.studmap.collector.fragments.FloorplanFragment;
import de.whs.fia.studmap.collector.fragments.MapFloorSelectorFragment;
import de.whs.fia.studmap.collector.fragments.MapFloorSelectorFragment.OnMapFloorSelectedListener;
import de.whs.studmap.client.core.data.Floor;
import de.whs.studmap.client.core.data.Map;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class MainActivity extends FragmentActivity implements
		OnMapFloorSelectedListener {

	public static final String MIME_TEXT_PLAIN = "text/plain";

	private NfcAdapter mNfcAdapter;
	private PendingIntent pendingIntent;
	private IntentFilter[] intentFiltersArray;
	private String[][] techListsArray;

	private FloorplanFragment mFloorplanFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		MapFloorSelectorFragment mMapFloorSelectorFragment = new MapFloorSelectorFragment(this);

		getSupportFragmentManager().beginTransaction()
				.add(R.id.collectorContentWrapper, mMapFloorSelectorFragment)
				.commit();

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		setupForegroundDispatchSystem();
	}

	private void setupForegroundDispatchSystem() {
		pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
				new Intent(this, getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		ndef.addCategory(Intent.CATEGORY_DEFAULT);
		try {
			ndef.addDataType(MIME_TEXT_PLAIN);
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		IntentFilter tdef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		intentFiltersArray = new IntentFilter[] { ndef, tdef };

		techListsArray = new String[][] { new String[] { Ndef.class.getName() } };
	}

	@Override
	public void onResume() {
		super.onResume();
		mNfcAdapter.enableForegroundDispatch(this, pendingIntent,
				intentFiltersArray, techListsArray);
	}

	@Override
	public void onPause() {
		super.onPause();
		mNfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

			Fragment loadedFragment = getSupportFragmentManager().findFragmentById(R.id.collectorContentWrapper);
			if(loadedFragment.getClass() == FloorplanFragment.class && mFloorplanFragment != null){
				mFloorplanFragment.handleIntent(intent);
			}
		}
	}

	@Override
	public void onMapFloorSelected(Map map, Floor floor) {

		mFloorplanFragment = new FloorplanFragment(floor.getId());

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();

		// Replace whatever is in the fragment_container view with this
		// fragment,
		// and add the transaction to the back stack so the user can navigate
		// back
		transaction.replace(R.id.collectorContentWrapper, mFloorplanFragment);
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();
	}

}
