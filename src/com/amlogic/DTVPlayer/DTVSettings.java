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
import com.amlogic.tvutil.TVRegion;

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

	public boolean getCheckProgramLock(){
		return mContext.getBooleanConfig("tv:check_program_lock");
	}

	public String getScanRegion(){
		String region = mContext.getStringConfig("tv:scan:dtv:region");	
		return region;
	}

	public void setRecordStoragePath(String path){
		mContext.setConfig("tv:dtv:record_storage_path",path);
	}

	public String getRecordStoragePath(){
	 	return mContext.getStringConfig("tv:dtv:record_storage_path");
	}

	public void setLCNStatus(boolean b){
		mContext.setConfig("tv:dtv:dvbt:lcn",b);
	}

	public boolean getLCNStatus(){
		return mContext.getBooleanConfig("tv:dtv:dvbt:lcn");
	}
		
	public void factoryReset(){
		mContext.restoreFactorySetting();
	}

	public String[] getRegions(){
		String[] countries = TVRegion.getCountryByDVBT(mContext);
		return countries;	
	}

	public String[] getATSCRegions(){
		String[] countries = TVRegion.getCountryByATSC(mContext);
		return countries;	
	}

	public int getATSCRegionsIndex(){
		String[] countries = TVRegion.getCountryByATSC(mContext);
		if(countries!=null){
			String cur_region = mContext.getStringConfig("tv:scan:dtv:region");
			//cur_region = cur_region.substring(0, cur_region.indexOf(','));
			for (int i=0; i<countries.length; i++){
				Log.d(TAG, "Coutry("+(i+1)+"/"+countries.length+"): " + countries[i]);
				if(cur_region.equals(countries[i]))
					return i;
			}
		}
		return 0;
	}

	private String region=null;
	public void setRegion(String region){
		this.region=region;
		String value=null;
		if(region.contains("ATSC")){
			value = region;
		}
		else{
			value = region+",Default DVB-T";
		}
		mContext.setConfig("tv:scan:dtv:region",value);
	} 
	
	public int getRegionsIndex(){
		String[] countries = TVRegion.getCountryByDVBT(mContext);
		if(countries!=null){
			String cur_region = mContext.getStringConfig("tv:scan:dtv:region");
			cur_region = cur_region.substring(0, cur_region.indexOf(','));
			for (int i=0; i<countries.length; i++){
				Log.d(TAG, "Coutry("+(i+1)+"/"+countries.length+"): " + countries[i]);
				if(cur_region.equals(countries[i]))
					return i;
			}
		}
		return 0;
	}

	public int getDvbtScanMode(){
		return dvbt_scan_mode;
	}

	private int dvbt_scan_mode = 0;
	public void setDvbtScanMode(int mode){
		dvbt_scan_mode = mode;
	}

	private int dvbt_band=0;
	public int getDvbtScanBand(){
		return dvbt_band;
	}

	public void setDvbtScanBand(int value){
		dvbt_band=value;	
	}

	private int dvbt_channel_index=0;
	public int getDvbtScanChannelIndex(){
		return dvbt_channel_index;
	}

	public void setDvbtScanChannelIndex(int index){
		dvbt_channel_index = index;
	}

	private int dvbt_scan_frequency=474000;
	public int getDvbtScanFrequency(){
		return dvbt_scan_frequency;
	}

	public void setDvbtScanFrequency(int fre){
		dvbt_scan_frequency=fre;
	}

	private int dvbt_band_width=0;
	public int getDvbtScanBandwidth(){
		return dvbt_band_width;
	}

	public void setDvbtScanBandwidth(int bandwidth){
		dvbt_band_width=bandwidth;
	}
}

