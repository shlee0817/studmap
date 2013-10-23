package de.whs.fia.studmap.collector.fragments;

import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.whs.fia.studmap.collector.R;
import de.whs.fia.studmap.collector.models.AP;
import de.whs.fia.studmap.collector.models.NfcTag;
import de.whs.fia.studmap.collector.models.Scan;

public class NFCReaderFragment extends Fragment {
	
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "Nfc";
    
	
	private NfcAdapter mNfcAdapter;
	private TextView mTextView;
	private EditText mEditText;
	private NfcTag mNfcTag = null; 
	
	public NFCReaderFragment(){
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_nfc_reader,
				container, false);
		
		mTextView = (TextView) rootView.findViewById(R.id.NfcReader_TextView);
		mEditText = (EditText) rootView.findViewById(R.id.NfcReader_Node);
						
		mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (mNfcAdapter == null) {
            //we definitely need NFC
            Toast.makeText(getActivity(), "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            return rootView;
        }
        if (!mNfcAdapter.isEnabled()) {        	
        		mTextView.setText("NFC is disabled.");        	
        } else {
        	if (mNfcTag == null)
        		mTextView.setText("NFC is enabled. Waiting for discovering a nfc tag.");
        	else
        		setTextView();
        }
        
        Button save = (Button) rootView.findViewById(R.id.NfcReader_Save);
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String node = mEditText.getText().toString();
				//TODO mNfcTag entsprechendem Knoten in DB zuweisen.				
			}
		});
        
		return rootView;
	}
	
	private void setTextView(){
		
		
		String s = "Tag discovered!\n\n";
		s += "UID: " + mNfcTag.getId() + "\n";
		s += "Inhalt: " + mNfcTag.getTagInfo();
		
		mTextView.setText(s);
	}
	
		
	public void handleIntent(Intent intent){
	    String action = intent.getAction();
	    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
	    	
	    	
	        String type = intent.getType();
	        if (MIME_TEXT_PLAIN.equals(type)) {
	            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	            NdefReaderTask ndefReader = new NdefReaderTask(new FragmentCallback() {
					
					@Override
					public void onTaskDone(String result) {
						mNfcTag = new NfcTag(result);
						setTextView();
					}
	            });
	            
	            ndefReader.execute(tag);
	            
	        } else {
	            Log.d(TAG, "Wrong mime type: " + type);
	        }
	    } 
	} 
	
	private interface FragmentCallback{
		public void onTaskDone(String result);
	}
	
	/**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
    	private FragmentCallback mFragmentCallback;
    	
    	public NdefReaderTask(FragmentCallback fragmentCallback){
    		mFragmentCallback = fragmentCallback;
    	}
    	
        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
               // if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                    	String s = readId(tag) + NfcTag.STRINGSEPARATOR + readText(ndefRecord); 
                        return s;
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                //}
            }
            return null;
        }
        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            /*
             * See NFC forum specification for "Text Record Type Definition" at 3.2.1
             *
             * http://www.nfc-forum.org/specs/
             *
             * bit_7 defines encoding
             * bit_6 reserved for future use, must be 0
             * bit_5..0 length of IANA language code
             */
            byte[] payload = record.getPayload();
            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            // Get the Language Code
            //int languageCodeLength = payload[0] & 0063;
            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"
            // Get the Text
           // return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
            return new String(payload, textEncoding);
        }
        
        private String readId(Tag tag) throws UnsupportedEncodingException {
            byte[] id = tag.getId();
            
            
            StringBuilder stringBuilder = new StringBuilder("0x");
            if (id == null || id.length <= 0) {
                return null;
            }

            //Convert byteArray to Hex
            char[] buffer = new char[2];
            for (int i = 0; i < id.length; i++) {
                buffer[0] = Character.forDigit((id[i] >>> 4) & 0x0F, 16);  
                buffer[1] = Character.forDigit(id[i] & 0x0F, 16);  
                System.out.println(buffer);
                stringBuilder.append(buffer);
            }

            return stringBuilder.toString();
        }
        
        @Override
        protected void onPostExecute(String result) {
            try {
            	if (result != null) {
            		mFragmentCallback.onTaskDone(result);
            	}	
			} catch (Exception e) {
				e.printStackTrace();        	
			}
        }
    }
	
	
	
}
