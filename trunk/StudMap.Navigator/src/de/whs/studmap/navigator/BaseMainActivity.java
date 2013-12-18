package de.whs.studmap.navigator;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.SearchView;
import de.whs.studmap.client.core.snippets.UserInfo;
import de.whs.studmap.fragments.MenuFragment;

public class BaseMainActivity extends Activity {


	private static final String MIME_TEXT_PLAIN = "text/plain";
	
	//ProgressDialog
	protected ProgressDialog pDialog;
	
	//NFC
	protected PendingIntent pendingIntent;
	protected IntentFilter[] intentFiltersArray;
	protected String[][] techListsArray;
	protected NfcAdapter mNfcAdapter;
	protected boolean mNfcSupported = true;
	
	//vars
	protected boolean isDrawerOpen = false;

	// UI References
	protected ActionBar mActionBar;
	protected ActionBarDrawerToggle mDrawerToggle;
	protected DrawerLayout mDrawerLayout;
	protected FrameLayout mLeftDrawer;
	protected MenuFragment mMenuFragment;

	protected SearchView mSearchView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pDialog = UserInfo.StudmapProgressDialog(this);
		pDialog.setIcon(R.drawable.ic_launcher);
		
		loadBaseActivity();
		initializeActionBar();
				
		if(mNfcSupported)
			setupForegroundDispatchSystem();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		
		// Get the SearchView and set the searchable configuration
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
	    mSearchView = (SearchView)searchMenuItem.getActionView();
	    // Assumes current activity is the searchable activity
	    if(mSearchView != null){

		    mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		    mSearchView.setIconifiedByDefault(true); 
	    }
		
		return super.onCreateOptionsMenu(menu);
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
		if(mNfcSupported)
			mNfcAdapter.enableForegroundDispatch(this, pendingIntent,
				intentFiltersArray, techListsArray);
	}

	@Override
	public void onPause() {
		super.onPause();
		if(mNfcSupported)
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
	
	private void loadBaseActivity() {
		setContentView(R.layout.activity_main);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mLeftDrawer = (FrameLayout) findViewById(R.id.left_drawer);
		mActionBar = getActionBar();

		mMenuFragment = MenuFragment.newInstance();

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.left_drawer, mMenuFragment).commit();
		
		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

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
		
		if (mNfcAdapter == null) {
            
			mNfcSupported = false;
        }
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
		mActionBar.setTitle(getString(R.string.app_name));			
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
