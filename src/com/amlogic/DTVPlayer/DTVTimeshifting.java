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

public class DTVTimeshifting extends DTVActivity{
	private static final String TAG="DTVTimeshifting";
	private DTVSettings mDTVSettings = null;
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvtimeshiftplayer); 
		mDTVSettings= new DTVSettings(this);
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		openVideo();
		/* there may be a conflict in startTimeshifting, so
		 * we need to sovle the RECORD_CONFLICT message.
		 */
		startTimeshifting();
		DTVTimeshiftingUIInit();
		timeshiftingHandler.postDelayed(timeshiftingTimer, 1000);
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
		timeshiftingHandler.removeCallbacks(timeshiftingTimer);
		unblock();
		//playValid();
		if(toast!=null)
			toast.cancel(); 
		this.finish();
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
				Log.d(TAG, "getErrorCode--"+msg.getErrorCode());
				switch(msg.getErrorCode()){
					case  TVMessage.REC_ERR_OPEN_FILE:
						DTVTimeShiftingStop();
						if(toast!=null)
							toast.cancel(); 
						toast = Toast.makeText(
							DTVTimeshifting.this,
				    		R.string.check_usb_device,
				    		Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						gotoDTVPlayer();
						break;
					case  TVMessage.REC_ERR_WRITE_FILE:	
						DTVTimeShiftingStop();
						if(toast!=null)
							toast.cancel(); 
						toast = Toast.makeText(
							DTVTimeshifting.this,
				    		R.string.usbdisk_is_full,
				    		Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						gotoDTVPlayer();							
					break;
					case  TVMessage.REC_ERR_ACCESS_FILE:
						gotoDTVPlayer();					
						break;
					case  TVMessage.REC_ERR_SYSTEM:
						gotoDTVPlayer();
						break;							
				}
				
				break;
			case TVMessage.TYPE_RECORD_CONFLICT:
				if (msg.getRecordConflict() == TVMessage.REC_CFLT_START_TIMESHIFT){
					if (!isFinishing()){
						/* solve this conflict */
						showStopRecordingDialog();
					}
				}
				break;
			default:
				break;
	
		}
	}

	private void gotoDTVPlayer(){
		Intent intent = new Intent();
		intent.setClass(DTVTimeshifting.this, DTVPlayer.class);
		startActivity(intent);
		DTVTimeshifting.this.finish();
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
	
	void DTVTimeshiftingUIInit(){
		findViewById(R.id.RelativeLayout_video).setOnClickListener(new MouseClick());
		bufferLayout = (RelativeLayout)findViewById(R.id.bufferLayout);
		bufferLayout.setVisibility(View.INVISIBLE);
		
		infoLayout = (RelativeLayout)findViewById(R.id.RelativeLayoutInforbar);
		TimeshiftingIcon = (ImageView) findViewById(R.id.ImageViewTimeshiftIcon);
		Timeshifting_icon_layout = (RelativeLayout)findViewById(R.id.RelativeLayoutTimeshiftIcon);
	
        more = (ImageButton)findViewById(R.id.moreBtn);
        play = (ImageButton)findViewById(R.id.PlayBtn);
        fastforword = (ImageButton)findViewById(R.id.FastForward);
        fastreverse = (ImageButton)findViewById(R.id.FastReverse);

        myProgressBar = (SeekBar)findViewById(R.id.SeekBar02);
		cur_time = (TextView)findViewById(R.id.TextView03);
        total_time = (TextView)findViewById(R.id.TextView04);

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


		more.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				//hideInforbar();
				showTimeshiftDialog();
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
		
		updateInforbar();
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

	private void showTimeshiftDialog(){
		new SureDialog(DTVTimeshifting.this){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.timeshifting_exit_message));
			}

			public void onSetNegativeButton(){
			}
			public void onSetPositiveButton(){
				DTVTimeShiftingStop();
				Intent intent = new Intent();
				intent.setClass(DTVTimeshifting.this, DTVPlayer.class);
				startActivity(intent);
				finish();	
			}
		};
	}

	private void showStopRecordingDialog(){
		new SureDialog(DTVTimeshifting.this){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.timeshift_when_recording));
			}
			public void onSetNegativeButton(){
				 DTVTimeshifting.this.finish();
			}
			public void onSetPositiveButton(){
				DTVPlayerStopRecording();

				startTimeshifting();
			}
		};
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

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
				Log.d(TAG,"KEYCODE_TTX");
				DTVPlayer.showTeltext(DTVTimeshifting.this);
				return true;	
			case KeyEvent.KEYCODE_ZOOM_OUT:
				
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if(DTVPlayer.dtvplyaer_b_txt&&DTVPlayer.DTVPlayerInTeletextStatus){
					DTVTTGotoNextPage();
				}	
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
				Log.d(TAG,"KEYCODE_DPAD_UP");
				if(DTVPlayer.dtvplyaer_b_txt&&DTVPlayer.DTVPlayerInTeletextStatus){
					DTVTTGotoPreviousPage();
				}	
				return true;
			case DTVActivity.KEYCODE_AUDIO_LANGUAGE:
				Log.d(TAG,"KEYCODE_AUDIO_LANGUAGE");
				DTVPlayer.showAudioLanguageDialog(DTVTimeshifting.this);
				return true;		
			case KeyEvent.KEYCODE_TAB: //info
				if(teletext_bar_flag){
				}
				return true;	
			case DTVActivity.KEYCODE_SUBTITLE:
				DTVPlayer.showSubtitleSettingMenu(DTVTimeshifting.this);
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

		//Text_channel_type.setText(DTVPlayer.dtvplayer_atsc_antenna_source);
		
		TextView Text_proname = (TextView) findViewById(R.id.Text_proname);
		Text_proname.setTextColor(Color.YELLOW);



		if(mDTVSettings.getScanRegion().contains("ATSC")==false){
			Text_proname.setText(Integer.toString(DTVPlayer.dtvplayer_pronumber)+"  "+DTVPlayer.dtvplayer_name);
		}
		else{
			if(DTVPlayer.dtvplayer_pronumber>=0)	
			Text_proname.setText(Integer.toString(DTVPlayer.dtvplayer_pronumber)+"-"+Integer.toString(DTVPlayer.dtvplayer_pronumber_minor)+"  "+DTVPlayer.dtvplayer_name);
		}
		//if(DTVPlayer.dtvplayer_pronumber>=0)	
			//Text_proname.setText(Integer.toString(DTVPlayer.dtvplayer_pronumber)+"  "+DTVPlayer.dtvplayer_name);

		if(DTVPlayer.dtvplayer_b_fav)
			ImageView_icon_fav.setVisibility(View.VISIBLE);
		else
			ImageView_icon_fav.setVisibility(View.INVISIBLE);

		if(DTVPlayer.dtvplayer_b_scrambled)
			ImageView_icon_scrambled.setVisibility(View.VISIBLE);
		else
			ImageView_icon_scrambled.setVisibility(View.INVISIBLE);

		if(DTVPlayer.dtvplayer_b_lock)
			ImageView_icon_lock.setVisibility(View.VISIBLE);
		else
			ImageView_icon_lock.setVisibility(View.INVISIBLE);

		if(DTVPlayer.dtvplayer_b_epg)
			ImageView_icon_epg.setVisibility(View.VISIBLE);
		else
			ImageView_icon_epg.setVisibility(View.INVISIBLE);

		if(DTVPlayer.dtvplyaer_b_txt)
			ImageView_icon_txt.setVisibility(View.VISIBLE);
		else
			ImageView_icon_txt.setVisibility(View.INVISIBLE);

		if(DTVPlayer.dtvplayer_b_sub)
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
			Text_MTS_info.setText(getString(R.string.stereo));			
		}
		else  if(mode==1){
			Text_MTS_info.setText(getString(R.string.left));			
		}
		else  if(mode==2){
			Text_MTS_info.setText(getString(R.string.right));				
		}	
		
		if(DTVGetSubtitleStatus()){
			Log.d(TAG,"subtitle is on");
			Text_parent_control_info_icon.setText("SUB:"+getString(R.string.on));	
		}
		else{
			Log.d(TAG,"subtitle is off");
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

	private Handler timeshiftingHandler = new Handler();
	private Runnable timeshiftingTimer = new Runnable() {
		public void run() {
			DTVPlaybackParams recPara = getPlaybackParams();
			if (recPara != null) {
				Log.d(TAG, "recPara: status("+recPara.getStatus()+
					"), time "+recPara.getCurrentTime()/1000+" / "+
					recPara.getTotalTime()/1000);
				statusChangeUpdate(recPara.getStatus());
				freshTimeAndSeekbar(recPara.getCurrentTime()/1000,recPara.getTotalTime()/1000);
			}
		
			timeshiftingHandler.postDelayed(this, 1000);
		}
	};

}

