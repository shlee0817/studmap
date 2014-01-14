package de.whs.studmap.navigator;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.Floor;
import de.whs.studmap.client.core.data.Map;
import de.whs.studmap.client.core.data.Node;
import de.whs.studmap.client.core.snippets.ErrorHandler;
import de.whs.studmap.client.core.snippets.NFC;
import de.whs.studmap.client.core.snippets.UserInfo;
import de.whs.studmap.client.core.web.JavaScriptInterface;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import de.whs.studmap.client.listener.OnGenericTaskListener;
import de.whs.studmap.client.listener.OnGetFloorsTaskListener;
import de.whs.studmap.client.listener.OnGetNodeForNFCTagTaskListener;
import de.whs.studmap.client.listener.OnGetNodeForQrCodeTaskListener;
import de.whs.studmap.client.listener.OnLoginDialogListener;
import de.whs.studmap.client.listener.OnLogoutTaskListener;
import de.whs.studmap.client.listener.OnPoIDialogListener;
import de.whs.studmap.client.listener.OnPositionDialogListener;
import de.whs.studmap.client.listener.OnRegisterDialogListener;
import de.whs.studmap.client.tasks.GetFloorsTask;
import de.whs.studmap.client.tasks.GetNodeForNFCTagTask;
import de.whs.studmap.client.tasks.GetNodeForQrCodeTask;
import de.whs.studmap.client.tasks.GetNodeInfoTask;
import de.whs.studmap.client.tasks.LogoutTask;
import de.whs.studmap.fragments.InitialSetupFragment;
import de.whs.studmap.fragments.InitialSetupFragment.OnMapSelectedListener;
import de.whs.studmap.fragments.WebViewFragment;
import de.whs.studmap.navigator.dialogs.ImpressumDialogFragment;
import de.whs.studmap.navigator.dialogs.LoginDialogFragment;
import de.whs.studmap.navigator.dialogs.PoIDialogFragment;
import de.whs.studmap.navigator.dialogs.PositionDialogFragment;
import de.whs.studmap.scanner.IntentIntegrator;
import de.whs.studmap.scanner.IntentResult;

