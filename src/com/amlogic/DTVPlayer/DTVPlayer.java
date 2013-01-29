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
import com.amlogic.tvutil.DTVPlaybackParams;
import com.amlogic.tvutil.DTVRecordParams;

import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.graphics.Color;

import com.amlogic.widget.PasswordDialog;
import com.amlogic.widget.SureDialog;
import com.amlogic.widget.SingleChoiseDialog;
import com.amlogic.widget.MutipleChoiseDialog;
import com.amlogic.widget.CustomDialog;
import com.amlogic.widget.CustomDialog.ICustomDialog;

public class DTVPlayer extends DTVActivity{
	private static final String TAG="DTVPlayer";
	private Toast toast=null;
	private Bundle bundle;	
	AlertDialog mAlertDialog=null;  //no signal or no data
	DTVSettings mDTVSettings=null;
	/**channel data**/
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvplayer);
		SystemProperties.set("vplayer.hideStatusBar.enable", "true");
		bundle = this.getIntent().getExtras();
		openVideo();
		
		DTVPlayerUIInit();
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		//set input source on DTV
		setInputSource(TVConst.SourceInput.SOURCE_DTV);
		if(isHavePragram()==false){
			showNoProgramDia();
		}
		
		mDTVSettings = new DTVSettings(this);
		mDTVSettings.setTeltextBound();
		
		if(bundle!=null){	
			//int db_id = DTVPlayerGetCurrentProgramID();
			//DTVPlayerPlayById(db_id);	
			playValid();
			ShowControlBar();
			updateInforbar();
			ShowProgramNo(pronumber);
		}
		
		if(DTVPlayerIsRecording()){
			showPvrIcon();
		}	
		else{
			hidePvrIcon();
		}	 

		int mode = DTVGetScreenMode();
		if(mode==0){
			DTVSetScreenMode(0);
		}
		else if(mode==2){
			DTVSetScreenMode(2);
		}
		else if(mode==3){
			DTVSetScreenMode(3);
		}
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
			case TVMessage.TYPE_PROGRAM_BLOCK:
				Log.d(TAG,"BLOCK");
				showPasswordDialog();
				break;
			case TVMessage.TYPE_PROGRAM_UNBLOCK:
				Log.d(TAG,"UNBLOCK");
				mDTVSettings.setCheckProgramLock(true);
				hidePasswordDialog();
				break;
			case TVMessage.TYPE_SIGNAL_LOST:
				showDia(1);
				break;
			case TVMessage.TYPE_SIGNAL_RESUME:
				if(mAlertDialog!=null)
					dismissDialog(1);
				break;	
			case TVMessage.TYPE_DATA_LOST:
				showDia(2);
				break;
			case TVMessage.TYPE_DATA_RESUME:
				if(mAlertDialog!=null)
					dismissDialog(2);
				break;
			case TVMessage.TYPE_PROGRAM_START:
				RelativeLayout_loading_icon.setVisibility(View.INVISIBLE);
				DTVPlayerGetCurrentProgramData();
				ShowControlBar();
				updateInforbar();
				break;
			case TVMessage.TYPE_STOP_RECORD_REQUEST:
				Log.d(TAG, "Request stop record, reason:");
				if (msg.getStopRecordRequestType() == TVMessage.REQ_TYPE_RECORD_CURRENT){
					Log.d(TAG, "Stop record for recording the current program");
					showStopPVRDialog();

				}else if (msg.getStopRecordRequestType() == TVMessage.REQ_TYPE_SWITCH_PROGRAM){
					Log.d(TAG, "Stop record for switching to new program");
					showStopPVRDialog();
					playProgram(msg.getStopRecordRequestProgramID());
				}
				break;
			case TVMessage.TYPE_RECORD_END:	
				switch(msg.getRecordErrorCode()){
					case  TVMessage.REC_ERR_OPEN_FILE:
						
						toast = Toast.makeText(
							DTVPlayer.this,
				    		R.string.check_usb_device,
				    		Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						
						break;
					case  TVMessage.REC_ERR_WRITE_FILE:	
						DTVTimeShiftingStop();
						toast = Toast.makeText(
							DTVPlayer.this,
				    		R.string.usbdisk_is_full,
				    		Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
										
					break;
					case  TVMessage.REC_ERR_ACCESS_FILE:
						break;
					case  TVMessage.REC_ERR_SYSTEM:
						
						break;							
				}

				hidePvrIcon();
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

	
	}

	@Override
	protected void onResume(){
		Log.d(TAG, ">>>>>>>>onResume<<<<<<<<");
		super.onResume();


	}
	
	
	@Override
	protected void onStart(){
		Log.d(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onStop(){
		Log.d(TAG, "onStop");
		super.onStop();
	}

	public void onDestroy() {
        Log.d(TAG, "onDestroy");
		SystemProperties.set("vplayer.hideStatusBar.enable", "false");
        super.onDestroy();
    }

	public void onNewIntent(Intent intent){
		Log.d(TAG, ">>>>>onNewIntent<<<<<");
		super.onNewIntent(intent);
	    setIntent(intent);

		if(isHavePragram()==false)
			showNoProgramDia();
		
		if(intent!=null){
			bundle = intent.getExtras();
		}
	
		if(bundle!=null){	
			if(bundle.getString("activity_tag").equals("DTVChannelList")){			
				int db_id = bundle.getInt("db_id");
				int serviceType = bundle.getInt("service_type");
				Log.d(TAG,"channel list db_id="+db_id +"---type="+serviceType );
				if(serviceType==TVProgram.TYPE_RADIO){
					setProgramType(TVProgram.TYPE_RADIO);
					Log.d(TAG,"setProgramType(TVProgram.TYPE_RADIO)");
				}	
				else{
					setProgramType(TVProgram.TYPE_TV);
					Log.d(TAG,"setProgramType(TVProgram.TYPE_TV)");
				}
				DTVPlayerPlayById(db_id);
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

		ShowControlBar();
		updateInforbar();
		ShowProgramNo(pronumber);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		// TODO Auto-generated method stub
		switch (keyCode) {
			case KeyEvent.KEYCODE_MUTE:
				Log.d(TAG,"KEYCODE_MUTE");
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				Log.d(TAG,"KEYCODE_DPAD_LEFT");
				ShowControlBar();
				updateInforbar();
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				Log.d(TAG,"KEYCODE_DPAD_RIGHT");
				ShowControlBar();
				updateInforbar();
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if(mainmenu_show_flag){
					break;
				}
				else{
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
				if(mainmenu_show_flag){
					break;
				}
				else{
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
				if(mainmenu_show_flag){
					HideMainMenu();
				}
				else if(inforbar_show_flag){
					HideControlBar();
				}
				else if(dtvplyaer_b_txt&&DTVPlayerInTeletextStatus){	
					DTVTTHide();
					DTVPlayerInTeletextStatus=false;
				}	
				else if(DTVPlayerIsRecording()){
					showStopPVRDialog();
				}	
				else{
					finishPlayer();
				}	
				Log.d(TAG,"KEYCODE_BACK");
				return true;
			case KeyEvent.KEYCODE_DPAD_CENTER:
				Log.d(TAG,"KEYCODE_DPAD_CENTER");
				if(inforbar_show_flag==false){
					ShowControlBar();
				}
				else if(mainmenu_show_flag==false)
					ShowMainMenu();
				return true;
			case KeyEvent.KEYCODE_ENTER:
				Log.d(TAG,"KEYCODE_ENTER");
				break;		 
			case KeyEvent.KEYCODE_ZOOM_IN:	
				Log.d(TAG,"KEYCODE_ZOOM_IN");
				showTeltext(DTVPlayer.this);
				return true;
			case KeyEvent.KEYCODE_ZOOM_OUT:
				Log.d(TAG,"KEYCODE_ZOOM_OUT");
				showPvrDurationTimeSetDialog(DTVPlayer.this);
				return true;
			case KeyEvent.KEYCODE_TV_REPEAT:
				Log.d(TAG,"KEYCODE_TV_REPEAT");
				showSubtitleSettingMenu(DTVPlayer.this);
				return true;
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				Log.d(TAG,"KEYCODE_MEDIA_REWIND");
				shortcut_key_deal("FAV");
				return true;	
			case KeyEvent.KEYCODE_TAB: //info
				Log.d(TAG,"KEYCODE_TAB");	
				return true;
			case KeyEvent.KEYCODE_TV_SWITCH: //tv/radio
				Log.d(TAG,"KEYCODE_TV_SWITCH");	
				return true;	
			case KeyEvent.KEYCODE_TV_SHORTCUTKEY_DISPAYMODE: //16:9/4:3
				Log.d(TAG,"KEYCODE_TV_SHORTCUTKEY_DISPAYMODE");
				shortcut_key_deal("pictrue_mode");
				return true;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS: //pre/next
				Log.d(TAG,"KEYCODE_MEDIA_PREVIOUS");	
				return true;
			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD: //epg
				Log.d(TAG,"KEYCODE_MEDIA_FAST_FORWARD");
				return true;
			case KeyEvent.KEYCODE_MEDIA_NEXT: //pvr manager
				Log.d(TAG,"KEYCODE_MEDIA_NEXT");
				DTVDealDigtalKey(keyCode);
				return true;
			case KeyEvent.KEYCODE_TV_SUBTITLE:
				Log.d(TAG,"KEYCODE_TV_SUBTITLE");
				shortcut_key_deal("SUBTITLE");
				return true;	
			case KeyEvent.KEYCODE_TV_SHORTCUTKEY_VOICEMODE:
				Log.d(TAG,"KEYCODE_TV_SHORTCUTKEY_VOICEMODE");
				showAudioLanguageDialog(DTVPlayer.this);
				break;	
			case KeyEvent.KEYCODE_MENU:
				if(mainmenu_show_flag)
					HideMainMenu();
				else
					ShowMainMenu();
				return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}


	private RelativeLayout RelativeLayout_inforbar=null;
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
		RelativeLayout_inforbar.setVisibility(View.INVISIBLE);
		RelativeLayout_radio_bg.setVisibility(View.INVISIBLE);
		RelativeLayout_loading_icon.setVisibility(View.VISIBLE);
		
		init_Animation();
		
	}

	private void showNoProgramDia(){
		new SureDialog(DTVPlayer.this){
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
				Intent_scan.setClass(DTVPlayer.this, DTVScanDVBT.class);
				startActivity(Intent_scan);
			}
		};
		return;
	}

	class MouseClick implements OnClickListener{
	    public void onClick(View v) {
			// TODO Auto-generated method stub	
			switch (v.getId()) {
				case R.id.RelativeLayout_video:
					if(inforbar_show_flag==false){
						ShowControlBar();
					}
					else if(mainmenu_show_flag==false){
						ShowMainMenu();
					}
					break;
				case R.id.Button_mainmenu_list:
					HideMainMenu();
					Intent pickerIntent = new Intent();
					Bundle bundle_list = new Bundle();
					bundle_list.putInt("db_id", DTVPlayerGetCurrentProgramID());
					pickerIntent.putExtras(bundle_list);
					pickerIntent.setClass(DTVPlayer.this, DTVChannelList.class);
 		           	startActivity(pickerIntent);
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
					Intent pickerIntent_pro = new Intent();
					Bundle bundle_promanage = new Bundle();
					bundle_promanage.putInt("db_id", DTVPlayerGetCurrentProgramID());
					pickerIntent_pro.putExtras(bundle_promanage);
					pickerIntent_pro.setClass(DTVPlayer.this, DTVProgramManager.class);
 		            startActivity(pickerIntent_pro);					
					break;
				case R.id.Button_mainmenu_timeshift:
					Intent Intent_timeshift = new Intent();
					Intent_timeshift.setClass(DTVPlayer.this, DTVTimeshifting.class);
					startActivity(Intent_timeshift);
					HideMainMenu();
					HideControlBar();
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

	private int bar_hide_count =0;
	private int inforbar_distime=1000;
	private int bar_auto_hide_duration=5;
	private Handler	timer_handler = new Handler();   
	private Runnable timer_runnable = new Runnable(){
		public void run() {
			if(inforbar_show_flag==true){	
				if(bar_hide_count >= bar_auto_hide_duration){
					HideControlBar();
					HideProgramNo();
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
		if((mainmenu_show_flag==false)&&(RelativeLayout_mainmenu!=null&&mainMenuShowAction!=null)){	
			RelativeLayout_mainmenu.startAnimation(mainMenuShowAction);   
			RelativeLayout_mainmenu.setVisibility(View.VISIBLE);
			RelativeLayout_mainmenu.requestFocus();
		}
		mainmenu_show_flag = true;	
	}

	private void HideMainMenu(){
		if(mainmenu_show_flag){	
			if(RelativeLayout_mainmenu!=null&&mainMenuHideAction!=null){
				RelativeLayout_mainmenu.startAnimation(mainMenuHideAction);   
				RelativeLayout_mainmenu.setVisibility(View.INVISIBLE);
			}
		}
		mainmenu_show_flag = false;
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

		if(mDTVSettings.getScanRegion().equals("ATSC"))
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

		if(mDTVSettings.getScanRegion().equals("ATSC")==false){
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
		if(mode==0){
			Text_screentype_info.setText(getString(R.string.auto));
		}
		else  if(mode==2){
			Text_screentype_info.setText(getString(R.string.type_4_3));
		}
		else  if(mode==3){
			Text_screentype_info.setText(getString(R.string.type_16_9));
		}
		
		
		mode = DTVGetAudioTrack();
		if(mode==0){ 						
			Text_MTS_info.setText(getString(R.string.type_16_9));			
		}
		else  if(mode==1){
			Text_MTS_info.setText(getString(R.string.left));			
		}
		else  if(mode==2){
			Text_MTS_info.setText(getString(R.string.right));				
		}	
		
		if(DTVGetSubtitleStatus()){
			Text_parent_control_info_icon.setText("SUB:"+getString(R.string.on));	
		}
		else{
			Text_parent_control_info_icon.setText("SUB:"+getString(R.string.off));
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
				if((DTVPlayerCheckNumerInputIsValid(pronumber)==false)||(pronumber<=0)){
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
								
							}
							public void onSetPositiveButton(){
								DTVPlayerStopRecording();
								DTVPlayerPlayByProNo(pronumber);
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
				number_key_down = false;
			}	
			prono_timer_handler.removeCallbacks(prono_timer_runnable);		
		}   
	};

	private static boolean DTVPlayerInTeletextStatus=false;
	private void DTVDealDigtalKey(int value){
		int number_key_value=0;

		if(mDTVSettings.getScanRegion().equals("ATSC")){
			if(DTVPlayerInTeletextStatus==false){
				prono_timer_handler.removeCallbacks(prono_timer_runnable);
				
				if(value==KeyEvent.KEYCODE_MEDIA_NEXT){
					boolean hasflag=false;
					if(pronumber_string!=null){
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
							}
						}	
					}
					else
						pronumber_string=Integer.toString(dtvplayer_pronumber_major)+"-";
				}
				else{
					number_key_value = value - KeyEvent.KEYCODE_0;
					pronumber = pronumber*10+number_key_value;
					Log.d(TAG,"pronumber_string1="+pronumber_string);
					pronumber_string =pronumber_string + Integer.toString(number_key_value);
					Log.d(TAG,"pronumber_string2="+pronumber_string);
				}
				if(pronumber>9999){
					pronumber = number_key_value;
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
			if(mode==0){
				ShowInformation(getString(R.string.type_4_3));
				DTVSetScreenMode(2);
				Text_screentype_info.setText(getString(R.string.type_4_3));
			}
			else  if(mode==2){
				ShowInformation(getString(R.string.type_16_9));		
				DTVSetScreenMode(3);
				Text_screentype_info.setText(getString(R.string.type_16_9));
			}
			else  if(mode==3){
				ShowInformation(getString(R.string.auto));		
				DTVSetScreenMode(0);
				Text_screentype_info.setText(getString(R.string.auto));
			}
		}
		else if(key.equals("AUDIOTRACK")){
			int mode = DTVGetAudioTrack();
			if(mode==0){ 						
				ShowInformation(getString(R.string.right));			
				DTVSetAudioTrack(0);
				Text_MTS_info.setText(getString(R.string.right));			
			}
			else  if(mode==1){
				ShowInformation(getString(R.string.stereo));			
				DTVSetAudioTrack(1);
				Text_MTS_info.setText(getString(R.string.stereo));			
			}
			else  if(mode==2){
				ShowInformation(getString(R.string.left));				
				DTVSetAudioTrack(2);
				Text_MTS_info.setText(getString(R.string.left));				
			}	
		}
		else if(key.equals("SUBTITLE")){
			if(DTVGetSubtitleStatus()){
				DTVSetSubtitleStatus(false);
				ShowInformation(getString(R.string.off));	
				Text_parent_control_info_icon.setText("SUB:"+getString(R.string.off));	
			}
			else{
				DTVSetSubtitleStatus(true);
				ShowInformation(getString(R.string.on));
				Text_parent_control_info_icon.setText("SUB:"+getString(R.string.on));	
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

	private void showDia(int id){
		mAlertDialog = (AlertDialog)onCreateDialog(id);
		if(mAlertDialog!=null){
			showDialog(id);
			mAlertDialog.getWindow().setLayout(500,100);
			WindowManager.LayoutParams lp=mAlertDialog.getWindow().getAttributes();
			Log.d(TAG,"x="+lp.x+"y="+lp.y);
			lp.x=0;
			lp.y=0;
			lp.dimAmount=0.0f;
			mAlertDialog.getWindow().setAttributes(lp);
			mAlertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		}
	}

	private boolean isHavePragram(){
		TVProgram[]  mTVProgramList=null;
		mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_TV,false);
		if(mTVProgramList!=null){
			if(mTVProgramList.length!=0)
				return true;
			else{
				mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_RADIO,false);
				if(mTVProgramList==null)
					return false;
				else if(mTVProgramList.length!=0)
					return true;
			}
		}
		else{
			mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_RADIO,false);
			if(mTVProgramList==null)
				return false;
		}
		return false;
	}

	private String pronumber_string="";
	private int pronumber=0;
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
	private void DTVPlayerGetCurrentProgramData(){
		mTVProgram=DTVPlayerGetDataByCurrentID();

		dtvplayer_program_number= mTVProgram.getNumber();
		if(mDTVSettings.getScanRegion().equals("ATSC")==false){
			
			dtvplayer_pronumber= mTVProgram.getNumber().getNumber();
			dtvplayer_pronumber_major = dtvplayer_program_number.getMajor();
			dtvplayer_pronumber_minor = dtvplayer_program_number.getMinor(); 
			Log.d(TAG,"pronumber="+dtvplayer_pronumber);
		}	
		else {
			dtvplayer_pronumber_major = dtvplayer_program_number.getMajor();
			dtvplayer_pronumber_minor = dtvplayer_program_number.getMinor();
			Log.d(TAG,"ATSC pronumber="+dtvplayer_pronumber_major+"-"+dtvplayer_pronumber_minor);
		}

		dtvplayer_service_type = mTVProgram.getType();
		dtvplayer_name = mTVProgram.getName();
		dtvplayer_b_lock = mTVProgram.getLockFlag();
		dtvplayer_b_fav = mTVProgram.getFavoriteFlag();
		dtvplayer_b_scrambled = mTVProgram.getScrambledFlag();
		dtvplayer_atsc_antenna_source = mDTVSettings.getAtscAntennaSource();

		mAudioCount=mTVProgram.getAudioCount();
		if(mAudioCount>0){
			mAudio= new TVProgram.Audio[mAudioCount];
			mAudioLang = new String[mAudioCount];
			for(int i=0;i<mAudioCount;i++){
				mAudio[i]=mTVProgram.getAudio(i);
				mAudioLang[i]= mAudio[i].getLang();
				Log.d(TAG,"Audio Lang:"+mAudioLang[i]);
			}
		}	

		mSubtitleCount=mTVProgram.getSubtitleCount();
		if(mSubtitleCount>0){
			dtvplayer_b_sub=true;
			mSubtitle = new TVProgram.Subtitle[mSubtitleCount];
			mSubtitleLang = new String[mSubtitleCount];
			for(int i=0;i<mSubtitleCount;i++){
				mSubtitle[i]=mTVProgram.getSubtitle(i);
				mSubtitleLang[i]= mSubtitle[i].getLang();
				Log.d(TAG,"sub Lang:"+mSubtitleLang[i]);
			}
		}	
		else
			dtvplayer_b_sub=false;

		int mTeletextCount = mTVProgram.getTeletextCount();
		if(mTeletextCount>0){
			dtvplyaer_b_txt=true;
			mTeletext = new TVProgram.Teletext[mTeletextCount];
		}	
		else
			dtvplyaer_b_txt=false;

		dtvplayer_cur_event=null;
		dtvplayer_event_des=null;
		dtvplayer_event_ext_des=null;
		dtvplayer_next_event=null;
		TVEvent mTVEventPresent=mTVProgram.getPresentEvent(this,getUTCTime());	
		if(mTVEventPresent!=null){
			dtvplayer_cur_event=mTVEventPresent.getName();
			dtvplayer_event_des=mTVEventPresent.getEventDescr();
			dtvplayer_event_ext_des=mTVEventPresent.getEventExtDescr();
		}

		TVEvent mTVEventFollow=mTVProgram.getFollowingEvent(this,getUTCTime());	
		if(mTVEventFollow!=null){
			dtvplayer_next_event=mTVEventFollow.getName();
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

		final CustomDialog mCustomDialog = new CustomDialog(mContext);
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
					((TVActivity)mContext).switchAudio(which);
				}
			};	
		}			
	}

	PasswordDialog mPasswordDialog=null;
	private void showPasswordDialog(){
		mPasswordDialog = new PasswordDialog(DTVPlayer.this){
			public void onCheckPasswordIsRight(){
				mDTVSettings.setCheckProgramLock(false);
			}

			public void onCheckPasswordIsFalse(){
			}

			public boolean onDealUpDownKey(){
				return true;
			}
		};
	}

	private void showPvrDurationTimeSetDialog(Context context){
		final Context mContext = context;
		final CustomDialog mCustomDialog = new CustomDialog(mContext);
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
					mEditText.setFilters(new  InputFilter[]{ new  InputFilter.LengthFilter(5)});
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
							int dration=Integer.parseInt(mEditText.getText().toString());  
							DTVPlayerStartRecordingWithTime(getUTCTime(),dration*60*1000);
							showPvrIcon();
							mCustomDialog.dismissDialog();
						}
					});	    
				}
			}	
		);		
	}
	
	private void showToolsMenu(){
	}
	
	private void showStopPVRDialog(){
		new SureDialog(DTVPlayer.this){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.dtvplayer_change_channel));
			}
			public void onSetNegativeButton(){
				 
			}
			public void onSetPositiveButton(){
				DTVPlayerStopRecording();
				hidePvrIcon();
			}
		};
	}
	
	private void finishPlayer(){
		DTVPlayerStopPlay();
		finish();
	}

}

