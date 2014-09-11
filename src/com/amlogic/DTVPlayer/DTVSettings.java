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

	public String getDtvMode(){
		return mContext.getStringConfig("tv:dtv:mode");
	}

	public void setDtvMode(String mode){
		mContext.setConfig("tv:dtv:mode",mode);
		if(mode.equals("dvbs")){
			mContext.setConfig("tv:scan:dtv:region","Default DVBS");
		}
		else if(mode.equals("dvbt")){
			mContext.setConfig("tv:scan:dtv:region","UK,Default DVB-T");
		}
		else if(mode.equals("isdbt")){
			mContext.setConfig("tv:scan:dtv:region","BRAZIL,Default ISDBT");
		}
		else if(mode.equals("atsc")){
			mContext.setConfig("tv:scan:dtv:region","U.S.,ATSC Air");
		}
	}

	public String getDtvDemodAndDmxConfig(){
		return mContext.getStringConfig("tv:dtv:config_demod_fe");
	}

	public void setDtvDemodAndDmxConfig(String mode){
		if(mode.equals("avl6211")){
			mContext.setConfig("tv:dtv:config_demod_fe","dtv_demod 0 driver Avl6211|dtv_demod 0 i2c_id 2|dtv_demod 0 i2c_addr 0xc0|dtv_demod 0 reset_gpio GPIOY_5|frontend 0 dtv_demod 0|frontend 0 ts 0|enable 0|");
			mContext.setConfig("tv:dtv:config_dmx","0 p 0 0");	
		}
		else if(mode.equals("mxl101")){
			mContext.setConfig("tv:dtv:config_demod_fe","dtv_demod 0 driver Mxl101|dtv_demod 0 i2c_id 2|dtv_demod 0 i2c_addr 0x60|dtv_demod 0 reset_gpio GPIOY_5|frontend 0 dtv_demod 0|frontend 0 ts 0|enable 0|");
			mContext.setConfig("tv:dtv:config_dmx","0 s 0 1");	
		}
		else if(mode.equals("si2168")){
			mContext.setConfig("tv:dtv:config_demod_fe","dtv_demod 0 driver Si2168|dtv_demod 0 i2c_id 2|dtv_demod 0 i2c_addr 0x64|dtv_demod 0 reset_gpio GPIOY_5|frontend 0 dtv_demod 0|frontend 0 ts 0|enable 0|");
			mContext.setConfig("tv:dtv:config_dmx","0 p 0 0");	
		}
		
	}
	
	public void setTeltextBound(){
		mContext.setConfig("tv:subtitle:margin_left",30);
		mContext.setConfig("tv:subtitle:margin_right",30);
		mContext.setConfig("tv:subtitle:margin_top",30);
		mContext.setConfig("tv:subtitle:margin_bottom",30);
	}

	public int getBlackoutPolicyConfig(){
		return mContext.getIntConfig("tv:dtv:blackout_policy");
	}

	public void setBlackoutPolicyConfig(int val){
		mContext.setConfig("tv:dtv:blackout_policy",val);
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

	public void setADSwitch(boolean b){
		mContext.setConfig("tv:dtv:ad_switch",b);
	}

	public boolean getADSwitch(){
		return mContext.getBooleanConfig("tv:dtv:ad_switch");
	}

	public void setADVolume(int value){
		mContext.setConfig("tv:dtv:ad_voume",value);
	}

	public int getADVolume(){
		return mContext.getIntConfig("tv:dtv:ad_voume");	
	}
		
	public void factoryReset(){
		mContext.restoreFactorySetting();
	}

	public String[] getRegions(){
		String[] countries=null;
		if(mContext.getStringConfig("tv:dtv:mode").equals("dvbt"))
			countries = TVRegion.getCountryByDVBT(mContext);
		else if(mContext.getStringConfig("tv:dtv:mode").equals("isdbt"))
			countries = TVRegion.getCountryByISDBT(mContext);
		
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
			if(mContext.getStringConfig("tv:dtv:mode").equals("dvbt"))
				value = region+",Default DVB-T";
			else if (mContext.getStringConfig("tv:dtv:mode").equals("isdbt"))
				value = region+",Default ISDBT";
		}
		mContext.setConfig("tv:scan:dtv:region",value);
	} 
	
	public int getRegionsIndex(){
		String[] countries =null;
		if(mContext.getStringConfig("tv:dtv:mode").equals("dvbt"))
			countries= TVRegion.getCountryByDVBT(mContext);
	      else if(mContext.getStringConfig("tv:dtv:mode").equals("isdbt"))
			countries= TVRegion.getCountryByISDBT(mContext);
		  
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


	public int getAtscScanMode(){
		return dvbt_scan_mode;
	}

	public void setAtscScanMode(int mode){
		dvbt_scan_mode = mode;
	}

	public int getAtscScanBand(){
		return dvbt_band;
	}

	public void setAtscScanBand(int value){
		dvbt_band=value;	
	}

	public int getAtscScanChannelIndex(){
		return dvbt_channel_index;
	}

	public void setAtscScanChannelIndex(int index){
		dvbt_channel_index = index;
	}

	public int getAtscScanFrequency(){
		return dvbt_scan_frequency;
	}

	public void setAtscScanFrequency(int fre){
		dvbt_scan_frequency=fre;
	}

	public void setStringConfig(String name, String value){
		mContext.setConfig(name, value);
	}

	public void setIntConfig(String name, int value){
		mContext.setConfig(name, value);
	}

	public void setBoolConfig(String name, Boolean value){
		mContext.setConfig(name, value);
	}

}

