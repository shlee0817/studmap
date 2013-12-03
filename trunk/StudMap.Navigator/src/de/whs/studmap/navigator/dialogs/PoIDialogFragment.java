package de.whs.studmap.navigator.dialogs;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import de.whs.studmap.client.core.data.Constants;
import de.whs.studmap.client.core.data.PoI;
import de.whs.studmap.client.listener.OnGenericTaskListener;
import de.whs.studmap.client.listener.OnPoIDialogListener;
import de.whs.studmap.client.tasks.GetPoITask;
import de.whs.studmap.navigator.R;

public class PoIDialogFragment extends DialogFragment implements Constants,
		OnGenericTaskListener<List<PoI>> {

	private GetPoITask mTask = null;
	private int mMapId;
	private OnPoIDialogListener mCallback;
	private ArrayAdapter<PoI> mListAdapter;
	private ListView mListView;

	public static PoIDialogFragment newInstance(int mapId){
		
		Bundle args = new Bundle();
		args.putInt("MapId", mapId);
		
		PoIDialogFragment dialog = new PoIDialogFragment();
		dialog.setArguments(args);
		
		return dialog;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.fragment_poi_dialog, null);
		builder.setView(rootView);
		
		if (getArguments().containsKey("MapId"))
			mMapId = getArguments().getInt("MapId");

		EditText mInputSearch = (EditText) rootView
				.findViewById(R.id.POI_inputSearch);
		mListView = (ListView) rootView.findViewById(R.id.POI_List);

		getPOIsFromWebService();

		// Listener
		mInputSearch.addTextChangedListener(new mTextWatcher());
		mListView.setOnItemClickListener(new ItemClickListener());

		return builder.create();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnPoIDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnLoginDialogListener");
		}
	}

	private class mTextWatcher implements TextWatcher {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			String input = s.toString().trim();
			mListAdapter.getFilter().filter(input);
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// nothing to do
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// nothing to do
		}
	}

	private class ItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			PoI selectedPoI = (PoI) parent.getItemAtPosition(position);
			mCallback.onPoISelected(selectedPoI.getNode());
			dismiss();
		}
	}

	private void getPOIsFromWebService() {

		if (mTask != null)
			return;

		mTask = new GetPoITask(this, mMapId);
		mTask.execute((Void) null);
	}

	@Override
	public void onSuccess(List<PoI> object) {

		mListAdapter = new ArrayAdapter<PoI>(getActivity(),
				R.layout.simple_list_item_white, object);
		mListView.setAdapter(mListAdapter);
	}

	@Override
	public void onCanceled() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(int responseError) {

	}
}
