package de.whs.fia.studmap.collector.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import de.whs.fia.studmap.collector.models.Scan;

public class ScansDataSource {

	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.SCANS_COLUMN_NODEID };

	public ScansDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void clear(){
		database.execSQL("delete from " + MySQLiteHelper.TABLE_SCANS);
	}
	
	public Scan createScan(int nodeId) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.SCANS_COLUMN_NODEID, nodeId);
		long insertId = database.insert(MySQLiteHelper.TABLE_SCANS, null,
				values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SCANS,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		Scan newScan = cursorToScan(cursor);
		cursor.close();
		return newScan;
	}
	
	public List<Scan> getScans(){
		List<Scan> scans = new ArrayList<Scan>();
		
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SCANS, allColumns, null, null, null, null, null);
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast()){
			Scan s = cursorToScan(cursor);
			scans.add(s);
			cursor.moveToNext();
		}
		cursor.close();
		return scans;
	}
	
	private Scan cursorToScan(Cursor cursor) {
		Scan scan = new Scan();
	    scan.setId(cursor.getInt(0));
	    scan.setNodeId(cursor.getInt(1));
	    return scan;
	  }
}
