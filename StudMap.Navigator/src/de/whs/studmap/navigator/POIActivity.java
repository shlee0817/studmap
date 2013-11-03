package de.whs.studmap.navigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import de.whs.studmap.data.Node;
import de.whs.studmap.web.Service;
import de.whs.studmap.web.WebServiceException;

public class POIActivity extends Activity {
	
	public static final String EXTRA_NODE_ID = "NodeID";
	
	private ListView mListView;
	private EditText mInputSearch;
	private ArrayAdapter<String> mListAdapter;
	private HashMap<String, Node> mPOIs = new HashMap<String, Node>();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poi);
		// Show the Up button in the action bar.
		setupActionBar();
		
		getPOIsFromWebService();
		
		mInputSearch = (EditText) findViewById(R.id.POI_inputSearch);		
		mListView = (ListView) findViewById(R.id.POI_List); 
		
		mListAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_white, new ArrayList<String>(mPOIs.keySet()));
		mListView.setAdapter(mListAdapter);
		
		//Listener
		mInputSearch.addTextChangedListener(new mTextWatcher());
		mListView.setOnItemClickListener(new ItemClickListener());
        
	}
	

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.poi, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void getPOIsFromWebService() {
		/*Test
		for(int i = 0; i < 3; i++){
			Node n = new Node(i,"ABC" + i);
			mPOIs.put(n.getName(),n);	
		}
		for(int i = 0; i < 3; i++){
			Node n = new Node(i+3,"DEF" + i);
			mPOIs.put(n.getName(),n);	
		}*/
		
		List<Node> nodes = null;
		try {
			nodes = Service.getPOIs();
			for (Node n : nodes){
				mPOIs.put(n.getName(),n);	
			}
		} catch (WebServiceException e) {
			// TODO handle WebServiceException 
			e.printStackTrace();
		}

		
	}
	
	private class mTextWatcher implements TextWatcher{
		@Override
		public void onTextChanged(CharSequence s, int start, int before,int count) {
			mListAdapter.getFilter().filter(s);
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
        	String item = (String) mListView.getItemAtPosition(position);
        	Node selectedNode = (Node) mPOIs.get(item);
        	
            Intent result = new Intent();
            result.putExtra(EXTRA_NODE_ID,selectedNode.getNodeID());
            setResult(Activity.RESULT_OK,result);
            finish();
        }
    }
	
}
