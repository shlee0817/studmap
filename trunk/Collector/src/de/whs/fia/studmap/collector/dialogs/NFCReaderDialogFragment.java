package de.whs.fia.studmap.collector.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.whs.fia.studmap.collector.R;
import de.whs.fia.studmap.collector.tasks.SaveNFCTagForNodeTask;
import de.whs.studmap.client.core.snippets.NFC;

public class NFCReaderDialogFragment extends DialogFragment {
	
	private NfcAdapter mNfcAdapter;
	private TextView mLogTextView;
	private EditText mNodeIdEditText;
	private String mNfcTag = null;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.fragment_nfc_reader, null);

		builder.setView(rootView).setPositiveButton(
				R.string.NfcReader_SaveButton,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						String node = mNodeIdEditText.getText().toString();
						SaveNFCTagForNodeTask task = new SaveNFCTagForNodeTask(
								Integer.parseInt(node), mNfcTag);
						task.execute((Void) null);

						mNfcTag = null;
						setTextView();
						mNodeIdEditText.setText("");
					}
				});

		mLogTextView = (TextView) rootView
				.findViewById(R.id.NfcReader_TextView);
		mNodeIdEditText = (EditText) rootView.findViewById(R.id.NfcReader_Node);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
		if (mNfcAdapter == null) {
			// we definitely need NFC
			Toast.makeText(getActivity(), "This device doesn't support NFC.",
					Toast.LENGTH_LONG).show();
			return builder.create();
		}

		if(getArguments().containsKey("TagUID")){
			mNfcTag = getArguments().getString("TagUID");
		}
		
		if(getArguments().containsKey("NodeId")){
			mNodeIdEditText.setText(getArguments().getString("NodeId"));
		}
		
		setTextView();

		return builder.create();
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
	
	public void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			mNfcTag = NFC.BytesToHexString(tag.getId());
			setTextView();
		}
	}

	public void setNFCTag(String nfcTag){
		mNfcTag = nfcTag;
		setTextView();
	}
}
