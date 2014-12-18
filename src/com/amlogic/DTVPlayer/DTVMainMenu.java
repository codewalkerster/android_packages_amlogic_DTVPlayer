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

import java.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.text.*;
import android.text.method.*;
import java.lang.reflect.Field;
import com.amlogic.widget.SingleChoiseDialog;
import com.amlogic.tvutil.TVDimension;
import com.amlogic.widget.PasswordDialog;

public class DTVMainMenu extends DTVActivity{
	private static final String TAG="DTVMainMenu";
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtv_main_menu);
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();		
		DTVMainMenuUIInit();
	}

	public void onDisconnected(){
		Log.d(TAG, "disconnected");
		super.onDisconnected();
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
		
	/*main menu*/
	Button Button_mainmenu_list=null;
	Button Button_mainmenu_epg=null;
	Button Button_mainmenu_settings=null;
	Button Button_mainmenu_program_manager=null;
	Button Button_mainmenu_timeshift=null;
	Button Button_mainmenu_prv=null;
	Button Button_mainmenu_manage=null;
	Button Button_mainmenu_skip=null;
	TextView Text_button_info=null;


	private Animation mainMenuShowAction;
	private Animation mainMenuHideAction;
	private Animation showPvrAction;
	private AnimationSet mainMenuButtonFocus ;
	private void init_Animation(){
	
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

	private void DTVMainMenuUIInit(){
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

		init_Animation();
	}

	class MouseClick implements OnClickListener{
	    public void onClick(View v) {
			// TODO Auto-generated method stub	
			switch (v.getId()) {

				case R.id.Button_mainmenu_list:
					//HideMainMenu();
					Intent pickerIntent = new Intent();
					Bundle bundle_list = new Bundle();
					bundle_list.putInt("db_id", DTVPlayerGetCurrentProgramID());
					pickerIntent.putExtras(bundle_list);
					pickerIntent.setClass(DTVMainMenu.this, DTVChannelList.class);
 		           	startActivity(pickerIntent);
					DTVMainMenu.this.finish();
 		            break;
				case R.id.Button_mainmenu_epg:
					//HideMainMenu();
					//HideControlBar();
					Intent Intent_epg = new Intent();
					Intent_epg.setClass(DTVMainMenu.this, DTVEpg.class);
					startActivity(Intent_epg);
					DTVMainMenu.this.finish();
					break;
				case R.id.Button_mainmenu_settings:
					Intent Intent_settings = new Intent();
					Bundle bundle = new Bundle();
					Intent_settings.putExtras(bundle); 
					Intent_settings.setClass(DTVMainMenu.this, DTVSettingsUI.class);
					startActivityForResult(Intent_settings,2);
					DTVMainMenu.this.finish();
					break;
				case R.id.Button_mainmenu_program_manager:
					//HideMainMenu();
					showProgramManagerDialog();
					
					break;
				case R.id.Button_mainmenu_timeshift:
					Intent Intent_timeshift = new Intent();
					Intent_timeshift.setClass(DTVMainMenu.this, DTVTimeshifting.class);
					startActivity(Intent_timeshift);
					//HideMainMenu();
					//HideControlBar();
					break;
				case R.id.Button_mainmenu_prv:	
					Intent Intent_rec = new Intent();
					Intent_rec.setClass(DTVMainMenu.this, DTVRecManager.class);
					startActivity(Intent_rec);
					//HideMainMenu();
					//HideControlBar();
					break;
				case R.id.Button_mainmenu_manage:
					Intent Intent_book = new Intent();
					Intent_book.setClass(DTVMainMenu.this, DTVBookingManager.class);
					startActivity(Intent_book);
					//HideMainMenu();
					//HideControlBar();
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		// TODO Auto-generated method stub
		if(!connected){
			return true;
			}
		switch (keyCode) {
			
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_MENU:		
				DTVMainMenu.this.finish();
				return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	private Toast toast=null;
	public void showProgramManagerDialog(){
		new PasswordDialog(DTVMainMenu.this){
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

	private void gotoProgramManager(){
		Intent pickerIntent_pro = new Intent();
		Bundle bundle_promanage = new Bundle();
		bundle_promanage.putInt("db_id", DTVPlayerGetCurrentProgramID());
		pickerIntent_pro.putExtras(bundle_promanage);
		pickerIntent_pro.setClass(DTVMainMenu.this, DTVProgramManager.class);
        startActivity(pickerIntent_pro);
		DTVMainMenu.this.finish();
	}
}

