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

import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.app.*;
import android.content.*;

public class DTVPlayer extends DTVActivity{
	private static final String TAG="DTVPlayer";
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");

		super.onCreate(savedInstanceState);

		setContentView(R.layout.dtvplayer);

		openVideo();
		
		DTVPlayerUIInit();

		if(isHavePragram()==false){
			AlertDialog.Builder builder = new AlertDialog.Builder(DTVPlayer.this);
			builder.setMessage(R.string.dtvplayer_no_channel_stored)
			.setCancelable(false)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent Intent_scan = new Intent();
					Bundle bundle = new Bundle();
					Intent_scan.putExtras(bundle); 
					Intent_scan.setClass(DTVPlayer.this, dvbMenuActivity.class);

					Intent_scan.setAction("android.intent.action.dvbMenuActivity");
					
					startActivity(Intent_scan);
					DTVPlayer.this.finish(); 
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
				
			default:
				break;
		}
	}


	public void onNewIntent(Intent intent){
		Log.d(TAG, ">>>>>onNewIntent<<<<<");
		super.onNewIntent(intent);
	    setIntent(intent);
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
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				Log.d(TAG,"KEYCODE_DPAD_RIGHT");
				ShowMainMenu();
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				Log.d(TAG,"KEYCODE_DPAD_DOWN");
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				Log.d(TAG,"KEYCODE_DPAD_UP");
				break;
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
				return true;
			case KeyEvent.KEYCODE_BACK:
				if(mainmenu_show_flag){
					HideMainMenu();
				}
				else if(inforbar_show_flag){
					HideControlBar();
				}
				else{
					finish();
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
					pickerIntent.setClass(DTVPlayer.this, DTVChannelList.class);
					//pickerIntent.putExtras(bundle_list);
			        startActivityForResult(pickerIntent,0);	
				}
				return true;
			case KeyEvent.KEYCODE_ENTER:
				Log.d(TAG,"KEYCODE_ENTER");
				break;		 
			case KeyEvent.KEYCODE_ZOOM_IN:	
				Log.d(TAG,"KEYCODE_ZOOM_IN");
				return true;
			case KeyEvent.KEYCODE_ZOOM_OUT:
				Log.d(TAG,"KEYCODE_ZOOM_OUT");
				return true;
			case KeyEvent.KEYCODE_TV_REPEAT:
				Log.d(TAG,"KEYCODE_TV_REPEAT");
				return true;
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				Log.d(TAG,"KEYCODE_MEDIA_REWIND");
				return true;	
			case KeyEvent.KEYCODE_TAB: //info
				Log.d(TAG,"KEYCODE_TAB");	
				return true;
			case KeyEvent.KEYCODE_TV_SWITCH: //tv/radio
				Log.d(TAG,"KEYCODE_TV_SWITCH");	
				return true;	
			case KeyEvent.KEYCODE_TV_SHORTCUTKEY_DISPAYMODE: //16:9/4:3
				Log.d(TAG,"KEYCODE_TV_SHORTCUTKEY_DISPAYMODE");
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
				break;	
			case KeyEvent.KEYCODE_TV_SHORTCUTKEY_VOICEMODE:
				Log.d(TAG,"KEYCODE_TV_SHORTCUTKEY_VOICEMODE");
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
		
		//findViewById(R.id.RelativeLayout_video).setOnClickListener(new MouseClick());
		RelativeLayout_inforbar.setVisibility(View.VISIBLE);
		RelativeLayout_radio_bg.setVisibility(View.INVISIBLE);
		
		init_Animation();
	}

	class MouseClick implements OnClickListener{
	    public void onClick(View v) {
			// TODO Auto-generated method stub	
			switch (v.getId()) {
				case R.id.Button_mainmenu_list:
				case R.id.Button_mainmenu_epg:
				case R.id.Button_mainmenu_settings:
				case R.id.Button_mainmenu_program_manager:	
				case R.id.Button_mainmenu_timeshift:
				case R.id.Button_mainmenu_prv:	
				case R.id.Button_mainmenu_manage:
				case R.id.Button_mainmenu_skip:				
					break;			
			}
		}
    }

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


	private boolean inforbar_show_flag=true;
	private void ShowControlBar(){
		if((inforbar_show_flag==false)&&(RelativeLayout_inforbar!=null&&showAction!=null&&RelativeLayout_recording_icon!=null&&showPvrAction!=null)){	
			
			RelativeLayout_inforbar.startAnimation(showAction);   
			RelativeLayout_inforbar.setVisibility(View.VISIBLE);
		
			RelativeLayout_recording_icon.startAnimation(showPvrAction);
			RelativeLayout_recording_icon.setVisibility(View.VISIBLE);
		}

		inforbar_show_flag = true;
		//timer_handler.postDelayed(timer_runnable, inforbar_distime); 
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
	}

	private boolean isHavePragram(){
		return false;
	}

}

