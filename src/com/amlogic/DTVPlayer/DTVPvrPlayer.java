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
import com.amlogic.tvutil.DTVPlaybackParams;
import com.amlogic.tvutil.DTVRecordParams;

import java.util.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.text.*;
import android.text.method.*;
import com.amlogic.widget.SureDialog;
import com.amlogic.widget.SingleChoiseDialog;
import com.amlogic.widget.CustomDialog;
import com.amlogic.widget.CustomDialog.ICustomDialog;

public class DTVPvrPlayer extends DTVActivity{
	private static final String TAG="DTVPvrPlayer";
	private int record_id =0;
	private String proname=null;
	private String file_name = null;
	private boolean current_playback_flag=false;
	private enum menu {
		TTX,
		AUDIO_LANGUAGE,
		SUBTITLE_SETTING,
		SHORTCUT_AUDIO_TRACK,
		SHORTCUT_PICTURE_MODE,
		EXIT_PLAYER,
		MENU_NUM,
	}
	private static final int[] MENUS = new int[menu.MENU_NUM.ordinal()];
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		MENUS[menu.TTX.ordinal()] = R.string.tel_text;
		MENUS[menu.AUDIO_LANGUAGE.ordinal()] = R.string.dtvplayer_audio_language_set;
		MENUS[menu.SUBTITLE_SETTING.ordinal()] = R.string.dtvplayer_subtitle_language_set;
		MENUS[menu.SHORTCUT_AUDIO_TRACK.ordinal()] = R.string.audio_track;
		MENUS[menu.SHORTCUT_PICTURE_MODE.ordinal()] = R.string.picture_mode;
		MENUS[menu.EXIT_PLAYER.ordinal()] = R.string.exit_player;
		setContentView(R.layout.dtvtimeshiftplayer); 
		Bundle bundle = this.getIntent().getExtras();
		if(bundle!=null){
	    	record_id = bundle.getInt("booking_id");
			file_name = bundle.getString("file_name");
		}	
		
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		openVideo();
		setBlackoutPolicy(1);
		startPlayback(file_name);
		DTVPvrPlayerUIInit();
		Log.d(TAG,"play file ="+file_name);
		pvrHandler.postDelayed(pvrTimer, 1000);
		current_playback_flag=false;
		setVideoViewWindow();
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
		pvrHandler.removeCallbacks(pvrTimer);
		stopPlayback();
		playValid();
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
			case TVMessage.TYPE_RECORD_END:	
				switch(msg.getErrorCode()){
					case  TVMessage.REC_ERR_OPEN_FILE:
						DTVRecordingStop();
						toast = Toast.makeText(
							DTVPvrPlayer.this,
				    		R.string.check_usb_device,
				    		Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						
						break;
					case  TVMessage.REC_ERR_WRITE_FILE:	
						DTVRecordingStop();
						toast = Toast.makeText(
							DTVPvrPlayer.this,
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
				if(isTopActivity(DTVPvrPlayer.this))
						gotoBack();
				break;
			case TVMessage.TYPE_PLAYBACK_START:
				PvrPlayerGetCurrentProgramData();
				updateInforbar();
				break;
			case TVMessage.TYPE_PLAYBACK_STOP:
				if(isTopActivity(DTVPvrPlayer.this))
					gotoBack();
				break;
			default:
				break;
	
		}
	}

	private static final int STAT_PLAY= 1;
	private static final int STAT_PAUSE = 2;
	private static final int STAT_FF = 3;
	private static final int STAT_FB = 4;
	private int play_status = STAT_PLAY;
	private int speed = 0;
	private TextView cur_time;
	private  TextView total_time;
	private SeekBar myProgressBar;
	private long curtime;
	private long totaltime = 0;
	private RelativeLayout bufferLayout;
	private RelativeLayout infoLayout;
	private RelativeLayout Timeshifting_icon_layout;
	ImageButton more;
	ImageButton play;
	ImageButton fastforword;
	ImageButton fastreverse;
	ImageView TimeshiftingIcon;
	private boolean SeekSliding = false;
	int myProgressBar_pos=0;
	private Toast toast=null;
	private boolean teletext_bar_flag=false;

	/*Infor bar*/
	TextView Text_screentype_info=null;
	TextView Text_parent_control_info_icon=null;
	TextView Text_MTS_info=null;
	
	void DTVPvrPlayerUIInit(){
		findViewById(R.id.RelativeLayout_video).setOnClickListener(new MouseClick());
		bufferLayout = (RelativeLayout)findViewById(R.id.bufferLayout);
		bufferLayout.setVisibility(View.INVISIBLE);
		infoLayout = (RelativeLayout)findViewById(R.id.RelativeLayoutInforbar);
		TimeshiftingIcon = (ImageView) findViewById(R.id.ImageViewTimeshiftIcon);
		Timeshifting_icon_layout = (RelativeLayout)findViewById(R.id.RelativeLayoutTimeshiftIcon);

		Text_MTS_info = (TextView) findViewById(R.id.Text_MTS_info);
		Text_screentype_info = (TextView) findViewById(R.id.Text_screentype_info);
		Text_parent_control_info_icon = (TextView) findViewById(R.id.Text_parent_control_info_icon);

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
			Text_MTS_info.setText(getString(R.string.stereo));			
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
	
        more = (ImageButton)findViewById(R.id.moreBtn);
        play = (ImageButton)findViewById(R.id.PlayBtn);
        fastforword = (ImageButton)findViewById(R.id.FastForward);
        fastreverse = (ImageButton)findViewById(R.id.FastReverse);

        myProgressBar = (SeekBar)findViewById(R.id.SeekBar02);

        cur_time = (TextView)findViewById(R.id.TextView03);
        total_time = (TextView)findViewById(R.id.TextView04);
    	//cur_time.setText(secToTime(curtime, false));
    	//total_time.setText(secToTime(totaltime, true));

		more.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				//hideInforbar();
				// showTimeshiftDialog();
				Context ctx = DTVPvrPlayer.this;
				String[] menuItems = new String[menu.MENU_NUM.ordinal()];
				for (int i = 0; i < menuItems.length; i++)
					menuItems[i] = ctx.getString(MENUS[i]);
				new MenuDialog(DTVPvrPlayer.this, menuItems, 0);
			}
        });	
	
				
        fastforword.setOnClickListener(new Button.OnClickListener(){
		public void onClick(View v) {
			if (play_status == STAT_FF){
				if (speed < 8)
					speed=speed*2;
			}
			else{
				speed = 2;
				play_status = STAT_FF;
			}
			
			DTVTimeShiftingForward(speed);
			
			play.setBackgroundResource(R.drawable.play_button);
			switch(speed)
			{
				case 2:
					TimeshiftingIcon.setImageResource(R.drawable.forward_speed_2);
					break;
				case 4:
					TimeshiftingIcon.setImageResource(R.drawable.forward_speed_4);
					break;
				case 8:
					TimeshiftingIcon.setImageResource(R.drawable.forward_speed_8);
					break;
			}
		}
        });
        
        fastreverse.setOnClickListener(new Button.OnClickListener(){
		public void onClick(View v) {
			if (play_status == STAT_FB)
			{
				if (speed < 8)
					speed=speed*2;
			}
			else
			{
				speed = 2;
				play_status = STAT_FB;
			}
			

			DTVTimeShiftingBackward(speed);

			play.setBackgroundResource(R.drawable.play_button);

			switch(speed)
			{
				case 2:
					TimeshiftingIcon.setImageResource(R.drawable.backward_speed_2);
					break;
				case 4:
					TimeshiftingIcon.setImageResource(R.drawable.backward_speed_4);
					break;
				case 8:
					TimeshiftingIcon.setImageResource(R.drawable.backward_speed_8);
					break;
			}

		}
        });
        
        if (play_status == STAT_PLAY)
        {
			play.setBackgroundResource(R.drawable.pause_button);
			play.setBackgroundResource(R.drawable.pause_button);
		}
		
        play.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (play_status == STAT_PLAY)
				{
					DTVTimeShiftingPause();
					play_status = STAT_PAUSE;
					play.setBackgroundResource(R.drawable.pause_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_pause);
				}
				else if (play_status == STAT_PAUSE)
				{	
					DTVTimeShiftingResume();
					play_status = STAT_PLAY;
					play.setBackgroundResource(R.drawable.play_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);
				}
				else if (play_status == STAT_FF)
				{
					DTVTimeShiftingForward(0);
					play_status = STAT_PLAY;
					speed = 0;
					play.setBackgroundResource(R.drawable.play_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);

				}
				else if (play_status == STAT_FB)
				{
					DTVTimeShiftingBackward(0);
					play_status = STAT_PLAY;
					speed = 0;
					play.setBackgroundResource(R.drawable.play_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);

				}
				else
				{
					DTVTimeShiftingPlay();
					play_status = STAT_PLAY;
					play.setBackgroundResource(R.drawable.play_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);

				}
			}
        });
                
 
        if (curtime != 0)
        	myProgressBar.setProgress((int)(curtime*100/totaltime/1000));
		
        	myProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				int dest = myProgressBar.getProgress();
				int pos = (int)totaltime * dest / 100;
				DTVTimeShiftingSeek(pos);
				myProgressBar_pos = (int)curtime*100/(int)totaltime;
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				SeekSliding = true;
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
				// TODO Auto-generated method stub	
			}
		});
	     
	    infoLayout.setVisibility(View.INVISIBLE);
		bufferLayout.setVisibility(View.INVISIBLE);
		infoLayout.setVisibility(View.VISIBLE);
		play.setEnabled(true);
		play.setBackgroundResource(R.drawable.play_button);
		play_status = STAT_PLAY;
		play.requestFocus();
		showTimeshiftingIcon();


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
			switch (MENUS[which]) {
				case R.string.tel_text:
					DTVPlayer.showTeltext(DTVPvrPlayer.this);
					break;
				case R.string.dtvplayer_audio_language_set:
					showAudioLanguageDialog(DTVPvrPlayer.this);
					break;
				case R.string.dtvplayer_subtitle_language_set:
					showSubtitleSettingMenu(DTVPvrPlayer.this);
					break;
				case R.string.audio_track:
					shortcut_key_deal("AUDIOTRACK");
					break;
				case R.string.picture_mode:
					shortcut_key_deal("pictrue_mode");
					break;
				case R.string.exit_player:
					showTimeshiftDialog();
					break;
			}
		}
	}

	class MouseClick implements OnClickListener{
	    public void onClick(View v) {
			// TODO Auto-generated method stub	
			switch (v.getId()) {
				case R.id.RelativeLayout_video:	
					if(teletext_bar_flag==false){				
						showInforbar();				
					}
					break;	
			}
		}
    }

	private void gotoBack(){
			
		Intent intent = new Intent();
		intent.setClass(DTVPvrPlayer.this, DTVPvrManager.class);
		startActivity(intent);
		//setResult(RESULT_OK,intent); 
		DTVPvrPlayer.this.finish();
	}

	private void showTimeshiftDialog(){
		new SureDialog(DTVPvrPlayer.this){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.timeshifting_exit_message));
			}

			public void onSetNegativeButton(){
			}
			public void onSetPositiveButton(){

				pvrHandler.removeCallbacks(pvrTimer);
				stopPlayback();
				gotoBack();
			}
		};
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
   		//showInforbar();
		if(!connected){
			return true;
			}
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if(teletext_bar_flag == false){
					showInforbar();
				}	
				break;
			case KeyEvent.KEYCODE_BACK:
				if(teletext_bar_flag){				
					DTVSubtitleStop();	
					DTVTeletextStop();
					bufferLayout.setVisibility(View.INVISIBLE);
					showInforbar();				
				}
				 else if(inforbar_show_flag){
					hideInforbar();
				 }
				 else{
					showTimeshiftDialog();
				}
				return true;
			case DTVActivity.KEYCODE_TTX:	
				DTVPlayer.showTeltext(DTVPvrPlayer.this);	
				return true;	
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if(dtvplyaer_b_txt&&PvrPlayerInTeletextStatus){
					DTVTTGotoNextPage();
				}	
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
				Log.d(TAG,"KEYCODE_DPAD_UP");
				if(dtvplyaer_b_txt&&PvrPlayerInTeletextStatus){
					DTVTTGotoPreviousPage();
				}	
				return true;
			case KeyEvent.KEYCODE_TV_SHORTCUTKEY_VOICEMODE:
				Log.d(TAG,"KEYCODE_TV_SHORTCUTKEY_VOICEMODE");
				showAudioLanguageDialog(DTVPvrPlayer.this);
				return true;		
			case KeyEvent.KEYCODE_TV_SUBTITLE:
				showSubtitleSettingMenu(DTVPvrPlayer.this);
				return true;	
				

			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				play.requestFocus();
				
				if (play_status == STAT_PLAY)
				{
					DTVTimeShiftingPause();
					play_status = STAT_PAUSE;
					play.setBackgroundResource(R.drawable.pause_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_pause);
				}
				else if (play_status == STAT_PAUSE)
				{	
					DTVTimeShiftingResume();
					play_status = STAT_PLAY;
					play.setBackgroundResource(R.drawable.play_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);
				}
				else if (play_status == STAT_FF)
				{
					DTVTimeShiftingForward(0);
					play_status = STAT_PLAY;
					speed = 0;
					play.setBackgroundResource(R.drawable.play_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);

				}
				else if (play_status == STAT_FB)
				{
					DTVTimeShiftingBackward(0);
					play_status = STAT_PLAY;
					speed = 0;
					play.setBackgroundResource(R.drawable.play_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);

				}
				else
				{
					DTVTimeShiftingPlay();
					play_status = STAT_PLAY;
					play.setBackgroundResource(R.drawable.play_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);

				}
				return true;
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				fastreverse.requestFocus();
				if (play_status == STAT_FB)
				{
					if (speed < 8)
						speed=speed*2;
				}
				else
				{
					speed = 2;
					play_status = STAT_FB;
				}
				

				DTVTimeShiftingBackward(speed);

				play.setBackgroundResource(R.drawable.play_button);

				switch(speed)
				{
					case 2:
						TimeshiftingIcon.setImageResource(R.drawable.backward_speed_2);
						break;
					case 4:
						TimeshiftingIcon.setImageResource(R.drawable.backward_speed_4);
						break;
					case 8:
						TimeshiftingIcon.setImageResource(R.drawable.backward_speed_8);
						break;	
				}

				return true;
			
			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD: 
				fastforword.requestFocus();
				if (play_status == STAT_FF)
				{
					if (speed < 8)
						speed=speed*2;
				}
				else
				{
					speed = 2;
					play_status = STAT_FF;
				}
				
				DTVTimeShiftingForward(speed);

				play.setBackgroundResource(R.drawable.play_button);
				switch(speed)
				{
					case 2:
						TimeshiftingIcon.setImageResource(R.drawable.forward_speed_2);
						break;
					case 4:
						TimeshiftingIcon.setImageResource(R.drawable.forward_speed_4);
						break;
					case 8:
						TimeshiftingIcon.setImageResource(R.drawable.forward_speed_8);
						break;	
				}
				return true;
			case DTVActivity.KEYCODE_AUDIO_TRACK:
				Log.d(TAG,"KEYCODE_AUDIO_TRACK");
				shortcut_key_deal("AUDIOTRACK");
				return true;
			
			case DTVActivity.KEYCODE_RED_BUTTON: //16:9/4:3
				Log.d(TAG,"KEYCODE_RED_BUTTON");
				shortcut_key_deal("pictrue_mode");
				return true;
			/*	
			case DTVActivity.KEYCODE_SUBTITLE:
				Log.d(TAG,"KEYCODE_SUBTITLE");
				shortcut_key_deal("SUBTITLE");
				return true;	
			*/	
		}
		
		return super.onKeyDown(keyCode, event);
	}

	private void shortcut_key_deal(String key){
		if(key.equals("pictrue_mode")){
			
			int mode = DTVGetScreenMode();
			if(mode==0){
				//ShowInformation(getString(R.string.type_4_3));
				DTVSetScreenMode(2);
				Text_screentype_info.setText(getString(R.string.type_4_3));
			}
			else  if(mode==2){
				//ShowInformation(getString(R.string.type_16_9));		
				DTVSetScreenMode(3);
				Text_screentype_info.setText(getString(R.string.type_16_9));
			}
			else  if(mode==3){
				//ShowInformation(getString(R.string.auto));		
				DTVSetScreenMode(0);
				Text_screentype_info.setText(getString(R.string.auto));
			}
		}
		else if(key.equals("AUDIOTRACK")){
			int mode = DTVGetAudioTrack();
			if(mode==1){ 						
				//ShowInformation(getString(R.string.right));			
				DTVSetAudioTrack(2);
				Text_MTS_info.setText(getString(R.string.right));			
			}
			else  if(mode==2){
				//ShowInformation(getString(R.string.stereo));			
				DTVSetAudioTrack(0);
				Text_MTS_info.setText(getString(R.string.stereo));			
			}
			else  if(mode==0){
				//ShowInformation(getString(R.string.left));				
				DTVSetAudioTrack(1);
				Text_MTS_info.setText(getString(R.string.left));				
			}	
		}
		else if(key.equals("SUBTITLE")){
			if(DTVGetSubtitleStatus()){
				DTVSetSubtitleStatus(false);
				//ShowInformation(getString(R.string.off));	
				Text_parent_control_info_icon.setText("SUB:"+getString(R.string.off));	
			}
			else{
				DTVSetSubtitleStatus(true);
				//ShowInformation(getString(R.string.on));
				Text_parent_control_info_icon.setText("SUB:"+getString(R.string.on));	
			}
		}
		
	}
	
	private void showTimeshiftingIcon(){
		final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
		animation.setDuration(1500); // duration - half a second
		animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
		animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
		animation.setRepeatMode(Animation.REVERSE); // 
		TimeshiftingIcon.setVisibility(View.VISIBLE);
		TimeshiftingIcon.setAnimation(animation);
		Timeshifting_icon_layout.setVisibility(View.VISIBLE);
	}

	private void hideTimeshiftingIcon(){
		TimeshiftingIcon.setVisibility(View.INVISIBLE);
		Timeshifting_icon_layout.setVisibility(View.INVISIBLE);
	}


	private boolean inforbar_show_flag=false;
	private void showInforbar(){
		if(inforbar_show_flag==false){
			infoLayout.setVisibility(View.VISIBLE);
			play.requestFocus();
			inforbar_show_flag=true;
		}
	
	}

	private void hideInforbar(){
		if(inforbar_show_flag){
			infoLayout.setVisibility(View.INVISIBLE);
			inforbar_show_flag=false;
		}
	}

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

	private String program_name = null;
	private boolean PvrPlayerInTeletextStatus=false;

	private TVProgram mTVProgram=null;
	private void PvrPlayerGetCurrentProgramData(){
		mTVProgram=DTVPlayerGetDataByCurrentID();
		
		dtvplayer_b_epg = false;
		
		program_name = mTVProgram.getName();
		dtvplayer_b_lock = mTVProgram.getLockFlag();
		dtvplayer_b_fav = mTVProgram.getFavoriteFlag();
		dtvplayer_b_scrambled = mTVProgram.getScrambledFlag();
		//dtvplayer_atsc_antenna_source = mDTVSettings.getAtscAntennaSource();

		mAudioCount=mTVProgram.getAudioCount();
		if(mAudioCount>0){
			mAudio= new TVProgram.Audio[mAudioCount];
			mAudioLang = new String[mAudioCount];
			for(int i=0;i<mAudioCount;i++){
				mAudio[i]=mTVProgram.getAudio(i);
				mAudioLang[i]= mAudio[i].getLang();
				Log.d(TAG,"Audio Lang:"+mAudioLang[i]);
			}
			mAudioIndex = mTVProgram.getCurrentAudio(getStringConfig("tv:audio:language"));
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

			mSubtitleIndex=mTVProgram.getCurrentSubtitle(getStringConfig("tv:subtitle:language"));
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

		PvrPlayerInTeletextStatus=false;
		
	}



	private void updateInforbar(){
		
		
		TextView Text_screentype_info = (TextView) findViewById(R.id.Text_screentype_info);
		TextView Text_parent_control_info_icon = (TextView) findViewById(R.id.Text_parent_control_info_icon);
		TextView Text_channel_type = (TextView) findViewById(R.id.Text_channel_type);
		TextView Text_MTS_info = (TextView) findViewById(R.id.Text_MTS_info);

		ImageView ImageView_icon_scrambled=(ImageView)findViewById(R.id.ImageView_icon_scrambled);
		ImageView ImageView_icon_fav=(ImageView)findViewById(R.id.ImageView_icon_fav);
		ImageView ImageView_icon_lock=(ImageView)findViewById(R.id.ImageView_icon_lock);

		ImageView ImageView_icon_epg=(ImageView)findViewById(R.id.ImageView_icon_epg);
		ImageView ImageView_icon_sub=(ImageView)findViewById(R.id.ImageView_icon_sub);
		ImageView ImageView_icon_txt=(ImageView)findViewById(R.id.ImageView_icon_txt);

		TextView Text_proname = (TextView) findViewById(R.id.Text_proname);
		Text_proname.setTextColor(Color.YELLOW);

		if(program_name!=null)	
			Text_proname.setText(/*Integer.toString(DTVPlayer.dtvplayer_pronumber)+*/"  "+program_name);

		//if(DTVPlayer.dtvplayer_pronumber>=0)	
			//Text_proname.setText(Integer.toString(DTVPlayer.dtvplayer_pronumber)+"  "+DTVPlayer.dtvplayer_name);

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
			Text_parent_control_info_icon.setText("SUB:"+getString(R.string.on));	
		}
		else{
			Text_parent_control_info_icon.setText("SUB:"+getString(R.string.off));
		}


	}


	private void freshTimeAndSeekbar(long cur_time,long total_time){
		myProgressBar = (SeekBar)findViewById(R.id.SeekBar02);
		TextView text_cur_time = (TextView)findViewById(R.id.TextView03);
        TextView text_total_time = (TextView)findViewById(R.id.TextView04);

		curtime = cur_time;
		totaltime = total_time;
		text_cur_time.setText(secToTime(curtime, false));
		text_total_time.setText(secToTime(totaltime, true));

		if (totaltime == 0){
			myProgressBar_pos = 0;
			myProgressBar.setProgress(0);
		}	
		else {
			if (!SeekSliding){
				myProgressBar.setProgress(((int)curtime*100)/(int)totaltime);
				myProgressBar_pos =((int)curtime*100)/(int)totaltime;
			}
		}
	}

	private static int playback_status = DTVPlaybackParams.PLAYBACK_ST_PLAYING;
	void statusChangeUpdate(int status){	
		if(playback_status!=status){
			switch(status) {
					case DTVPlaybackParams.PLAYBACK_ST_PLAYING:
						play.requestFocus();
						play.setBackgroundResource(R.drawable.pause_button);
						TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);
						break;
					case DTVPlaybackParams.PLAYBACK_ST_PAUSED:
						play.setBackgroundResource(R.drawable.play_button);
						TimeshiftingIcon.setImageResource(R.drawable.timeshifting_pause);
						break;
					case DTVPlaybackParams.PLAYBACK_ST_EXIT:
	 					SeekSliding = false;
						break;
					case DTVPlaybackParams.PLAYBACK_ST_STOPPED:
					case DTVPlaybackParams.PLAYBACK_ST_FFFB:
						break;
			}	
			playback_status=status;
		}	
	}
	private Handler pvrHandler = new Handler();
	private Runnable pvrTimer = new Runnable() {
		public void run() {
			DTVPlaybackParams recPara = getPlaybackParams();
			if (recPara != null) {
				Log.d(TAG, "recPara: status("+recPara.getStatus()+
					"), time "+recPara.getCurrentTime()/1000+" / "+
					recPara.getTotalTime()/1000);
				statusChangeUpdate(recPara.getStatus());
				freshTimeAndSeekbar(recPara.getCurrentTime()/1000,recPara.getTotalTime()/1000);
			}
	
			pvrHandler.postDelayed(this, 1000);
		}
	};

	private void setVideoViewWindow(){
		RelativeLayout video_position= (RelativeLayout) findViewById(R.id.RelativeLayout_video);
		int[] location = new  int[2] ;
		//video_position.getLocationInWindow(location); 
		video_position.getLocationOnScreen(location);
		int height = video_position.getMeasuredHeight();
		int width = video_position.getMeasuredWidth();
		Log.d(TAG,"x=="+location[0]+"-----y=="+location[1]+"---width=="+width+"----height=="+height);
		setVideoWindow(new Rect(location[0], location[1], width, height));
	}

	public void showTeltext(Context c){
		Context mContext = c;
		if(dtvplyaer_b_txt){
			if(PvrPlayerInTeletextStatus==false){
				((DTVActivity)mContext).DTVTTShow();
				PvrPlayerInTeletextStatus=true;
			}	
			else{
				((DTVActivity)mContext).DTVTTHide();
				PvrPlayerInTeletextStatus=false;
			}	
		}	
	}


	public void showAudioLanguageDialog(Context context){	
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
			};	
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
	
}

