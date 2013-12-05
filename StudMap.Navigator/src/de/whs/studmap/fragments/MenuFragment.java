package de.whs.studmap.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import de.whs.studmap.client.core.data.Floor;
import de.whs.studmap.client.core.data.Map;
import de.whs.studmap.navigator.MenuExpandableListAdapter;
import de.whs.studmap.navigator.OnMenuItemListener;
import de.whs.studmap.navigator.R;
import de.whs.studmap.navigator.models.MenuGroup;
import de.whs.studmap.navigator.models.MenuItem;

public class MenuFragment extends Fragment {

	private SparseArray<MenuGroup> mMenuGroups = new SparseArray<MenuGroup>();
	private int menuGroupKey = 0;
	private ExpandableListView expandableListView;
	private MenuExpandableListAdapter mMenuListAdapter;
	private OnMenuItemListener mCallback;

	public MenuFragment() {
		// Empty constructor required for fragment subclasses
	}

	public static MenuFragment newInstance() {

		MenuFragment f = new MenuFragment();
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_menu, container,
				false);

		expandableListView = (ExpandableListView) rootView
				.findViewById(R.id.expandableListView);

		expandableListView.setGroupIndicator(getResources().getDrawable(
				android.R.color.transparent));

		expandableListView.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);

		expandableListView
				.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

					@Override
					public boolean onGroupClick(ExpandableListView parent,
							View v, int groupPosition, long id) {

						mCallback.onMenuItemClicked(mMenuGroups.get(
								groupPosition).getString());
						return false;
					}
				});

		expandableListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				mCallback.onFloorChanged(mMenuGroups.get(groupPosition)
						.getChildren().get(childPosition).getId());
				
				setCheckedItem(groupPosition, childPosition);
				return false;
			}
		});

		addDefaultMenuItems();

		mMenuListAdapter = new MenuExpandableListAdapter(getActivity(),
				mMenuGroups);

		expandableListView.setAdapter(mMenuListAdapter);

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnMenuItemListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnMenuItemListener");
		}
	}

	private void addDefaultMenuItems() {

		ArrayList<String> menuItems = new ArrayList<String>(
				Arrays.asList(getResources().getStringArray(
						R.array.menu_items_array)));

		for (int i = 0; i < menuItems.size(); i++) {

			String menuItem = menuItems.get(i);
			mMenuGroups.append(menuGroupKey++, new MenuGroup(menuItem));
		}
	}

	public void setLoadedMap(Map map, List<Floor> floors) {

		List<MenuItem> children = new ArrayList<MenuItem>();
		for (Floor floor : floors) {
			children.add(new MenuItem(floor.getName(), floor.getId()));
		}
		MenuGroup mapGroup = new MenuGroup("Westfälische Hochschule");
		mapGroup.setChildren(children);

		resetGroups();
		mMenuGroups.append(menuGroupKey++, mapGroup);

		expandableListView.expandGroup(menuGroupKey - 1);

		if (children.size() > 0) {
			setCheckedItem(menuGroupKey - 1, 0);
		}

		addDefaultMenuItems();

		mMenuListAdapter.notifyDataSetChanged();
	}

	public void selectFloorItem(int floorId) {
		
		int groupPosition = 0, itemPosition = 0;
		for (int i = 0; i < mMenuGroups.size(); i++) {

			MenuGroup g = mMenuGroups.get(i);
			
			for(int j = 0; j < g.getChildren().size(); j++) {
				
				MenuItem item = g.getChildren().get(j);
				if(item.getId() == floorId){
					groupPosition = i;
					itemPosition = j;
					break;
				}
			}
		}
		
		setCheckedItem(groupPosition, itemPosition);
	}
	
	public void toggleLoginItem(boolean isLoggedIn) {

		MenuGroup logInOut = null;
		for (int i = 0; i < mMenuGroups.size(); i++) {

			MenuGroup g = mMenuGroups.get(i);
			String item = g.getString();

			if (item.equals(getString(R.string.menu_login))
					|| item.equals(getString(R.string.menu_logout))) {

				logInOut = g;
				break;
			}
		}

		if (logInOut != null) {

			if (isLoggedIn) {

				logInOut.setString(getString(R.string.menu_logout));
			} else {

				logInOut.setString(getString(R.string.menu_login));
			}
			mMenuListAdapter.notifyDataSetChanged();
		}
	}

	private void setCheckedItem(int groupIndex, int childIndex) {

		int index = expandableListView.getFlatListPosition(ExpandableListView
				.getPackedPositionForChild(groupIndex, childIndex));
		expandableListView.setItemChecked(index, true);
	}

	private void resetGroups() {
		mMenuGroups.clear();
		menuGroupKey = 0;
	}
}
