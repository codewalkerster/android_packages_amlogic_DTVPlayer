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

public class DTVSettings extends DTVActivity{
	private static final String TAG="DTVSettings";
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		
	}

	public void onDisconnected(){
		Log.d(TAG, "disconnected");
	}

	public void onMessage(TVMessage msg){
		Log.d(TAG, "message "+msg.getType());
		switch (msg.getType()) {
			case TVMessage.TYPE_SCAN_PROGRESS:
				
				break;
			case TVMessage.TYPE_SCAN_STORE_BEGIN:
				Log.d(TAG, "Storing ...");
				break;
			case TVMessage.TYPE_SCAN_STORE_END:
				Log.d(TAG, "Store Done !");
				
				break;
			case TVMessage.TYPE_SCAN_END:
				Log.d(TAG, "Scan End");
				break;
			default:
				break;
		}
	}

	public void setTeltextBound(){
		setConfig("tv:subtitle:margin_left",60);
		setConfig("tv:subtitle:margin_right",60);
		setConfig("tv:subtitle:margin_top",60);
		setConfig("tv:subtitle:margin_bottom",60);
	}
	public boolean getSubtitleStatus(){
		if(getBooleanConfig("tv:subtitle:enable"))
			return true;
		else 
			return false;
	}

	public void setSubtitleStatus(boolean value){
		setConfig("tv:subtitle:enable",value);
	}

	public int getScreenMode(){
		return getIntConfig("tv:dtv:screen_mode");
	}

	public void setScreenMode(int mode){
		setConfig("tv:dtv:screen_mode",mode);
	}

	public int getAudioTrack(){
		return getIntConfig("tv:dtv:audio_tarck");
	}

	public void setAudioTrack(int mode){
		setConfig("tv:dtv:audio_tarck",mode);
	}
	public int getTimeShiftingDuration(){
		return getIntConfig("tv:dtv:timeshifting_time_long")/1000;	
	}

	public void  setTimeShiftingDuration(int value){
		setConfig("tv:dtv:timeshifting_time_long",value*1000);
	}
	
	public int getParentalRating(){
		return getIntConfig("tv:dtv:dtbt:parent_rate");	
	}

	public void setParentalRating(int value){
		setConfig("tv:dtv:dtbt:parent_rate",value);
	}

	public int getTeletextRegion(){
		return 0;
	}

	public void setTeletextRegion(int value){

	}

	public String getTeletextRegionName(){
		String value = "en";
		return value;
	}

	public void forceParentalRatingCheck(){
		return ;
	}

	public String getPassWord(){
		String password = "0000";
		password = getStringConfig("tv:dtv:password");
		return password;
	}

	public void setPassWord(String password){
		setConfig("tv:dtv:password",password);
	}

	public String DTVPlayerGetScanRegion(){
		return getStringConfig("tv:scan:dtv:region");	
	}
	
	public void factoryReset(){
		restoreFactorySetting();
	}
}

