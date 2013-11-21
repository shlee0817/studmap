package de.whs.studmap.navigator;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import de.whs.studmap.data.Constants;
import de.whs.studmap.data.PoI;
import de.whs.studmap.snippets.UserInfo;
import de.whs.studmap.web.ResponseError;
import de.whs.studmap.web.Service;
import de.whs.studmap.web.WebServiceException;

public class POIActivity extends Activity implements Constants{
	
	public static final String EXTRA_NODE_ID = "NodeID";
	public static final String EXTRA_USERNAME = "UserName";
	
	private ListView mListView;
	private EditText mInputSearch;
	private ArrayAdapter<PoI> mListAdapter;
	private List<PoI> mPOIs = new ArrayList<PoI>();
	private GetDataTask mTask = null;
	private String mUsername  = "";
	private View mStatusView;
	private View mFormView;

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poi);
		
		Intent intent = getIntent();
		mUsername = intent.getStringExtra(EXTRA_USERNAME);
		
		mFormView = findViewById(R.id.getPoI_form);
		mStatusView = findViewById(R.id.getPoIData_status);
		
		mInputSearch = (EditText) findViewById(R.id.POI_inputSearch);		
		mListView = (ListView) findViewById(R.id.POI_List); 
			
		getPOIsFromWebService();
		
		//Listener
		mInputSearch.addTextChangedListener(new mTextWatcher());
		mListView.setOnItemClickListener(new ItemClickListener());
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.poi, menu);
		return true;
	}
	
	private class mTextWatcher implements TextWatcher{
		@Override
		public void onTextChanged(CharSequence s, int start, int before,int count) {
			String input = s.toString().trim();			
			mListAdapter.getFilter().filter(input);
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			//nothing to do
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			//nothing to do
		}
	}
	
    private class ItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	PoI selectedPoI = (PoI) parent.getItemAtPosition(position);
        	
            Intent result = new Intent();
            result.putExtra(EXTRA_NODE_ID,selectedPoI.getNode().getNodeID());
            setResult(Activity.RESULT_OK,result);
            finish();
        }
    }
    
    private void getPOIsFromWebService() {
    	if (mTask != null)
    		return;
    	
    	showProgress(true);
		mTask = new GetDataTask(this);
		mTask.execute((Void) null);
    }
    
    /**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mStatusView.setVisibility(View.VISIBLE);
			mStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mFormView.setVisibility(View.VISIBLE);
			mFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
    
    /**
	 * Represents an asynchronous task used to get Data from Webservice
	 * the user.
	 */
	public class GetDataTask extends AsyncTask<Void, Void, Boolean> {
		private Context mContext = null;
		private boolean bShowDialog = false;
		
		public GetDataTask(Context ctx){
			mContext = ctx;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				List<PoI> pois = Service.getPoIsForMap(MAP_ID);
				mPOIs.addAll(pois);
				return true;
			} catch (WebServiceException e) {
				Log.d(LOG_TAG_POI__ACTIVITY, "GetDataTask - WebServiceException");
				JSONObject jObject = e.getJsonObject();
				
				try {
					int errorCode = jObject.getInt(Service.RESPONSE_ERRORCODE);
					
					switch (errorCode) {
					case ResponseError.DatabaseError:
						bShowDialog = true;
						break;
					default:
						break;
					}
				} catch (JSONException ignore) {
					Log.d(LOG_TAG_POI__ACTIVITY, "GetDataTask - Parsing the WebServiceException failed!");
				}
			} catch (ConnectException e){
				Log.d(LOG_TAG_POI__ACTIVITY, "GetDataTask - ConnectException");
				bShowDialog = true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mTask = null;
			showProgress(false);
			if (success) {
				mListAdapter = new ArrayAdapter<PoI>(mContext, R.layout.simple_list_item_white, mPOIs);
				mListView.setAdapter(mListAdapter);
			}
			else{
				if (bShowDialog)
					UserInfo.dialog(mContext, mUsername, getString(R.string.error_connection));
			}				
		}

		@Override
		protected void onCancelled() {
			mTask = null;
			showProgress(false);
		}
	}
	
}
