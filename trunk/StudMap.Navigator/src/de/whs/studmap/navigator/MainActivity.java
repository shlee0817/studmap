package de.whs.studmap.navigator;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.FragmentManager;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.Floor;
import de.whs.studmap.client.core.data.Node;
import de.whs.studmap.client.core.snippets.NFC;
import de.whs.studmap.client.core.snippets.UserInfo;
import de.whs.studmap.client.core.web.JavaScriptInterface;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import de.whs.studmap.client.listener.OnGetFloorsTaskListener;
import de.whs.studmap.client.listener.OnGetNodeForNFCTagTaskListener;
import de.whs.studmap.client.listener.OnGetNodeForQrCodeTaskListener;
import de.whs.studmap.client.listener.OnGetRoomsTaskListener;
import de.whs.studmap.client.listener.OnLoginDialogListener;
import de.whs.studmap.client.listener.OnLogoutTaskListener;
import de.whs.studmap.client.listener.OnPoIDialogListener;
import de.whs.studmap.client.listener.OnPositionDialogListener;
import de.whs.studmap.client.listener.OnRegisterDialogListener;
import de.whs.studmap.client.tasks.GetFloorsTask;
import de.whs.studmap.client.tasks.GetNodeForNFCTagTask;
import de.whs.studmap.client.tasks.GetNodeForQrCodeTask;
import de.whs.studmap.client.tasks.GetRoomsTask;
import de.whs.studmap.client.tasks.LogoutTask;
import de.whs.studmap.fragments.WebViewFragment;
import de.whs.studmap.navigator.dialogs.ImpressumDialogFragment;
import de.whs.studmap.navigator.dialogs.LoginDialogFragment;
import de.whs.studmap.navigator.dialogs.PoIDialogFragment;
import de.whs.studmap.navigator.dialogs.PositionDialogFragment;
import de.whs.studmap.scanner.IntentIntegrator;
import de.whs.studmap.scanner.IntentResult;

public class MainActivity extends BaseMainActivity implements
		OnLoginDialogListener, OnPoIDialogListener, OnRegisterDialogListener,
		OnGetFloorsTaskListener, OnGetRoomsTaskListener,
		OnGetNodeForQrCodeTaskListener, JavaScriptInterface,
		OnLogoutTaskListener, OnGetNodeForNFCTagTaskListener, OnPositionDialogListener {

	// vars
	private boolean mIsLoggedIn = false;
	private String mUserName = "";
	private Node mSelectedNode = null;
	private int mMapId = Constants.MAP_ID;

	private List<Floor> mFloorList = new ArrayList<Floor>();
	private List<Node> mRoomList = new ArrayList<Node>();

	private WebViewFragment mWebViewFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		registerListener();
		loadWebViewFragment();
		getDataFromWebService();

		// TODO: initialer Start -> Map wählen, nachher über Einstellung
		// änderbar
	}

	@Override
	protected void onDestroy() {

		try {
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
			}.execute((Void) null).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				if (scanResult != null) {
					showProgress(true);
					GetNodeForQrCodeTask mGetNodeForQrCodeTask = new GetNodeForQrCodeTask(
							this, mMapId, scanResult);
					mGetNodeForQrCodeTask.execute((Void) null);
					break;
				}
			}
			UserInfo.toast(this, "Scan: Es liegt kein Ergebnis vor.", false);
			break;
		}
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

			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String nfcTag = NFC.BytesToHexString(tag.getId());

			showProgress(true);

			GetNodeForNFCTagTask mGetNodeForNFCTagTask = new GetNodeForNFCTagTask(
					this, mMapId, nfcTag);
			mGetNodeForNFCTagTask.execute((Void) null);
		}
	}

	private void registerListener() {

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

		mDrawerListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onDrawerItemClick(position);
			}
		});
	}
	
	private void onDrawerItemClick(int position) {

		DrawerItemEnum sel_position = DrawerItemEnum.values()[position];

		switch (sel_position) {
		case LOG_IN_OUT:
			if (mIsLoggedIn) {
				showProgress(true);
				LogoutTask mLogoutTask;
				mLogoutTask = new LogoutTask(this, mUserName);
				mLogoutTask.execute((Void) null);

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
			PoIDialogFragment dialog = PoIDialogFragment.newInstance(mMapId);
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
	}

	private void getDataFromWebService() {

		showProgress(true);
		GetRoomsTask mGetRoomsTask = new GetRoomsTask(this, mMapId);
		mGetRoomsTask.execute((Void) null);

		GetFloorsTask mGetFloorsTask = new GetFloorsTask(this, mMapId);
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
		this.mIsLoggedIn = true;
		mDrawerItems.remove(DrawerItemEnum.LOG_IN_OUT.ordinal());
		mDrawerItems.add(DrawerItemEnum.LOG_IN_OUT.ordinal(),
				getString(R.string.logout));

		showProgress(false);
	}

	@Override
	public void onPoISelected(Node node) {

		mSelectedNode = node;
		changeFloorIfRequired();
		showProgress(false);
	}

	@Override
	public void onRegister() {

		// TODO Login View aufrufen
		showProgress(false);
	}

	@Override
	public void onGetRoomsSuccess(List<Node> nodes) {
		mRoomList = nodes;
		ArrayAdapter<Node> searchAdapter = new ArrayAdapter<Node>(this,
				android.R.layout.simple_list_item_1, mRoomList);
		mSearchTextView.setAdapter(searchAdapter);
		showProgress(false);
	}

	@Override
	public void onGetRoomsError(int responseError) {
		// TODO: ErrorHandler
		showProgress(false);
	}

	@Override
	public void onGetRoomsCanceled() {
		showProgress(false);
	}

	@Override
	public void onGetFloorsSuccess(List<Floor> floors) {
		mFloorList = floors;
		ArrayAdapter<Floor> floorAdapter = new ArrayAdapter<Floor>(this,
				R.layout.simple_list_item_no_bg_font_white, mFloorList);
		floorAdapter.setDropDownViewResource(R.layout.simple_list_item_black);
		mFloorSpinner.setAdapter(floorAdapter);
		showProgress(false);
	}

	@Override
	public void onGetFloorsError(int responseError) {
		// TODO: ErrorHandler
		showProgress(false);
	}

	@Override
	public void onGetFloorsCanceled() {
		showProgress(false);
	}

	@Override
	public void onGetNodeForQrCodeSuccess(Node node) {

		mSelectedNode = node;
		changeFloorIfRequired();
		showProgress(false);
	}

	@Override
	public void onGetNodeForQrCodeError(int responseError) {
		// TODO Auto-generated method stub
		showProgress(false);

	}

	@Override
	public void onGetNodeForQrCodeCanceled() {
		showProgress(false);
	}

	@Override
	public void onLogoutSuccess() {

		mIsLoggedIn = false;
		mDrawerItems = Arrays.asList(getResources().getStringArray(
				R.array.menue_item_array));
		mUserName = getString(R.string.username);
		UserInfo.toast(this, getString(R.string.logout_successfull), true);
		showProgress(false);
	}

	@Override
	public void onLogoutError(int responseError) {
		// TODO Auto-generated method stub

		// Present the error from doInBackground to the user
		// UserInfo.dialog(mContext, mUserName,
		// getString(R.string.error_connection));
		showProgress(false);
	}

	@Override
	public void onLogoutCanceled() {
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
		// TODO Auto-generated method stub

		showProgress(false);
	}

	@Override
	public void onGetNodeForNFCTagCanceled() {
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
}
