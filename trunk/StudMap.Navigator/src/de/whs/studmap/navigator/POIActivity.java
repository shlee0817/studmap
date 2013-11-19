package de.whs.studmap.navigator;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import de.whs.studmap.data.PoI;
import de.whs.studmap.web.Service;
import de.whs.studmap.web.WebServiceException;

public class POIActivity extends Activity {
	
	public static final String EXTRA_NODE_ID = "NodeID";
	
	private ListView mListView;
	private EditText mInputSearch;
	private ArrayAdapter<String> mListAdapter;
	private HashMap<String, PoI> mPOIs = new HashMap<String, PoI>();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poi);
		
		getPOIsFromWebService();
		
		mInputSearch = (EditText) findViewById(R.id.POI_inputSearch);		
		mListView = (ListView) findViewById(R.id.POI_List); 
		
		mListAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_white, new ArrayList<String>(mPOIs.keySet()));
		mListView.setAdapter(mListAdapter);
		
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

	private void getPOIsFromWebService() {
		
		List<PoI> pois = null;
		try {
			pois = Service.getPOIs();
			for (PoI n : pois){
				mPOIs.put(n.toString(),n);	
			}
		} catch (WebServiceException e) {
			// TODO handle WebServiceException 
			e.printStackTrace();
		} catch (ConnectException e){
			
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
        	PoI selectedPoI = (PoI) mPOIs.get(item);
        	
            Intent result = new Intent();
            result.putExtra(EXTRA_NODE_ID,selectedPoI.getNodeId());
            setResult(Activity.RESULT_OK,result);
            finish();
        }
    }
	
}
