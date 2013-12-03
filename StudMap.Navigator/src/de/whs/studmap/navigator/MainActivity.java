package de.whs.studmap.navigator;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.Floor;
import de.whs.studmap.client.core.data.Node;
import de.whs.studmap.client.core.snippets.NFC;
import de.whs.studmap.client.core.snippets.UserInfo;
import de.whs.studmap.client.core.web.JavaScriptInterface;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import de.whs.studmap.client.listener.OnGetFloorsTaskListener;
import de.whs.studmap.client.listener.OnGetRoomsTaskListener;
import de.whs.studmap.client.listener.OnLoginDialogListener;
import de.whs.studmap.client.listener.OnPoIDialogListener;
import de.whs.studmap.client.listener.OnRegisterDialogListener;
import de.whs.studmap.client.tasks.GetFloorsTask;
import de.whs.studmap.client.tasks.GetRoomsTask;
import de.whs.studmap.fragments.WebViewFragment;
import de.whs.studmap.navigator.dialogs.ImpressumDialogFragment;
import de.whs.studmap.navigator.dialogs.LoginDialogFragment;
import de.whs.studmap.navigator.dialogs.PoIDialogFragment;
import de.whs.studmap.navigator.dialogs.PositionDialogFragment;
import de.whs.studmap.scanner.IntentIntegrator;
import de.whs.studmap.scanner.IntentResult;

