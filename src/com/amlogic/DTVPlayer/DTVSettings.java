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
		return 0;
	}

	public void setScreenMode(int mode){
		return;
	}

	public int getAudioTrack(){
		return 2;
	}

	public int getTimeShiftingDuration(){
		return 600;
	}

	public void  setTimeShiftingDuration(int value){
	}
	
	public int getParentalRating(){
		return 0;
	}

	public void setParentalRating(int value){
		return;
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

}

