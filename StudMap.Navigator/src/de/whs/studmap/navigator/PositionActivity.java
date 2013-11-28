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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.Node;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;
import de.whs.studmap.snippets.UserInfo;

public class PositionActivity extends Activity {
	
	public static final String EXTRA_NODEID = "NodeId";
	
	//UI References
	private View mPositionFormView;
	private View mPositionStatusView;
	private TextView mPositionQuestion;
	private Button mCancelButton;
	
	//common
	private GetNodeInfoTask mAsynTask = null;
	private String mUsername  = "";
	private Integer nodeId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_position);
		
		mUsername = MainActivity.mUserName;
		
		Intent intent = getIntent();
		nodeId = Integer.parseInt(intent.getStringExtra(EXTRA_NODEID));
		
		//UI References
		mPositionFormView = findViewById(R.id.position_form);
		mPositionStatusView = findViewById(R.id.position_status);
		mPositionQuestion = (TextView) findViewById(R.id.position_question);
		
		//AsyncTask
		showProgress(true);
		mAsynTask = new GetNodeInfoTask(this);
		mAsynTask.execute( nodeId);
		
		
		//Cancel button
		mCancelButton = (Button) findViewById(R.id.position_cancel);
		mCancelButton.requestFocus();
		mCancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		//Navigation choice
		List<String> items = new ArrayList<String>();
		items.add(getString(R.string.navigationStart));
		items.add(getString(R.string.navigationDestination));
		
		ListView mListView = (ListView) findViewById(R.id.positionList);
		ArrayAdapter<String> mListAdapter = new ArrayAdapter<String>(this,R.layout.simple_list_item_white, items);
		mListView.setAdapter(mListAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					MainActivity.mJScriptService.sendStart(nodeId);
					finish();
					break;

				case 1:
					MainActivity.mJScriptService.sendDestination(nodeId);
					finish();
					break;
				}
			}			
		});
		
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

			mPositionStatusView.setVisibility(View.VISIBLE);
			mPositionStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mPositionStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mPositionFormView.setVisibility(View.VISIBLE);
			mPositionFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mPositionFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mPositionStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mPositionFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public class GetNodeInfoTask extends AsyncTask<Integer, Void, Node> {
		private Context mContext = null;
		private boolean bShowDialog = false;
		
		public GetNodeInfoTask(Context ctx){
			mContext = ctx;
		}
		
		@Override
		protected Node doInBackground(Integer... params) {
			
			try {
				Node node = Service.getNodeInformationForNode(params[0]);
				return node;
			} catch (WebServiceException e) {
				Log.d(Constants.LOG_TAG_POSITION_ACTIVITY, "GetNodeInfoTask - WebServiceException");
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
					Log.d(Constants.LOG_TAG_POSITION_ACTIVITY, "GetNodeInfoTask - Parsing the WebServiceException failed!");
				}
			} catch (ConnectException e){
				Log.d(Constants.LOG_TAG_POSITION_ACTIVITY, "GetNodeInfoTask - ConnectException");
				bShowDialog = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Node node) {
			mAsynTask = null;
			showProgress(false);

			if (node != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(node.getDisplayName());
				sb.append("\n");
				sb.append(getString(R.string.navigationTitle));
				mPositionQuestion.setText(sb.toString());
			}
			else{
				if (bShowDialog)
					UserInfo.dialog(mContext,mUsername, getString(R.string.error_connection));
			}	
		}

		@Override
		protected void onCancelled() {
			mAsynTask = null;
			showProgress(false);
		}
	}

}
