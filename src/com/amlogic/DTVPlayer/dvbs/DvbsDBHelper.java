package com.amlogic.DTVPlayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DvbsDBHelper extends SQLiteOpenHelper{
	private static final String DB_NAME = "sat_transponder.db";
	
	public static final String DB_TABLE_SAT_INFO ="SatTableInfo";
	public static final String DB_TABLE_TS_INFO ="TsTableInfo";
	public static final String DB_TABLE_SCANLIST_SAT_INFO = "ScanListSatTableInfo";
	public static final String DB_TABLE_SCANLIST_TS_INFO = "ScanListTsTableInfo";

	private static final String CREATE_TBL_SAT_INFO = "create table " + " SatTableInfo(id integer primary key, sat_id integer, name integer, flags integer, position integer,lnb_type integer,lnb_low integer,lnb_high integer, lnb_power integer,lnb_22k integer,lnb_toneburst integer,lnb_diseqc_mode integer,lnb_diseqc_mode_config10 integer,lnb_diseqc_mode_config11 integer,lnb_moto_mode integer,fast_diseqc integer,diseqc_repeat integer,diseqc_sequence integer)";
	private static final String CREATE_TBL_TS_INFO = "create table " + " TsTableInfo(id integer primary key, ts_id integer, sat_id integer, frequency integer, symbol integer,polarization integer, fec_inner integer)";
	private static final String CREATE_TAL_SCAN_LIST_SAT_INFO = "create table " + " ScanListSatTableInfo(id integer primary key,scan_id integer, sat_id integer, name integer, flags integer, position integer,position_no integer, lnb_no integer,lnb_type integer, lnb_low integer,lnb_high integer,lnb_threshold integer, lnb_power integer,lnb_22k integer,lnb_toneburst integer,lnb_diseqc_mode integer,lnb_diseqc_mode_config10 integer,lnb_diseqc_mode_config11 integer,lnb_moto_mode integer,fast_diseqc integer,diseqc_repeat integer,diseqc_sequence integer,moto_no integer)";
	private static final String CREATE_TBL_SCAN_LIST_TS_INFO = "create table " + " ScanListTsTableInfo(id integer primary key, ts_id integer,scan_id integer, sat_id integer, frequency integer, symbol integer,polarization integer, fec_inner integer)";

	
	public SQLiteDatabase db;
	
	public DvbsDBHelper(Context context) {
		super(context, DB_NAME, null, 2);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub
		this.db= arg0;
		System.out.println("onCreate DvbsDBHelper");
		db.execSQL(CREATE_TBL_SAT_INFO);
		db.execSQL(CREATE_TBL_TS_INFO);
		db.execSQL(CREATE_TAL_SCAN_LIST_SAT_INFO);
		db.execSQL(CREATE_TBL_SCAN_LIST_TS_INFO);
	}
	
	public void insertSatInfo(ContentValues values)
	{
		SQLiteDatabase db = getWritableDatabase();
		db.insert(DB_TABLE_SAT_INFO, null, values);
	
	}
	
	public void insertTsInfo(ContentValues values)
	{
		SQLiteDatabase db = getWritableDatabase();
		db.insert(DB_TABLE_TS_INFO, null, values);
		
	}
	
	public Cursor querySatInfo(String[] colums, String seletion, String[] selectionArgs)
	{
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(DB_TABLE_SAT_INFO, colums, seletion, selectionArgs, null, null, "sat_id asc");
		db.close();
		return c;
	}
	
	public Cursor queryTsInfo(String[] colums, String seletion, String[] selectionArgs)
	{
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(DB_TABLE_TS_INFO, colums, seletion, selectionArgs, null, null, null);
		return c;
	}
	
	public void deleteSatInfo()
	{
		SQLiteDatabase db = getWritableDatabase();
		db.delete(DB_TABLE_SAT_INFO, null, null);	
		//db.delete(DB_TABLE_SAT_INFO, "id=?", new String[]{String.valueOf(id)});	
	}
	 
	public void deleteTsInfo()
	{
		SQLiteDatabase db = getWritableDatabase();
		db.delete(DB_TABLE_TS_INFO, null, null);	
		//db.delete(DB_TABLE_TS_INFO, "id=?", new String[]{String.valueOf(id)});	
	}
	
	public void updateSatInfo(ContentValues values,int id)
	{
		SQLiteDatabase db = getWritableDatabase();
		db.update(DB_TABLE_SAT_INFO, values, "id=?", new String[]{String.valueOf(id)});
	}
	
	public void updateTsInfo(ContentValues values, int id)
	{
		SQLiteDatabase db = getWritableDatabase();
		db.update(DB_TABLE_TS_INFO, values, "id=?", new String[]{String.valueOf(id)});
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	public boolean deleteDatabase(Context context){
		//return context.deleteDatabase(DB_NAME);
		context.deleteDatabase(DB_NAME);
		return context.deleteDatabase("sat_transponder.db-journal");
	}
	
}

