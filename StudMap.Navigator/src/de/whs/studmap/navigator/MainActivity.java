package de.whs.studmap.navigator;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import de.whs.studmap.data.Constants;
import de.whs.studmap.data.DrawerItemEnum;
import de.whs.studmap.data.Floor;
import de.whs.studmap.data.Node;
import de.whs.studmap.scanner.IntentIntegrator;
import de.whs.studmap.scanner.IntentResult;
import de.whs.studmap.snippets.UserInfo;
import de.whs.studmap.web.JavaScriptService;
import de.whs.studmap.web.ResponseError;
import de.whs.studmap.web.Service;
import de.whs.studmap.web.WebServiceException;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout mLeftDrawer;
    private ActionBar mActionBar;
    private Spinner mFloorSpinner;
    private AutoCompleteTextView mSearchTextView;
    private static WebView mMapWebView;
    
    private CharSequence mTitle;
    private List<String> mItemTitles;
    
    private boolean mLoggedIn = false;
    private String mUserName = "";
    private static int mSelectedFloorID = 0;
    private List<Floor> mFloorList = new ArrayList<Floor>();
    private List<Node> mRoomList = new ArrayList<Node>();
    private GetInitDataTask mGetTasks = null;
    private Node mSelectedNode;
	private String mScanResult = ""; 
	public static JavaScriptService mJScriptService;

    private final int REQUEST_ID_LOGIN = 101;
    private final int REQUEST_ID_POIS = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadActivity();
    }
    
    public void loadActivity(){
        setContentView(R.layout.activity_main);
        
        mItemTitles =new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.menue_item_array)));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListView = (ListView) findViewById(R.id.left_drawer_listView);
        mLeftDrawer = (LinearLayout) findViewById(R.id.left_drawer);
        mActionBar = getActionBar();        

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerListView.setAdapter(new ArrayAdapter<String>(this,R.layout.simple_list_item_black, mItemTitles));
        
               
        mDrawerListView.setOnItemClickListener(new DrawerItemClickListener());
        
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
            	mActionBar.setDisplayShowCustomEnabled(true);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                mActionBar.setDisplayShowCustomEnabled(false);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        loadMainFragment();

        
        initializeActionBar();
        getDataFromWebService();
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
        if(item.getItemId() == R.id.action_search){
			//Scan
        	IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        	scanIntegrator.initiateScan();  	  
      	  return false;
        }
        else
        	return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
    	new AsyncTask<Void, Void, Boolean>() {
     		
    		@Override
    		protected Boolean doInBackground(Void... params) {
    			try {
    				Service.logout(mUserName);
    			} catch (WebServiceException ignore){			
    			} catch (ConnectException ignore) {}
    			return true;
    		}
		}.execute((Void) null);
    	super.onDestroy();
    }
    
    /**
     *  The click listener for ListView in the navigation drawer 
     *  */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
    	
    	DrawerItemEnum sel_position = DrawerItemEnum.values()[position];
    	
    	switch (sel_position){    		
    	case LOG_IN_OUT:
    		if (mLoggedIn){
    			try {
					Service.logout(mUserName);
					UserInfo.toast(this, getString(R.string.logout_successfull), true);
				} catch (WebServiceException e) {
					e.printStackTrace();
				} catch (ConnectException e) {
					UserInfo.dialog(this,mUserName,getString(R.string.error_connection));
				}
    		}
    		else
    			startActivityForResult(new Intent(this,LoginActivity.class),REQUEST_ID_LOGIN);
    		
	        mDrawerLayout.closeDrawer(mLeftDrawer);
    		break;
    		
    	case POI:
    		Intent intent = new Intent(this, POIActivity.class);
    		intent.putExtra(POIActivity.EXTRA_USERNAME, mUserName);
    		startActivityForResult(intent,REQUEST_ID_POIS);
	        mDrawerLayout.closeDrawer(mLeftDrawer);
    		break;
    		
    	case RELOAD:
    		loadActivity();
    	default:
    		UserInfo.toast(this,"Auswahl nicht gefunden!", false);
    		break;
    	
    	}
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	    switch (requestCode) {
    	    case REQUEST_ID_LOGIN:
    	    	if (resultCode == RESULT_OK){
    	    		mUserName = data.getStringExtra(LoginActivity.EXTRA_USERNAME);
    	    		mLoggedIn = true;
    	    		mItemTitles.remove(DrawerItemEnum.LOG_IN_OUT.ordinal());
    	    		mItemTitles.add(DrawerItemEnum.LOG_IN_OUT.ordinal(),getString(R.string.logout));
    	    	}
    	    	break;
			case REQUEST_ID_POIS :
				if (resultCode == RESULT_OK){
					int nodeID =data.getIntExtra(POIActivity.EXTRA_NODE_ID, -1);
					//TODO: mittels der KnotenID einen neuen Zielpunkt setzen und die neue Route anzeigen	

					UserInfo.toast(this, String.valueOf(nodeID),false);					
				}
				break;
			case IntentIntegrator.REQUEST_CODE:
	    		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
	    		if(result != null){
	    			mScanResult = result.getContents();
	    			if (mScanResult != null){
		    			//TODO
	    				mJScriptService.sendTarget(Integer.valueOf(mScanResult));
		    			UserInfo.toast(this, mScanResult, false);
	    				loadURLtoMapWebView();
		    			break;
	    			}
	    		}  			
	    		UserInfo.toast(getApplicationContext(), "Scan: Es liegt kein Ergebnis vor.", false);				
	    		break;
			} 

    }
    
    
    public void loadMainFragment(){
        // update the main content by replacing fragments
        Fragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }


    /**
     * Fragment that appears in the "MainFragment", shows the Map/Image
     */
    public static class MainFragment extends Fragment {
        public static final String ARG_MAIN_NUMBER = "item_number";

        public MainFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
          
            mMapWebView = (WebView) rootView.findViewById(R.id.map_web_view);
            mMapWebView.setWebViewClient(new WebViewClient());
            JavaScriptInterface jsInterface = new JavaScriptInterface(rootView.getContext());
            mMapWebView.getSettings().setJavaScriptEnabled(true);
            mMapWebView.addJavascriptInterface(jsInterface, "jsinterface");

            loadURLtoMapWebView();
            
            return rootView;
        }
    }
    
    public static void loadURLtoMapWebView(){
    	mMapWebView.loadUrl("http://193.175.199.115/StudMapClient/?floorID=" + mSelectedFloorID);
        mMapWebView.requestFocus();
        
        mJScriptService = new JavaScriptService(mMapWebView);
    }
    
    /**
     * Initialize the custom Action Bar with an Spinner for selecting the Floor and an Textfield for Searching
     */
    public void initializeActionBar(){    	
    	
        // enable ActionBar app icon to behave as action to toggle nav drawer
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        //mActionBar.setIcon(R.drawable.ic_launcher);
        mActionBar.setTitle("");

        // implement Layout action_bar.xml as custom Action Bar
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflator.inflate(R.layout.action_bar, null);
        mActionBar.setCustomView(actionBarView);
        
        //Initialize Search TextView
        ArrayAdapter<Node> searchAdapter = new ArrayAdapter<Node>(this,android.R.layout.simple_list_item_1, mRoomList);
        mSearchTextView = (AutoCompleteTextView) findViewById(R.id.actionBarSearch);
        mSearchTextView.setAdapter(searchAdapter);
        mSearchTextView.setThreshold(1);				//AutoComplete by the First Letter
        
        mSearchTextView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
				mSelectedNode = (Node) adapterView.getItemAtPosition(pos);
				UserInfo.toast(getApplicationContext(), "Suche " + mSelectedNode.toString(), false);
				
				//TODO - Übergabe der Auswahl an JavaScript
				mJScriptService.sendTarget(mSelectedNode.getNodeID());
				loadURLtoMapWebView();
			}			
		});
        
        //Get Spinner for selecting the Level in the Action Bar
        mFloorSpinner = (Spinner) findViewById(R.id.levelSpinner);        
		mFloorSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
				Floor floor = (Floor) mFloorSpinner.getItemAtPosition(pos);
				mSelectedFloorID = floor.getId();
				
				String floorName = floor.toString() + " mit ID: " + mSelectedFloorID;
				UserInfo.toast(getApplicationContext(), floorName, true);
				loadURLtoMapWebView();
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent){
			}
		});
    }
    
    
    public void getDataFromWebService(){
    	if (mGetTasks == null){
            showProgress(true);
            mGetTasks = new GetInitDataTask(this);
            mGetTasks.execute((Void) null);
    	}
    	else
    		return;
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
							mDrawerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mInitStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mDrawerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
    
    
	public class GetInitDataTask extends AsyncTask<Void, Void, Boolean> {
		private Context mContext = null;
		private boolean mDbError = false;
		private boolean mConnError = false;
		
		public GetInitDataTask(Context ctx){
			mContext = ctx;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				mFloorList.clear();
				mRoomList.clear();
				mFloorList.addAll(Service.getFloorsForMap(Constants.MAP_ID)); 
				mRoomList.addAll(Service.getRoomsForMap(Constants.MAP_ID));
				return true;
			} catch (WebServiceException e) {
				JSONObject jObject = e.getJsonObject();
				try {
					int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
					
					switch (errorCode) {
					case ResponseError.DatabaseError:
						mDbError = true;
					}
				} catch (JSONException ignore) {}
			} catch (ConnectException e){
				mConnError = true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mGetTasks = null;
			showProgress(false);
			
			if(success){
				//Fill the Floor Spinner in the Action Bar
		        ArrayAdapter<Floor> levelAdapter = new ArrayAdapter<Floor>(mContext, 
		        												R.layout.simple_list_item_no_bg_font_white, mFloorList);
		        levelAdapter.setDropDownViewResource(R.layout.simple_list_item_black);
		        mFloorSpinner.setAdapter(levelAdapter);	
		        mSelectedFloorID = ((Floor) mFloorSpinner.getSelectedItem()).getId();
	            loadURLtoMapWebView();	        
			}
			else
				mConnError = true;
	        
			//Anzeigen der Fehler aus doInBackground
			if (mDbError)
				UserInfo.dialog(mContext, mUserName, getString(R.string.error_database));
			
			if (mConnError)
				UserInfo.dialog(mContext, mUserName, getString(R.string.error_connection));
		}

		@Override
		protected void onCancelled() {
			mGetTasks = null;
			showProgress(false);
		}
	}
}
