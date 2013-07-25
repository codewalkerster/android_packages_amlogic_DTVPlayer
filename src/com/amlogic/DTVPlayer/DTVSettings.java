package com.amlogic.DTVPlayer;

import android.util.Log;
import android.os.Bundle;
import android.content.Context;
import com.amlogic.tvutil.TVMessage;
import com.amlogic.tvutil.TVConst;
import com.amlogic.tvutil.TVProgram;
import com.amlogic.tvutil.TVProgramNumber;
import com.amlogic.tvactivity.TVActivity;
import com.amlogic.tvutil.TVChannelParams;
import com.amlogic.tvutil.TVScanParams;
import com.amlogic.tvutil.TVConst;

public class DTVSettings{
	private static final String TAG="DTVSettings";
	private DTVActivity mContext=null;

	public DTVSettings(Context context){
		mContext = (DTVActivity)context;
		
	}
	
	public void setTeltextBound(){
		mContext.setConfig("tv:subtitle:margin_left",30);
		mContext.setConfig("tv:subtitle:margin_right",30);
		mContext.setConfig("tv:subtitle:margin_top",30);
		mContext.setConfig("tv:subtitle:margin_bottom",30);
	}
	
	public boolean getSubtitleStatus(){
		if(mContext.getBooleanConfig("tv:subtitle:enable"))
			return true;
		else 
			return false;
	}

	public void setSubtitleStatus(boolean value){
		mContext.setConfig("tv:subtitle:enable",value);
	}

	public String getSubtitleLanguage(){
		return mContext.getStringConfig("tv:subtitle:language");
	}

	public void setSubtitleLanguage(String lan){
		mContext.setConfig("tv:subtitle:language",lan);
	}

	public String getAudLanguage(){
		return mContext.getStringConfig("tv:audio:language");
		
	}

	public void setAudLanguage(String lan){
		mContext.setConfig("tv:audio:language",lan);
	}

	public int getScreenMode(){
		return mContext.getIntConfig("tv:dtv:screen_mode");
	}

	public void setScreenMode(int mode){
		mContext.setConfig("tv:dtv:screen_mode",mode);
	}

	public int getAudioTrack(){
		return mContext.getIntConfig("tv:dtv:audio_tarck");
	}

	public void setAudioTrack(int mode){
		mContext.setConfig("tv:dtv:audio_tarck",mode);
	}
	public int getTimeShiftingDuration(){
		return mContext.getIntConfig("tv:dtv:timeshifting_time_long")/1000;	
	}

	public void  setTimeShiftingDuration(int value){
		mContext.setConfig("tv:dtv:timeshifting_time_long",value*1000);
	}
	
	public int getParentalRating(){
		return mContext.getIntConfig("tv:dtv:dvb:parent_rate");	
	}

	public void setParentalRating(int value){
		mContext.setConfig("tv:dtv:dvb:parent_rate",value);
	}

	public String getTeletextRegion(){
		return mContext.getStringConfig("tv:teletext:region");
	}

	public void setTeletextRegion(String value){
		mContext.setConfig("tv:teletext:region",value);
	}

	public void forceParentalRatingCheck(){
		return ;
	}

	public String getPassWord(){
		String password = "0000";
		password = mContext.getStringConfig("tv:dtv:password");
		return password;
	}

	public void setPassWord(String password){
		if(password!=null)
			mContext.setConfig("tv:dtv:password",password);
	}

	public String getAtscAntennaSource(){
		return mContext.getStringConfig("tv:atsc:antenna:source");
	}	

	public void setAtscAntennaSource(String value){
		mContext.setConfig("tv:atsc:antenna:source",value);
	}	

	public void setCheckProgramLock(boolean b){
		mContext.setConfig("tv:check_program_lock",b);
	}

	public String getScanRegion(){
		String region = mContext.getStringConfig("tv:scan:dtv:region");	
		return region;
	}

	public void setRecordStoragePath(String path){
		mContext.setConfig("tv:dtv:record_storage_path",path);
	}
		
	public void factoryReset(){
		mContext.restoreFactorySetting();
	}
	
}

