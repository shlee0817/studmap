package de.whs.studmap.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import de.whs.studmap.scanner.IntentIntegrator;

public class BaseMainActivity extends Activity {


	private static final String MIME_TEXT_PLAIN = "text/plain";
	
	//ProgressDialog
	protected ProgressDialog pDialog;
	
	//NFC
	protected PendingIntent pendingIntent;
	protected IntentFilter[] intentFiltersArray;
	protected String[][] techListsArray;
	protected NfcAdapter mNfcAdapter;
	
	//vars
	protected boolean isDrawerOpen = false;

	// UI References
	protected WebView mMapWebView;
	protected ActionBar mActionBar;
	protected ActionBarDrawerToggle mDrawerToggle;
	protected DrawerLayout mDrawerLayout;
	protected LinearLayout mLeftDrawer;
	protected List<String> mDrawerItems;
	protected Spinner mFloorSpinner;
	protected AutoCompleteTextView mSearchTextView;
	protected ListView mDrawerListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);
		pDialog.setMessage("Bitte warten...");
		
		loadActivity();
		initializeActionBar();
		
		setupForegroundDispatchSystem();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle action buttons
		if (item.getItemId() == R.id.action_scan) {
			IntentIntegrator scanIntegrator = new IntentIntegrator(this);
			scanIntegrator.initiateScan();
			return false;
		} else
			return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (isDrawerOpen)
			mDrawerLayout.closeDrawer(mLeftDrawer);
		else
			super.onBackPressed();
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
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	protected void loadActivity() {
		setContentView(R.layout.activity_main);

		mDrawerItems = new ArrayList<String>(Arrays.asList(getResources()
				.getStringArray(R.array.menue_item_array)));
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerListView = (ListView) findViewById(R.id.left_drawer_listView);
		mLeftDrawer = (LinearLayout) findViewById(R.id.left_drawer);
		mActionBar = getActionBar();

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerListView.setAdapter(new ArrayAdapter<String>(this,
				R.layout.simple_list_item_black, mDrawerItems));

		

		// Drawer icon in the actionbar toggles due to the drawer state
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				isDrawerOpen = false;
				mActionBar.setDisplayShowCustomEnabled(true);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				isDrawerOpen = true;
				closeKeyboard(drawerView);
				mActionBar.setDisplayShowCustomEnabled(false);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
	}

	/**
	 * Initialize the custom Action Bar with an Spinner for selecting the floor
	 * and a text field for searching
	 */
	private void initializeActionBar() {

		// enable ActionBar app icon to behave as action to toggle nav drawer
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setTitle("");

		LayoutInflater inflator = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View actionBarView = inflator.inflate(R.layout.action_bar, null);
		mActionBar.setCustomView(actionBarView);

		// Initialize Search TextView
		mSearchTextView = (AutoCompleteTextView) findViewById(R.id.actionBarSearch);
		mSearchTextView.setThreshold(1); // AutoComplete by the first letter		

		// Get Spinner for selecting the Level in the Action Bar
		mFloorSpinner = (Spinner) findViewById(R.id.levelSpinner);		
	}
	
	protected void closeKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	/**
	 * Shows the progress UI and hides the login form.
	 */
	protected void showProgress(final boolean show) {
		
		if(show)
			pDialog.show();
		else
			pDialog.dismiss();
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
}
