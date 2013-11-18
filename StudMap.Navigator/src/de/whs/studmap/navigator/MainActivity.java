package de.whs.studmap.navigator;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import de.whs.studmap.data.DrawerItemEnum;
import de.whs.studmap.snippets.UserInfo;
import de.whs.studmap.web.Service;
import de.whs.studmap.web.WebServiceException;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout mLeftDrawer;
    private ActionBar mActionBar;
    private Spinner mLevelSpinner;

    private CharSequence mTitle;
    private List<String> mItemTitles;
    
    private boolean mLoggedIn = false;
    private String mUserName = "";

    private final int REQUEST_ID_LOGIN = 101;
    private final int REQUEST_ID_POIS = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            	mActionBar.setTitle("");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle("StudMap");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
        
        initializeActionBar();
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
        switch(item.getItemId()) {
        /* case R.id.action_websearch:
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
            }
            return true;
        */
        default:
            return super.onOptionsItemSelected(item);
        }
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
    
    

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
    	
    	DrawerItemEnum sel_position = DrawerItemEnum.values()[position];
    	
    	switch (sel_position){
    	case MAP:
	        // update the main content by replacing fragments
	        Fragment fragment = new MainFragment();
	        Bundle args = new Bundle();
	        args.putInt(MainFragment.ARG_MAIN_NUMBER, position);
	        fragment.setArguments(args);
	
	        FragmentManager fragmentManager = getFragmentManager();
	        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
	
	        // update selected item and title, then close the drawer
	        mDrawerListView.setItemChecked(position, true);
	        mDrawerLayout.closeDrawer(mLeftDrawer);
    		break;
    		
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
    		startActivityForResult(new Intent(this,POIActivity.class),REQUEST_ID_POIS);
	        mDrawerLayout.closeDrawer(mLeftDrawer);
    		break;
    		
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
			}    	
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
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
            /*
            int i = getArguments().getInt(ARG_MAIN_NUMBER);            
            String planet = getResources().getStringArray(R.array.menue_item_array)[i];

            int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
                            "drawable", getActivity().getPackageName());
            */
            
            final Context c = rootView.getContext();
            /*
            int i = getArguments().getInt(ARG_MAIN_NUMBER);            
            String planet = getResources().getStringArray(R.array.menue_item_array)[i];

            int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
                            "drawable", getActivity().getPackageName());
            */
            
            final WebView MapWebView = (WebView) rootView.findViewById(R.id.map_web_view);
            MapWebView.setWebViewClient(new WebViewClient());
            JavaScriptInterface jsInterface = new JavaScriptInterface(c);
            MapWebView.getSettings().setJavaScriptEnabled(true);
            MapWebView.addJavascriptInterface(jsInterface, "jsinterface");
           
            MapWebView.loadUrl("file:///android_asset/index.html");
            
         
            
           // MapWebView.loadUrl("javascript:showPath([{x:100,y:100},{x:110,y:110},{x:110,y:100},{x:100,y:110},{x:200,y:110},{x:100,y:210}])");
          //  MapWebView.loadUrl("javascript:alert('Test')");
            
            
            new Thread() {
                @Override
                public void run() { 
                	try {
						Thread.sleep(4000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	MapWebView.loadUrl("javascript:" + "showPath([{x:100,y:100},{x:110,y:110},{x:110,y:100},{x:100,y:110},{x:200,y:110},{x:100,y:210}])");
                	MapWebView.loadUrl("javascript:" + "showCircles([{x:100,y:200},{x:120,y:210},{x:130,y:220}])");
               
                	 
                }
            }.start();
           // ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageId);
           // getActivity().setTitle(planet);
            return rootView;
        }
    }
    
    public void initializeActionBar(){
        //Example ArrayList for testing
        String[] s = {"Mensa", "Bibliothek", "Pförtner", "Aquarium", "Daniel's Büro", "A1.1.10", "A1.1.9", "Physik Labor"};
        ArrayList<String> list1 = new ArrayList<String>(Arrays.asList(s));

        // enable ActionBar app icon to behave as action to toggle nav drawer
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        //mActionBar.setIcon(R.drawable.ic_launcher);
        mActionBar.setTitle("");
        
        // implement Layout action_bar.xml as custom Action Bar
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.action_bar, null);
        mActionBar.setCustomView(v);

        ArrayAdapter<String> searchAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, list1);
        AutoCompleteTextView textView = (AutoCompleteTextView) v
                .findViewById(R.id.actionBarSearch);
        textView.setAdapter(searchAdapter);
        textView.setThreshold(1);
        
        //Spinner for selecting the Level in the Action Bar
        mLevelSpinner = (Spinner) findViewById(R.id.levelSpinner);
        String[] levels = {"Ebene 0", "Ebene 1", "Ebene 2"};
        ArrayList<String> levelList = new ArrayList<String>(Arrays.asList(levels));
       
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_black, levelList);
        // Specify the layout to use when the list of choices appears
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mLevelSpinner.setAdapter(levelAdapter);
        
        final Activity main = this;
		mLevelSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
				UserInfo.toast(main, "pos: " + pos + ", ID: " + id, true);
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent){
				
			}
		});
    }
}
