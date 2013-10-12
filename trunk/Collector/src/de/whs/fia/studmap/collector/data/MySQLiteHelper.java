package de.whs.fia.studmap.collector.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_SCANS = "scans";
	public static final String COLUMN_ID = "_id";
	public static final String SCANS_COLUMN_NODEID = "NodeId";
	
	public static final String TABLE_APS = "accesspoints";
	public static final String APS_COLUMN_SCANID = "ScanId";
	public static final String APS_COLUMN_BSSID = "BSSID";
	public static final String APS_COLUMN_RSS = "RSS";
	
	private static final String DATABASE_NAME = "studmap.db";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE_SCANS = "create table "
			+ TABLE_SCANS + "(" 
			+ COLUMN_ID	+ " integer primary key autoincrement, " 
			+ SCANS_COLUMN_NODEID + " integer not null);";
	
	private static final String DATABASE_CREATE_APS = "create table "
			+ TABLE_APS + "(" 
			+ COLUMN_ID	+ " integer primary key autoincrement, " 
			+ APS_COLUMN_SCANID + " integer not null, "
			+ APS_COLUMN_BSSID + " text not null, "
			+ APS_COLUMN_RSS + " integer not null, " 
			+ "FOREIGN KEY (" + APS_COLUMN_SCANID + ") REFERENCES " + TABLE_SCANS + " (" + COLUMN_ID + "));";

	public MySQLiteHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		
	    database.execSQL(DATABASE_CREATE_SCANS);
	    database.execSQL(DATABASE_CREATE_APS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCANS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_APS);
		onCreate(db);
	}

}
