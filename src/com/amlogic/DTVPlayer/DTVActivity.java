package com.amlogic.DTVPlayer;

import android.util.Log;
import android.os.Bundle;
import com.amlogic.tvutil.TVMessage;
import com.amlogic.tvutil.TVConst;
import com.amlogic.tvutil.TVProgram;
import com.amlogic.tvutil.TVProgramNumber;
import com.amlogic.tvactivity.TVActivity;
import com.amlogic.tvutil.TVChannelParams;
import com.amlogic.tvutil.TVScanParams;
import com.amlogic.tvutil.TVConst;

import android.os.SystemProperties;
import android.database.*;

/**
 *DTV Activity
 */
abstract public class DTVActivity extends TVActivity{
    private static final String TAG="DTVActivity";
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
		SystemProperties.set("vplayer.hideStatusBar.enable", "true");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
		SystemProperties.set("vplayer.hideStatusBar.enable", "false");
        super.onDestroy();
    }

	public void DTVforwardTimeShifting(int speed){

	}

	public void DTVbackwardTimeShifting(int speed){

	}

	public void DTVpauseTimeShifting(){
	}

	public void DTVresumeTimeShifting(){}
	public void DTVplayTimeShifting(){}
	public void DTVseekTimeShifting(int pos){}


	public void DTVSubtitleStop(){}
	public void DTVTeletextStop(){}
	public void DTVstopTimeShifting(){}

	/****DTV EPG*****/
	public int DTVEpgGetID(){
		return 1;
	}

	public String DTVEpg_get_currentprogname(){
		String value = "test:102  BBC channle";
		return value; 
	}

	public Cursor DTVEpg_getCurrentDateEIT(int srv_id,long firstsecond,long current_date_second){
		Cursor value = null; 
		/*

		cursor = resolver.query(DVBClient.TABLE_EVENT ,
								new String[]{EIT_MAIN_KEY,EIT_SRV_ID,EIT_EVENT_NUM,EIT_EVENT_START,EIT_EVENT_END, EIT_EVENT_BOOK,EIT_EVENT_DES,EIT_EVENT_EXT_DES},  
								"db_srv_id="+db_srv_id+" and end  > " + firstsecond +" and start  < " + (current_date_second + 24 * 60 * 60) ,  null, EIT_EVENT_START);
		*/
		return value;
	}

	public Cursor DTVEpg_getDateEIT(int srv_id,long firstsecond){
		Cursor value = null; 
		/*
		cursor = resolver.query(DVBClient.TABLE_EVENT ,
								new String[]{EIT_MAIN_KEY,EIT_SRV_ID,EIT_EVENT_NUM,EIT_EVENT_START,EIT_EVENT_END, EIT_EVENT_BOOK,EIT_EVENT_DES,EIT_EVENT_EXT_DES}, 
								"source_id="+db_srv_id+" and end  > " + firstsecond +" and start  < " + (firstsecond + 24 * 60 * 60) ,  null, EIT_EVENT_START);

		*/
		return value;
	}

	public String DTVEpg_get_detailinfo(int srv_id){
		String value=null;
		/*
		Cursor cursor = resolver.query(DVBClient.TABLE_EVENT ,
		new String[]{"descr", "ext_descr"}, 
		"db_id="+db_evt_id,null,null);
		*/
		return value;
	}

	 
	public Cursor DTVEpg_select_passedeit(int db_srv_id, long firstsecond, long nowsecond){
		Cursor cursor =null;  

		/*
		if(dtv_standard_atsc)		
	  		cursor = resolver.query(DVBClient.TABLE_EVENT ,
								new String[]{EIT_MAIN_KEY,EIT_SRV_ID,EIT_EVENT_NUM,EIT_EVENT_START,EIT_EVENT_END, EIT_EVENT_BOOK,EIT_EVENT_DES,EIT_EVENT_EXT_DES}, 
								"source_id="+db_srv_id+" and end  > " + firstsecond +" and start  < " + nowsecond ,  null, EIT_EVENT_START);

		else
			cursor = resolver.query(DVBClient.TABLE_EVENT ,
								new String[]{EIT_MAIN_KEY,EIT_SRV_ID,EIT_EVENT_NUM,EIT_EVENT_START,EIT_EVENT_END, EIT_EVENT_BOOK,EIT_EVENT_DES,EIT_EVENT_EXT_DES}, 
								"db_srv_id="+db_srv_id+" and end  > " + firstsecond +" and start  < " + nowsecond ,  null, EIT_EVENT_START);

		*/
		return cursor;
	}

	public void DTVEpg_updateEvent(int db_id){
		//this.getContentResolver().update(DVBClient.TABLE_EVENT, cv, "db_id= "+eventid, null); 
	}

	public void DTVEpg_deleteRec(int eventid){
		//this.getContentResolver().delete(DVBClient.TABLE_RECORD, "db_evt_id=" +  eventid,null);
	}


	public void DTVEpg_subscribeEPGEvent(int eventid){

	}

	public void DTVEpg_recordByEventID(int eventid){

	}

}
	
