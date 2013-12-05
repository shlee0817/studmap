package de.whs.studmap.navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.whs.studmap.client.core.data.Node;
import de.whs.studmap.client.listener.OnGetRoomsTaskListener;
import de.whs.studmap.client.tasks.GetRoomsTask;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class RoomsSuggestionsProvider extends ContentProvider implements
		OnGetRoomsTaskListener {

	public static String AUTHORITY = "de.whs.fia.studmap.navigator.RoomsSuggestionsProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/rooms");

	// MIME types used for searching words or looking up a single definition
	public static final String WORDS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.whs.fia.studmap.navigator";

	// UriMatcher stuff
	private static final int SEARCH_ROOM = 0;
	private static final int SEARCH_SUGGEST = 1;
	private static final UriMatcher sURIMatcher = buildUriMatcher();

	private static final String[] COLUMNS = new String[] { "_id",
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_QUERY};

	private List<Node> mRoomList = new ArrayList<Node>();

	/**
	 * Builds up a UriMatcher for search suggestion and shortcut refresh
	 * queries.
	 */
	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		// to get definitions...
		matcher.addURI(AUTHORITY, "rooms", SEARCH_ROOM);
		// to get suggestions...
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
				SEARCH_SUGGEST);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*",
				SEARCH_SUGGEST);
		return matcher;
	}

	@Override
	public String getType(Uri uri) {

		switch (sURIMatcher.match(uri)) {
		case SEARCH_ROOM:
			return WORDS_MIME_TYPE;
		case SEARCH_SUGGEST:
			return SearchManager.SUGGEST_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public boolean onCreate() {

		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Use the UriMatcher to see what kind of query we have and format the
		// db query accordingly
		switch (sURIMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			if (selectionArgs == null) {
				throw new IllegalArgumentException(
						"selectionArgs must be provided for the Uri: " + uri);
			}
			return getSuggestions(selectionArgs[0]);
		case SEARCH_ROOM:
			if (selectionArgs == null) {
				throw new IllegalArgumentException(
						"selectionArgs must be provided for the Uri: " + uri);
			}
			return search(selectionArgs[0]);
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	private Cursor getSuggestions(String query) {
		query = query.toLowerCase(new Locale("de"));

		final MatrixCursor cursor = new MatrixCursor(COLUMNS);

		if (!query.isEmpty()) {

			for (Node room : mRoomList) {

				String displayName = room.getDisplayName().toLowerCase(new Locale("de"));
				String roomName = room.getRoomName().toLowerCase(new Locale("de"));

				if (displayName.contains(query) || roomName.contains(query)) {

					String[] row = new String[] {
							String.valueOf(room.getNodeID()), displayName,
							roomName, room.toJson() };
					cursor.addRow(row);
				}
			}
		}

		return cursor;
	}

	private Cursor search(String query) {

		final MatrixCursor cursor = new MatrixCursor(COLUMNS);
		query = query.toLowerCase(new Locale("de"));

		Log.i("ProviderSearch", query);

		if (!query.isEmpty()) {

			for (Node room : mRoomList) {

				String displayName = room.getDisplayName().toLowerCase(new Locale("de"));
				String roomName = room.getRoomName().toLowerCase(new Locale("de"));

				if (displayName.equals(query) || roomName.equals(query)) {

					String[] row = new String[] {
							String.valueOf(room.getNodeID()), displayName,
							roomName, room.toJson() };
					cursor.addRow(row);
				}
			}
		}

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	private int mMapId;

	public void setMapId(int mapId) {

		this.mMapId = mapId;
		GetRoomsTask mGetRoomsTask = new GetRoomsTask(this, mMapId);
		mGetRoomsTask.execute((Void) null);
	}

	@Override
	public void onGetRoomsSuccess(List<Node> nodes) {

		mRoomList = nodes;
	}

	@Override
	public void onGetRoomsError(int responseError) {
	}

	@Override
	public void onGetRoomsCanceled() {
	}
}
