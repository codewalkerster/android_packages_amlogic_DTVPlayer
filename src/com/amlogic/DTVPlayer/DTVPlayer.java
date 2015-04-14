package com.amlogic.DTVPlayer;

import java.util.*;
import java.text.*;
import android.util.Log;
import android.os.Bundle;
import android.os.SystemProperties;
import com.amlogic.tvutil.TVMessage;
import com.amlogic.tvutil.TVConst;
import com.amlogic.tvutil.TVProgram;
import com.amlogic.tvutil.TVProgramNumber;
import com.amlogic.tvactivity.TVActivity;
import com.amlogic.tvutil.TVChannelParams;
import com.amlogic.tvutil.TVScanParams;
import com.amlogic.tvutil.TVConst;
import com.amlogic.tvutil.TVEvent;
import com.amlogic.tvutil.DTVPlaybackParams;
import com.amlogic.tvutil.DTVRecordParams;
import com.amlogic.tvutil.TVSatellite;
import com.amlogic.widget.CheckUsbdevice;
import com.amlogic.tvutil.TVConfigValue;
import com.amlogic.tvservice.TVConfig;

import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.app.*;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.*;
import android.content.*;
import android.os.*;
import android.os.PowerManager;
import android.text.*;
import android.text.method.*;
import android.graphics.Color;

import com.amlogic.widget.PasswordDialog;
import com.amlogic.widget.SureDialog;
import com.amlogic.widget.SingleChoiseDialog;
import com.amlogic.widget.MutipleChoiseDialog;
import com.amlogic.widget.CustomDialog;
import com.amlogic.widget.CustomDialog.ICustomDialog;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FileReader;
import java.lang.Process; 

public class DTVPlayer extends DTVActivity{
	private static final String TAG="DTVPlayer";

	private static final String Version = "1.0.0";

