package de.whs.studmap.navigator;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import de.whs.studmap.navigator.models.MenuGroup;

public class MenuExpandableListAdapter extends BaseExpandableListAdapter {

	private final SparseArray<MenuGroup> groups;
	private LayoutInflater inflater;

	public MenuExpandableListAdapter(Activity act,
			SparseArray<MenuGroup> menuGroups) {

		this.groups = menuGroups;
		inflater = act.getLayoutInflater();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {

		return groups.get(groupPosition).getChildren().get(childPosition).getString();
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		final String children = (String) getChild(groupPosition, childPosition);
		TextView text = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.menu_details, null);
		}
		text = (TextView) convertView.findViewById(R.id.menu_details_text);
		text.setText(children);
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {

		return groups.get(groupPosition).getChildren().size();
	}

	@Override
	public Object getGroup(int groupPosition) {

		return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {

		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {

		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.menu_group, null);
		}
		MenuGroup group = (MenuGroup) getGroup(groupPosition);
		((CheckedTextView) convertView).setText(group.getString());
		((CheckedTextView) convertView).setChecked(isExpanded);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {

		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {

		return true;
	}

}
