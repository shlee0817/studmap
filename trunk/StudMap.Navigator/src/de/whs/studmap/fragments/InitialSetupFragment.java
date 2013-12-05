package de.whs.studmap.fragments;

import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = (View) inflater.inflate(
				R.layout.fragment_initial_setup, container, false);

		getDialog().setTitle(getString(R.string.initialSetup_title));
		setCancelable(false);
		
		initializeSpinner(rootView);
		initializeButton(rootView);
		fillMapSpinner();

		return rootView;
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

	private void initializeButton(View rootView) {
		Button loadFloorplanBtn = (Button) rootView
				.findViewById(R.id.initialSetup_LoadBtn_id);
		loadFloorplanBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mSelectedMap == null) {
					Toast.makeText(getActivity(),
							"Es wird eine Map für Messungen benötigt!",
							Toast.LENGTH_LONG).show();
					return;
				}

				mCallback.onMapSelected(mSelectedMap);
				getDialog().dismiss();
			}
		});
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
