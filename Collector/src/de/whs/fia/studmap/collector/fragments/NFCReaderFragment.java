package de.whs.fia.studmap.collector.fragments;

import java.net.ConnectException;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.whs.fia.studmap.collector.R;
import de.whs.studmap.client.core.web.Service;
import de.whs.studmap.client.core.web.WebServiceException;

public class NFCReaderFragment extends Fragment {

	private NfcAdapter mNfcAdapter;
	private TextView mLogTextView;
	private EditText mNodeIdEditText;
	private String mNfcTag = null;

	public NFCReaderFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_nfc_reader,
				container, false);

		mLogTextView = (TextView) rootView.findViewById(R.id.NfcReader_TextView);
		mNodeIdEditText = (EditText) rootView.findViewById(R.id.NfcReader_Node);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
		if (mNfcAdapter == null) {
			// we definitely need NFC
			Toast.makeText(getActivity(), "This device doesn't support NFC.",
					Toast.LENGTH_LONG).show();
			return rootView;
		}
		
		setTextView();

		Button save = (Button) rootView.findViewById(R.id.NfcReader_Save);
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String node = mNodeIdEditText.getText().toString();
				SaveNFCTagForNodeTask task = new SaveNFCTagForNodeTask(Integer
						.parseInt(node), mNfcTag);
				task.execute((Void) null);
				
				mNfcTag = null;
				setTextView();
				mNodeIdEditText.setText("");
			}
		});

		return rootView;
	}

	private void setTextView() {

		if (!mNfcAdapter.isEnabled()) {
			mLogTextView.setText("NFC is disabled.");
		} else {
			if (mNfcTag == null)
				mLogTextView
						.setText("NFC is enabled. Waiting for discovering a nfc tag.");
			else {

				String s = "Tag discovered!\n\n";
				s += "UID: " + mNfcTag + "\n";

				mLogTextView.setText(s);
			}
		}
	}

	private String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}

		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++) {
			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
			stringBuilder.append(buffer);
		}

		return stringBuilder.toString();
	}

	public void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			mNfcTag = bytesToHexString(tag.getId());
			setTextView();
		}
	}

	class SaveNFCTagForNodeTask extends AsyncTask<Void, Void, Boolean> {

		private int nodeId;
		private String nfcTag;

		public SaveNFCTagForNodeTask(int nodeId, String nfcTag) {
			this.nodeId = nodeId;
			this.nfcTag = nfcTag;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				boolean result = Service.SaveNFCTagForNode(nodeId, nfcTag);
				return result;
			} catch (WebServiceException e) {
				e.printStackTrace();
			} catch (ConnectException e) {
				e.printStackTrace();
			}
			return false;
		}
	}

}
