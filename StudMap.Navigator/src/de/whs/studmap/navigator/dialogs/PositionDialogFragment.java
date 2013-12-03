package de.whs.studmap.navigator.dialogs;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.whs.studmap.client.core.data.Node;
import de.whs.studmap.client.core.web.ResponseError;
import de.whs.studmap.client.listener.OnGenericTaskListener;
import de.whs.studmap.client.listener.OnPositionDialogListener;
import de.whs.studmap.client.tasks.GetNodeInfoTask;
import de.whs.studmap.navigator.R;

public class PositionDialogFragment extends DialogFragment implements
		OnGenericTaskListener<Node> {

	// UI References
	private TextView mPositionQuestion;

	// common
	private Integer mNodeId;

	private OnPositionDialogListener mCallback;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.fragment_position_dialog,
				null);

		// Cancel button
		builder.setView(rootView).setNegativeButton(R.string.cancel_button,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						dismiss();
					}
				});

		if (getArguments().containsKey("NodeId"))
			mNodeId = getArguments().getInt("NodeId");

		// UI References
		mPositionQuestion = (TextView) rootView
				.findViewById(R.id.position_question);

		// AsyncTask
		GetNodeInfoTask mAsynTask = new GetNodeInfoTask(this, mNodeId);
		mAsynTask.execute();

		// Navigation choice
		List<String> items = new ArrayList<String>();
		items.add(getString(R.string.navigationStart));
		items.add(getString(R.string.navigationDestination));

		ListView mListView = (ListView) rootView
				.findViewById(R.id.positionList);
		ArrayAdapter<String> mListAdapter = new ArrayAdapter<String>(
				getActivity(), R.layout.simple_list_item_white, items);
		mListView.setAdapter(mListAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					mCallback.onSetTarget(mNodeId);
					dismiss();
					break;

				case 1:
					mCallback.onSetDestination(mNodeId);
					dismiss();
					break;
				}
			}
		});

		return builder.create();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnPositionDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnPositionDialogListener");
		}
	}
	
	@Override
	public void onSuccess(Node node) {

		if (node != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(node.getDisplayName());
			sb.append("\n");
			sb.append(getString(R.string.navigationTitle));
			mPositionQuestion.setText(sb.toString());
		}
	}

	@Override
	public void onError(int responseError) {

		switch (responseError) {
		case ResponseError.DatabaseError:
			// bShowDialog = true;
			break;
		default:
			break;
		}
	}

	@Override
	public void onCanceled() {
		// TODO Auto-generated method stub

	}
}
