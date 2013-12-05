package de.whs.studmap.fragments;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import de.whs.studmap.client.core.data.Map;
import de.whs.studmap.client.listener.OnGenericTaskListener;
import de.whs.studmap.client.tasks.GetMapsTask;
import de.whs.studmap.navigator.R;

public class InitialSetupFragment extends DialogFragment implements
		OnGenericTaskListener<List<Map>> {

	private ArrayAdapter<Map> mMapsAdapter;
	private Spinner mMapSpinner;
	protected Map mSelectedMap;

	OnMapSelectedListener mCallback;

	public static InitialSetupFragment newInstance() {

		InitialSetupFragment f = new InitialSetupFragment();

		return f;
	}

	public interface OnMapSelectedListener {

		public void onMapSelected(Map map);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.fragment_initial_setup, null);
		builder.setTitle(getString(R.string.initialSetup_title));
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.intialSetup_start_txt, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (mSelectedMap == null) {
					Toast.makeText(getActivity(),
							"Es wird eine Map für Messungen benötigt!",
							Toast.LENGTH_LONG).show();
					return;
				}

				mCallback.onMapSelected(mSelectedMap);
				dismiss();
			}
		});
		
		builder.setView(rootView);
		
		initializeSpinner(rootView);
		fillMapSpinner();

		return builder.create();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnMapSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	private void fillMapSpinner() {

		GetMapsTask mTask = new GetMapsTask(this);
		mTask.execute((Void) null);
	}

	private void initializeSpinner(View rootView) {

		mMapsAdapter = new ArrayAdapter<Map>(rootView.getContext(),
				android.R.layout.simple_spinner_item);
		mMapsAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mMapSpinner = (Spinner) rootView
				.findViewById(R.id.initialSetup_mapSpinner);

		mMapSpinner.setAdapter(mMapsAdapter);

		mMapSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {

				mSelectedMap = (Map) parent.getItemAtPosition(pos);

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

				mSelectedMap = null;
			}
		});
	}

	//GetMapsTask
	@Override
	public void onSuccess(List<Map> maps) {
		if (maps != null)
			mMapsAdapter.addAll(maps);
	}

	//GetMapsTask
	@Override
	public void onError(int responseError) {
		// TODO Auto-generated method stub
		
	}

	//GetMapsTask
	@Override
	public void onCanceled() {
		// TODO Auto-generated method stub
		
	}
}
