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
import com.amlogic.tvutil.TVEvent;
import com.amlogic.tvutil.TVGroup;
import com.amlogic.tvutil.DTVPlaybackParams;
import com.amlogic.tvutil.DTVRecordParams;

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
	private TVProgram TVProgram=null;
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
	
	/***************DTVPlayer****************/
	public String secToTime(long i, Boolean isTotalTime){
		String retStr = null;
		long hour = 0;
		long minute = 0;
		long second = 0;
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
    
    public String unitFormat(long i){
		String retStr = null;
		if (i >= 0 && i < 10)
			retStr = "0" + Long.toString(i);
		else
			retStr = Long.toString(i);
		return retStr;
    }

	public long DTVPlayerGetRecordDuration(){
		DTVRecordParams recPara = getRecordingParams();
		if (recPara != null) {
			//Log.d(TAG, "Record: time "+recPara.getCurrentRecordTime()/1000+" / "+
				//recPara.getTotalRecordTime()/1000+", size "+recPara.getCurrentRecordSize());
			return recPara.getCurrentRecordTime()/1000;
		} else {
			//Log.d(TAG, "Cannot get record params");
		}
		return 0;
	}

	public boolean DTVPlayerIsRecording(){
		DTVRecordParams recPara = getRecordingParams();
		if (recPara != null) 
			return true;
		return false;
	}

	public void DTVPlayerStartRecording(){
		startRecording();
	}


	public void DTVPlayerStopRecording(){
		stopRecording();
	}

	

	public void DTVPlayerPlayCurrentProgram(){

	}

	public void DTVPlayerPlayByProNo(int pronumber){
		TVProgramNumber mTVProgramNumber = new TVProgramNumber(pronumber);
		playProgram(mTVProgramNumber);
	}
	public void DTVPlayerPlayById(int db_id){
		playProgram(db_id);
	}

	public void DTVPlayerPlayDown(){
		channelDown();
	}

	public void DTVPlayerPlayUp(){
		channelUp();
	}

	public void DTVPlayerStopPlay(){
		stopPlaying();
	}

	public boolean DTVPlayerSetFav(boolean f){
		int db_id=-1;
		db_id=DTVPlayerGetCurrentProgramID();
		TVProgram mTVProgram = TVProgram.selectByID(this,db_id);
		if(mTVProgram.getFavoriteFlag()!=f){
			mTVProgram.setFavoriteFlag(f);
			return true;
		}	
		else
			return false;
	}

	/*************TeleText**************/

	public void DTVTTShow(){
		ttShow();
	}

	public void DTVTTHide(){
		ttHide();
	}

	public void DTVTTGotoNextPage() {
        ttGotoNextPage();
    }
	
    public void DTVTTGotoPreviousPage() {
      ttGotoPreviousPage();
    }
	
    public void DTVTTGotoPage(int page) {
        ttGotoPage(page);
    }

    public void DTVTTGoHome() {
        ttGoHome();
    }
	
    public void DTVTTGotoColorLink(int color) {
        ttGotoColorLink(color);
    }


	/**********Timeshifting*************/
	public void DTVTimeShiftingForward(int speed){
		fastForward(speed);
	}

	public void DTVTimeShiftingBackward(int speed){
		fastBackward(speed);
	}

	public void DTVTimeShiftingPause(){
		pause();
	}

	public void DTVTimeShiftingResume(){
		resume();
	}
	
	public void DTVTimeShiftingSeek(int pos){
		seekTo(pos);
	}

	public void DTVTimeShiftingPlay(){
		
	}
	public void DTVTimeShiftingStop(){
		stopTimeshifting();
	}

	public void DTVSubtitleStop(){}
	public void DTVTeletextStop(){}

	/*DTVProgramManager*/
	public void DTVProgramManagerDeletePro(int id){
		
	}

	public void DTVProgramManagerEditPro(int id){
		
	}

	public void DTVProgramManagerAddLock(int id){}

	public void DTVProgramManagerSkipPro(int id,boolean f){}
	public void DTVProgramManagerFavSet(int id,boolean f){}
	public void DTVProgramManagerAddGroup(String name){
		TVGroup.addGroup(this,name);
	}

	public void DTVProgramManagerDeleteGroup(int id){
		TVGroup.deleteGroup(this,id);
	}
	
	public void DTVProgramManagerEditGroupName(int id,String name){
		TVGroup.editGroup(this,id,name);
	}
	
	public TVGroup[] DTVProgramManagerGetGroupList(){
		TVGroup[] mProgramGroup=null;
		mProgramGroup = TVGroup.selectByGroup(this,true);
		return mProgramGroup;
	}

	public TVProgram[] DTVProgramManagerGetFav(){
		TVProgram[] mTVProgram=null;
		mTVProgram = TVProgram.selectByFavorite(this,true);
		return mTVProgram;
	}
	
	public TVProgram[] DTVProgramManagerGetProByGroup(int id){
		TVProgram[] mTVProgram=null;
		mTVProgram = TVProgram.selectByGroupMap(this,id,true);
		return mTVProgram;
	}

	/***********DTV EPG****************/
	public int DTVEpgGetID(){
		return getCurrentProgramID();
	}

	public String DTVEpg_get_currentprogname(){
		String value = " ";
		TVProgram mTVProgram=TVProgram.selectByID(this,DTVEpgGetID());
		value = mTVProgram.getName();
		return value; 
	}

	public TVEvent[] DTVEpg_getDateEIT(long start,long duration){

		TVProgram mTVProgram=TVProgram.selectByID(this,DTVEpgGetID());
		return  mTVProgram.getScheduleEvents(this,start,duration);
		
	}

	public String DTVEpg_get_detailinfo(int srv_id){
		String value = " ";
		TVEvent mTVEvent=TVEvent.selectByID(this,srv_id);
		value = mTVEvent.getEventDescr();
		return value; 
	}

	public void DTVEpg_updateEvent(int db_id){
	}

	public void DTVEpg_deleteRec(int eventid){
	}


	public void DTVEpg_subscribeEPGEvent(int eventid){

	}

	public void DTVEpg_recordByEventID(int eventid){

	}

	/*********settings************/
	public boolean DTVGetSubtitleStatus(){
		return getBooleanConfig("tv:subtitle:enable");
	}

	public void DTVSetSubtitleStatus(boolean value){
		setConfig("tv:subtitle:enable",value);
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
		String password = "0000";
		password = getStringConfig("tv:dtv:password");
		return password;
	}

	public int DTVPlayerGetCurrentProgramID(){
		return getCurrentProgramID();
	}

	public TVProgram DTVPlayerGetDataByCurrentID(){
		int db_id=-1;
		db_id=DTVPlayerGetCurrentProgramID();
		Log.d(TAG,"current id="+db_id);
	
		return TVProgram.selectByID(this,db_id);
	}

	
	
}
	