public class MainActivity extends Activity implements OnLoginDialogListener,
		OnPoIDialogListener, OnRegisterDialogListener, OnGetFloorsTaskListener,
		OnGetRoomsTaskListener, JavaScriptInterface {
	// UI References
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private LinearLayout mLeftDrawer;
	private ActionBar mActionBar;
	private Spinner mFloorSpinner;
	private AutoCompleteTextView mSearchTextView;

	// vars
	private boolean mIsLoggedIn = false;
	private boolean isDrawerOpen = false;
	private List<Floor> mFloorList = new ArrayList<Floor>();
	private List<Node> mRoomList = new ArrayList<Node>();
	private List<String> mDrawerItems;
	private GetNodeForQrCodeTask mGetNodeForQrCodeTask = null;
	private LogoutTask mLogoutTask = null;
	private GetNodeForNFCTagTask mGetNodeForNFCTagTask = null;

	private PendingIntent pendingIntent;
	private IntentFilter[] intentFiltersArray;
	private String[][] techListsArray;

	private NfcAdapter mNfcAdapter;
	private WebViewFragment mWebViewFragment;

	static WebView mMapWebView;

	public static final String MIME_TEXT_PLAIN = "text/plain";
	public String mUserName = "";
	public static Node mSelectedNode = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadActivity();
		loadWebViewFragment();
		initializeActionBar();
		getDataFromWebService();

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
	protected void onDestroy() {

		// TODO: Warten aufs Abmelden
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					Service.logout(mUserName);
				} catch (WebServiceException ignore) {
				} catch (ConnectException ignore) {
				}
				return true;
			}
		}.execute((Void) null);
		super.onDestroy();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case IntentIntegrator.REQUEST_CODE: // Return from QR-Scanner
			IntentResult result = IntentIntegrator.parseActivityResult(
					requestCode, resultCode, data);
			if (result != null) {
				String scanResult = result.getContents();
				if (scanResult != null) {
					if (mGetNodeForQrCodeTask == null) {
						showProgress(true);
						mGetNodeForQrCodeTask = new GetNodeForQrCodeTask(this);
						mGetNodeForQrCodeTask.execute(scanResult);
					}
					break;
				}
			}
			UserInfo.toast(getApplicationContext(),
					"Scan: Es liegt kein Ergebnis vor.", false);
			break;
		}
	}

	private void loadActivity() {
		setContentView(R.layout.activity_main);

		mUserName = getString(R.string.username);

		mDrawerItems = new ArrayList<String>(Arrays.asList(getResources()
				.getStringArray(R.array.menue_item_array)));
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		ListView mDrawerListView = (ListView) findViewById(R.id.left_drawer_listView);
		mLeftDrawer = (LinearLayout) findViewById(R.id.left_drawer);
		mActionBar = getActionBar();

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerListView.setAdapter(new ArrayAdapter<String>(this,
				R.layout.simple_list_item_black, mDrawerItems));

		mDrawerListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onDrawerItemClick(position);
			}
		});

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

		// TODO: initialer Start -> Map wählen, nachher über Einstellung
		// änderbar
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

		mSearchTextView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int pos, long id) {
				mSelectedNode = (Node) adapterView.getItemAtPosition(pos);
				UserInfo.toast(getApplicationContext(), "Suche "
						+ mSelectedNode.toString(), false);

				// clear textview & hide soft keyboard
				mSearchTextView.setText("");
				mMapWebView.requestFocus();
				closeKeyboard(view);

				changeFloorIfRequired();

			}
		});

		// Get Spinner for selecting the Level in the Action Bar
		mFloorSpinner = (Spinner) findViewById(R.id.levelSpinner);
		mFloorSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				Floor floor = (Floor) mFloorSpinner.getItemAtPosition(pos);
				int selectedFloorID = floor.getId();

				String floorName = floor.toString() + " mit ID: "
						+ selectedFloorID;
				UserInfo.toast(getApplicationContext(), floorName, true);
				loadFloorToMap(selectedFloorID);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void loadWebViewFragment() {

		mWebViewFragment = WebViewFragment.newInstance();

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, mWebViewFragment).commit();
	}

	private void loadFloorToMap(int floorId) {
		if (mWebViewFragment == null)
			loadWebViewFragment();
		else
			mWebViewFragment.setFloorId(floorId);
	}

	private void onDrawerItemClick(int position) {

		DrawerItemEnum sel_position = DrawerItemEnum.values()[position];

		switch (sel_position) {
		case LOG_IN_OUT:
			if (mIsLoggedIn) {
				if (mLogoutTask == null) {
					showProgress(true);
					mLogoutTask = new LogoutTask(this);
					mLogoutTask.execute((Void) null);
				} else {

					LoginDialogFragment dialog = new LoginDialogFragment();
					dialog.show(getFragmentManager(), "Login");
				}

			} else {
				LoginDialogFragment dialog = new LoginDialogFragment();
				dialog.show(getFragmentManager(), "Login");
			}
			mDrawerLayout.closeDrawer(mLeftDrawer);
			break;

		case RESET_NAV:
			if (mWebViewFragment != null)
				mWebViewFragment.resetMap();
			mDrawerLayout.closeDrawer(mLeftDrawer);
			break;

		case POI:
			PoIDialogFragment dialog = PoIDialogFragment.newInstance(Constants.MAP_ID);
			dialog.show(getFragmentManager(), "PoI Dialog");
			mDrawerLayout.closeDrawer(mLeftDrawer);
			break;

		case RELOAD:
			loadActivity();
			mDrawerLayout.closeDrawer(mLeftDrawer);
			break;

		case IMPRESSUM:
			ImpressumDialogFragment impressumDialog = new ImpressumDialogFragment();
			impressumDialog.show(getFragmentManager(), "Impressum");
			mDrawerLayout.closeDrawer(mLeftDrawer);
			break;
		default:
			UserInfo.toast(this, "Auswahl nicht gefunden!", false);
			break;

		}
	}

	private void closeKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private void getDataFromWebService() {

		GetRoomsTask mGetRoomsTask = new GetRoomsTask(this, Constants.MAP_ID);
		mGetRoomsTask.execute((Void) null);

		GetFloorsTask mGetFloorsTask = new GetFloorsTask(this, Constants.MAP_ID);
		mGetFloorsTask.execute((Void) null);
	}

	private void changeFloorIfRequired() {
		Floor currentFloor = (Floor) mFloorSpinner.getSelectedItem();
		if (mSelectedNode.getFloorID() == currentFloor.getId())
			mWebViewFragment.sendTarget(mSelectedNode.getNodeID());
		else {
			for (int i = 0; i < mFloorList.size(); i++) {
				Floor tmpFloor = (Floor) mFloorSpinner.getItemAtPosition(i);
				if (mSelectedNode.getFloorID() == tmpFloor.getId()) {
					mFloorSpinner.setSelection(i);
					return;
				}
			}
		}
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String nfcTag = NFC.BytesToHexString(tag.getId());

			if (mGetNodeForNFCTagTask == null) {

				showProgress(true);

				GetNodeForNFCTagTask mGetNodeForNFCTagTask = new GetNodeForNFCTagTask(
						this);
				mGetNodeForNFCTagTask.execute(nfcTag);
			}

		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		final View mInitStatusView = (View) findViewById(R.id.init_status);

		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mInitStatusView.setVisibility(View.VISIBLE);
			mInitStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mInitStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mDrawerLayout.setVisibility(View.VISIBLE);
			mDrawerLayout.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mDrawerLayout.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mInitStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mDrawerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	private class GetNodeForQrCodeTask extends AsyncTask<String, Void, Node> {
		private Context mContext = null;
		private boolean bShowDialog = false;

		public GetNodeForQrCodeTask(Context ctx) {
			mContext = ctx;
		}

		@Override
		protected Node doInBackground(String... params) {

			try {
				Node node = Service.getNodeForQRCode(Constants.MAP_ID,
						params[0]);
				return node;
			} catch (WebServiceException e) {
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
						"GetNodeForQrCodeTask - WebServiceException");

				JSONObject jObject = e.getJsonObject();
				try {
					int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);

					switch (errorCode) {
					case ResponseError.DatabaseError:
						bShowDialog = true;
					}
				} catch (JSONException ignore) {
					Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
							"GetNodeForQrCodeTask - Parsing the WebServiceException failed!");
				}
			} catch (ConnectException e) {
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
						"GetDataTask - ConnectException");
				bShowDialog = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Node node) {
			mGetNodeForQrCodeTask = null;
			showProgress(false);

			if (node != null) {
				mSelectedNode = node;
				changeFloorIfRequired();
			} else if (bShowDialog)
				// Present the error from doInBackground to the user
				UserInfo.dialog(mContext, mUserName,
						getString(R.string.error_connection));
		}

		@Override
		protected void onCancelled() {
			mGetNodeForQrCodeTask = null;
			showProgress(false);
		}
	}

	private class LogoutTask extends AsyncTask<Void, Void, Boolean> {
		private Context mContext = null;
		private boolean bShowDialog = false;

		public LogoutTask(Context ctx) {
			mContext = ctx;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				boolean success = Service.logout(mUserName);
				return success;
			} catch (WebServiceException e) {
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
						"LogoutTask - WebServiceException");

				JSONObject jObject = e.getJsonObject();
				try {
					int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);

					switch (errorCode) {
					case ResponseError.DatabaseError:
						bShowDialog = true;
					}
				} catch (JSONException ignore) {
					Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
							"LogoutTask - Parsing the WebServiceException failed!");
				}
			} catch (ConnectException e) {
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
						"LogoutTask - ConnectException");
				bShowDialog = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mLogoutTask = null;
			showProgress(false);

			if (success) {
				mIsLoggedIn = false;
				mDrawerItems = Arrays.asList(getResources().getStringArray(
						R.array.menue_item_array));
				mUserName = getString(R.string.username);
				UserInfo.toast(mContext,
						getString(R.string.logout_successfull), true);
			} else if (bShowDialog)
				// Present the error from doInBackground to the user
				UserInfo.dialog(mContext, mUserName,
						getString(R.string.error_connection));
		}

		@Override
		protected void onCancelled() {
			mLogoutTask = null;
			showProgress(false);
		}
	}

	private class GetNodeForNFCTagTask extends AsyncTask<String, Void, Node> {
		private Context mContext = null;
		private boolean bShowDialog_ConnectionError = false;
		private boolean bShowDialog_NFCTagDoesNotExist = false;

		public GetNodeForNFCTagTask(Context ctx) {
			mContext = ctx;
		}

		@Override
		protected Node doInBackground(String... params) {

			try {
				Node node = Service.getNodeForNFCTag(Constants.MAP_ID,
						params[0]);
				return node;
			} catch (WebServiceException e) {
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
						"GetNodeForNFCTagTask - WebServiceException");

				JSONObject jObject = e.getJsonObject();
				try {
					int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);

					switch (errorCode) {
					case ResponseError.DatabaseError:
						bShowDialog_ConnectionError = true;
						break;
					case ResponseError.NFCTagDoesNotExist:
						bShowDialog_NFCTagDoesNotExist = true;
						break;
					}
				} catch (JSONException ignore) {
					Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
							"GetNodeForNFCTagTask - Parsing the WebServiceException failed!");
				}
			} catch (ConnectException e) {
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY,
						"GetNodeForNFCTagTask - ConnectException");
				bShowDialog_ConnectionError = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Node node) {
			mGetNodeForNFCTagTask = null;
			showProgress(false);

			if (node != null) {
				String position = getString(R.string.currentPosition) + "\n"
						+ node.getRoomName() + "\n" + node.getDisplayName();
				UserInfo.dialog(mContext, mUserName, position,
						getString(R.string.setAsStart));
			} else {
				if (bShowDialog_ConnectionError) {
					// Present the error from doInBackground to the user
					UserInfo.dialog(mContext, mUserName,
							getString(R.string.error_connection));
				}
				if (bShowDialog_NFCTagDoesNotExist) {
					UserInfo.dialog(mContext, mUserName,
							getString(R.string.error_NfcTagDoesNotExist));
				}

			}

		}

		@Override
		protected void onCancelled() {
			mGetNodeForNFCTagTask = null;
			showProgress(false);
		}
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
	public void onLogin(String userName) {

		this.mUserName = userName;
		this.mIsLoggedIn = true;
		mDrawerItems.remove(DrawerItemEnum.LOG_IN_OUT.ordinal());
		mDrawerItems.add(DrawerItemEnum.LOG_IN_OUT.ordinal(),
				getString(R.string.logout));
	}

	@Override
	@JavascriptInterface
	public void punkt(String nodeId) {

		Bundle args = new Bundle();
		args.putInt("NodeId", Integer.parseInt(nodeId));

		PositionDialogFragment dialog = new PositionDialogFragment();
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "Positionierungsdialog");
	}

	@Override
	@JavascriptInterface
	public void onFinish() {

		if (mSelectedNode != null && mWebViewFragment != null) {
			mWebViewFragment.sendTarget(mSelectedNode.getNodeID());
		}
	}

	@Override
	public void onPoISelected(Node node) {

		mSelectedNode = node;
		changeFloorIfRequired();
	}

	@Override
	public void onRegister() {

		// TODO Login View aufrufen
	}

	@Override
	public void onGetRoomsSuccess(List<Node> nodes) {
		mRoomList = nodes;
		ArrayAdapter<Node> searchAdapter = new ArrayAdapter<Node>(this,
				android.R.layout.simple_list_item_1, mRoomList);
		mSearchTextView.setAdapter(searchAdapter);
	}

	@Override
	public void onGetRoomsError(int responseError) {
		// TODO: ErrorHandler
	}

	@Override
	public void onGetRoomsCanceled() {
		// TODO: ggf Wartespinner ausblenden
	}

	@Override
	public void onGetFloorsSuccess(List<Floor> floors) {
		mFloorList = floors;
		ArrayAdapter<Floor> floorAdapter = new ArrayAdapter<Floor>(this,
				R.layout.simple_list_item_no_bg_font_white, mFloorList);
		floorAdapter.setDropDownViewResource(R.layout.simple_list_item_black);
		mFloorSpinner.setAdapter(floorAdapter);
	}

	@Override
	public void onGetFloorsError(int responseError) {
		// TODO: ErrorHandler
	}

	@Override
	public void onGetFloorsCanceled() {
		// TODO: ggf Wartespinner ausblenden
	}

}