public class MainActivity extends BaseMainActivity implements
		OnLoginDialogListener, OnPoIDialogListener, OnRegisterDialogListener,
		OnGetFloorsTaskListener, OnGetNodeForQrCodeTaskListener,
		JavaScriptInterface, OnLogoutTaskListener,
		OnGetNodeForNFCTagTaskListener, OnPositionDialogListener,
		OnMenuItemListener, OnMapSelectedListener {

	// vars
	private ErrorHandler mErrorHandler = null;
	private String mUserName = ""; //$NON-NLS-1$
	private Node mSelectedNode = null;
	private boolean mSetAsStart = false;
	private boolean mWebViewIsReady = false;
	private boolean mIsStartUp = false;
	private int mMapId = Constants.MAP_ID;
	private int mFloorId;

	private List<Floor> mFloorList = new ArrayList<Floor>();

	private WebViewFragment mWebViewFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (!isNetworkAvailable()) {

			UserInfo.dialog(
					this,
					Messages.getString("MainActivity.NoDataConnectionHeadline"),  //$NON-NLS-1$
					Messages.getString("MainActivity.NoDataConnection")); //$NON-NLS-1$
			return;
		}

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			
			getWindow().setFlags(
			                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
			                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

			findViewById(R.id.content_frame).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		
		
		mErrorHandler = new ErrorHandler(this);

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean isFirstStart = sharedPref.getBoolean(
				getString(R.string.pref_first_time), true);

		// TODO hostName verwenden!
		// String hostName =
		// sharedPref.getString(getString(R.string.pref_host_key),
		// "193.175.199.115");

		if (isFirstStart) {
			sharedPref.edit()
					.putBoolean(getString(R.string.pref_first_time), false)
					.commit();
			new InitialSetupFragment().show(getFragmentManager(),
					"InitialSetup"); //$NON-NLS-1$
		} else {
			String mapId = sharedPref.getString(
					getString(R.string.pref_map_key), "3"); //$NON-NLS-1$
			mMapId = Integer.parseInt(mapId);
			loadActivity();
		}
	}

	@Override
	protected void onDestroy() {

		try {
			new AsyncTask<Void, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(Void... params) {
					try {
						Service.logout(mUserName);
					} catch (WebServiceException e) {
						Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
								"OnDestroy - Logout - DatabaseError"); //$NON-NLS-1$
					} catch (ConnectException e) {
						Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
								"OnDestroy - Logout - Webservice nicht erreichbar"); //$NON-NLS-1$
					}
					return true;
				}
			}.execute((Void) null).get();
		} catch (InterruptedException e) {
			Log.e(Constants.LOG_TAG_MAIN_ACTIVITY, e.getMessage());
		} catch (ExecutionException e) {
			Log.e(Constants.LOG_TAG_MAIN_ACTIVITY, e.getMessage());
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case IntentIntegrator.REQUEST_CODE: // Return from QR-Scanner
			IntentResult result = IntentIntegrator.parseActivityResult(
					requestCode, resultCode, data);
			if (result != null) {
				String scanResult = result.getContents();
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY, "QRScan result - " //$NON-NLS-1$
						+ scanResult);
				if (scanResult != null) {
					showProgress(true);
					GetNodeForQrCodeTask mGetNodeForQrCodeTask = new GetNodeForQrCodeTask(
							this, mMapId, scanResult);
					mGetNodeForQrCodeTask.execute((Void) null);
					break;
				}
			}
			UserInfo.toast(this, getString(R.string.error_QRCodeNoResult),
					false);
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
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
		} else if (item.getItemId() == R.id.menu_reload) {

			mWebViewFragment.reloadMap();

			if (mSelectedNode != null) {
				if (mWebViewIsReady) {

					SetNodeAndChangeFloorIfRequired(mSelectedNode, false);
				}
			}
			mDrawerLayout.closeDrawer(mLeftDrawer);
			return false;
		} else
			return super.onOptionsItemSelected(item);
	}

	private void loadActivity() {

		showProgress(true);
		mIsStartUp = true;
		loadWebViewFragment();
		getDataFromWebService();

		Uri uri = RoomsSuggestionsProvider.CONTENT_URI;
		ContentResolver cr = getContentResolver();
		ContentProviderClient cpc = cr.acquireContentProviderClient(uri);
		if (cpc != null) {

			RoomsSuggestionsProvider provider = (RoomsSuggestionsProvider) cpc
					.getLocalContentProvider();
			provider.setMapId(mMapId);
			cpc.release();
		}

		handleIntent(getIntent());
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String nfcTag = NFC.BytesToHexString(tag.getId());

			showProgress(true);

			GetNodeForNFCTagTask mGetNodeForNFCTagTask = new GetNodeForNFCTagTask(
					this, mMapId, nfcTag);
			mGetNodeForNFCTagTask.execute((Void) null);
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);

			Node selectedNode = Node.fromJson(query);
			if (selectedNode != null)
				SetNodeAndChangeFloorIfRequired(selectedNode, false);
			else
				UserInfo.toast(
						this,
						Messages.getString("MainActivity.NodeDoesNotExist"), false); //$NON-NLS-1$

			mSearchView.onActionViewCollapsed();
		}
	}

	private void loadWebViewFragment() {

		mWebViewFragment = WebViewFragment.newInstance(mMapId);

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, mWebViewFragment).commit();
	}

	private void loadFloorToMap(int floorId) {
		if (mWebViewFragment == null)
			loadWebViewFragment();
		else
			mWebViewFragment.setFloorId(floorId);

		mFloorId = floorId;
	}

	private void getDataFromWebService() {

		showProgress(true);
		GetFloorsTask mGetFloorsTask = new GetFloorsTask(this, mMapId);
		mGetFloorsTask.execute((Void) null);
	}

	private void SetNodeAndChangeFloorIfRequired(Node selectedNode,
			Boolean setAsStart) {

		int selectedFloorId = selectedNode.getFloorID();
		if (selectedFloorId == mFloorId) {
			if (setAsStart) {
				mWebViewFragment.sendStart(selectedNode);
			}
			else
				mWebViewFragment.sendTarget(selectedNode);
		} else {
			mSelectedNode = selectedNode;
			mSetAsStart = setAsStart;
			loadFloorToMap(selectedFloorId);
			mMenuFragment.selectFloorItem(selectedFloorId);

			if (mWebViewIsReady) {
				if (setAsStart) {
					mWebViewFragment.sendStart(selectedNode);
				}
				else
					mWebViewFragment.sendTarget(selectedNode);
			}
		}
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	@Override
	@JavascriptInterface
	public void punkt(String nodeId) {

		Bundle args = new Bundle();
		args.putInt("NodeId", Integer.parseInt(nodeId)); //$NON-NLS-1$

		PositionDialogFragment dialog = new PositionDialogFragment();
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "Positionierungsdialog"); //$NON-NLS-1$
	}

	@Override
	@JavascriptInterface
	public void onFinish() {

		if (mSelectedNode != null && mWebViewFragment != null) {

			if (mSetAsStart) {
				mSetAsStart = false;
				mWebViewFragment.sendStart(mSelectedNode);
			} else
				mWebViewFragment.sendTarget(mSelectedNode);
			mSelectedNode = null;
		} else if (mIsStartUp) {
			
			mIsStartUp = false;
			showProgress(false);
		}

		mWebViewIsReady = true;
	}

	@Override
	public void onLogin(String userName) {

		this.mUserName = userName;
		mMenuFragment.toggleLoginItem(true);
		UserInfo.toast(this, getString(R.string.login_successfull), true);
		showProgress(false);
	}

	@Override
	public void onPoISelected(Node node) {
		SetNodeAndChangeFloorIfRequired(node, false);
		showProgress(false);
	}

	@Override
	public void onGetFloorsSuccess(List<Floor> floors) {

		mFloorList = floors;

		if (mFloorList.size() > 0) {

			loadFloorToMap(mFloorList.get(0).getId());
			mMenuFragment.setLoadedMap(null, mFloorList);
		}

		if(!mIsStartUp || mWebViewIsReady) {

			showProgress(false);
			mIsStartUp = false;
		}
	}

	@Override
	public void onGetFloorsError(int responseError) {
		mErrorHandler.handle(responseError);

		showProgress(false);
	}

	@Override
	public void onGetFloorsCanceled() {
		mErrorHandler.handle(ResponseError.TaskCancelled);
		showProgress(false);
	}

	@Override
	public void onGetNodeForQrCodeSuccess(Node node) {
		
		GetNodeInfoTask mAsyncTask = new GetNodeInfoTask(new OnGenericTaskListener<Node>() {
			
			@Override
			public void onSuccess(Node object) {
				
				SetNodeAndChangeFloorIfRequired(object, true);
				showProgress(false);
			}
			
			@Override
			public void onError(int responseError) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCanceled() {
				// TODO Auto-generated method stub
				
			}
		}, node.getNodeID());
		mAsyncTask.execute();
	}

	@Override
	public void onGetNodeForQrCodeError(int responseError) {
		mErrorHandler.handle(responseError);
		showProgress(false);
	}

	@Override
	public void onGetNodeForQrCodeCanceled() {
		mErrorHandler.handle(ResponseError.TaskCancelled);
		showProgress(false);
	}

	@Override
	public void onLogoutSuccess() {

		mMenuFragment.toggleLoginItem(false);
		UserInfo.toast(this, getString(R.string.logout_successfull), true);
		showProgress(false);
	}

	@Override
	public void onLogoutError(int responseError) {
		mErrorHandler.handle(responseError);
		showProgress(false);
	}

	@Override
	public void onLogoutCanceled() {
		mErrorHandler.handle(ResponseError.TaskCancelled);
		showProgress(false);
	}

	@Override
	public void onGetNodeForNFCTagSuccess(Node node) {

		GetNodeInfoTask mAsyncTask = new GetNodeInfoTask(new OnGenericTaskListener<Node>() {
			
			@Override
			public void onSuccess(Node object) {
				
				SetNodeAndChangeFloorIfRequired(object, true);
				showProgress(false);
			}
			
			@Override
			public void onError(int responseError) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCanceled() {
				// TODO Auto-generated method stub
				
			}
		}, node.getNodeID());
		mAsyncTask.execute();
	}

	@Override
	public void onGetNodeForNFCTagError(int responseError) {
		mErrorHandler.handle(responseError);
		showProgress(false);
	}

	@Override
	public void onGetNodeForNFCTagCanceled() {
		mErrorHandler.handle(ResponseError.TaskCancelled);
		showProgress(false);
	}

	@Override
	public void onSetDestination(Node node) {
		
		if(node != null) {
			
			mWebViewFragment.sendDestination(node);
		}
	}

	@Override
	public void onSetStart(Node node) {
		
		if(node != null) {

			mWebViewFragment.sendStart(node);
		}
	}

	@Override
	public void onMenuItemClicked(String itemName) {

		if (itemName.equals(getString(R.string.menu_login))) {

			LoginDialogFragment dialog = new LoginDialogFragment();
			dialog.show(getFragmentManager(), "Login"); //$NON-NLS-1$
			mDrawerLayout.closeDrawer(mLeftDrawer);
		} else if (itemName.equals(getString(R.string.menu_logout))) {

			showProgress(true);
			LogoutTask mLogoutTask;
			mLogoutTask = new LogoutTask(this, mUserName);
			mLogoutTask.execute((Void) null);
			mDrawerLayout.closeDrawer(mLeftDrawer);
		} else if (itemName.equals(getString(R.string.menu_nav_reset))) {

			if (mWebViewFragment != null)
				mWebViewFragment.resetMap();
			mDrawerLayout.closeDrawer(mLeftDrawer);
		} else if (itemName.equals(getString(R.string.menu_poi))) {

			PoIDialogFragment dialog = PoIDialogFragment.newInstance(mMapId);
			dialog.show(getFragmentManager(), "PoI Dialog"); //$NON-NLS-1$
			mDrawerLayout.closeDrawer(mLeftDrawer);
		} else if (itemName.equals(getString(R.string.menu_about))) {

			ImpressumDialogFragment impressumDialog = new ImpressumDialogFragment();
			impressumDialog.show(getFragmentManager(), "Impressum"); //$NON-NLS-1$
			mDrawerLayout.closeDrawer(mLeftDrawer);

		} else if (itemName.equals(getString(R.string.menu_settings))) {
			Intent preferenceIntent = new Intent(MainActivity.this,
					PreferencesActivity.class);
			startActivityForResult(preferenceIntent, 0);
			mDrawerLayout.closeDrawer(mLeftDrawer);
		}
	}

	@Override
	public void onFloorChanged(int floorId) {

		loadFloorToMap(floorId);
		mDrawerLayout.closeDrawer(mLeftDrawer);
	}

	@Override
	public void onRegister(String username) {

		Bundle args = new Bundle();
		args.putString("username", username); //$NON-NLS-1$

		LoginDialogFragment dialog = new LoginDialogFragment();
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "Login"); //$NON-NLS-1$
		showProgress(false);
	}

	@Override
	public void onMapSelected(Map map) {
		mMapId = map.getId();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		settings.edit()
				.putString(getString(R.string.pref_map_key),
						String.valueOf(mMapId)).commit();
		loadActivity();
	}
}
