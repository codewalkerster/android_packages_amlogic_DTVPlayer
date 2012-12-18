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

import java.util.*;
import java.text.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.app.*;
import android.app.AlertDialog.*;
import android.content.*;
import android.graphics.*;
import android.view.ViewGroup.*;
import android.text.*;
import android.text.method.*;
import android.database.*;
import android.os.SystemProperties;
import android.os.*;
import java.lang.reflect.Field;
/**
 *DTV Activity
 */
abstract public class DTVActivity extends TVActivity{
    private static final String TAG="DTVActivity";
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
	
	/***************DTVPlayer****************/
	public String secToTime(int i, Boolean isTotalTime){
		String retStr = null;
		int hour = 0;
		int minute = 0;
		int second = 0;
		if (i <= 0){
			if (isTotalTime && i<0)
				return "99:59:59";
			else
				return "00:00:00";
		}
		else{
			minute = i/60;
			if (minute < 60){
				second = i%60;
				retStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
			}
			else{
				hour = minute/60;
				if (hour > 99)
					return "99:59:59";
				minute = minute%60;
				second = i%60;
				retStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
			}
		}
		return retStr;
	}
    
    public String unitFormat(int i){
		String retStr = null;
		if (i >= 0 && i < 10)
			retStr = "0" + Integer.toString(i);
		else
			retStr = Integer.toString(i);
		return retStr;
    }

	public int DTVPlayerGetRecordDuration(){
		return 0;
	}

	public boolean DTVPlayerIsRecording(){
		return false;
	}

	public void DTVPlayerStopRecording(){

	}

	public void DTVPlayerPlayCurrentProgram(){

	}

	public void DTVPlayerPlayByProNo(int pronumber){

	}

	public void DTVPlayerPlayById(int db_id){

	}

	/**********Timeshifting*************/
	public void DTVTimeShiftingForward(int speed){

	}

	public void DTVTimeShiftingBackward(int speed){

	}

	public void DTVTimeShiftingPause(){
	}

	public void DTVTimeShiftingResume(){}
	
	public void DTVTimeShiftingPlay(){}
	public void DTVTimeShiftingSeek(int pos){}
	public void DTVTimeShiftingStop(){}

	public void DTVSubtitleStop(){}
	public void DTVTeletextStop(){}


	/***********DTV EPG****************/
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

	/*********settings************/
	public boolean DTVGetSubtitleStatus(){
		return true;
	}

	public int DTVGetScreenMode(){
		return 0;
	}

	public void DTVSetScreenMode(int mode){
		return;
	}

	public int DTVGetAudioTrack(){
		return 2;
	}

	public void DTVSetAudioTrack(int mode){

	}

	public int DTVGetTimeShiftingDuration(){
		return 600;
	}

	public void  DTVSetTimeShiftingDuration(int value){
	}
	
	public int DTVGetParentalRating(){
		return 0;
	}

	public void DTVSetParentalRating(int value){
		return;
	}

	public int DTVGetTeletextRegion(){
		return 0;
	}

	public void DTVSetTeletextRegion(int value){

	}

	public String DTVGetTeletextRegionName(){
		String value = "en";
		return value;
	}

	public void DTVForceParentalRatingCheck(){
		return ;
	}

	public String DTVPlayerGetPassword(){
		return null; 
	}

	public int DTVPlayerGetCurrentProgramID(){
		return 0;
	}

}
	
