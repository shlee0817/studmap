package de.whs.studmap.navigator;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.JavascriptInterface;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.Floor;
import de.whs.studmap.client.core.data.Node;
import de.whs.studmap.client.core.snippets.ErrorHandler;
import de.whs.studmap.client.core.snippets.NFC;
import de.whs.studmap.client.core.snippets.UserInfo;
import de.whs.studmap.client.core.web.JavaScriptInterface;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
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
import de.whs.studmap.client.tasks.LogoutTask;
import de.whs.studmap.fragments.PreferencesFragment;
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
		OnMenuItemListener {

	// vars
	private boolean mWaitGetRoomsOrFloorsTask = false;
	private ErrorHandler mErrorHandler = null;
	private String mUserName = "";
	private Node mSelectedNode = null;
	private int mMapId = Constants.MAP_ID;
	private int mFloorId;

	private List<Floor> mFloorList = new ArrayList<Floor>();

	private WebViewFragment mWebViewFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadWebViewFragment();
		getDataFromWebService();
		mErrorHandler = new ErrorHandler(this);

		loadActivity();

		// TODO: initialer Start -> Map wählen, nachher über Einstellung änderbar
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String mapId = sharedPref.getString(getString(R.string.pref_map_key), "3");
		String hostName = sharedPref.getString(getString(R.string.pref_host_key), "193.175.199.115");
		mMapId = Integer.parseInt(mapId);

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
								"OnDestroy - Logout - DatabaseError");
					} catch (ConnectException e) {
						Log.e(Constants.LOG_TAG_MAIN_ACTIVITY,
								"OnDestroy - Logout - Webservice nicht erreichbar");
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
				Log.d(Constants.LOG_TAG_MAIN_ACTIVITY, "QRScan result - "
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
	
	private void loadActivity(){
		loadWebViewFragment();
		getDataFromWebService();
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
			
			mSelectedNode = Node.fromJson(query);
			changeFloorIfRequired();
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

	private void changeFloorIfRequired() {

		int selectedNodeId = mSelectedNode.getNodeID();
		int selectedFloorId = mSelectedNode.getFloorID();
		if (selectedFloorId == mFloorId)
			mWebViewFragment.sendTarget(selectedNodeId);
		else {
			loadFloorToMap(selectedFloorId);
			mMenuFragment.selectFloorItem(selectedFloorId);
			mWebViewFragment.sendTarget(selectedNodeId);
		}
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
	public void onLogin(String userName) {

		this.mUserName = userName;
		mMenuFragment.toggleLoginItem(true);
		UserInfo.toast(this, getString(R.string.login_successfull), true);
		showProgress(false);
	}

	@Override
	public void onPoISelected(Node node) {

		mSelectedNode = node;
		changeFloorIfRequired();
		showProgress(false);
	}

	@Override
	public void onGetFloorsSuccess(List<Floor> floors) {

		mFloorList = floors;

		if (mFloorList.size() > 0) {

			loadFloorToMap(mFloorList.get(0).getId());
			mMenuFragment.setLoadedMap(null, mFloorList);
		}


		if (!mWaitGetRoomsOrFloorsTask)
			showProgress(false);
		mWaitGetRoomsOrFloorsTask = false;
	}

	@Override
	public void onGetFloorsError(int responseError) {
		mErrorHandler.handle(responseError);

		if (!mWaitGetRoomsOrFloorsTask)
			showProgress(false);
		mWaitGetRoomsOrFloorsTask = false;
	}

	@Override
	public void onGetFloorsCanceled() {
		mErrorHandler.handle(ResponseError.TaskCancelled);
		if (!mWaitGetRoomsOrFloorsTask)
			showProgress(false);
		mWaitGetRoomsOrFloorsTask = false;
	}

	@Override
	public void onGetNodeForQrCodeSuccess(Node node) {

		mSelectedNode = node;
		changeFloorIfRequired();
		showProgress(false);
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

		mSelectedNode = node;
		changeFloorIfRequired();
		showProgress(false);
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
	public void onSetDestination(int nodeId) {
		mWebViewFragment.sendDestination(nodeId);
	}

	@Override
	public void onSetStart(int nodeId) {
		mWebViewFragment.sendStart(nodeId);
	}

	@Override
	public void onMenuItemClicked(String itemName) {

		if (itemName.equals(getString(R.string.menu_login))) {

			LoginDialogFragment dialog = new LoginDialogFragment();
			dialog.show(getFragmentManager(), "Login");
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
			dialog.show(getFragmentManager(), "PoI Dialog");
			mDrawerLayout.closeDrawer(mLeftDrawer);
		} else if (itemName.equals(getString(R.string.menu_about))) {

			ImpressumDialogFragment impressumDialog = new ImpressumDialogFragment();
			impressumDialog.show(getFragmentManager(), "Impressum");
			mDrawerLayout.closeDrawer(mLeftDrawer);
		} else if (itemName.equals(getString(R.string.menu_settings))) {

			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new PreferencesFragment())
					.commit();
			mDrawerLayout.closeDrawer(mLeftDrawer);
		} else {
		}
	}

	@Override
	public void onFloorChanged(int floorId) {

		int selectedFloorID = floorId;

		String floorName = "Floor mit ID: " + selectedFloorID;
		UserInfo.toast(getApplicationContext(), floorName, true);
		loadFloorToMap(selectedFloorID);
		mDrawerLayout.closeDrawer(mLeftDrawer);
	}

	@Override
	public void onRegister(String username) {

		Bundle args = new Bundle();
		args.putString("username", username);

		LoginDialogFragment dialog = new LoginDialogFragment();
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "Login");
		showProgress(false);
	}
}
