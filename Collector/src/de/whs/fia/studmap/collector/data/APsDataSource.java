package de.whs.fia.studmap.collector.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import de.whs.fia.studmap.collector.models.AP;

public class APsDataSource {
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.APS_COLUMN_BSSID, MySQLiteHelper.APS_COLUMN_RSS,
			MySQLiteHelper.APS_COLUMN_SCANID };

	public APsDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public AP createAP(String BSSID, int RSS, int ScanId) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.APS_COLUMN_BSSID, BSSID);
		values.put(MySQLiteHelper.APS_COLUMN_RSS, RSS);
		values.put(MySQLiteHelper.APS_COLUMN_SCANID, ScanId);
		long insertId = database.insert(MySQLiteHelper.TABLE_APS, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_APS, allColumns,
				MySQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null,
				null);
		cursor.moveToFirst();
		AP newAP = cursorToAP(cursor);
		cursor.close();
		return newAP;
	}

	public List<AP> getAPsToScan(int scanId) {

		List<AP> aps = new ArrayList<AP>();
		
		Cursor cursor = database.query(MySQLiteHelper.TABLE_APS, allColumns,
				MySQLiteHelper.APS_COLUMN_SCANID + " = " + scanId, null, null,
				null, null);
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast()){
			
			AP ap = cursorToAP(cursor);
			aps.add(ap);
			cursor.moveToNext();
		}
		cursor.close();
		
		return aps;
	}

	private AP cursorToAP(Cursor cursor) {
		AP ap = new AP();
		ap.setId(cursor.getInt(0));
		ap.setScanId(cursor.getInt(1));
		ap.setBSSID(cursor.getString(2));
		ap.setRSS(cursor.getInt(3));
		return ap;
	}
}
