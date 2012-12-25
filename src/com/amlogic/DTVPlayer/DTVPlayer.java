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

public class DTVPlayer extends DTVActivity{
	private static final String TAG="DTVPlayer";
	private Toast toast=null;
	private Bundle bundle;	
	AlertDialog mAlertDialog=null;  //no signal or no data

	/**channel data**/

	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvplayer);
		SystemProperties.set("vplayer.hideStatusBar.enable", "true");
		bundle = this.getIntent().getExtras();
		openVideo();
		DTVPlayerUIInit();
		if(isHavePragram()==false)
			showNoProgramDia();
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		//set input source on DTV
		setInputSource(TVConst.SourceInput.SOURCE_DTV);
		
		if(bundle!=null){	
			int db_id = DTVPlayerGetCurrentProgramID();
			DTVPlayerPlayById(db_id);		
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
				break;
			case TVMessage.TYPE_PROGRAM_UNBLOCK:
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
				DTVPlayerGetCurrentProgramData();
				ShowControlBar();
				updateInforbar();
				break;
			default:
				break;
		}
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
			bundle = this.getIntent().getExtras();
			int db_id = DTVPlayerGetCurrentProgramID();
			DTVPlayerPlayById(db_id);		
			ShowControlBar();
			updateInforbar();
			ShowProgramNo(pronumber);
		}
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
					
					else if(DTVPlayerIsRecording()){
							AlertDialog.Builder builder = new AlertDialog.Builder(DTVPlayer.this); 
								builder.setMessage(R.string.dtvplayer_change_channel)
								.setCancelable(false)
								.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
									public void onClick(DialogInterface dialog, int id) {						
										DTVPlayerStopRecording();
										DTVPlayerPlayDown();
										dialog.cancel();
									}        
								 })        
								.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();            
										}        
								 }); 
							AlertDialog alert = builder.create();
				            alert.show();

							WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
							lp.dimAmount=0.0f;
							alert.getWindow().setAttributes(lp);
							alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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
					else if(DTVPlayerIsRecording()){
						AlertDialog.Builder builder = new AlertDialog.Builder(DTVPlayer.this); 
							builder.setMessage(R.string.dtvplayer_change_channel)
							.setCancelable(false)
							.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog, int id) {						
									DTVPlayerStopRecording();
									DTVPlayerPlayUp();
									dialog.cancel();
								}        
							 })        
							.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();            
									}        
							 }); 
							AlertDialog alert = builder.create();
							alert.show();

							WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
							lp.dimAmount=0.0f;
							alert.getWindow().setAttributes(lp);
							alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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
				else{
					Intent pickerIntent = new Intent();
					Bundle bundle_list = new Bundle();
					bundle_list.putInt("db_id", DTVPlayerGetCurrentProgramID());
					pickerIntent.putExtras(bundle_list);
					pickerIntent.setClass(DTVPlayer.this, DTVChannelList.class);
 		            startActivity(pickerIntent);	
				}
				return true;
			case KeyEvent.KEYCODE_ENTER:
				Log.d(TAG,"KEYCODE_ENTER");
				break;		 
			case KeyEvent.KEYCODE_ZOOM_IN:	
				Log.d(TAG,"KEYCODE_ZOOM_IN");
				if(dtvplyaer_b_txt){
					if(DTVPlayerInTeletextStatus==false){
						DTVTTShow();
						DTVPlayerInTeletextStatus=true;
					}	
					else{
						DTVTTHide();
						DTVPlayerInTeletextStatus=false;
					}	
				}	
				return true;
			case KeyEvent.KEYCODE_ZOOM_OUT:
				Log.d(TAG,"KEYCODE_ZOOM_OUT");
				return true;
			case KeyEvent.KEYCODE_TV_REPEAT:
				Log.d(TAG,"KEYCODE_TV_REPEAT");
				showSubtitleSettingMenu();
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
				return true;
			case KeyEvent.KEYCODE_TV_SUBTITLE:
				Log.d(TAG,"KEYCODE_TV_SUBTITLE");
				shortcut_key_deal("SUBTITLE");
				return true;	
			case KeyEvent.KEYCODE_TV_SHORTCUTKEY_VOICEMODE:
				Log.d(TAG,"KEYCODE_TV_SHORTCUTKEY_VOICEMODE");
				showAudioLanguageDialog();
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
	private RelativeLayout RelativeLayout_loading_icon=null;
	private RelativeLayout RelativeLayout_recording_icon=null;

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
		RelativeLayout_loading_icon = (RelativeLayout)findViewById(R.id.RelativeLayoutLoadingIcon);
		RelativeLayout_videobcak= (RelativeLayout)findViewById(R.id.RelativeLayout_video);

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
               }else{
				   Text_button_info.setText(null); 
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
               }else{
				   Text_button_info.setText(null); 
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
               }else{
				   Text_button_info.setText(null); 
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
               }else{
				   Text_button_info.setText(null); 
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
               }else{
				   Text_button_info.setText(null); 
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
               }else{
				   Text_button_info.setText(null); 
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
               }else{
				   Text_button_info.setText(null); 
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
               }else{
				   Text_button_info.setText(null);
               }
            }
        });
		
		findViewById(R.id.RelativeLayout_video).setOnClickListener(new MouseClick());
		RelativeLayout_inforbar.setVisibility(View.INVISIBLE);
		RelativeLayout_radio_bg.setVisibility(View.INVISIBLE);
		
		init_Animation();
	}

	private void showNoProgramDia(){
		AlertDialog.Builder builder = new AlertDialog.Builder(DTVPlayer.this);
		builder.setMessage(R.string.dtvplayer_no_channel_stored)
		.setCancelable(false)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent Intent_scan = new Intent();
				Bundle bundle = new Bundle();
				Intent_scan.putExtras(bundle); 
				Intent_scan.setClass(DTVPlayer.this, DTVScanDVBT.class);
				startActivity(Intent_scan);
				//DTVPlayer.this.finish(); 
				dialog.dismiss();
	        }})
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			DTVPlayer.this.finish();  
			dialog.dismiss();
			}
        });
		AlertDialog alert = builder.create();
		alert.show();	
		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=1.00f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

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
					break;
				case R.id.Button_mainmenu_timeshift:
					Intent Intent_timeshift = new Intent();
					Intent_timeshift.setClass(DTVPlayer.this, DTVTimeshifting.class);
					startActivity(Intent_timeshift);
					break;
				case R.id.Button_mainmenu_prv:	
				case R.id.Button_mainmenu_manage:
				case R.id.Button_mainmenu_skip:				
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
	private void ShowControlBar(){
		timer_handler.removeCallbacks(timer_runnable);
		if((inforbar_show_flag==false)&&(RelativeLayout_inforbar!=null&&showAction!=null&&RelativeLayout_recording_icon!=null&&showPvrAction!=null)){	
			RelativeLayout_inforbar.startAnimation(showAction);   
			RelativeLayout_inforbar.setVisibility(View.VISIBLE);
			RelativeLayout_recording_icon.startAnimation(showPvrAction);
			RelativeLayout_recording_icon.setVisibility(View.VISIBLE);
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
				RelativeLayout_recording_icon.startAnimation(hidePvrAction);   
    			RelativeLayout_recording_icon.setVisibility(View.INVISIBLE);
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

		if(dtvplayer_pronumber>=0)	
		Text_proname.setText(Integer.toString(dtvplayer_pronumber)+"  "+dtvplayer_name);

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


		
	}

	void ShowProgramNo(int value){
		if(value!=0){
			RelativeLayout RelativeLayoutProNumer = (RelativeLayout)findViewById(R.id.RelativeLayoutProNumer);
			TextView Text_program_no = (TextView)findViewById(R.id.Text_program_no);
			//if(dtv_standard_atsc)
				//program_no.setText(getMajorAndMinorNumber(channel_number));	
			//else
				Text_program_no.setText(String.valueOf(value));
			RelativeLayoutProNumer.setVisibility(View.VISIBLE);
		}	
	}

	void HideProgramNo(){
		RelativeLayout RelativeLayoutProNumer = (RelativeLayout)findViewById(R.id.RelativeLayoutProNumer);
		RelativeLayoutProNumer.setVisibility(View.INVISIBLE);
	}

	void showPvrIcon(){
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
		timer_handler.postDelayed(timer_runnable, inforbar_distime);
	}

	void hidePvrIcon(){
		ImageView ImageView_recordingIcon = (ImageView) findViewById(R.id.ImageView_recordingIcon);
		Text_recording_time = (TextView) findViewById(R.id.Text_recording_time);
		RelativeLayout RelativeLayoutPvrIcon = (RelativeLayout)findViewById(R.id.RelativeLayoutPvrIcon);
	
		ImageView_recordingIcon.setVisibility(View.INVISIBLE);
		Text_recording_time.setVisibility(View.INVISIBLE);
		RelativeLayoutPvrIcon.setVisibility(View.INVISIBLE);
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
		int cur_recording_time=0;
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
				//hidePasswordDialog();
				if((DTVPlayerCheckNumerInputIsValid(pronumber)==false)||(pronumber<=0)){
					toast = Toast.makeText(
						DTVPlayer.this, 
			    		R.string.invalid_input,
			    		Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
						pronumber = 0;				
						HideProgramNo();
				}
				else{	
					if(DTVPlayerIsRecording()){
						AlertDialog.Builder builder = new AlertDialog.Builder(DTVPlayer.this); 
							builder.setMessage(R.string.dtvplayer_change_channel)
							.setCancelable(false)
							.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog, int id) {						
									DTVPlayerStopRecording();
									DTVPlayerPlayByProNo(pronumber);
									dialog.cancel();
								}        
							 })        
							.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();            
									}        
							 }); 
						AlertDialog alert = builder.create();
			            alert.show();
						WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
						lp.dimAmount=0.0f;
						alert.getWindow().setAttributes(lp);
						alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
					}
					else{
						DTVPlayerPlayByProNo(pronumber);
						pronumber = 0;
					}
				}	
				number_key_down = false;
			}	
			prono_timer_handler.removeCallbacks(prono_timer_runnable);		
		}   
	};
    
	private boolean DTVPlayerInTeletextStatus=false;

	private void DTVDealDigtalKey(int value){
		int number_key_value=0;
		
		if(DTVPlayerInTeletextStatus==false){
			prono_timer_handler.removeCallbacks(prono_timer_runnable);
			number_key_value = value - KeyEvent.KEYCODE_0;
			pronumber = pronumber*10+number_key_value;
			if(pronumber>9999){
				pronumber = number_key_value;
			}	
			ShowProgramNo(pronumber);
			prono_timer_handler.postDelayed(prono_timer_runnable, 1000);
			number_key_down = true;
		}
		else{
			prono_timer_handler.removeCallbacks(prono_timer_runnable);
			number_key_value = value - KeyEvent.KEYCODE_0;
			pronumber = pronumber*10+number_key_value;
			if(pronumber>9999){
				pronumber = number_key_value;
			}	
			DTVTTGotoPage(pronumber);
			prono_timer_handler.postDelayed(prono_timer_runnable, 1000);
			number_key_down = true;
		}
	}

	private void DealLeftAndRightKey(){
		
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
		if(alert_password!=null){
			alert_password.cancel();
			password_dialog_show_flag=false;
		}
	}


	private AlertDialog.Builder editBuilder;
    private EditText editText;
	public void showPasswordDialog(){	
		if(password_dialog_show_flag==false){
			editText = new EditText(this);
			editText.setFilters(new  InputFilter[]{ new  InputFilter.LengthFilter(4)});
			editText.setTransformationMethod(PasswordTransformationMethod.getInstance());

			editBuilder.setTitle(R.string.enter_password);
			editBuilder.setView(editText); 

			alert_password = editBuilder.create();
			password_dialog_show_flag=true;
			alert_password.setOnKeyListener( new DialogInterface.OnKeyListener(){
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					switch(keyCode)
					{
						case KeyEvent.KEYCODE_DPAD_UP:	
							dispatchKeyEvent(event);
							return true;
						case KeyEvent.KEYCODE_DPAD_DOWN:				
							dispatchKeyEvent(event);
							return true;
						case KeyEvent.KEYCODE_DPAD_CENTER:
							String password = editText.getText().toString();
							cur_password = DTVPlayerGetPassword();
							if((cur_password==null)){
								if(password.equals("1234")||password.equals("0000")){
									PlayLockedProgram();
									alert_password.cancel();
									password_dialog_show_flag = false;	
								}
								else{
									editText.setText(null);								
									toast = Toast.makeText(
										DTVPlayer.this, 
										R.string.invalid_password,
										Toast.LENGTH_SHORT);
									toast.setGravity(Gravity.CENTER, 0, 0);
									toast.show();
								}
							}
							else
							{
								if(password.equals(cur_password)||password.equals("0000")){
									PlayLockedProgram();
									alert_password.cancel();
									password_dialog_show_flag = false;
								}
								else{
									editText.setText(null);
									
									toast = Toast.makeText(
									DTVPlayer.this, 
						    		R.string.invalid_password,
						    		Toast.LENGTH_SHORT);
									toast.setGravity(Gravity.CENTER, 0, 0);
									toast.show();
								}
							}
							return true;
						case KeyEvent.KEYCODE_BACK:
							alert_password.cancel();
							password_dialog_show_flag = false;
							ShowControlBar();
							return true;
					}
					return false;
				}
			});	
			alert_password.show();
			alert_password.getWindow().setLayout(500, 400);
			WindowManager.LayoutParams lp=alert_password.getWindow().getAttributes();
			lp.dimAmount=0.5f;
			alert_password.getWindow().setAttributes(lp);
			alert_password.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		}	 
	}

	private boolean programIsLocked(){
		return false;
	}

	private boolean programIsBlocked(){
		return false;
	}

	private int getProgramServiceType(){
		return 0;
	}
	
	private void PlayLockedProgram(){
		if(programIsLocked()||programIsBlocked()){
			hidePasswordDialog();

			if(getProgramServiceType()==0)
				showRadioBg();
			else
				hideRadioBg();
			
			try {
				int db_id = DTVPlayerGetCurrentProgramID();
				DTVPlayerPlayById(db_id);
				
			}catch (NumberFormatException e){
				e.printStackTrace();
			}
		}
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

	private int pronumber=0;
	private int dtvplayer_pronumber=0;
	private String dtvplayer_name=" ";
	private String dtvplayer_cur_event=null;
	private String dtvplayer_next_event=null;
	private String dtvplayer_event_des=null;
	private String dtvplayer_event_ext_des=null;
	private boolean dtvplayer_b_lock=false;
	private boolean dtvplayer_b_fav=false;
	private boolean dtvplayer_b_scrambled=false;
	private boolean dtvplayer_b_epg=false;
	private boolean dtvplyaer_b_txt=false;
	private boolean dtvplayer_b_sub=false;
	TVProgram.Subtitle mSubtitle[]=null;
	TVProgram.Teletext mTeletext[]=null;
	private int mSubtitleCount=0;
	private int mTeletextCount=0;
	private int mSubtitleIndex=0;
	private String mSubtitleLang[]=null;

	TVProgram.Audio mAudio[]=null;
	private int mAudioCount=0;
	private int mAudioIndex=0;
	private String mAudioLang[]=null;

	private TVProgram mTVProgram=null;
	private void DTVPlayerGetCurrentProgramData(){
		mTVProgram=DTVPlayerGetDataByCurrentID();
		dtvplayer_pronumber= mTVProgram.getNumber().getNumber();
		dtvplayer_name = mTVProgram.getName();
		dtvplayer_b_lock = mTVProgram.getLockFlag();
		dtvplayer_b_fav = mTVProgram.getFavoriteFlag();
		dtvplayer_b_scrambled = mTVProgram.getScrambledFlag();

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

		TVEvent[] mTVEvent=mTVProgram.getScheduleEvents(this,getUTCTime(),getUTCTime()*24*60*60*1000);	
		if(mTVEvent!=null){
			dtvplayer_cur_event=null;
			dtvplayer_event_des=null;
			dtvplayer_event_ext_des=null;
			dtvplayer_next_event=null;
			if(mTVEvent.length>=1){
				dtvplayer_cur_event=mTVEvent[0].getName();
				dtvplayer_event_des=mTVEvent[0].getEventDescr();
				dtvplayer_event_ext_des=mTVEvent[0].getEventExtDescr();
			}
			if(mTVEvent.length>=2)
				dtvplayer_next_event=mTVEvent[1].getName();
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

	private int cur=0;
	private Button BtnSubtitleLanguage=null;
	private void showSubtitleLanguageDialog(){
		if(mSubtitleCount>0){
			AlertDialog.Builder builder = new AlertDialog.Builder(DTVPlayer.this); 
		 	builder.setTitle(R.string.dtvplayer_subtitle_language_set);
			builder.setIcon( android.R.drawable.ic_dialog_info);
			builder.setSingleChoiceItems(mSubtitleLang,mSubtitleIndex, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {	
				cur= which;
				}
			});
			builder.setNegativeButton(R.string.cancel, new  DialogInterface.OnClickListener(){	
				//@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					 dialog.dismiss();
				}
			});
			builder.setPositiveButton(R.string.ok, new  DialogInterface.OnClickListener(){

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mSubtitleIndex= cur;
					Log.d(TAG,"mSubtitleIndex="+mSubtitleIndex);
					if(mSubtitleCount>0)
						BtnSubtitleLanguage.setText(mSubtitle[mSubtitleIndex].getLang());
					dialog.dismiss();
				}
			});	
						
			AlertDialog dialog = builder.create();
			dialog.show();  
			dialog.getWindow().setLayout(400,-1);
			WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
			lp.dimAmount=0.0f;
			dialog.getWindow().setAttributes(lp);
			dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		}
	}

	
	void showSubtitleSettingMenu(){	
		AlertDialog.Builder settingBuilder = new AlertDialog.Builder(this);
		LayoutInflater layoutInflater = LayoutInflater.from(this);  
		View subtitle_settings;

		if(mSubtitleCount>0)
		 	subtitle_settings = layoutInflater.inflate(R.layout.dtv_subtitle_settings, null); 
		else
			subtitle_settings = layoutInflater.inflate(R.layout.dtv_subtitle_settings_no_lan, null); 
		settingBuilder.setTitle(R.string.dtvplayer_subtitle_language_set);
		settingBuilder.setView(subtitle_settings);

		if(mSubtitleCount>0){
			BtnSubtitleLanguage =(Button)subtitle_settings.findViewById(R.id.BtnSubtitleLanguage);
			Log.d(TAG,"index0="+mSubtitleIndex);
			String ss = mSubtitleLang[mSubtitleIndex];
			BtnSubtitleLanguage.setText(ss);

			BtnSubtitleLanguage.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {
		        	 showSubtitleLanguageDialog();
		          }});
		}		  
		final CheckBox checkboxSubtitleSwitch = (CheckBox)subtitle_settings.findViewById(R.id.checkSubtitleSwitch);
		if(DTVGetSubtitleStatus()){
			checkboxSubtitleSwitch.setChecked(true);
		}
		else{
			checkboxSubtitleSwitch.setChecked(false);
		}
		
		settingBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {			
				 public void onClick(DialogInterface dialog, int which) {
					if(checkboxSubtitleSwitch.isChecked()){
						if(DTVGetSubtitleStatus()){
							switchSubtitle(mSubtitleIndex);
						}
						else{
							switchSubtitle(mSubtitleIndex);
							DTVSetSubtitleStatus(true);
						}
					}
					else{
						DTVSetSubtitleStatus(false);	
					}
						
					dialog.dismiss();
			}
		});
			 
		settingBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(		) {
			
			public void onClick(DialogInterface dialog, int which) {				
			}
		});


		AlertDialog alert = settingBuilder.create();
		alert.show();	
		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.00f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}
	
	int selected_index=0;
	private void showAudioLanguageDialog(){
		
		if(mAudioCount>0){
			AlertDialog.Builder builder = new AlertDialog.Builder(DTVPlayer.this); 
		 	builder.setTitle(R.string.dtvplayer_audio_language_set);
			//builder.setIcon( android.R.drawable.ic_dialog_info);
			builder.setSingleChoiceItems(mAudioLang,mAudioIndex, new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int which) {					  	
						Log.d(TAG,"Audio clicked index="+which);	
						selected_index = which;
				      }
				     });
			builder.setNegativeButton(R.string.cancel, new  DialogInterface.OnClickListener(){
					//@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						 dialog.dismiss();
					}
				 
			 });
			builder.setPositiveButton(R.string.ok, new  DialogInterface.OnClickListener(){

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.d("#####","#####"+which);	
						mAudioIndex=selected_index;
						switchAudio(mAudioIndex);
						dialog.dismiss();
					}
			 });	

			AlertDialog dialog = builder.create();
			dialog.show();  
			dialog.getWindow().setLayout(400,-1);
			WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
			lp.dimAmount=0.0f;
			dialog.getWindow().setAttributes(lp);
			dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		}			
	}

	private void finishPlayer(){
		DTVPlayerStopPlay();
		finish();
	}

	
}