	private Toast toast=null;
	private Bundle bundle;
	private TVConfig config;
	AlertDialog mAlertDialog=null;  //no signal or no data
	DTVSettings mDTVSettings=null;	
	private PowerManager.WakeLock wakeLock = null;
	private HomeKeyEventBroadCastReceiver home_receiver=null;
	private enum menu{
		PROGRAM,
		SEARCH,
		SYSTEM,
		AV,
		TTX,
		REC,
		RECALL_LIST,
		SUBTITLE_SETTING,
		SHORTCUT_FAV,
		SHORTCUT_AUDIO_TRACK,
		SHORTCUT_PICTURE_MODE,
		SHORTCUT_SUBTITLE,
		AUDIO_LANGUAGE,
		TIMESHIFTING,
		AD,
		AD_VOLUME_UP,
		AD_VOLUME_DOWN,
		EXIT_PLAYER,
		MENU_NUM,
	}
	private static final int[] MENUS = new int[menu.MENU_NUM.ordinal()];

	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		MENUS[menu.PROGRAM.ordinal()] = R.string.setting_menu_program;
		MENUS[menu.SEARCH.ordinal()] = R.string.setting_menu_search;
		MENUS[menu.SYSTEM.ordinal()] = R.string.setting_menu_system;
		MENUS[menu.AV.ordinal()] = R.string.setting_menu_av;
		MENUS[menu.TTX.ordinal()] = R.string.tel_text;
		MENUS[menu.REC.ordinal()] = R.string.rec;
		MENUS[menu.RECALL_LIST.ordinal()] = R.string.recall_list;
		MENUS[menu.SUBTITLE_SETTING.ordinal()] = R.string.dtvplayer_subtitle_language_set;
		MENUS[menu.SHORTCUT_FAV.ordinal()] = R.string.favorite;
		MENUS[menu.SHORTCUT_AUDIO_TRACK.ordinal()] = R.string.audio_track;
		MENUS[menu.SHORTCUT_PICTURE_MODE.ordinal()] = R.string.picture_mode;
		MENUS[menu.SHORTCUT_SUBTITLE.ordinal()] = R.string.subtitle;
		MENUS[menu.AUDIO_LANGUAGE.ordinal()] = R.string.dtvplayer_audio_language_set;
		MENUS[menu.TIMESHIFTING.ordinal()] = R.string.timeshift_time_set;
		MENUS[menu.AD.ordinal()] = R.string.ad;
		MENUS[menu.AD_VOLUME_UP.ordinal()] = R.string.ad_volume_up;
		MENUS[menu.AD_VOLUME_DOWN.ordinal()] = R.string.ad_volume_down;
		MENUS[menu.EXIT_PLAYER.ordinal()] = R.string.exit_player;
		writeSysFile("/sys/class/ppmgr/angle","2");
		setContentView(R.layout.dtvplayer);
		SystemProperties.set("vplayer.hideStatusBar.enable", "true");
		bundle = this.getIntent().getExtras();
		mDialogManager = new DialogManager(DTVPlayer.this);

		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
			PowerManager.ACQUIRE_CAUSES_WAKEUP |
			PowerManager.ON_AFTER_RELEASE, TAG);
		wakeLock.acquire();	

		config = new TVConfig(this);
		if(home_receiver==null)
			home_receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(home_receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		openVideo();
		DTVPlayerUIInit();
		mDTVSettings = new DTVSettings(this);
		//TVMessage msg = TVMessage.inputSourceChanged((int)(int) TVConst.SourceInput.SOURCE_DTV.ordinal());
		//onMessage(msg);
		if(mDialogManager!=null)
			mDialogManager.setActive(true);
		controlUpdate(0/*cmd:setVersion*/, 0, Version);
	}

	public void onDisconnected(){
		Log.d(TAG, "disconnected");
		super.onDisconnected();
	}

	public void onMessage(TVMessage msg){
		super.onMessage(msg);
		Log.d(TAG, "message "+msg.getType());
		switch (msg.getType()) {
			case TVMessage.TYPE_SCAN_PROGRESS:
				
				break;
			case TVMessage.TYPE_SCAN_STORE_BEGIN:
				Log.d(TAG, "Storing ...");
				showTeltext(this);
				break;
			case TVMessage.TYPE_SCAN_STORE_END:
				Log.d(TAG, "Store Done !");
				
				break;
			case TVMessage.TYPE_SCAN_END:
				Log.d(TAG, "Scan End");
				break;
			case TVMessage.TYPE_PROGRAM_STOP:
				
				break;
			case TVMessage.TYPE_PROGRAM_SWITCH:
				hidePasswordDialog();
				DismissDialog();
				break;
			case TVMessage.TYPE_PROGRAM_START:
				SubAsyncTask mTask = new SubAsyncTask();  
				mTask.execute();  
				break;
			case TVMessage.TYPE_BOOKING_START:
				if(DTVPlayerIsRecording()){
					showPvrIcon();
				}
				break;
			case TVMessage.TYPE_RECORD_CONFLICT:
				int recordConflict = msg.getRecordConflict();
				
				Log.d(TAG, "Record conflict:");
				if (recordConflict == TVMessage.REC_CFLT_START_NEW){
					Log.d(TAG, "Stop record for new recording");
				}else if (recordConflict == TVMessage.REC_CFLT_SWITCH_PROGRAM){
					Log.d(TAG, "Stop record for switching to new program");
				}else{
					break;
				}
				if(isTopActivity(this))
					showStopPVRDialog(recordConflict, msg.getProgramID());
				break;
			case TVMessage.TYPE_RECORD_END:	
				switch(msg.getErrorCode()){
					case  TVMessage.REC_ERR_OPEN_FILE:
						if(toast!=null)
							toast.cancel(); 
						toast = Toast.makeText(
							DTVPlayer.this,
				    		R.string.check_usb_device,
				    		Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						showRecordPathSettingsDia();
						break;
					case  TVMessage.REC_ERR_WRITE_FILE:	
						if(toast!=null)
							toast.cancel(); 
						toast = Toast.makeText(
							DTVPlayer.this,
				    		R.string.usbdisk_is_full,
				    		Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();

							if(isRecord == true){
								mDebugRecordProgressDialog.cancel();
								DTVPlayerStopRecording();
				                progressThread.setState(ProgressThread.STATE_DONE);
				                showWarningDialog();
				                isRecord = false;
							}


					break;
					case  TVMessage.REC_ERR_ACCESS_FILE:
						break;
					case  TVMessage.REC_ERR_SYSTEM:
						
						break;							
				}
				if(DTVPlayerIsRecording()==false){
					hidePvrIcon();
				}
				break;	
			case TVMessage.TYPE_SCREEN_ON:
				Log.d(TAG,"---TYPE_SCREEN_ON---");
				int mode = DTVGetScreenMode();
				DTVSetScreenMode(mode);
				
				if (mDTVSettings.getBlackoutPolicyConfig()==1){			   
					setBlackoutPolicy(1);
				}
				else{
					setBlackoutPolicy(0);
				}	  
				break;
			case TVMessage.TYPE_PLAYBACK_STOP:
				break;
			case TVMessage.TYPE_PLAYBACK_START:
				TVProgram pvrTVProgram=null;
				pvrTVProgram=DTVPlayerGetDataByCurrentID();
				if(pvrTVProgram.getVideo().getPID()<0x1fff){
					hideRadioBg();
				}
				else{
					showRadioBg();
				}
				break;
			case TVMessage.TYPE_INPUT_SOURCE_CHANGED:
				if((msg.getSource()==(int) TVConst.SourceInput.SOURCE_DTV.ordinal())){
					if(isHavePragram()==false){ 
						bHavePragram = false;
						hideRadioBg();
						showNoProgramDia(); 
					}
					else{
						bHavePragram = true;
						if(bundle!=null){	
							if(tryChannelListPlay()){
								Log.d(TAG, "channel list play started");
							}
							else if (tryBookingPlay()){
								Log.d(TAG, "booking play started");
							}
							else{
								int db_id = DTVPlayerGetCurrentProgramID();
								DTVPlayerPlayById(db_id);
							}
						}
						else{
							Log.d(TAG,">>>playValid<<<");
							playValid();
						}

						int ScreenMode = DTVGetScreenMode();
						DTVSetScreenMode(ScreenMode);
						
						if (mDTVSettings.getBlackoutPolicyConfig()==1){			   
							setBlackoutPolicy(1);
						}
						else{
							setBlackoutPolicy(0);
						}	  
					}
					newIntentFlag=false;
				}
				break;
			case TVMessage.TYPE_NIT_TABLE_VER_CHANGED:
				Log.d(TAG,"----------TYPE_NIT_TABLE_VER_CHANGED--------"+msg.getReservedValue());
				
				if(nitTempVersion != msg.getReservedValue()){
					nitTempVersion = msg.getReservedValue();
					nit_dialog = false;
				}
				if(DTVgetNitVersion()==-1){
					DTVSaveNitVersion(msg.getReservedValue());
				}	
				else if( DTVgetNitVersion() != -1 &&DTVgetNitVersion()  != msg.getReservedValue() && nit_dialog==false ){					
					showNitVersionChangedDialog();
				}
				
				break;
			default:
				break;
		}
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		Log.d(TAG,">>>>>onRestart<<<<<<");
		super.onRestart();
		writeSysFile("/sys/class/video/disable_video","2");
	}

	@Override
	protected void onResume(){
		Log.d(TAG, ">>>>>>>>onResume<<<<<<<<");
		if(bHavePragram)
			mDialogManager.setActive(true);
		else
			mDialogManager.setActive(false);
		super.onResume();
		mDialogManager.resumeDialog();
		DTVPlayerGetCurrentProgramData();
		updateInforbar();
	}

	@Override
	protected void onPause(){
		Log.d(TAG, ">>>>>>>>onPause<<<<<<<<");
		super.onPause();
		mDialogManager.setActive(false);
		mDialogManager.pauseDialog();
	}
	
	@Override
	protected void onStart(){
		Log.d(TAG, "onStart");
		super.onStart();
		//writeSysFile("/sys/class/graphics/fb0/free_scale","0");
		//writeSysFile("/sys/class/graphics/fb0/request2XScale","8");
	}

	@Override
	protected void onStop(){
		Log.d(TAG, "onStop");
		switchScreenType(0);
		//stopPlaying();
		if(toast!=null)
			toast.cancel(); 
		//writeSysFile("/sys/class/graphics/fb0/free_scale","1");
		//writeSysFile("/sys/class/graphics/fb0/request2XScale","2");
		super.onStop();
	}

	public void onDestroy() {
		Log.d(TAG, "onDestroy");

		 if(home_receiver!=null)
			unregisterReceiver(home_receiver);
	
		SystemProperties.set("vplayer.hideStatusBar.enable", "false");
		if (wakeLock != null){
			wakeLock.release();
			wakeLock = null;
		}

		if(mDTVSettings!=null)
			mDTVSettings.setCheckProgramLock(false);

		if(mDialogManager!=null)
			mDialogManager.dialogManagerDestroy();
		super.onDestroy();
	}

	private boolean newIntentFlag=false;
	private boolean  bHavePragram = false;
	public void onNewIntent(Intent intent){
		Log.d(TAG, ">>>>>onNewIntent<<<<<");
		super.onNewIntent(intent);
	    	setIntent(intent);
		if(intent!=null){
			bundle = intent.getExtras();
		}
		
		if(getStringConfig("tv:input_source").equals("atv")){
			newIntentFlag=true;
			setInputSource(TVConst.SourceInput.SOURCE_DTV);
		}
		else if(getStringConfig("tv:input_source").equals("dtv")){
			if(isHavePragram()==false){ 
				bHavePragram = false;
				hideRadioBg();
				showNoProgramDia(); 
			}else{
				bHavePragram = true;
				if(bundle!=null){	
					if(tryChannelListPlay()){
						Log.d(TAG, "channel list play started");
					}
					else if (tryBookingPlay()){
						Log.d(TAG, "booking play started");
					}
					else if(checkFactoryReset()){
						bHavePragram = false;
						hideRadioBg();
						showNoProgramDia(); 
					}
					else{
						int db_id = DTVPlayerGetCurrentProgramID();
						DTVPlayerPlayById(db_id);
					}
				}
				else{
					Log.d(TAG,">>>playValid<<<");
					playValid();
				}

				int mode = DTVGetScreenMode();	
				DTVSetScreenMode(mode);				
				if (mDTVSettings.getBlackoutPolicyConfig()==1){			   
					setBlackoutPolicy(1);
				}
				else{
					setBlackoutPolicy(0);
				}	  
			}	
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		// TODO Auto-generated method stub	
		//Log.d(TAG,"-----"+event.getDownTime()+"---"+SystemClock.uptimeMillis());
		if(SystemClock.uptimeMillis()-event.getDownTime()>300)
			return true;

		int ad_volume=0;
		switch (keyCode) {
			case KeyEvent.KEYCODE_MUTE:
				Log.d(TAG,"KEYCODE_MUTE");
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				Log.d(TAG,"KEYCODE_DPAD_LEFT");
				if(mSureDialog==null||(mSureDialog!=null&&mSureDialog.isShowing()==false)){
					ShowControlBar();
					updateInforbar();
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				Log.d(TAG,"KEYCODE_DPAD_RIGHT");
				if(mSureDialog==null||(mSureDialog!=null&&mSureDialog.isShowing()==false)){
					ShowControlBar();
					updateInforbar();
				}	
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				//if(mainmenu_show_flag){
					//break;
				//}
				//else
				{
					if(dtvplyaer_b_txt&&DTVPlayerInTeletextStatus){
						DTVTTGotoPreviousPage();
					}	
					else{
						DTVPlayerPlayDown();
					}
					return true;
				}
			case KeyEvent.KEYCODE_DPAD_UP:
				Log.d(TAG,"KEYCODE_DPAD_UP");
				//if(mainmenu_show_flag){
					//break;
				//}
				//else
				{
					if(dtvplyaer_b_txt&&DTVPlayerInTeletextStatus){
						DTVTTGotoNextPage();
					}	
					else{
						DTVPlayerPlayUp();
					}
					return true;
				}
			case KeyEvent.KEYCODE_0:
			case KeyEvent.KEYCODE_1:
			case KeyEvent.KEYCODE_2:
			case KeyEvent.KEYCODE_3:
			case KeyEvent.KEYCODE_4:
			case KeyEvent.KEYCODE_5:
			case KeyEvent.KEYCODE_6:
			case KeyEvent.KEYCODE_7:
			case KeyEvent.KEYCODE_8:
			case KeyEvent.KEYCODE_9:
				Log.d(TAG,"KEYCODE_0----9");
				DTVDealDigtalKey(keyCode);
				return true;
			case KeyEvent.KEYCODE_BACK:
				//if(mainmenu_show_flag){
					//HideMainMenu();
				//}
				//else 
				if(inforbar_show_flag){
					HideControlBar();
				}
				else if(dtvplyaer_b_txt&&DTVPlayerInTeletextStatus){	
					Log.d(TAG,"dtvplyaer_b_txt="+dtvplyaer_b_txt+"--DTVPlayerInTeletextStatus="+DTVPlayerInTeletextStatus);
					DTVTTHide();
					DTVPlayerInTeletextStatus=false;
				}	
				else if(DTVPlayerIsRecording()){
					showStopPVRDialog();
				}else if(debugAVInfoShowFlag){
					HideDebugAVInfoDialog();
				}
				else{
					if(mDTVSettings.getIsLauncher()!=1){
						finishPlayer();
						//setInputSource(TVConst.SourceInput.SOURCE_ATV);
						//System.exit(0);	
						getApplication().onTerminate();
					}	
				}	
				Log.d(TAG,"KEYCODE_BACK");
				return true;
			case KeyEvent.KEYCODE_MENU:
				//if(mainmenu_show_flag)
					//HideMainMenu();
				//else
				//if(DTVPlayerInTeletextStatus==false)
					HideControlBar();
					ShowMainMenu();
				return true;	
			case KeyEvent.KEYCODE_DPAD_CENTER:
				Log.d(TAG,"KEYCODE_DPAD_CENTER");
				//if(inforbar_show_flag==false){
					//ShowControlBar();
				//}
				//else //if(mainmenu_show_flag==false)
				{
					HideControlBar();
					ShowChannelList();
				}	
				return true;
			case KeyEvent.KEYCODE_ENTER:
				Log.d(TAG,"KEYCODE_ENTER");
				break;	
			case DTVActivity.KEYCODE_TTX:	
				showTeltext(DTVPlayer.this);
				return true;
			case DTVActivity.KEYCODE_REC:
				if(DTVPlayerIsRecording()==false){
					showPvrDurationTimeSetDialog(DTVPlayer.this);
				}
				return true;
			case DTVActivity.KEYCODE_RECALL_BUTTON:
				showRecallListDialog(DTVPlayer.this);
				return true;
			case DTVActivity.KEYCODE_SUBTITLE:
				Log.d(TAG,"KEYCODE_SUBTITLE");
				HideControlBar();
				showSubtitleSettingMenu(DTVPlayer.this);
				return true;
			case DTVActivity.KEYCODE_FAV_BUTTON:
				shortcut_key_deal("FAV");
				return true;	
			case DTVActivity.KEYCODE_AUDIO_TRACK:
				Log.d(TAG,"KEYCODE_AUDIO_TRACK");
				shortcut_key_deal("AUDIOTRACK");
				return true;
			case DTVActivity.KEYCODE_GREEN_BUTTON: //tv/radio
				Log.d(TAG,"KEYCODE_GREEN_BUTTON");	
				if(dtvplyaer_b_txt&&DTVPlayerInTeletextStatus){
					DTVTTGotoColorLink(3);
				}	
				else
					showChannelInfo();
				return true;	
			case DTVActivity.KEYCODE_RED_BUTTON: //16:9/4:3
				Log.d(TAG,"KEYCODE_RED_BUTTON");
				if(dtvplyaer_b_txt&&DTVPlayerInTeletextStatus){
					DTVTTGotoColorLink(0);
				}	
				else
					shortcut_key_deal("pictrue_mode");
				return true;
			case DTVActivity.KEYCODE_YELLOW_BUTTON:
				if(dtvplyaer_b_txt&&DTVPlayerInTeletextStatus){
					DTVTTGotoColorLink(1);
				}	
				return true;
			case DTVActivity.KEYCODE_EPG: //epg
				
				return true;
			case DTVActivity.KEYCODE_BLUE_BUTTON: //pvr manager
				Log.d(TAG,"KEYCODE_BLUE_BUTTON");
				if(dtvplyaer_b_txt&&DTVPlayerInTeletextStatus){
					DTVTTGotoColorLink(2);
				}	
				return true;
			case DTVActivity.KEYCODE_SUBTITLE_SWITCH:
				Log.d(TAG,"KEYCODE_SUBTITLE");
				shortcut_key_deal("SUBTITLE");
				return true;	
			case DTVActivity.KEYCODE_AUDIO_LANGUAGE:
				Log.d(TAG,"KEYCODE_AUDIO_LANGUAGE");
				showAudioLanguageDialog(DTVPlayer.this);
				return true;
			case DTVActivity.KEYCODE_TIMESHIFTING:
				if(mDTVSettings.getCheckProgramLock()==false){
					if(isHaveExternalStorage()==false){
						CheckUsbdevice dev = new CheckUsbdevice(DTVPlayer.this);
						String path  = dev.getDevice();
						if(path!=null){
						       if(mDTVSettings!=null)
						               mDTVSettings.setRecordStoragePath(path);
						}       
					}
					
					if(isHaveExternalStorage()){						
						Intent Intent_timeshifting = new Intent();
						Intent_timeshifting.setClass(DTVPlayer.this,DTVTimeshifting.class);
						startActivity(Intent_timeshifting);
						HideControlBar();
					}
					else{
						if(toast!=null)
							toast.cancel(); 
						toast = Toast.makeText(
						DTVPlayer.this,
						R.string.check_usb_device,
						Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}	
				}
				return true;
			case DTVActivity.KEYCODE_AD_BUTTION:
				if(mDTVSettings.getADSwitch()){
					mDTVSettings.setADSwitch(false);	
					if(toast!=null)
							toast.cancel(); 
						toast = Toast.makeText(
						DTVPlayer.this,
						"AD"+getString(R.string.off),
						Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
				}
				else{
					mDTVSettings.setADSwitch(true);
					if(toast!=null)
							toast.cancel(); 
						toast = Toast.makeText(
						DTVPlayer.this,
						"AD"+getString(R.string.on),
						Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
				}

				return true;
			case DTVActivity.KEYCODE_AD_VOLUME_UP:
				if(mDTVSettings.getADSwitch()){
					
					ad_volume = mDTVSettings.getADVolume();
					if(ad_volume<100){
						ad_volume++;
						mDTVSettings.setADVolume(ad_volume);
					}
					if(toast!=null)
						toast.cancel(); 
					toast = Toast.makeText(
					DTVPlayer.this,
					Integer.toString(ad_volume),
					Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}	
				return true;
			case DTVActivity.KEYCODE_AD_VOLUME_DOWN:
				if(mDTVSettings.getADSwitch()){
					ad_volume = mDTVSettings.getADVolume();
					if(ad_volume>0){
						ad_volume--;
						mDTVSettings.setADVolume(ad_volume);
					}
					if(toast!=null)
						toast.cancel(); 
					toast = Toast.makeText(
					DTVPlayer.this,
					Integer.toString(ad_volume),
					Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}	
				return true;
			case KeyEvent.KEYCODE_B:
				if(debugMenuDialogShowFlag == 0xf){
					Usbdevice = new CheckUsbdevice(this);
					append_flag = false;
					showDebugMenuDialog();
				}
				debugMenuDialogShowFlag = 0x0;
				return true;
			case KeyEvent.KEYCODE_E:
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt("db_id", DTVPlayerGetCurrentProgramID());
				intent.putExtras(bundle);
				intent.setClass(this, DTVProgramEdit.class);
				startActivity(intent);	
				
				break;	
		}

		return super.onKeyDown(keyCode, event);
	}


	private RelativeLayout RelativeLayout_inforbar=null;
	private RelativeLayout RelativeLayout_debugInfoDialog=null;
	private RelativeLayout RelativeLayout_mainmenu=null;
	private RelativeLayout RelativeLayout_videobcak=null;
	private RelativeLayout RelativeLayout_program_number=null;
	private RelativeLayout RelativeLayout_radio_bg=null;
	private RelativeLayout RelativeLayout_recording_icon=null;
	private RelativeLayout RelativeLayout_loading_icon=null;

	/*main menu*/
	Button Button_mainmenu_list=null;
	Button Button_mainmenu_epg=null;
	Button Button_mainmenu_settings=null;
	Button Button_mainmenu_program_manager=null;
	Button Button_mainmenu_timeshift=null;
	Button Button_mainmenu_prv=null;
	Button Button_mainmenu_manage=null;
	Button Button_mainmenu_skip=null;

	/*Infor bar*/
	TextView Text_screentype_info=null;
	TextView Text_parent_control_info_icon=null;
	TextView Text_channel_type=null;
	TextView Text_MTS_info=null;

	ImageView ImageView_icon_scrambled=null;
	ImageView ImageView_icon_fav=null;
	ImageView ImageView_icon_lock=null;
	ImageView ImageView_icon_epg=null;
	ImageView ImageView_icon_sub=null;
	ImageView ImageView_icon_txt=null;

	ImageView ImageView_recordingIcon=null;
	TextView Text_recording_time=null;
	TextView Text_button_info=null;

	private void DTVPlayerUIInit(){
		RelativeLayout_inforbar = (RelativeLayout)findViewById(R.id.RelativeLayoutInforbar);
		RelativeLayout_debugInfoDialog = (RelativeLayout)findViewById(R.id.RelativeLayoutDebugAVInfoDialog);
		RelativeLayout_mainmenu = (RelativeLayout)findViewById(R.id.RelativeLayoutMainMenu);
		RelativeLayout_program_number = (RelativeLayout)findViewById(R.id.RelativeLayoutProNumer);
		RelativeLayout_radio_bg = (RelativeLayout)findViewById(R.id.RelativeLayoutRadioBg);
		RelativeLayout_recording_icon = (RelativeLayout)findViewById(R.id.RelativeLayoutPvrIcon);
		RelativeLayout_videobcak= (RelativeLayout)findViewById(R.id.RelativeLayout_video);
		RelativeLayout_loading_icon = (RelativeLayout)findViewById(R.id.RelativeLayoutLoadingIcon);

		Text_MTS_info = (TextView) findViewById(R.id.Text_MTS_info);
		Text_screentype_info = (TextView) findViewById(R.id.Text_screentype_info);
		Text_parent_control_info_icon = (TextView) findViewById(R.id.Text_parent_control_info_icon);
		Text_channel_type = (TextView) findViewById(R.id.Text_channel_type);
		

		ImageView_icon_scrambled=(ImageView)findViewById(R.id.ImageView_icon_scrambled);
		ImageView_icon_fav=(ImageView)findViewById(R.id.ImageView_icon_fav);
		ImageView_icon_lock=(ImageView)findViewById(R.id.ImageView_icon_lock);

		ImageView_icon_epg=(ImageView)findViewById(R.id.ImageView_icon_epg);
		ImageView_icon_sub=(ImageView)findViewById(R.id.ImageView_icon_sub);
		ImageView_icon_txt=(ImageView)findViewById(R.id.ImageView_icon_txt);

		ImageView_recordingIcon = (ImageView) findViewById(R.id.ImageView_recordingIcon);
		Text_recording_time = (TextView) findViewById(R.id.Text_recording_time);
		
		Text_button_info = (TextView) findViewById(R.id.Text_button_info);
		Button_mainmenu_list = (Button)findViewById(R.id.Button_mainmenu_list);
		Button_mainmenu_list.setOnClickListener(new MouseClick());
		Button_mainmenu_list.setOnFocusChangeListener(new Button.OnFocusChangeListener(){
              public void onFocusChange(View v, boolean hasFocus) {
               // TODO Auto-generated method stub
               if(hasFocus == true){
				   Text_button_info.setText(R.string.dtvplayer_menu_button_list); 
				   v.startAnimation(mainMenuButtonFocus);
               }else{
				   Text_button_info.setText(null); 
				   v.clearAnimation();
               }
            }
        });
		
		Button_mainmenu_epg = (Button)findViewById(R.id.Button_mainmenu_epg);
		Button_mainmenu_epg.setOnClickListener(new MouseClick());
		Button_mainmenu_epg.setOnFocusChangeListener(new Button.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {
               // TODO Auto-generated method stub
               if(hasFocus == true) {    
				   Text_button_info.setText(R.string.dtvplayer_menu_button_epg); 
				   v.startAnimation(mainMenuButtonFocus);
               }else{
				   Text_button_info.setText(null); 
				   v.clearAnimation();
               }
            }
        });
		
		Button_mainmenu_settings = (Button)findViewById(R.id.Button_mainmenu_settings);
		Button_mainmenu_settings.setOnClickListener(new MouseClick());
		Button_mainmenu_settings.setOnFocusChangeListener(new Button.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {
               // TODO Auto-generated method stub
               if(hasFocus == true) {    
				   Text_button_info.setText(R.string.dtvplayer_menu_button_settings); 
				   v.startAnimation(mainMenuButtonFocus);
               }else{
				   Text_button_info.setText(null);
				   v.clearAnimation();
               }
            }
        });
		
		Button_mainmenu_program_manager = (Button)findViewById(R.id.Button_mainmenu_program_manager);
		Button_mainmenu_program_manager.setOnClickListener(new MouseClick());
		Button_mainmenu_program_manager.setOnFocusChangeListener(new Button.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {
               // TODO Auto-generated method stub
               if(hasFocus == true) {    
				   Text_button_info.setText(R.string.dtvplayer_menu_button_program_manager); 
				   v.startAnimation(mainMenuButtonFocus);
               }else{
				   Text_button_info.setText(null); 
				   v.clearAnimation();
               }
            }
        });
		
		Button_mainmenu_timeshift = (Button)findViewById(R.id.Button_mainmenu_timeshift);
		Button_mainmenu_timeshift.setOnClickListener(new MouseClick());
		Button_mainmenu_timeshift.setOnFocusChangeListener(new Button.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {
               // TODO Auto-generated method stub
               if(hasFocus == true) {    
				   Text_button_info.setText(R.string.dtvplayer_menu_button_timeshifting); 
				   v.startAnimation(mainMenuButtonFocus);
               }else{
				   Text_button_info.setText(null); 
				   v.clearAnimation();
               }
            }
        });
		
		Button_mainmenu_prv = (Button)findViewById(R.id.Button_mainmenu_prv);
		Button_mainmenu_prv.setOnClickListener(new MouseClick());
		Button_mainmenu_prv.setOnFocusChangeListener(new Button.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {
               // TODO Auto-generated method stub
               if(hasFocus == true) {    
				   Text_button_info.setText(R.string.dtvplayer_menu_button_timeshifting); 
				   v.startAnimation(mainMenuButtonFocus);
               }else{
				   Text_button_info.setText(null); 
				   v.clearAnimation();
               }
            }
        });
		
		Button_mainmenu_manage = (Button)findViewById(R.id.Button_mainmenu_manage);
		Button_mainmenu_manage.setOnClickListener(new MouseClick());
		Button_mainmenu_manage.setOnFocusChangeListener(new Button.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {
               // TODO Auto-generated method stub
               if(hasFocus == true) {    
				   Text_button_info.setText(R.string.dtvplayer_menu_button_pvr_manager); 
				   v.startAnimation(mainMenuButtonFocus);
               }else{
				   Text_button_info.setText(null); 
				   v.clearAnimation();
               }
            }
        });

		Button_mainmenu_skip = (Button)findViewById(R.id.Button_mainmenu_skip);
		Button_mainmenu_skip.setOnClickListener(new MouseClick());
		Button_mainmenu_skip.setOnFocusChangeListener(new Button.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {
               // TODO Auto-generated method stub
               if(hasFocus == true) {
				   Text_button_info.setText(R.string.dtvplayer_menu_button_hide);
				   v.startAnimation(mainMenuButtonFocus);
               }else{
				   Text_button_info.setText(null);
				   v.clearAnimation();
               }
            }
        });
		
		findViewById(R.id.RelativeLayout_video).setOnClickListener(new MouseClick());
		
		findViewById(R.id.RelativeLayout_video).setOnLongClickListener(new View.OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				HideControlBar();
				ShowMainMenu(); 	
				return false;
			}
		});
		
		RelativeLayout_inforbar.setVisibility(View.INVISIBLE);
		RelativeLayout_radio_bg.setVisibility(View.INVISIBLE);
		RelativeLayout_loading_icon.setVisibility(View.INVISIBLE);
		RelativeLayout_debugInfoDialog.setVisibility(View.INVISIBLE);

		init_Animation();
		// findViewById(R.id.return_icon).setFocusable(false);
		// findViewById(R.id.return_icon).setOnClickListener(new MouseClick());
		findViewById(R.id.menu_icon).setFocusable(false);
		findViewById(R.id.menu_icon).setOnClickListener(new MouseClick());
	}

	private boolean tryBookingPlay(){
		boolean ret = false;
		
		if (bundle != null && bundle.containsKey("booking_id")){
			int bookingID = bundle.getInt("booking_id");

			Log.d(TAG, "Try to set input source to DTV.");
			setInputSource(TVConst.SourceInput.SOURCE_DTV);

			if(isHaveExternalStorage()==false){
				CheckUsbdevice dev = new CheckUsbdevice(DTVPlayer.this);
				String path  = dev.getDevice();
				if(path!=null){
					if(mDTVSettings!=null)
						mDTVSettings.setRecordStoragePath(path);
				}	
			}
			
			Log.d(TAG, "Start playing for booking " + bookingID + " ...");
			startBooking(bookingID);

			ret = true;
		}

		return ret;
	}

	private boolean tryChannelListPlay(){
		boolean ret = false;
		
		if(bundle != null && bundle.containsKey("activity_tag")){
			if (bundle.getString("activity_tag").equals("DTVChannelList")){
				int db_id = bundle.getInt("db_id");
				int serviceType = bundle.getInt("service_type");
				Log.d(TAG,"channel list db_id="+db_id +"---type="+serviceType );
				hidePasswordDialog();
				if(serviceType==TVProgram.TYPE_RADIO){
					setProgramType(TVProgram.TYPE_RADIO);
					Log.d(TAG,"setProgramType(TVProgram.TYPE_RADIO)");
				}	
				else{
					setProgramType(TVProgram.TYPE_TV);
					Log.d(TAG,"setProgramType(TVProgram.TYPE_TV)");
				}
				DTVPlayerPlayById(db_id);

				ret = true;
			}
		}

		return ret;
	}

	private boolean checkFactoryReset(){
		boolean ret = false;
		
		if(bundle != null && bundle.containsKey("activity_tag")){
			if (bundle.getString("activity_tag").equals("factory_reset")){
				ret = true;
			}
		}
		return ret;
	}

	
	private class InfoDialog extends Dialog {
		public InfoDialog(Context context, int theme) {
			super(context, theme);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				cancel();
				mDialogManager.setActive(false);
				ShowControlBar();
				return true;
			}
			return super.onTouchEvent(event);
		}
	}
	
	private Handler dialogManagerHandler = new Handler();
	private Runnable dialogManagerTimer = new Runnable() {
		public void run() {
			if(mDialogManager!=null)
				mDialogManager.checkDialogDisplay();
			dialogManagerHandler.postDelayed(this, 2000);
		}
	};
	
	public static DialogManager mDialogManager=null;
	public class DialogManager{
		public Context mContext=null;
		public PasswordDialog mPasswordDialog=null;
		public InfoDialog mDialog=null;
		public Toast toast=null;
		public String passdialog_text=null;
		private boolean showDialogActive = true;
		private int signal_range_min =0;
		private int signal_range_max =0;

		public void dialogManagerDestroy(){
			if(dialogManagerHandler!=null)
				dialogManagerHandler.removeCallbacks(dialogManagerTimer);
		}

		public void setActive(boolean v){
			showDialogActive = v;
		}	

		public DialogManager(Context context) {
			this.mContext = context;
			
			if(dialogManagerHandler!=null)
				dialogManagerHandler.postDelayed(dialogManagerTimer, 5000);
		}

		public void checkDialogDisplay(){
				if(showDialogActive){
					
					int snr = getFrontendSNR();
					if(mDTVSettings!=null){
						signal_range_min=mDTVSettings.getSignalQualityRangeMin();
						signal_range_max=mDTVSettings.getSignalQualityRangeMax();
					}
			
					//Log.d(TAG,"--snr-"+snr+"-min-"+signal_range_min+"-max-"+signal_range_max);
					if(getDTVSignalStatus()==false){
						showDia(1);
					}
					else if(snr>signal_range_min&&snr<signal_range_max){
						showDia(4);
					}
					else if(snr <signal_range_min ){
						showDia(1);
					}
					else if(getDTVAVDataStatus()==false){
						showDia(2);
					}
					else if(getDTVLockedStatus()){
						DismissDialog();
						showPasswordDialog(null);
					}
					else if(getDTVScrambledStatus()==true){
						showDia(3);
					}
					else if(getDTVAc3LienceStatus()==false){
						showDia(5);
					}
					else {
						hidePasswordDialog();
						DismissDialog();
					}

					
				}
				else{
					hidePasswordDialog();
					DismissDialog();
				}
				
		}
	
		public void resumeDialog(){
			if(dialogManagerHandler!=null)
				dialogManagerHandler.postDelayed(dialogManagerTimer, 2000);
			if(mDialog!=null)
				mDialog.show();
			if((mPasswordDialog!=null&&mDTVSettings.getCheckProgramLock())&&mDialog==null){
				mPasswordDialog.showDialog();
			}	
		}

		public void pauseDialog(){
			if(dialogManagerHandler!=null)
				dialogManagerHandler.removeCallbacks(dialogManagerTimer);
			if(mDialog!=null)
				mDialog.cancel();
			if(mPasswordDialog!=null)
				mPasswordDialog.cancelDialog();
		}

		public void showPasswordDialog(String t){
			if (isFinishing()){
				return;
			}
			passdialog_text = t;
			if(mDialog==null){
				if(mPasswordDialog==null&&mDTVSettings.getCheckProgramLock()==true){
					mPasswordDialog = new PasswordDialog(mContext){
						public void onCheckPasswordIsRight(){
							unblock();	
							mPasswordDialog=null;
						}

						public void onCheckPasswordIsFalse(){
						}

						public boolean onDealUpDownKey(){
							return true;
						}
					};
					mPasswordDialog.setDialogContent(t);
				}	
			}
		}

		public void hidePasswordDialog(){
			if(mPasswordDialog!=null){
				mPasswordDialog.dismissDialog();
				mPasswordDialog=null;
			}
		}

		public void showDia(int id){
			if(mDTVSettings!=null){
				if(mDTVSettings.getCheckProgramLock()){
					if(mPasswordDialog!=null){
						mPasswordDialog.cancelDialog();
					}
				}
			}

			if (isFinishing()){
				return;
			}
			
			//mDialog = DisplayInfo();
			if(mDialog==null){
				mDialog = new InfoDialog(mContext,R.style.MyDialog);
			}
			mDialog.setCancelable(true);
			mDialog.setCanceledOnTouchOutside(true);
			mDialog.setOnKeyListener( new DialogInterface.OnKeyListener(){
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					dialog.cancel();
					dispatchKeyEvent(event);
					return true;
				}
			});
			
			if(mDialog!=null){
				mDialog.show();
				mDialog.setContentView(R.layout.no_signal);
				Window window = mDialog.getWindow();
				TextView text = (TextView)window.findViewById(R.id.title);
				switch(id){
					case 1:
						text.setText(R.string.dtvplayer_no_signal);
						text.setTextSize(27); 
						text.setGravity(Gravity.CENTER);
						break;
					case 2:
						text.setText(R.string.dtvplayer_no_program);
						text.setTextSize(27); 
						text.setGravity(Gravity.CENTER);
						break;
					case 3:
						text.setText(R.string.dtvplayer_scrambled);
						text.setTextSize(27); 
						text.setGravity(Gravity.CENTER);
						break;
					case 4:
						text.setText(R.string.dtvplayer_signal_bad);
						text.setTextSize(27); 
						text.setGravity(Gravity.CENTER);
						break;
					case 5:	
						text.setText(R.string.dtvplayer_ac3_no_licence);
						text.setTextSize(27); 
						text.setGravity(Gravity.CENTER);
						break;
				}
			}
		}

		public void hideDia(){
			if(mDialog!=null){
				mDialog.cancel();
			}
		}

		public void DismissDialog(){
			if(mDialog!=null){
				mDialog.dismiss();
				mDialog=null;
			}


			if(mDTVSettings!=null){
				if(mDTVSettings.getCheckProgramLock()==true){
					if(mPasswordDialog!=null){
						mPasswordDialog.showDialog();
					}
					else
						showPasswordDialog(passdialog_text);
				}
			}
		}
		
	}
	
	SureDialog mSureDialog=null;
	private void showNoProgramDia(){
		if(mSureDialog==null||(mSureDialog!=null&&mSureDialog.isShowing()==false)){
			mSureDialog = new SureDialog(DTVPlayer.this,false){
				public void onSetMessage(View v){
					((TextView)v).setText(getString(R.string.dtvplayer_no_channel_stored));
				}

				public void onSetNegativeButton(){
					DTVPlayer.this.finish();  
				}
				public void onSetPositiveButton(){
					Intent Intent_scan = new Intent();
					Bundle bundle = new Bundle();
					Intent_scan.putExtras(bundle); 

					String region;
					try {
						region = mDTVSettings.getScanRegion();
					} catch (Exception e) {
						e.printStackTrace();
						Log.d(TAG, "Cannot read dtv region !!!");
						return;
					}

					Log.d(TAG, "region = " + region);

					if(region.contains("DVB-T"))
					{
						Log.d(TAG, "goto DTVScanDVBT");
						Intent_scan.setClass(DTVPlayer.this, DTVSettingsMenu.class);
					}
					else if(region.contains("ATSC"))
					{
						Log.d(TAG, "goto DTVScanATSC");
						Intent_scan.setClass(DTVPlayer.this, DTVSettingsMenu.class);
					}
					else if(region.contains("DVBS"))
					{
						Log.d(TAG, "goto DTVScanDVBS");
						//Intent_scan.setClass(DTVPlayer.this, DTVScanDVBS.class);
						Intent_scan.setClass(DTVPlayer.this, DTVSettingsMenu.class);
					}	
					else if(region.contains("DVB-C"))
					{
						Log.d(TAG, "goto DTVScanDVBC");
						Intent_scan.setClass(DTVPlayer.this, DTVSettingsMenu.class);				
					}
					else
					{
						Log.d(TAG, "goto DTVScanDVBS");
						//Intent_scan.setClass(DTVPlayer.this, DTVScanDVBS.class);
						Intent_scan.setClass(DTVPlayer.this, DTVSettingsMenu.class);
					}		
					
					startActivity(Intent_scan);
				}
			};
		}
		return;
	}

	private void showRecordPathSettingsDia(){
		new SureDialog(DTVPlayer.this,false){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.check_usb_device));
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(){
				Intent Intent_record_device = new Intent();
		        	Intent_record_device.setClass(DTVPlayer.this, DTVRecordDevice.class);
		        	startActivityForResult(Intent_record_device,12);
			}
		};
		return;
	}

	class AudioTrackDialog extends SingleChoiseDialog {
		public AudioTrackDialog(Context context, String[] item, int pos) {
			super(context, item, pos);
		}

		@Override
		public void onSetMessage(View v) {
			((TextView)v).setText(getString(R.string.menu));
		}

		@Override
		public void onSetNegativeButton() {
		}

		@Override
		public void onSetPositiveButton(int which) {
			switch (which) {
				case 0:
					// left
					DTVSetAudioTrack(1);
					break;
				case 1:
					// right
					DTVSetAudioTrack(2);
					break;
				case 2:
					// stereo
					DTVSetAudioTrack(0);
					break;
			}
		}
	}

	class MenuDialog extends SingleChoiseDialog {
		public MenuDialog(Context context, String[] item, int pos) {
			super(context, item, pos);
		}

		@Override
		public void onSetMessage(View v) {
			((TextView)v).setText(getString(R.string.menu));
		}

		@Override
		public void onSetNegativeButton() {
		}

		@Override
		public void onSetPositiveButton(int which) {
			int ad_volume = 0;
			switch (MENUS[which]) {
				case R.string.setting_menu_program:
				case R.string.setting_menu_search:
				case R.string.setting_menu_system:
				case R.string.setting_menu_av:
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putInt("menu", MENUS[which]);
					intent.putExtras(bundle);
					intent.setClass(DTVPlayer.this, DTVSettingsMenu.class);
					startActivity(intent);
					break;
				case R.string.tel_text:
					showTeltext(DTVPlayer.this);
					break;
				case R.string.rec:
					showPvrDurationTimeSetDialog(DTVPlayer.this);
					break;
				case R.string.recall_list:
					showRecallListDialog(DTVPlayer.this);
					break;
				case R.string.dtvplayer_subtitle_language_set:
					HideControlBar();
					showSubtitleSettingMenu(DTVPlayer.this);
					break;
				case R.string.favorite:
					shortcut_key_deal("FAV");
					break;
				case R.string.audio_track:
					shortcut_key_deal("AUDIOTRACK");
					break;
				case R.string.picture_mode:
					shortcut_key_deal("pictrue_mode");
					break;
				case R.string.subtitle:
					shortcut_key_deal("SUBTITLE");
					break;
				case R.string.dtvplayer_audio_language_set:
					showAudioLanguageDialog(DTVPlayer.this);
					break;
				case R.string.timeshift_time_set:
					if (!mDTVSettings.getCheckProgramLock()) {
						if (isHaveExternalStorage()) {
							Intent timeshift = new Intent();
							timeshift.setClass(DTVPlayer.this, DTVTimeshifting.class);
							startActivity(timeshift);
							HideControlBar();
						} else {
							if (toast != null)
								toast.cancel();
							toast = Toast.makeText(DTVPlayer.this, R.string.check_usb_device, Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
					}
					break;
				case R.string.ad:
					if(mDTVSettings.getADSwitch()){
						mDTVSettings.setADSwitch(false);	
						if(toast!=null)
							toast.cancel(); 
						toast = Toast.makeText(DTVPlayer.this, R.string.off, Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					else{
						mDTVSettings.setADSwitch(true);
						if(toast!=null)
							toast.cancel(); 
						toast = Toast.makeText(DTVPlayer.this, R.string.on, Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					break;
				case R.string.ad_volume_up:
					if(mDTVSettings.getADSwitch()){
						ad_volume = mDTVSettings.getADVolume();
						if(ad_volume<100){
							ad_volume++;
							mDTVSettings.setADVolume(ad_volume);
						}
						if(toast!=null)
							toast.cancel(); 
						toast = Toast.makeText(DTVPlayer.this, Integer.toString(ad_volume), Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					break;
				case R.string.ad_volume_down:
					if(mDTVSettings.getADSwitch()){
						ad_volume = mDTVSettings.getADVolume();
						if(ad_volume>0){
							ad_volume--;
							mDTVSettings.setADVolume(ad_volume);
						}
						if(toast!=null)
							toast.cancel(); 
						toast = Toast.makeText(DTVPlayer.this, Integer.toString(ad_volume), Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					break;
				case R.string.exit_player:
					if (dtvplyaer_b_txt && DTVPlayerInTeletextStatus) {
						DTVTTHide();
						DTVPlayerInTeletextStatus = false;
					}
					finishPlayer();
					//setInputSource(TVConst.SourceInput.SOURCE_ATV);
					//System.exit(0);
					getApplication().onTerminate();
					break;
			}
		}
	}

	class MouseClick implements OnClickListener{
	    public void onClick(View v) {
			// TODO Auto-generated method stub	
			switch (v.getId()) {
				case R.id.menu_icon:
					Context ctx = DTVPlayer.this;
					String[] menuItems = new String[menu.MENU_NUM.ordinal()];
					for (int i = 0; i < menuItems.length; i++)
						menuItems[i] = ctx.getString(MENUS[i]);
					new MenuDialog(DTVPlayer.this, menuItems, 0);
					break;
				case R.id.return_icon:
					Log.d(TAG, v + " touch exit!!!!!!!!!!!!!!!!!!!!!");
					if(dtvplyaer_b_txt&&DTVPlayerInTeletextStatus){	
						DTVTTHide();
						DTVPlayerInTeletextStatus=false;
					}	
					
					if(DTVPlayerIsRecording()){
						DTVPlayerStopRecording();
					}	
					
					finishPlayer();
					//setInputSource(TVConst.SourceInput.SOURCE_ATV);
					//System.exit(0);
					getApplication().onTerminate();
					break;
				case R.id.RelativeLayout_video:
					//if(inforbar_show_flag==false){
						//ShowControlBar();
					//}
					//else// if(mainmenu_show_flag==false)
					{
						HideControlBar();
						ShowChannelList();
					}
					break;
				case R.id.Button_mainmenu_list:
					HideMainMenu();
					ShowChannelList();
 		            		break;
				case R.id.Button_mainmenu_epg:
					HideMainMenu();
					HideControlBar();
					Intent Intent_epg = new Intent();
					Intent_epg.setClass(DTVPlayer.this, DTVEpg.class);
					startActivity(Intent_epg);
					break;
				case R.id.Button_mainmenu_settings:
					Intent Intent_settings = new Intent();
					Bundle bundle = new Bundle();
					Intent_settings.putExtras(bundle); 
					Intent_settings.setClass(DTVPlayer.this, DTVSettingsUI.class);
					startActivityForResult(Intent_settings,2);
					break;
				case R.id.Button_mainmenu_program_manager:
					HideMainMenu();
					showProgramManagerDialog();				
					break;
				case R.id.Button_mainmenu_timeshift:
					if(isHaveExternalStorage()){
						Intent Intent_timeshift = new Intent();
						Intent_timeshift.setClass(DTVPlayer.this, DTVTimeshifting.class);
						startActivity(Intent_timeshift);
						HideMainMenu();
						HideControlBar();
					}
					else{
						toast = Toast.makeText(
						DTVPlayer.this,
						R.string.check_usb_device,
						Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					break;
				case R.id.Button_mainmenu_prv:	
					Intent Intent_rec = new Intent();
					Intent_rec.setClass(DTVPlayer.this, DTVRecManager.class);
					startActivity(Intent_rec);
					HideMainMenu();
					HideControlBar();
					break;
				case R.id.Button_mainmenu_manage:
					Intent Intent_book = new Intent();
					Intent_book.setClass(DTVPlayer.this, DTVBookingManager.class);
					startActivity(Intent_book);
					HideMainMenu();
					HideControlBar();
					break;
				case R.id.Button_mainmenu_skip:	
					/*
					Intent Intent_test = new Intent();
					Intent_test.setClass(DTVPlayer.this, MyScroler.class);
					startActivity(Intent_test);
					HideMainMenu();
					HideControlBar();	
					*/
					break;	
				
			}
		}
    }

	public void gotoProgramManager(){
		Intent pickerIntent_pro = new Intent();
		Bundle bundle_promanage = new Bundle();
		bundle_promanage.putInt("db_id", DTVPlayerGetCurrentProgramID());
		pickerIntent_pro.putExtras(bundle_promanage);
		pickerIntent_pro.setClass(DTVPlayer.this, DTVProgramManager.class);
        startActivity(pickerIntent_pro);
	}

	public void showProgramManagerDialog(){
		new PasswordDialog(DTVPlayer.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
				gotoProgramManager();	
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				
			}
			public boolean onDealUpDownKey(){
				return false;
			}
		};
	}
		
	private int bar_hide_count =0;
	private int inforbar_distime=1000;
	private int bar_auto_hide_duration=5;
	private Handler	timer_handler = new Handler();   
	private Runnable timer_runnable = new Runnable(){
		public void run() {
			if(inforbar_show_flag==true&&getProgramServiceType()!=TVProgram.TYPE_RADIO){
				if(mDTVSettings!=null&&bar_hide_count >= mDTVSettings.getInforBarShowTime()){
					HideControlBar();
					HideProgramNo();
					mDialogManager.setActive(true);
				}
				else{
					bar_hide_count ++;
				}		
			} 
		 
			HideInformation();
			//recording time
			if(DTVPlayerIsRecording()){
				DTVPlayerSetRecordingTime();
			}	
			timer_handler.postDelayed(this, inforbar_distime);  
		}   
	};


	private Animation showAction;
	private Animation hideAction;
	private Animation mainMenuShowAction;
	private Animation mainMenuHideAction;
	private Animation showPvrAction;
	private Animation hidePvrAction;
	//private Animation mainMenuButtonFocus;
	private AnimationSet mainMenuButtonFocus ;
	private void init_Animation(){
		showAction = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
			Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		//showAction = new ScaleAnimation(
	    //	1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		showAction.setDuration(300);
		 
		showPvrAction = new TranslateAnimation(
	    	Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
			Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
	    //showAction = new ScaleAnimation(
	        //    1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		showPvrAction.setDuration(300);
		 
		hideAction = new TranslateAnimation(
	                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
	        //hideAction = new ScaleAnimation(
	        //        1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,0.0f);
		hideAction.setDuration(300);
	     
		hidePvrAction = new TranslateAnimation(
	                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
	        //hideAction = new ScaleAnimation(
	        //      1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,0.0f);
		hidePvrAction.setDuration(300);

		mainMenuShowAction  = new TranslateAnimation(
	                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				 Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
	        //showAction = new ScaleAnimation(
	        //    1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		mainMenuShowAction.setDuration(300);	

		mainMenuHideAction = new TranslateAnimation(
	                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
				 Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
	        //showAction = new ScaleAnimation(
	        //    1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		mainMenuHideAction.setDuration(300);
	
		/*
		mainMenuButtonFocus = new TranslateAnimation(
	                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				 Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		mainMenuButtonFocus.setDuration(300);
		*/
		mainMenuButtonFocus = new AnimationSet(true);
	    ScaleAnimation  scale  = new ScaleAnimation(1.2f, 0.8f, 1.2f, 0.8f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
	     mainMenuButtonFocus.addAnimation(scale);
	     scale.setDuration(300);
	     scale.setRepeatCount(0);
		 mainMenuButtonFocus.addAnimation(scale);
	}


	private boolean mainmenu_show_flag=false;
	private void ShowMainMenu(){
		/*
		if((mainmenu_show_flag==false)&&(RelativeLayout_mainmenu!=null&&mainMenuShowAction!=null)){	
			RelativeLayout_mainmenu.startAnimation(mainMenuShowAction);   
			RelativeLayout_mainmenu.setVisibility(View.VISIBLE);
			RelativeLayout_mainmenu.requestFocus();
		}
		*/
		mainmenu_show_flag = true;	

		Intent intent = new Intent();
		//intent.setClass(DTVPlayer.this, DTVMainMenu.class);
		intent.setClass(DTVPlayer.this, DTVSettingsMenu.class);
        	startActivity(intent);
		//overridePendingTransition(R.anim.slide_left, R.anim.slide_right); 
	}

	private void HideMainMenu(){
		/*
		if(mainmenu_show_flag){	
			if(RelativeLayout_mainmenu!=null&&mainMenuHideAction!=null){
				RelativeLayout_mainmenu.startAnimation(mainMenuHideAction);   
				RelativeLayout_mainmenu.setVisibility(View.INVISIBLE);
			}
		}
		*/
		mainmenu_show_flag = false;
	}

	private void ShowChannelList(){
		Intent pickerIntent = new Intent();
		Bundle bundle_list = new Bundle();
		bundle_list.putInt("db_id", DTVPlayerGetCurrentProgramID());
		pickerIntent.putExtras(bundle_list);
		pickerIntent.setClass(DTVPlayer.this, DTVChannelList.class);
		startActivity(pickerIntent);
	}
	
	private boolean inforbar_show_flag=false;
	private boolean pvr_show_flag=false;
	private void ShowControlBar(){
		timer_handler.removeCallbacks(timer_runnable);
		if((inforbar_show_flag==false)&&(RelativeLayout_inforbar!=null&&showAction!=null&&RelativeLayout_recording_icon!=null&&showPvrAction!=null)){	
			RelativeLayout_inforbar.startAnimation(showAction);   
			RelativeLayout_inforbar.setVisibility(View.VISIBLE);
			if(DTVPlayerIsRecording()){
				RelativeLayout_recording_icon.startAnimation(showPvrAction);   
				RelativeLayout_recording_icon.setVisibility(View.VISIBLE);
				pvr_show_flag=true;
			}	
			else{
				hidePvrIcon();
			}
		}
		
		bar_hide_count = 0;
		inforbar_show_flag = true;
		timer_handler.postDelayed(timer_runnable, inforbar_distime); 
	}

	private void HideControlBar(){
		//inforbarLayout.setVisibility(View.INVISIBLE);
		if(inforbar_show_flag){	
			if(RelativeLayout_inforbar!=null&&hideAction!=null){
				RelativeLayout_inforbar.startAnimation(hideAction);   
    			RelativeLayout_inforbar.setVisibility(View.INVISIBLE);
			}
			if(RelativeLayout_recording_icon!=null&&hidePvrAction!=null){
				hidePvrIcon();
			}
		}
		inforbar_show_flag = false;
	}	


	private Handler timer_channel_info_handler = new Handler();   
	private Runnable timer_channel_info_runnable = new Runnable() {

		public void run() {
		    	updataSignalInfo();
			timer_channel_info_handler.postDelayed(this, 1000);
		}   
	};

	private void updataSignalInfo()
	{
		
		TextView strenght = (TextView)channel_info_view.findViewById(R.id.channel_strenght);
		TextView signal_quality = (TextView)channel_info_view.findViewById(R.id.channel_signal_quality);
		//TextView error_rate = (TextView)channel_info_view.findViewById(R.id.channel_error_rate);

		int strength=getFrontendSignalStrength();
		if(strength>100)
			strength=0;
		strenght.setText(this.getResources().getString(R.string.channel_strenght)+": "+Integer.toString(strength));
		signal_quality.setText(this.getResources().getString(R.string.channel_signal_quality)+": "+Integer.toString(getFrontendSNR()));
		//error_rate.setText(this.getResources().getString(R.string.channel_error_rate)+": "+Integer.toString(getFrontendBER()));

		editBuilder.setView(channel_info_view); 

		alert_password.setOnKeyListener( new DialogInterface.OnKeyListener(){
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				switch(keyCode)
				{	
					case  DTVActivity.KEYCODE_INFO:
							dialog.cancel();
							return true;
				}
				return false;
			}
		});
	}

	LinearLayout channel_info_view = null; 
	private AlertDialog.Builder editBuilder;
	private void showChannelInfo(){
		Log.d(TAG, "report : ber:" + getFrontendBER() + " snr:" +getFrontendSNR() + " strength:" + getFrontendSignalStrength());

		editBuilder = new AlertDialog.Builder(this);	
		
		channel_info_view = (LinearLayout)getLayoutInflater().inflate(R.layout.dvbs_channel_info, null);
		TextView fre_text = null;
		int frequency = 0;

		if(mDTVSettings.getScanRegion().contains("DVBS")){
			if(mTVProgram!=null){
				if(mTVProgram.getChannel()!=null){
					frequency = mTVProgram.getChannel().getParams().getFrequency();
					int sym = mTVProgram.getChannel().getParams().getSymbolRate();
					int polartion = mTVProgram.getChannel().getParams().getPolarisation();
					String pol="";
					if(polartion ==0)
					pol="V";
					else
					pol="H";
					int sat_id = mTVProgram.getChannel().getParams().getSatId();
					String sat_name = TVSatellite.tvSatelliteSelect(DTVPlayer.this,sat_id).getSatelliteName();
					fre_text = (TextView)channel_info_view.findViewById(R.id.fre);
					fre_text.setText(String.valueOf(frequency/1000)+"MHz   "+pol+"   "+String.valueOf(sym/1000)+"KS/s   "+sat_name);
				
					TextView satellites_name = (TextView)channel_info_view.findViewById(R.id.sat_name);
					satellites_name.setText(this.getResources().getString(R.string.sat_name)+": "+sat_name);

					TextView symb = (TextView)channel_info_view.findViewById(R.id.symb);
					symb.setText(this.getResources().getString(R.string.ts_symbol)+": "+Integer.toString(sym/1000)+" KS/s");

					TextView polar = (TextView)channel_info_view.findViewById(R.id.polar);
					String polar_s = (polartion==0)?"H":"V";
					polar.setText(this.getResources().getString(R.string.ts_polarity)+": "+polar_s);
				}
			}
		}	
		else{
			channel_info_view = (LinearLayout)getLayoutInflater().inflate(R.layout.channel_info, null);
		
			if(mTVProgram!=null){
				if(mTVProgram.getChannel()!=null){
					frequency = mTVProgram.getChannel().getParams().getFrequency();
				}
			}
			fre_text = (TextView)channel_info_view.findViewById(R.id.fre);
			fre_text.setText(this.getResources().getString(R.string.channel_fre)+": "+Integer.toString(frequency/1000)+" kHZ");
		}	 
		TextView channel_name = (TextView)channel_info_view.findViewById(R.id.channel_name);
		
		TextView fre = (TextView)channel_info_view.findViewById(R.id.fre);
		TextView strenght = (TextView)channel_info_view.findViewById(R.id.channel_strenght);
		TextView signal_quality = (TextView)channel_info_view.findViewById(R.id.channel_signal_quality);
		//TextView error_rate = (TextView)channel_info_view.findViewById(R.id.channel_error_rate);

		TextView vid = (TextView)channel_info_view.findViewById(R.id.video_pid);
		TextView aid = (TextView)channel_info_view.findViewById(R.id.audio_pid);

		channel_name.setText(this.getResources().getString(R.string.channel_name)+": "+dtvplayer_name);

		int strength=getFrontendSignalStrength();
		if(strength>100)
			strength=0;
		strenght.setText(this.getResources().getString(R.string.channel_strenght)+": "+Integer.toString(strength));
		signal_quality.setText(this.getResources().getString(R.string.channel_signal_quality)+": "+Integer.toString(getFrontendSNR()));
		//error_rate.setText(this.getResources().getString(R.string.channel_error_rate)+": "+Integer.toString(getFrontendBER()));

		int  video_pid = 0x1fff;
		int audio_pid = 0x1fff;
		
		if(mTVProgram!=null){
			
			video_pid  = mTVProgram.getVideo().getPID();
			audio_pid  = mTVProgram.getAudio().getPID();
		}		

		vid.setText(this.getResources().getString(R.string.channel_video_pid)+": "+Integer.toString(video_pid));
		aid.setText(this.getResources().getString(R.string.channel_audio_pid)+": "+Integer.toString(audio_pid));	

		editBuilder.setTitle(R.string.channel_info);
		editBuilder.setView(channel_info_view); 
 
		alert_password = editBuilder.create();

		alert_password.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
			}         
		}); 	

		alert_password.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog) {
				timer_channel_info_handler.removeCallbacks(timer_channel_info_runnable);  
			}         
		});	

		alert_password.show();
		//alert_password.getWindow().setLayout(400, -1);
			
		WindowManager.LayoutParams lp=alert_password.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		alert_password.getWindow().setAttributes(lp);
		alert_password.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
		timer_channel_info_handler.postDelayed(timer_channel_info_runnable, 1000);

	}


	private boolean radio_bg_flag=false;
	private void showRadioBg(){
		if(radio_bg_flag==false){
			RelativeLayout_radio_bg.setVisibility(View.VISIBLE);
			radio_bg_flag = true;
		}	
	}

	private void hideRadioBg(){
		if(radio_bg_flag){
			RelativeLayout_radio_bg.setVisibility(View.INVISIBLE);
			radio_bg_flag = false;
		}
	}

	private void updateInforbar(){
		if (mDTVSettings == null)
			return;
		dtvplayer_atsc_antenna_source = mDTVSettings.getAtscAntennaSource();
		Text_screentype_info = (TextView) findViewById(R.id.Text_screentype_info);
		Text_parent_control_info_icon = (TextView) findViewById(R.id.Text_parent_control_info_icon);
		Text_channel_type = (TextView) findViewById(R.id.Text_channel_type);
		Text_MTS_info = (TextView) findViewById(R.id.Text_MTS_info);

		ImageView_icon_scrambled=(ImageView)findViewById(R.id.ImageView_icon_scrambled);
		ImageView_icon_fav=(ImageView)findViewById(R.id.ImageView_icon_fav);
		ImageView_icon_lock=(ImageView)findViewById(R.id.ImageView_icon_lock);

		ImageView_icon_epg=(ImageView)findViewById(R.id.ImageView_icon_epg);
		ImageView_icon_sub=(ImageView)findViewById(R.id.ImageView_icon_sub);
		ImageView_icon_txt=(ImageView)findViewById(R.id.ImageView_icon_txt);

		if(mDTVSettings.getScanRegion().contains("ATSC"))
			Text_channel_type.setText(dtvplayer_atsc_antenna_source);
		else
			Text_channel_type.setText(null);
		
		TextView Text_proname = (TextView) findViewById(R.id.Text_proname);
		Text_proname.setTextColor(Color.YELLOW);

		TextView Text_curevent = (TextView) findViewById(R.id.Text_curevent);
		Text_curevent.setTextColor(Color.YELLOW);
		if(dtvplayer_cur_event==null)
			Text_curevent.setText(getString(R.string.dtvplayer_no_current_event));
		else
			Text_curevent.setText(dtvplayer_cur_event);
		
		TextView Text_title_info = (TextView) findViewById(R.id.Text_title_info);
		if(dtvplayer_event_des==null)
			Text_title_info.setText(getString(R.string.dtvplayer_title_info));
		else
			Text_title_info.setText(dtvplayer_event_des);
	
		TextView Text_detail_info = (TextView) findViewById(R.id.Text_detail_info);
		if(dtvplayer_event_ext_des==null)
			Text_detail_info.setText(getString(R.string.dtvplayer_detail_info));
		else
			Text_detail_info.setText(dtvplayer_event_ext_des);

		TextView Text_nextevent = (TextView) findViewById(R.id.Text_nextevent);
		if(dtvplayer_next_event==null)
			Text_nextevent.setText(getString(R.string.dtvplayer_no_next_event));
		else
			Text_nextevent.setText(dtvplayer_next_event);

		TextView Text_parent_rate = (TextView) findViewById(R.id.Text_parent_rate);	
		

		if(mDTVSettings.getScanRegion().contains("ATSC")==false){
			Text_proname.setText(Integer.toString(dtvplayer_pronumber)+"  "+dtvplayer_name);
		}
		else{
			if(dtvplayer_pronumber>=0)	
			Text_proname.setText(Integer.toString(dtvplayer_pronumber_major)+"-"+Integer.toString(dtvplayer_pronumber_minor)+"  "+dtvplayer_name);
		}
		
		if(dtvplayer_b_fav)
			ImageView_icon_fav.setVisibility(View.VISIBLE);
		else
			ImageView_icon_fav.setVisibility(View.INVISIBLE);

		if(dtvplayer_b_scrambled)
			ImageView_icon_scrambled.setVisibility(View.VISIBLE);
		else
			ImageView_icon_scrambled.setVisibility(View.INVISIBLE);

		if(dtvplayer_b_lock)
			ImageView_icon_lock.setVisibility(View.VISIBLE);
		else
			ImageView_icon_lock.setVisibility(View.INVISIBLE);

		if(dtvplayer_b_epg)
			ImageView_icon_epg.setVisibility(View.VISIBLE);
		else
			ImageView_icon_epg.setVisibility(View.INVISIBLE);

		if(dtvplyaer_b_txt)
			ImageView_icon_txt.setVisibility(View.VISIBLE);
		else
			ImageView_icon_txt.setVisibility(View.INVISIBLE);

		if(dtvplayer_b_sub)
			ImageView_icon_sub.setVisibility(View.VISIBLE);
		else
			ImageView_icon_sub.setVisibility(View.INVISIBLE);
		
		int mode = DTVGetScreenMode();
		int pos = 0;
		final int step = 5;
		if(mode==1){
			pos = 0;
		}
		else {
			pos = mode-step;
		}
				 
		 switch(pos){
			case 0:
				Text_screentype_info.setText(R.string.full_screen);						
				break;
			case 1:
				Text_screentype_info.setText(R.string.type_4_3_IGNORE);						
				break;
			case 2:
				Text_screentype_info.setText(R.string.type_4_3_LETTER_BOX);
			break;
			case 3:
				Text_screentype_info.setText(R.string.type_4_3_PAN_SCAN);
				break;	
			case 4:
				Text_screentype_info.setText(R.string.type_4_3_COMBINED);						
				break;
			case 5:
				Text_screentype_info.setText(R.string.type_16_9_IGNORE);						
				break;
			case 6:
				Text_screentype_info.setText(R.string.type_16_9_LETTER_BOX);						
				break;
			case 7:
				Text_screentype_info.setText(R.string.type_16_9_PAN_SCAN);						
				break;
			case 8:
				Text_screentype_info.setText(R.string.type_16_9_COMBINED);						
				break;
			
		}
		
		mode = DTVGetAudioTrack();
		if(mode==0){ 						
			Text_MTS_info.setText(getString(R.string.stereo));			
		}
		else  if(mode==1){
			Text_MTS_info.setText(getString(R.string.left));			
		}
		else  if(mode==2){
			Text_MTS_info.setText(getString(R.string.right));				
		}	
		
		if(DTVGetSubtitleStatus()){
			Text_parent_control_info_icon.setText(getString(R.string.sub)+":"+getString(R.string.on));	
		}
		else{
			Text_parent_control_info_icon.setText(getString(R.string.sub)+":"+getString(R.string.off));
		}

		if(getProgramServiceType()==TVProgram.TYPE_RADIO)
			showRadioBg();
		else
			hideRadioBg();	
	}

	void showProgramNo(String value){
		if(value!=null){
			RelativeLayout RelativeLayoutProNumer = (RelativeLayout)findViewById(R.id.RelativeLayoutProNumer);
			TextView Text_program_no = (TextView)findViewById(R.id.Text_program_no);
			Text_program_no.setText(value);	
			RelativeLayoutProNumer.setVisibility(View.VISIBLE);
		}	
	}
	
	void ShowProgramNo(int value){
		if(value!=0){
			RelativeLayout RelativeLayoutProNumer = (RelativeLayout)findViewById(R.id.RelativeLayoutProNumer);
			TextView Text_program_no = (TextView)findViewById(R.id.Text_program_no);
			Text_program_no.setText(String.valueOf(value));
			RelativeLayoutProNumer.setVisibility(View.VISIBLE);
		}	
	}

	void HideProgramNo(){
		RelativeLayout RelativeLayoutProNumer = (RelativeLayout)findViewById(R.id.RelativeLayoutProNumer);
		RelativeLayoutProNumer.setVisibility(View.INVISIBLE);
	}

	void showPvrIcon(){
		if(pvr_show_flag==false){
			final Animation animation = new AlphaAnimation(0, 1); // Change alpha from fully visible to invisible
			animation.setDuration(1000); // duration - half a second
			animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
			animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
			animation.setRepeatMode(Animation.REVERSE); // 

			ImageView ImageView_recordingIcon = (ImageView) findViewById(R.id.ImageView_recordingIcon);
			ImageView_recordingIcon.setVisibility(View.VISIBLE);
			ImageView_recordingIcon.setAnimation(animation);

			Text_recording_time = (TextView) findViewById(R.id.Text_recording_time);
			Text_recording_time.setText("00:00:00");	
			Text_recording_time.setVisibility(View.VISIBLE);
			Text_recording_time.setTextColor(Color.RED);

			RelativeLayout RelativeLayoutPvrIcon = (RelativeLayout)findViewById(R.id.RelativeLayoutPvrIcon);
			RelativeLayoutPvrIcon.setVisibility(View.VISIBLE);
			pvr_show_flag=true;
			timer_handler.postDelayed(timer_runnable, inforbar_distime);
		}	
	}

	void hidePvrIcon(){
		if(pvr_show_flag){
			RelativeLayout RelativeLayoutPvrIcon = (RelativeLayout)findViewById(R.id.RelativeLayoutPvrIcon);
			RelativeLayoutPvrIcon.setVisibility(View.INVISIBLE);
			pvr_show_flag=false;
		}	
	}

	private boolean showInformation_flag=false;
	void ShowInformation(String text){
		timer_handler.removeCallbacks(timer_runnable);
		if(showInformation_flag==false){
			RelativeLayout layout =  (RelativeLayout)findViewById(R.id.information);
	
			TextView information_text = (TextView)findViewById(R.id.information_text);
			information_text.setText(text);
			layout.setVisibility(View.VISIBLE);
			showInformation_flag=true;
		}	
		else {
			TextView information_text = (TextView)findViewById(R.id.information_text);
			information_text.setText(text);
		}
		
		timer_handler.postDelayed(timer_runnable, inforbar_distime); 	
	}

	void HideInformation(){
		if(showInformation_flag==true){
			RelativeLayout layout =  (RelativeLayout)findViewById(R.id.information);
			layout.setVisibility(View.INVISIBLE);
			layout.setVisibility(View.GONE);
			showInformation_flag=false;
		}	
	}
	
	private void DTVPlayerSetRecordingTime(){
		long cur_recording_time=0;
		cur_recording_time = DTVPlayerGetRecordDuration();	
		TextView Text_recording_time = (TextView) findViewById(R.id.Text_recording_time);
		Text_recording_time.setTextColor(Color.RED);
		Text_recording_time.setText(secToTime(cur_recording_time,false));
	}

	private boolean DTVPlayerCheckNumerInputIsValid(int pronumber){
		//pronumber is valid or hided
		return true;
	}

	boolean number_key_down = false;
    private Handler prono_timer_handler = new Handler();  
	private Runnable prono_timer_runnable = new Runnable() {
		public void run() {
			if(number_key_down){	
				hidePasswordDialog();
				if(mDTVSettings.getScanRegion().contains("ATSC")){
					Log.d(TAG,"prono_timer_runnable ---pronumber_string="+pronumber_string);
					pronumber = 0;	
					pronumber_string ="";
					HideProgramNo();
					
					Log.d(TAG,"PROGRAM NUMBER:---"+input_major+"-"+input_minor);
					DTVPlayerPlayAtscByProNo(input_major,input_minor);
					input_major=0;	
					input_minor=0;
				}
				else{	
					if(DTVPlayerInTeletextStatus){
						pronumber = 0;	
						pronumber_string ="";
						HideProgramNo();
					}
					else if((DTVPlayerCheckNumerInputIsValid(pronumber)==false)||(pronumber<=0)){
						toast = Toast.makeText(
							DTVPlayer.this, 
				    		R.string.invalid_input,
				    		Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
							pronumber = 0;	
							pronumber_string ="";
							HideProgramNo();
					}
					else{	
						if(DTVPlayerIsRecording()){

							new SureDialog(DTVPlayer.this){
								public void onSetMessage(View v){
									((TextView)v).setText(getString(R.string.dtvplayer_change_channel));
								}

								public void onSetNegativeButton(){
									pronumber = 0;	
									pronumber_string ="";
									HideProgramNo();
								}
								public void onSetPositiveButton(){
									DTVPlayerStopRecording();
									DTVPlayerPlayByProNo(pronumber);
									pronumber = 0;	
									pronumber_string ="";
									HideProgramNo();
								}
							};
						}
						else{
							DTVPlayerPlayByProNo(pronumber);
							pronumber = 0;	
							pronumber_string ="";
							HideProgramNo();
						}
					}	
				}	
				number_key_down = false;
			}	
			prono_timer_handler.removeCallbacks(prono_timer_runnable);		
		}   
	};

	private int input_major = 0;
	private int input_minor = 0;
	public static boolean DTVPlayerInTeletextStatus=false;
	private void DTVDealDigtalKey(int value){
		int number_key_value=0;

		if(value == KeyEvent.KEYCODE_2 && debugMenuDialogShowFlag == 0x0){
			debugMenuDialogShowFlag |= 0x1;
		}else if(value == KeyEvent.KEYCODE_4 && debugMenuDialogShowFlag == 0x1){
			debugMenuDialogShowFlag |= 0x2;
		}else if(value == KeyEvent.KEYCODE_6 && debugMenuDialogShowFlag == 0x3){
			debugMenuDialogShowFlag |= 0x4;
		}else if(value == KeyEvent.KEYCODE_8 && debugMenuDialogShowFlag == 0x7){
			debugMenuDialogShowFlag |= 0x8;
		}else{
			debugMenuDialogShowFlag = 0x0;
		}

		if(mDTVSettings.getScanRegion().contains("ATSC")){
			if(DTVPlayerInTeletextStatus==false){
				prono_timer_handler.removeCallbacks(prono_timer_runnable);
				
				if(value==KeyEvent.KEYCODE_MEDIA_NEXT){
					boolean hasflag=false;
					if(pronumber_string.equals("")==false){
						for(int i=0;i<pronumber_string.length();i++){
							if(pronumber_string.regionMatches(i,"-",0,1)){
								Log.d(TAG,"has--------"+pronumber_string);
								hasflag=true;
								break;
							}	
						}	

						if(hasflag==false){
							if(pronumber==0){
								pronumber_string=Integer.toString(dtvplayer_pronumber_major)+"-";	
							}
							else{
								pronumber_string=Integer.toString(pronumber)+"-";
								input_major = pronumber;
								input_minor = 0;
							}
						}	
						else{
							
							pronumber_string=Integer.toString(dtvplayer_pronumber_major);
							input_major = dtvplayer_pronumber_major;
							input_minor = dtvplayer_pronumber_minor;
						}
					}
					else{
						pronumber_string=Integer.toString(dtvplayer_pronumber_major)+"-";
						input_major = dtvplayer_pronumber_major;
						input_minor = 0;
					}	
					pronumber=0;
				}
				else{
					boolean hasflag=false;
					
					for(int i=0;i<pronumber_string.length();i++){
						if(pronumber_string.regionMatches(i,"-",0,1)){
							Log.d(TAG,"has--------"+pronumber_string);
							hasflag=true;
							break;
						}	
					}						
					
					number_key_value = value - KeyEvent.KEYCODE_0;
					
					if(hasflag){
						if(pronumber>=10){
							pronumber = number_key_value;
						}	
						else{
							pronumber = pronumber*10+number_key_value;
						}	
						input_minor = pronumber;
						pronumber_string =Integer.toString(input_major)+"-"+Integer.toString(input_minor);
					}
					else{
						if(pronumber>=100){
							pronumber = number_key_value;
						}	
						else{
							pronumber = pronumber*10+number_key_value;
						}	
						input_major = pronumber;
						pronumber_string =Integer.toString(input_major);
					}
				}
				
				Log.d(TAG,"pronumber_string="+pronumber_string);
				showProgramNo(pronumber_string);
				prono_timer_handler.postDelayed(prono_timer_runnable, 2000);
				number_key_down = true;
			}
			else{
				prono_timer_handler.removeCallbacks(prono_timer_runnable);
				number_key_value = value - KeyEvent.KEYCODE_0;
				pronumber = pronumber*10+number_key_value;
				if(pronumber>9999){
					pronumber = number_key_value;
				}	
				ShowProgramNo(pronumber);
				DTVTTGotoPage(pronumber);
				prono_timer_handler.postDelayed(prono_timer_runnable, 2000);
				number_key_down = true;
			}
		}
		else{
		
			if(DTVPlayerInTeletextStatus==false){
				prono_timer_handler.removeCallbacks(prono_timer_runnable);
				number_key_value = value - KeyEvent.KEYCODE_0;
				pronumber = pronumber*10+number_key_value;
				if(pronumber>9999){
					pronumber = number_key_value;
				}	
				ShowProgramNo(pronumber);
				prono_timer_handler.postDelayed(prono_timer_runnable, 2000);
				number_key_down = true;
			}
			else{
				prono_timer_handler.removeCallbacks(prono_timer_runnable);
				number_key_value = value - KeyEvent.KEYCODE_0;
				pronumber = pronumber*10+number_key_value;
				if(pronumber>9999){
					pronumber = number_key_value;
				}	
				ShowProgramNo(pronumber);
				DTVTTGotoPage(pronumber);
				prono_timer_handler.postDelayed(prono_timer_runnable, 2000);
				number_key_down = true;
			}
		}
	}

	private void shortcut_key_deal(String key){
		if(key.equals("pictrue_mode")){
			
			int mode = DTVGetScreenMode();
			if(mode==1){
				DTVSetScreenMode(mode+5);
			}
			else  if(mode==13){
					
				DTVSetScreenMode(1);
			}
			else{
				
				DTVSetScreenMode(mode+1);
			}
			String info = getScreenTypeStrings();
			if(info!=null){
				int pos = info.indexOf(":");
				info = info.substring(pos+1,info.length());
			}
			ShowInformation(info);		
		}
		else if(key.equals("AUDIOTRACK")){
			int mode = DTVGetAudioTrack();
			if(mode==1){ 						
				ShowInformation(getString(R.string.right));			
				DTVSetAudioTrack(2);
				Text_MTS_info.setText(getString(R.string.right));			
			}
			else  if(mode==2){
				ShowInformation(getString(R.string.stereo));			
				DTVSetAudioTrack(0);
				Text_MTS_info.setText(getString(R.string.stereo));			
			}
			else  if(mode==0){
				ShowInformation(getString(R.string.left));				
				DTVSetAudioTrack(1);
				Text_MTS_info.setText(getString(R.string.left));				
			}	
		}
		else if(key.equals("SUBTITLE")){
			if(DTVGetSubtitleStatus()){
				DTVSetSubtitleStatus(false);
				ShowInformation(getString(R.string.sub)+":"+getString(R.string.off));	
				Text_parent_control_info_icon.setText(getString(R.string.sub)+":"+getString(R.string.off));	
			}
			else{
				DTVSetSubtitleStatus(true);
				ShowInformation(getString(R.string.sub)+":"+getString(R.string.on));
				Text_parent_control_info_icon.setText(getString(R.string.sub)+":"+getString(R.string.on));	
			}
		}
		else if(key.equals("FAV")){
			if(DTVPlayerSetFav()){
				ShowInformation(getString(R.string.favorite));	
			}	
			else{
				ShowInformation(getString(R.string.unfavorite));
			}
		}
	}

	AlertDialog alert_password=null;
	private boolean password_dialog_show_flag=false;
	private String cur_password=null;
	public void hidePasswordDialog(){
		if(mPasswordDialog!=null){
			mPasswordDialog.dismissDialog();
			mPasswordDialog=null;
		}
	}

	private int getProgramServiceType(){
		//if(dtvplayer_service_type==TVProgram.TYPE_TV)
		return dtvplayer_service_type;
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
	        case 1:
				mAlertDialog = new AlertDialog.Builder(DTVPlayer.this).create();
				TextView text = new TextView(this);
				text.setText(R.string.dtvplayer_no_signal);
				text.setTextSize(27); 
				text.setGravity(Gravity.CENTER);
				mAlertDialog.setView(text);

				WindowManager.LayoutParams lp=mAlertDialog.getWindow().getAttributes();
				lp.dimAmount=0.0f; 
				mAlertDialog.getWindow().setAttributes(lp);
				mAlertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);			
						
				mAlertDialog.setOnKeyListener( new DialogInterface.OnKeyListener(){
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						dialog.cancel();
						//mAlertDialog_flag = false;
						dispatchKeyEvent(event);
						return true;
					}
				});
			    return 	mAlertDialog;
			case 2:
				mAlertDialog = new AlertDialog.Builder(DTVPlayer.this).create();
				TextView text1 = new TextView(this);
				text1.setText(R.string.dtvplayer_no_program);
				text1.setTextSize(27); 
				text1.setGravity(Gravity.CENTER);
				mAlertDialog.setView(text1);
				mAlertDialog.setOnKeyListener( new DialogInterface.OnKeyListener(){
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						dialog.cancel();
						//mAlertDialog_avdata_flag = false;
						dispatchKeyEvent(event);
						return true;
					}
				});
			    return 	mAlertDialog;
			case 3:
		        mAlertDialog = new AlertDialog.Builder(DTVPlayer.this).create();
				TextView text2 = new TextView(this);
				text2.setText(R.string.dtvplayer_scrambled);
				text2.setTextSize(27); 
				text2.setGravity(Gravity.CENTER);
				mAlertDialog.setView(text2);		
				mAlertDialog.setOnKeyListener( new DialogInterface.OnKeyListener(){
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						dialog.cancel();
						//mAlertDialog_avscamble_flag = false;
						dispatchKeyEvent(event);
						return true;
					}
				});
			    return 	mAlertDialog;
		}
        return null;
    }

	private Dialog mDialog=null;
	private Dialog DisplayInfo(){
		mDialog = new Dialog(this,R.style.MyDialog);
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.setOnKeyListener( new DialogInterface.OnKeyListener(){
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				//dialog.cancel();
				dispatchKeyEvent(event);
				return true;
			}
		});
		return mDialog;
	}

	private void DismissDialog(){
		if(mDialog!=null){
			mDialog.dismiss();
			mDialog=null;
		}
	}

	private void showDia(int id){
		mDialog = DisplayInfo();
		if(mDialog!=null){
			mDialog.show();
			mDialog.setContentView(R.layout.no_signal);
			Window window = mDialog.getWindow();
			TextView text = (TextView)window.findViewById(R.id.title);
			switch(id){
				case 1:
					text.setText(R.string.dtvplayer_no_signal);
					text.setTextSize(27); 
					text.setGravity(Gravity.CENTER);
					break;
				case 2:
					
					text.setText(R.string.dtvplayer_no_program);
					text.setText(R.string.dtvplayer_no_signal);
					text.setTextSize(27); 
					text.setGravity(Gravity.CENTER);
				case 3:
					text.setText(R.string.dtvplayer_scrambled);
					text.setTextSize(27); 
					text.setGravity(Gravity.CENTER);
					break;
			}
			/*
			mDialog.getWindow().setLayout(500,100);
			WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
			Log.d(TAG,"x="+lp.x+"y="+lp.y);
			lp.x=0;
			lp.y=0;
			lp.dimAmount=0.0f;
			mDialog.getWindow().setAttributes(lp);
			mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			*/
		}
	}

	private String pronumber_string="";
	private int pronumber=0;
	private int recordDurationMin=1;
	public int dtvplayer_service_type = 0;
	public static int dtvplayer_pronumber=0;
	public static int dtvplayer_pronumber_major=0;
	public static int dtvplayer_pronumber_minor=0;
	public static TVProgramNumber dtvplayer_program_number;
	public static String dtvplayer_name=" ";
	public String dtvplayer_cur_event=null;
	public String dtvplayer_next_event=null;
	public String dtvplayer_event_des=null;
	public String dtvplayer_event_ext_des=null;
	public static boolean dtvplayer_b_lock=false;
	public static boolean dtvplayer_b_fav=false;
	public static boolean dtvplayer_b_scrambled=false;
	public static boolean dtvplayer_b_epg=false;
	public static boolean dtvplyaer_b_txt=false;
	public static boolean dtvplayer_b_sub=false;
	public static String  dtvplayer_atsc_antenna_source=null;
	
	private static TVProgram.Subtitle mSubtitle[]=null;
	private static TVProgram.Teletext mTeletext[]=null;
	private static int mSubtitleCount=0;
	private static int mTeletextCount=0;
	private static int mSubtitleIndex=0;
	private static String mSubtitleLang[]=null;

	TVProgram.Audio mAudio[]=null;
	private static int mAudioCount=0;
	private static int mAudioIndex=0;
	private static String mAudioLang[]=null;

	private TVProgram mTVProgram=null;
	private synchronized void DTVPlayerGetCurrentProgramData(){
		mTVProgram=DTVPlayerGetDataByCurrentID();

		if(mTVProgram!=null){
			TVProgram mTempTVProgram = mTVProgram;
			dtvplayer_program_number= mTempTVProgram.getNumber();
			if(mDTVSettings.getScanRegion().contains("ATSC")==false){
				
				dtvplayer_pronumber= mTempTVProgram.getNumber().getNumber();
				dtvplayer_pronumber_major = dtvplayer_program_number.getMajor();
				dtvplayer_pronumber_minor = dtvplayer_program_number.getMinor(); 
				Log.d(TAG,"pronumber="+dtvplayer_pronumber);
			}	
			else {
				dtvplayer_pronumber_major = dtvplayer_program_number.getMajor();
				dtvplayer_pronumber_minor = dtvplayer_program_number.getMinor();
				Log.d(TAG,"ATSC pronumber="+dtvplayer_pronumber_major+"-"+dtvplayer_pronumber_minor);
			}

			dtvplayer_b_epg = false;
			dtvplayer_service_type = mTempTVProgram.getType();
			dtvplayer_name = mTempTVProgram.getName();
			dtvplayer_b_lock = mTempTVProgram.getLockFlag();
			dtvplayer_b_fav = mTempTVProgram.getFavoriteFlag();
			dtvplayer_b_scrambled = mTempTVProgram.getScrambledFlag();
			dtvplayer_atsc_antenna_source = mDTVSettings.getAtscAntennaSource();

			mAudioCount=mTempTVProgram.getAudioCount();
			if(mAudioCount>0){
				mAudio= new TVProgram.Audio[mAudioCount];
				mAudioLang = new String[mAudioCount];
				for(int i=0;i<mAudioCount;i++){
					mAudio[i]=mTempTVProgram.getAudio(i);
					mAudioLang[i]= mAudio[i].getLang();
					Log.d(TAG,"Audio Lang:"+mAudioLang[i]);
				}
				mAudioIndex = mTempTVProgram.getCurrentAudio(getStringConfig("tv:audio:language"));
			}	

			mSubtitleCount=mTempTVProgram.getSubtitleCount();
			if(mSubtitleCount>0){
				dtvplayer_b_sub=true;
				mSubtitle = new TVProgram.Subtitle[mSubtitleCount];
				mSubtitleLang = new String[mSubtitleCount];
				for(int i=0;i<mSubtitleCount;i++){
					mSubtitle[i]=mTempTVProgram.getSubtitle(i);
					mSubtitleLang[i]= mSubtitle[i].getLang();
					Log.d(TAG,"sub Lang:"+mSubtitleLang[i]);
				}

				mSubtitleIndex=mTempTVProgram.getCurrentSubtitle(getStringConfig("tv:subtitle:language"));
			}	
			else
				dtvplayer_b_sub=false;

			int mTeletextCount = mTempTVProgram.getTeletextCount();
			if(mTeletextCount>0){
				dtvplyaer_b_txt=true;
				mTeletext = new TVProgram.Teletext[mTeletextCount];
			}	
			else
				dtvplyaer_b_txt=false;

			DTVPlayerInTeletextStatus=false;

			dtvplayer_cur_event=null;
			dtvplayer_event_des=null;
			dtvplayer_event_ext_des=null;
			dtvplayer_next_event=null;
			TVEvent mTVEventPresent=mTempTVProgram.getPresentEvent(this,getUTCTime());	
			if(mTVEventPresent!=null){
				Date dt_start =  new Date(mTVEventPresent.getStartTime());
			    	Date dt_end   =  new Date(mTVEventPresent.getEndTime());
			    		
		    		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
		    		String str_start = sdf.format(dt_start); 
		    		String str_end   = sdf.format(dt_end); 
				String time = ""+str_start + "--" + str_end;	
				
				dtvplayer_cur_event=time+"  "+mTVEventPresent.getName();
				dtvplayer_event_des=mTVEventPresent.getEventDescr();
				dtvplayer_event_ext_des=mTVEventPresent.getEventExtDescr();
				dtvplayer_b_epg = true;
			}

			TVEvent mTVEventFollow=mTempTVProgram.getFollowingEvent(this,getUTCTime());	
			if(mTVEventFollow!=null){
				Date dt_start =  new Date(mTVEventFollow.getStartTime());
			    	Date dt_end   =  new Date(mTVEventFollow.getEndTime());
			    		
		    		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
		    		String str_start = sdf.format(dt_start); 
		    		String str_end   = sdf.format(dt_end); 
				String time = ""+str_start + "--" + str_end;	
				
				dtvplayer_next_event=time+"  "+mTVEventFollow.getName();
				dtvplayer_b_epg = true;
			}

			Log.d(TAG,"dtvplayer_cur_event: = "+dtvplayer_cur_event);
			Log.d(TAG,"dtvplayer_event_des: = "+dtvplayer_event_des);
			Log.d(TAG,"dtvplayer_event_ext_des: = "+dtvplayer_event_ext_des);
			Log.d(TAG,"dtvplayer_next_event: = "+dtvplayer_next_event);
		}
		
	}

	private boolean DTVPlayerSetFav(){
		if(mTVProgram!=null){
			if(mTVProgram.getFavoriteFlag()){
				mTVProgram.setFavoriteFlag(false);
				dtvplayer_b_fav=false;
			}	
			else{
				mTVProgram.setFavoriteFlag(true);
				dtvplayer_b_fav=true;
			}
			
			if(dtvplayer_b_fav){
				ImageView_icon_fav.setVisibility(View.VISIBLE);
				return true;
			}
			else{
				ImageView_icon_fav.setVisibility(View.INVISIBLE);
				return false;
			}	
		}
		return false;
	}

	public static void showTeltext(Context c){
		Context mContext = c;
		if(dtvplyaer_b_txt){
			if(DTVPlayerInTeletextStatus==false){
				((DTVActivity)mContext).DTVTTShow();
				DTVPlayerInTeletextStatus=true;
			}	
			else{
				((DTVActivity)mContext).DTVTTHide();
				DTVPlayerInTeletextStatus=false;
			}	
		}	
	}

	public static void hideTeltext(Context c){
		Context mContext = c;
		((DTVActivity)mContext).DTVTTHide();
		DTVPlayerInTeletextStatus=false;
	
	}

	private class RecallListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context cont;
		private int selectItem;
		TVProgram[] mTVProgramList;
		class ViewHolder {
			TextView prono;
			TextView text;	
			ImageView icon_scrambled;
			ImageView icon_fav;
			ImageView icon;
		}
		
		public RecallListAdapter(Context context, TVProgram[] list) {
			super();
			cont = context;
			mTVProgramList = list;
			mInflater=LayoutInflater.from(context);			  
		}

		public int getCount() {
			if(mTVProgramList==null)
				return 0;
			else
				return mTVProgramList.length;
		}

		public Object getItem(int position) {
			return position;
		}
		
		public long getItemId(int position) {
			return position;
		}

		public void setSelectItem(int position){
			this.selectItem = position;
		}
        
        public int getSelectItem(){
			return this.selectItem;
        }
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;	
			if (convertView == null){    
				convertView = mInflater.inflate(R.layout.dtvchannellist_item, null);
				
				holder = new ViewHolder();
				holder.prono = (TextView)convertView.findViewById(R.id.prono);
				holder.text = (TextView) convertView.findViewById(R.id.ItemText);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.icon_scrambled = (ImageView)convertView.findViewById(R.id.icon_scrambled);
				holder.icon_fav = (ImageView)convertView.findViewById(R.id.icon_fav);
				convertView.setTag(holder);
			}
			else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
		
			// Bind the data efficiently with the holder.

			if(mDTVSettings.getScanRegion().contains("ATSC")==false){
				holder.prono.setText(Integer.toString(mTVProgramList[position].getNumber().getNumber()));
			}
			else{
				holder.prono.setText(Integer.toString(mTVProgramList[position].getNumber().getNumber())+"-"+Integer.toString(mTVProgramList[position].getNumber().getMinor()));
			}
			
			holder.text.setText(mTVProgramList[position].getName());
			//convertView.setBackgroundColor(Color.TRANSPARENT); 
			holder.text.setTextColor(Color.WHITE);
				
			if(mTVProgramList[position].getLockFlag()){
				holder.icon.setBackgroundResource(R.drawable.dtvplayer_icon_lock); 
			}	
			else{
				holder.icon.setBackgroundResource(Color.TRANSPARENT);
			}

			if(mTVProgramList[position].getFavoriteFlag()){
				holder.icon_fav.setBackgroundResource(R.drawable.dtvplayer_icon_fav); 
			}	
			else{
				holder.icon_fav.setBackgroundResource(Color.TRANSPARENT);
			}	

			if(mTVProgramList[position].getScrambledFlag()){
				holder.icon_scrambled.setBackgroundResource(R.drawable.dtvplayer_icon_scrambled); 
			}	
			else{
				holder.icon_scrambled.setBackgroundResource(Color.TRANSPARENT);
			}			  
			return convertView;
		}
	}

	private TVProgram[] recall_tvprogram =null;
	private void showRecallListDialog(Context context){
		recall_tvprogram = DTVPlayerGetRecallList();
		if(recall_tvprogram==null)
			return;
		
		final Context mContext = context;

		final CustomDialog mCustomDialog = new CustomDialog(mContext){
			public void onShowEvent(){
				if(mDialogManager!=null)
					mDialogManager.setActive(false);
			}

			public void onDismissEvent(){
				if(mDialogManager!=null)
					mDialogManager.setActive(true);
			}
		};
		if(recall_tvprogram.length>=1&&DTVPlayergetRecallNumber()>1){
			mCustomDialog.showDialog(R.layout.dtv_recall_list, new ICustomDialog(){
					public boolean onKeyDown(int keyCode, KeyEvent event){
						if(keyCode == KeyEvent.KEYCODE_BACK)
							mCustomDialog.dismissDialog();
						return false;
					}
					public void showWindowDetail(Window window){
						TextView Text_title = (TextView)window.findViewById(R.id.Text_title);
						Text_title.setTextColor(Color.YELLOW);
						Text_title.setText(getString(R.string.recall_list));
							
						ListView ListView_channel = (ListView) window.findViewById(R.id.ListView_channel);
						RecallListAdapter myAdapter = new RecallListAdapter(DTVPlayer.this,recall_tvprogram);

						ListView_channel.setOnItemClickListener(new AdapterView.OnItemClickListener(){

							public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
								// TODO Auto-generated method stub
								int db_id=recall_tvprogram[position].getID();	
								int serviceType = recall_tvprogram[position].getType();
								if(serviceType==TVProgram.TYPE_RADIO){
									setProgramType(TVProgram.TYPE_RADIO);
								}	
								else{
									setProgramType(TVProgram.TYPE_TV);
								}
								DTVPlayerPlayById(db_id);
								mCustomDialog.dismissDialog();
							}
				        	    
				        });
					
						ListView_channel.setAdapter(myAdapter);

					}
				}
			);
		}	
		else if(recall_tvprogram.length==1){
			int serviceType = recall_tvprogram[0].getType();
			if(serviceType==TVProgram.TYPE_RADIO){
				setProgramType(TVProgram.TYPE_RADIO);
			}	
			else{
				setProgramType(TVProgram.TYPE_TV);
			}
			DTVPlayerPlayById(recall_tvprogram[0].getID());
		}
	}

	private static Button BtnSubtitleLanguage=null;
	private static void showSubtitleLanguageDialog(Context context){
		final Context mContext = context;
		if(mSubtitleCount>0){
			new SingleChoiseDialog(mContext,mSubtitleLang,mSubtitleIndex){
				public void onSetMessage(View v){
					((TextView)v).setText(mContext.getString(R.string.dtvplayer_subtitle_language_set));
				}
				public void onSetNegativeButton(){
					
				}
				public void onSetPositiveButton(int which){
					mSubtitleIndex= which;
					Log.d(TAG,"mSubtitleIndex="+mSubtitleIndex);
					if(mSubtitleCount>0)
						BtnSubtitleLanguage.setText(mSubtitle[mSubtitleIndex].getLang());
				}

			};	
		}
	}

	public static void showSubtitleSettingMenu(Context context){	
		final Context mContext = context;

		final CustomDialog mCustomDialog = new CustomDialog(mContext){
			public void onShowEvent(){
					if(mDialogManager!=null)
						mDialogManager.setActive(false);
				}

				public void onDismissEvent(){
					if(mDialogManager!=null)
						mDialogManager.setActive(true);
				}
		};
		if(mSubtitleCount>0){
			mCustomDialog.showDialog(R.layout.dtv_subtitle_settings, new ICustomDialog(){
					public boolean onKeyDown(int keyCode, KeyEvent event){
						if(keyCode == KeyEvent.KEYCODE_BACK)
							mCustomDialog.dismissDialog();
						return false;
					}
					public void showWindowDetail(Window window){
						TextView title = (TextView)window.findViewById(R.id.title);
						title.setText(R.string.dtvplayer_subtitle_language_set);
						final CheckBox checkboxSubtitleSwitch = (CheckBox)window.findViewById(R.id.checkSubtitleSwitch);
						if(((DTVActivity)mContext).DTVGetSubtitleStatus()){
							checkboxSubtitleSwitch.setChecked(true);
						}
						else{
							checkboxSubtitleSwitch.setChecked(false);
						}

						if(mSubtitleCount>0){
							BtnSubtitleLanguage =(Button)window.findViewById(R.id.BtnSubtitleLanguage);
							Log.d(TAG,"index0="+mSubtitleIndex);
							String ss = mSubtitleLang[mSubtitleIndex];
							BtnSubtitleLanguage.setText(ss);

							BtnSubtitleLanguage.setOnClickListener(new OnClickListener(){
						          public void onClick(View v) {
						        	 showSubtitleLanguageDialog(mContext);
						          }});
						}	

						Button no = (Button)window.findViewById(R.id.no);
						no.setText(R.string.no);
						Button yes = (Button)window.findViewById(R.id.yes);
						yes.setText(R.string.yes);
						no.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								mCustomDialog.dismissDialog();
							}
						});	 
						yes.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {	
								if(checkboxSubtitleSwitch.isChecked()){
									if(((DTVActivity)mContext).DTVGetSubtitleStatus()){
										((TVActivity)mContext).switchSubtitle(mSubtitleIndex);
									}
									else{
										((TVActivity)mContext).switchSubtitle(mSubtitleIndex);
										((DTVActivity)mContext).DTVSetSubtitleStatus(true);
									}
								}
								else{
									((DTVActivity)mContext).DTVSetSubtitleStatus(false);	
								}
								mCustomDialog.dismissDialog();
							}
						});	    
					}
				}
			);
		}	
		else{
			mCustomDialog.showDialog(R.layout.dtv_subtitle_settings_no_lan, new ICustomDialog(){
					public boolean onKeyDown(int keyCode, KeyEvent event){
						if(keyCode == KeyEvent.KEYCODE_BACK)
							mCustomDialog.dismissDialog();
						return false;
					}
					public void showWindowDetail(Window window){
						TextView title = (TextView)window.findViewById(R.id.title);
						title.setText(R.string.dtvplayer_subtitle_language_set);
						final CheckBox checkboxSubtitleSwitch = (CheckBox)window.findViewById(R.id.checkSubtitleSwitch);
						if(((DTVActivity)mContext).DTVGetSubtitleStatus()){
							checkboxSubtitleSwitch.setChecked(true);
						}
						else{
							checkboxSubtitleSwitch.setChecked(false);
						}

						Button no = (Button)window.findViewById(R.id.no);
						no.setText(R.string.no);
						Button yes = (Button)window.findViewById(R.id.yes);
						yes.setText(R.string.yes);
						no.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								mCustomDialog.dismissDialog();
							}
						});	 
						yes.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {	
								if(checkboxSubtitleSwitch.isChecked()){
									if(((DTVActivity)mContext).DTVGetSubtitleStatus()){
										((TVActivity)mContext).switchSubtitle(mSubtitleIndex);
									}
									else{
										((TVActivity)mContext).switchSubtitle(mSubtitleIndex);
										((DTVActivity)mContext).DTVSetSubtitleStatus(true);
									}
								}
								else{
									((DTVActivity)mContext).DTVSetSubtitleStatus(false);	
								}
								mCustomDialog.dismissDialog();
							}
						});	  
					}
				}
			);
		}		
		
	}
	
	public static void showAudioLanguageDialog(Context context){	
		final Context mContext = context;
		if(mAudioCount>0){
			new SingleChoiseDialog(mContext,mAudioLang,mAudioIndex){
				public void onSetMessage(View v){
					((TextView)v).setText(mContext.getString(R.string.dtvplayer_audio_language_set));
				}
				public void onSetNegativeButton(){
					
				}
				public void onSetPositiveButton(int which){
					mAudioIndex=which; 
					((TVActivity)mContext).switchAudio(which);
				}
				
				public void onShowEvent(){
					if(mDialogManager!=null)
						mDialogManager.setActive(false);
				}

				public void onDismissEvent(){
					if(mDialogManager!=null)
						mDialogManager.setActive(true);
				}
				
			};	
		}			
	}

	PasswordDialog mPasswordDialog=null;
	private void showPasswordDialog(String t){
		mPasswordDialog = new PasswordDialog(DTVPlayer.this){
			public void onCheckPasswordIsRight(){
				unblock();				
			}

			public void onCheckPasswordIsFalse(){
				
			}

			public boolean onDealUpDownKey(){
				return true;
			}
		};
		mPasswordDialog.setDialogContent(t);
	}

	private void showPvrDurationTimeSetDialog(Context context){
		final Context mContext = context;
		final CustomDialog mCustomDialog = new CustomDialog(mContext,R.style.MyDialog){
			public void onShowEvent(){
				if(mDialogManager!=null)
					mDialogManager.setActive(false);
			}

			public void onDismissEvent(){
				if(mDialogManager!=null)
					mDialogManager.setActive(true);
			}
		};
		mCustomDialog.showDialog(R.layout.dtv_pvr_duration_time_dialog, new ICustomDialog(){
				public boolean onKeyDown(int keyCode, KeyEvent event){
					if(keyCode == KeyEvent.KEYCODE_BACK)
						mCustomDialog.dismissDialog();
					return false;
				}
				public void showWindowDetail(Window window){
					TextView title = (TextView)window.findViewById(R.id.title);
					title.setText(R.string.dtvplayer_pvr_duration_time);

					TextView head = (TextView)window.findViewById(R.id.head);
					head.setText(R.string.dtvplayer_pvr_duration_time_head);

					TextView tail = (TextView)window.findViewById(R.id.tail);
					tail.setText(R.string.dtvplayer_pvr_min);

					final EditText mEditText = (EditText)window.findViewById(R.id.edit);
					mEditText.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(5)});
					mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
					mEditText.setText(null);
					Button no = (Button)window.findViewById(R.id.no);
					no.setText(R.string.no);
					Button yes = (Button)window.findViewById(R.id.yes);
					yes.setText(R.string.yes);
					no.setOnClickListener(new OnClickListener(){
						public void onClick(View v) {
							mCustomDialog.dismissDialog();
						}
					});	 
					yes.setOnClickListener(new OnClickListener(){
						public void onClick(View v) {	
							//DTVPlayerStartRecording();
							if(mEditText.getText().toString().equals("")){
								toast = Toast.makeText(
								DTVPlayer.this, 
					    		R.string.invalid_input,
					    		Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
							}
							else{
								int dration=Integer.parseInt(mEditText.getText().toString());  
								if(dration==0){
									toast = Toast.makeText(
									DTVPlayer.this, 
						    		R.string.invalid_input,
						    		Toast.LENGTH_SHORT);
									toast.setGravity(Gravity.CENTER, 0, 0);
									toast.show();
								}
								else{
									if(isHaveExternalStorage()==false){
										CheckUsbdevice dev = new CheckUsbdevice(DTVPlayer.this);
										String path  = dev.getDevice();
										if(path!=null){
											if(mDTVSettings!=null)
												mDTVSettings.setRecordStoragePath(path);
										}	
									}
									recordDurationMin = dration;
									DTVPlayerStartRecording(dration*60*1000);
									showPvrIcon();
									mCustomDialog.dismissDialog();
								}
							}
						}
					});	    
				}
			}	
		);		
	}
	
	private void showToolsMenu(){
	}

	private void showStopPVRDialog(){
		if(!isFinishing()){
			showStopPVRDialog(-1, -1);
		}
	}
	
	private void showStopPVRDialog(final int conflict, final int programID){
		new SureDialog(DTVPlayer.this){
			public void onSetMessage(View v){
				String strMsg = "";
				
				if (conflict == TVMessage.REC_CFLT_SWITCH_PROGRAM){
					strMsg = getString(R.string.dtvplayer_change_channel);
				}else{
					strMsg = getString(R.string.dtvplayer_pvr_is_running);
				}
					
				((TextView)v).setText(strMsg);
			}
			public void onSetNegativeButton(){
			}
			public void onSetPositiveButton(){
				DTVPlayerStopRecording();
				hidePvrIcon();
				if (conflict == TVMessage.REC_CFLT_START_NEW){
					if (getCurrentProgramID() != programID){
						playProgram(programID);
					}
					DTVPlayerStartRecording(recordDurationMin*60*1000);
					showPvrIcon();
				}else if (conflict == TVMessage.REC_CFLT_SWITCH_PROGRAM){
					playProgram(programID);
				}
			}
			
			public void onShowEvent(){				
				if(mDialogManager!=null){
					mDialogManager.setActive(false);
					Log.d(TAG,"----setActive(false)-----");
				}	
			}

			public void onDismissEvent(){
				if(mDialogManager!=null)
					mDialogManager.setActive(true);
			}
		};
	}


	private boolean nit_dialog=false;
	private int nitTempVersion = -1;
  	protected void starNitScan()
	{
		if(mDTVSettings.getScanRegion().contains("DVB-T")){
			if(mTVProgram!=null){
				Intent intent_scan= new Intent();
				intent_scan.setClass(DTVPlayer.this,DvbtScanResult.class);
				Bundle bundle_scan_dvbt = new Bundle();	
				bundle_scan_dvbt.putString("scan-mode","dvbt-nit-scan");
				bundle_scan_dvbt.putInt("scan-fre",mTVProgram.getChannel().getParams().getFrequency()/1000);
				bundle_scan_dvbt.putInt("scan-band-width",mTVProgram.getChannel().getParams().getBandwidth());
				Log.d(TAG,"fre="+mTVProgram.getChannel().getParams().getFrequency()+"---bd="+mTVProgram.getChannel().getParams().getBandwidth());
					
				intent_scan.putExtras(bundle_scan_dvbt);
				startActivity(intent_scan);	
			}
		}
	}

	private void showNitVersionChangedDialog(){
		new SureDialog(DTVPlayer.this){
			public void onSetMessage(View v){
				String strMsg = "NIT version is changed,scan programs again?";	
				//strMsg = getString(R.string.dtvplayer_pvr_is_running);		
				((TextView)v).setText(strMsg);
			}
			public void onSetNegativeButton(){
				 nit_dialog = true;
			}
			public void onSetPositiveButton(){
				nit_dialog = true;
				starNitScan();
			}
				
			public void onShowEvent(){				
				if(mDialogManager!=null){
					mDialogManager.setActive(false);
					Log.d(TAG,"----setActive(false)-----");
				}	
			}

			public void onDismissEvent(){
				if(mDialogManager!=null)
					mDialogManager.setActive(true);
			}
		};
	}
	
	private void finishPlayer(){
		Log.d(TAG,"DTVPlayer finish player!");
		switchScreenType(0);
		setBlackoutPolicy(1);
		DTVTimeShiftingStop();
		DTVPlayerStopPlay();
	}

	private void writeSysFile(String path,String value){
		try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            try {
                writer.write(value);
                } finally {
                    writer.close();
                }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e) {
                Log.e(TAG,"set File ERROR!",e);
        } 
	}
	
	public class SubAsyncTask extends AsyncTask<Object, Integer, String>{ 
	        @Override
	        protected void onPreExecute() {  
	            super.onPreExecute();  
	        }  
	          
	        @Override
	        protected String doInBackground(Object... params) {  
			DTVPlayerGetCurrentProgramData();
			if(mTVProgram!=null)
				DTVPlayerSetRecallList(mTVProgram.getID());
			return null;  
	        }  
	  
	        @Override
	        protected void onProgressUpdate(Integer... progress) { 
			super.onProgressUpdate(progress);  
	        }  
	  
	        @Override
	        protected void onPostExecute(String result) {  
			RelativeLayout_loading_icon.setVisibility(View.INVISIBLE);
			ShowControlBar();
			updateInforbar();
			super.onPostExecute(result);  
	        }  
	  
	}  

	class HomeKeyEventBroadCastReceiver extends BroadcastReceiver { 
		static final String SYSTEM_REASON = "reason"; 
		static final String SYSTEM_HOME_KEY = "homekey";//home key 
		static final String SYSTEM_RECENT_APPS = "recentapps";//long home key 

		@Override
		public void onReceive(Context context, Intent intent) { 
			String action = intent.getAction(); 
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) { 
				String reason = intent.getStringExtra(SYSTEM_REASON); 
				if (reason != null) { 
					if (reason.equals(SYSTEM_HOME_KEY)) { 
						if(mDTVSettings.getIsLauncher()!=1){
							finishPlayer();
							getApplication().onTerminate();
						}
					} else if (reason.equals(SYSTEM_RECENT_APPS)) {

					}
				}
			}
		}
	}

	public static CheckUsbdevice Usbdevice = null;
	public static FileWriter dvbAVInfoWriter = null;
	private int debugMenuDialogShowFlag=0;

	private void showDebugMenuDialog(){
		AlertDialog.Builder debugMenuDialog = new AlertDialog.Builder(this)
			.setTitle("User Debug Menu")
			.setItems(new String[] {"AV Info","Tv Record","Tv Config"}, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//Log.d(TAG, "###Recive click"+which);
					switch (which) {
						case 0:
							Log.d(TAG, "###show av info");
							ShowDebugAVInfoDialog();
							break;
						case 1:
							Log.d(TAG, "###go to record");
							showDebugRecordDialog();
							break;
						case 2:
							Log.d(TAG, "### show config Menu");
							showDebugConfigDialog();
							break;
						default:
							break;
					}
				}
			});

			debugMenuDialog.create();
			debugMenuDialog.show();

	}

	private void showWarningDialog(){
		AlertDialog.Builder debugWarningBuilder = new AlertDialog.Builder(this)
		.setTitle("Record infomation")
		.setPositiveButton("sure", null);
		debugWarningBuilder.setMessage("WARNING! please check the USB devices !");
		debugWarningBuilder.show();
	}

	private void showDebugRecordDialog(){
		if (isHaveExternalStorage()){
			AlertDialog.Builder debugRecordBuilder = new AlertDialog.Builder(this)
			.setTitle("Record infomation")
			.setNegativeButton("yes", new DialogInterface.OnClickListener() {
		     @Override
		     public void onClick(DialogInterface dialog, int which) {
		      // TODO Auto-generated method stub
		     	Log.d(TAG, "@@@ready to record...");
		     	DTVPlayerStartRecording(60*60*1000);
		     	showDebugRecordProgressDialog();
		     }
		    })
		    .setPositiveButton("no", new DialogInterface.OnClickListener() {

		     @Override
		     public void onClick(DialogInterface dialog, int which) {
		      // TODO Auto-generated method stub
		     	Log.d(TAG, "@@@cancel to record...");
		     }
		    });

			TextView text = new TextView(this);
			text.setText("Are you sure to record?");
			text.setTextSize(27);
			text.setGravity(Gravity.CENTER);
			debugRecordBuilder.setView(text);
			debugRecordBuilder.create();
			debugRecordBuilder.show();
		}else{
			showWarningDialog();
		}
	}

	//static final int PROGRESS_DIALOG = 0;
	boolean isRecord = false;
    ProgressThread progressThread;
    ProgressDialog mDebugRecordProgressDialog;
    private void showDebugRecordProgressDialog(){
    	isRecord = true;
		mDebugRecordProgressDialog = new ProgressDialog(this);
        mDebugRecordProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDebugRecordProgressDialog.setTitle("Record infomation");
		mDebugRecordProgressDialog.setMessage("In recording, click stop-key to cancel...");
		mDebugRecordProgressDialog.setProgress(60);
		mDebugRecordProgressDialog.setMax(60);
        progressThread = new ProgressThread(handler);
        progressThread.start();
        mDebugRecordProgressDialog.setButton("stop", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int i)
	        {
	        	isRecord = false;
	        	DTVPlayerStopRecording();
	            dialog.cancel();
	        }
    	});
        mDebugRecordProgressDialog.show();
    	setDialogFontSize(mDebugRecordProgressDialog, 20);
    }
	final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int total = msg.getData().getInt("total");
            mDebugRecordProgressDialog.setProgress(total);
            if (total >= 60){
                isRecord = false;
                mDebugRecordProgressDialog.cancel();
				DTVPlayerStopRecording();
                progressThread.setState(ProgressThread.STATE_DONE);
            }
        }
    };
    private class ProgressThread extends Thread {
        Handler mHandler;
        final static int STATE_DONE = 0;
        final static int STATE_RUNNING = 1;
        int mState;
        int total;
        ProgressThread(Handler h) {
            mHandler = h;
        }
        public void run() {
            mState = STATE_RUNNING;
            total = 0;
            while (mState == STATE_RUNNING) {
                try {
                    Thread.sleep(60*1000);
                } catch (InterruptedException e) {
                    Log.e("ERROR", "Thread Interrupted");
                }
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putInt("total", total);
                msg.setData(b);
                mHandler.sendMessage(msg);
                total++;
            }
        }
        /* sets the current state for the thread,
         * used to stop the thread */
        public void setState(int state) {
            mState = state;
        }
    }

	private void setDialogFontSize(Dialog dialog,int size)
    {
        Window window = dialog.getWindow();
        View view = window.getDecorView();
        setViewFontSize(view,size);
    }
    private void setViewFontSize(View view,int size)
    {
        if(view instanceof ViewGroup)
        {
            ViewGroup parent = (ViewGroup)view;
            int count = parent.getChildCount();
            for (int i = 0; i < count; i++)
            {
                setViewFontSize(parent.getChildAt(i),size);
            }
        }
        else if(view instanceof TextView){
            TextView textview = (TextView)view;
            textview.setTextSize(size);
        }
    }

	private boolean debugAVInfoShowFlag=false;
	//private int debugMenuDialogShowFlag=0;
	private void ShowDebugAVInfoDialog(){
		Log.d(TAG, "********remove call back first!!!!");
		debug_info_timer_handler.removeCallbacks(debug_info_timer_runnable);
		if((debugAVInfoShowFlag==false)&&(RelativeLayout_debugInfoDialog!=null&&showAction!=null)){
			RelativeLayout_debugInfoDialog.startAnimation(showAction);
			RelativeLayout_debugInfoDialog.setVisibility(View.VISIBLE);
		}

		debugAVInfoShowFlag = true;
		debug_info_timer_handler.postDelayed(debug_info_timer_runnable, debug_inforbar_distime);
	}

	private void HideDebugAVInfoDialog(){
		//inforbarLayout.setVisibility(View.INVISIBLE);
		if(debugAVInfoShowFlag){
			if(RelativeLayout_debugInfoDialog!=null&&hideAction!=null){
				RelativeLayout_debugInfoDialog.startAnimation(hideAction);
    			RelativeLayout_debugInfoDialog.setVisibility(View.INVISIBLE);
			}
		}
		debugAVInfoShowFlag = false;
	}


	private boolean append_flag = false;
	private int debug_inforbar_distime=1000;
	private Handler	debug_info_timer_handler = new Handler();
	private Runnable debug_info_timer_runnable = new Runnable(){
		public void run() {
			if(debugAVInfoShowFlag==true){

				Log.d(TAG, "\t\t@=====================================append flag:"+ append_flag);
				String satellites_db = null;
				satellites_db = Usbdevice.getDevice();
				Log.d(TAG, "\t\t################ Usb Devices:"+satellites_db);
				try {

						Log.d(TAG, "\t\t################ new fileWriter");
						if(append_flag==true){
							dvbAVInfoWriter = new FileWriter(satellites_db+"/dvb_debug_avinfo.txt", true);
						}else{
							dvbAVInfoWriter = new FileWriter(satellites_db+"/dvb_debug_avinfo.txt");
							append_flag = true;
						}

		            try {
		                dvbAVInfoWriter.write("audio pts:"+getAudioPts()+"\n");
		                dvbAVInfoWriter.write("video pts:"+getVideoPts()+"\n");
		                dvbAVInfoWriter.write("demux audio pts:"+getDemuxAudioPts()+"\n");
		                dvbAVInfoWriter.write("demux video pts:"+getDemuxVideoPts()+"\n");
		                dvbAVInfoWriter.write("audio buffer level:"+getBufferLevel(2)+"\n");
		                dvbAVInfoWriter.write("video buffer level:"+getBufferLevel(1)+"\n");
		                dvbAVInfoWriter.write("\n\n\n");
		                } finally {
		                    dvbAVInfoWriter.close();
		                }
		        }catch (FileNotFoundException e) {
		            e.printStackTrace();
		        }catch (Exception e) {
		                Log.e(TAG,"set File ERROR!",e);
		        }
		        updteDebugInforbar();
				Log.d(TAG, "\t\t@=====================================@");
			}
			debug_info_timer_handler.postDelayed(this, debug_inforbar_distime);
		}
	};

	private String getAudioPts(){
		File file=null;
		BufferedReader br=null;
		String audio_pts=null;
		try{
			file=new File("/sys/class/tsync/pts_audio");
			br=new BufferedReader(new FileReader(file));

			audio_pts=br.readLine();
			if(br != null){
				br.close();
			}
			Log.d(TAG, "=====>>read audio pts: "+audio_pts);
		}catch(Exception e){
		}
		return audio_pts;
	}

	private String getVideoPts(){
		File file=null;
		BufferedReader br=null;
		String video_pts=null;
		try{
			file=new File("/sys/class/tsync/pts_video");
			br=new BufferedReader(new FileReader(file));

			video_pts=br.readLine();
			if(br != null){
				br.close();
			}
			Log.d(TAG, "=====>>read video pts: "+video_pts);
		}catch(Exception e){
		}
		return video_pts;
	}
	private String getDemuxAudioPts(){
		File file=null;
		BufferedReader br=null;
		String demux_audio_pts=null;
		try{
			file=new File("/sys/class/stb/audio_pts");
			br=new BufferedReader(new FileReader(file));

			demux_audio_pts=br.readLine();
			if(br != null){
				br.close();
			}
			Log.d(TAG, "=====>>read demux audio pts: "+demux_audio_pts);
		}catch(Exception e){
		}
		return demux_audio_pts;
	}
	private String getDemuxVideoPts(){
		File file=null;
		BufferedReader br=null;
		String demux_video_pts=null;
		try{
			file=new File("/sys/class/stb/video_pts");
			br=new BufferedReader(new FileReader(file));

			demux_video_pts=br.readLine();
			if(br != null){
				br.close();
			}
			Log.d(TAG, "=====>>read demux video pts: "+demux_video_pts);
		}catch(Exception e){
		}
		return demux_video_pts;
	}
	private String getBufferLevel(int index){
		String video_buffer_level=null;
		String audio_buffer_level=null;
		File file=null;
		BufferedReader br=null;
		String temp=null;
		String temp_sub=null;

		try{
			file=new File("sys/class/amstream/bufs");
			br=new BufferedReader(new FileReader(file));

			temp=br.readLine();

			while(temp!=null){
				temp=br.readLine();
				if(temp.length() >= 9){
					temp_sub=temp.substring(1, 10);
					if(temp_sub.equals("buf level")){
						if(video_buffer_level==null){
							video_buffer_level=temp.substring(11);
							Log.d(TAG, "@@@@@@@@@@@@@ read video_buffer_level:"+video_buffer_level);
						}
						else if(audio_buffer_level==null){
							audio_buffer_level=temp.substring(11);
							Log.d(TAG, "@@@@@@@@@@@@@ read audio_buffer_level:"+audio_buffer_level);
						}
					}
				}

			}
			if(br != null){
				br.close();
			}

		}catch(Exception e){
		}
		if(index == 1){
			return audio_buffer_level;
		}else if(index == 2){
			return video_buffer_level;
		}
		return null;
	}

	private void updteDebugInforbar(){
		if (mDTVSettings == null)
			return;

		TextView Text_v_buf_level= (TextView) findViewById(R.id.Text_v_buf_level);
		Text_v_buf_level.setText("video buffer level: "+getBufferLevel(2));
		TextView Text_a_buf_level= (TextView) findViewById(R.id.Text_a_buf_level);
		Text_a_buf_level.setText("audio buffer level: "+getBufferLevel(1));

		TextView Text_v_pts= (TextView) findViewById(R.id.Text_v_pts);
		Text_v_pts.setText("video pts: "+getVideoPts());
		TextView Text_a_pts= (TextView) findViewById(R.id.Text_a_pts);
		Text_a_pts.setText("audio pts: "+getAudioPts());

		TextView Text_demux_v_pts= (TextView) findViewById(R.id.Text_demux_v_pts);
		Text_demux_v_pts.setText("demux video pts: "+getDemuxVideoPts());
		TextView Text_demux_a_pts= (TextView) findViewById(R.id.Text_demux_a_pts);
		Text_demux_a_pts.setText("demux audio pts: "+getDemuxAudioPts());

	}

	private String[] m_Items;
	private int cfgCount = 0;
	private void showDebugConfigDialog(){

		AlertDialog.Builder tvConfigDialog = new AlertDialog.Builder(this)
			.setTitle("Config Menu");

		File file=null;
		BufferedReader br=null;
		String temp=null;
		String temp_sub=null;
		int count = 0;
		//final CharSequence[] items = null;// = {region, region};

		m_Items = new String[65];

		try{
			file=new File("data/data/com.amlogic.tvservice/files/tv.cfg");
			br=new BufferedReader(new FileReader(file));

			temp=br.readLine();

			while(temp!=null){
				m_Items[count] = temp;
				temp=br.readLine();
				Log.d(TAG, "read tv cfg:"+m_Items[count]+" count is "+count);
				count++;
			}
			cfgCount = count;
			if(br != null){
				br.close();
			}

		}catch(Exception e){
		}


		tvConfigDialog.setItems(m_Items,  new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				showDebugConfigInputDialog(m_Items[which], which);
			}
		});
		tvConfigDialog.create();
		tvConfigDialog.show();
	}


	private void showDebugConfigInputDialog(String text, final int index){
		final String title = text.substring(0, text.indexOf('='));
		String value = text.substring(text.indexOf('=')+1);

		final EditText editText = new EditText(this);
		//editText.setText(text);
		editText.setText(value);
		AlertDialog.Builder configInputDialog = new AlertDialog.Builder(this)

		//.setTitle("please input the value")
		.setTitle(title)
		.setView(editText)
		.setPositiveButton("yes", new DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "recive input string:"+editText.getText().toString());

				m_Items[index] = title + "=" + editText.getText().toString();

				String vstr  = editText.getText().toString();
				TVConfigValue value;

				if(vstr.matches("[ \\t\\n]*\".*\"[ \\t\\n]*")){
					String sv = vstr.replaceAll("[ \\t\\n]*\"(.*)\"[ \\t\\n]*", "$1");
					value = new TVConfigValue(sv);
					Log.d(TAG, title+"=\""+sv+"\"");

				}else if(vstr.matches("[ \\t\\n]*true[ \\t\\n]*")){
					value = new TVConfigValue(true);
					Log.d(TAG, title+"=true");

				}else if(vstr.matches("[ \\t\\n]*false[ \\t\\n]*")){
					value = new TVConfigValue(false);
					Log.d(TAG, title+"=false");

				}else{
					String istrs[] = vstr.split(",");
					if(istrs.length == 1){
						String istr = istrs[0].replaceAll("[ \\t\\n]*([+-]?\\d*)", "$1");
						value = new TVConfigValue(Integer.parseInt(istr));
						Log.d(TAG, title+"="+Integer.parseInt(istr));
					}else{
						int v[] = new int[istrs.length];
						int i;

						for(i = 0; i < istrs.length; i++){
							String istr = istrs[i].replaceAll("[ \\t\\n]*([+-]?\\d*)", "$1");
							v[i] = Integer.parseInt(istr);
						}

						value = new TVConfigValue(v);
						Log.d(TAG, title+"="+vstr);
					}
				}
				try{

					if(value.getType() == 1){
						mDTVSettings.setStringConfig(title, value.getString());
					}else if(value.getType() == 2){
						mDTVSettings.setIntConfig(title, value.getInt());
					}else if(value.getType() == 3){
						mDTVSettings.setBoolConfig(title, value.getBoolean());
					}

				}catch(Exception e){
				}

		        showDebugMenuDialog();
			}
		})
		.setNegativeButton("no", null);
		configInputDialog.show();
	}

}
