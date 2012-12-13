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
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.text.*;
import android.text.method.*;

public class DTVTimeshifting extends DTVActivity{
	private static final String TAG="DTVTimeshifting";
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvtimeshiftplayer); 
		DTVTimeshiftingUIInit();
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

	private static final int STAT_PLAY= 1;
	private static final int STAT_PAUSE = 2;
	private static final int STAT_FF = 3;
	private static final int STAT_FB = 4;
	private int play_status = 0;
	private int speed = 0;
	private TextView cur_time;
	private  TextView total_time;
	private SeekBar myProgressBar;
	private int curtime;
	private int totaltime = 0;
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
	
	void DTVTimeshiftingUIInit(){
		//findViewById(R.id.RelativeLayout_video).setOnClickListener(new MouseClick());
		bufferLayout = (RelativeLayout)findViewById(R.id.bufferLayout);
		infoLayout = (RelativeLayout)findViewById(R.id.infobarLayout);
		TimeshiftingIcon = (ImageView) findViewById(R.id.ImageViewTimeshiftIcon);
		Timeshifting_icon_layout = (RelativeLayout)findViewById(R.id.RelativeLayoutTimeshiftIcon);
	
        more = (ImageButton)findViewById(R.id.moreBtn);
        play = (ImageButton)findViewById(R.id.PlayBtn);
        fastforword = (ImageButton)findViewById(R.id.FastForward);
        fastreverse = (ImageButton)findViewById(R.id.FastReverse);

        myProgressBar = (SeekBar)findViewById(R.id.SeekBar02);

        cur_time = (TextView)findViewById(R.id.TextView03);
        total_time = (TextView)findViewById(R.id.TextView04);
    	//cur_time.setText(secToTime(curtime, false));
    	//total_time.setText(secToTime(totaltime, true));
		
		play.setEnabled(false);
		myProgressBar.setEnabled(false);
		fastforword.setEnabled(false);
		fastreverse.setEnabled(false);
		
		fastforword.setBackgroundResource(R.drawable.ff_disable);
		fastreverse.setBackgroundResource(R.drawable.rewind_disable);
    	
		more.setOnClickListener(new Button.OnClickListener(){
		public void onClick(View v) {
			hideInforbar();
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
			
			DTVforwardTimeShifting(speed);
			
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
			

			DTVbackwardTimeShifting(speed);

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
					DTVpauseTimeShifting();
					play_status = STAT_PAUSE;
					play.setBackgroundResource(R.drawable.pause_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_pause);
				}
				else if (play_status == STAT_PAUSE)
				{	
					DTVresumeTimeShifting();
					play_status = STAT_PLAY;
					play.setBackgroundResource(R.drawable.play_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);
				}
				else if (play_status == STAT_FF)
				{
					DTVforwardTimeShifting(0);
					play_status = STAT_PLAY;
					speed = 0;
					play.setBackgroundResource(R.drawable.play_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);

				}
				else if (play_status == STAT_FB)
				{
					DTVbackwardTimeShifting(0);
					play_status = STAT_PLAY;
					speed = 0;
					play.setBackgroundResource(R.drawable.play_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);

				}
				else
				{
					DTVplayTimeShifting();
					play_status = STAT_PLAY;
					play.setBackgroundResource(R.drawable.play_button);
					TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);

				}
			}
        });
                
 
        if (curtime != 0)
        	myProgressBar.setProgress(curtime*100/totaltime/1000);
		
        myProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			public void onStopTrackingTouch(SeekBar seekBar) 
			{
				// TODO Auto-generated method stub
				int dest = myProgressBar.getProgress();
				int pos = totaltime * dest / 100;
				
				DTVseekTimeShifting(pos);

				myProgressBar_pos = curtime*100/totaltime/1000;
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) 
			{
				// TODO Auto-generated method stub
				SeekSliding = true;
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
				// TODO Auto-generated method stub
				
			}
		});
	     
	    infoLayout.setVisibility(View.INVISIBLE);
		
 
	}

	private void showTimeshiftDialog(){

		AlertDialog.Builder builder = new AlertDialog.Builder(DTVTimeshifting.this); 
			builder.setMessage(R.string.timeshifting_exit_message)
			.setCancelable(false)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id) {
					DTVstopTimeShifting();
					finish();	
					dialog.dismiss();
				}        
			 })        
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();            
					}        
			 }); 
			AlertDialog alert = builder.create();
			alert.show();

			WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
			lp.dimAmount=0.0f;
			alert.getWindow().setAttributes(lp);
			alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}

	

	private void showSubtitleLanguageDialog(int db_id)	{
		
						
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
   		//showInforbar();
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if(teletext_bar_flag == false)
				{
					showInforbar();
				}	
				break;
			case KeyEvent.KEYCODE_BACK:
				 if(teletext_bar_flag)
				{				
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
			case KeyEvent.KEYCODE_ZOOM_IN:	
			
				
				return true;	
			   case KeyEvent.KEYCODE_ZOOM_OUT:
				
				return true;
			case KeyEvent.KEYCODE_TAB: //info
				if(teletext_bar_flag){
				}
				return true;	
			case KeyEvent.KEYCODE_TV_SUBTITLE:
				
				break;
			case KeyEvent.KEYCODE_TV_SHORTCUTKEY_VOICEMODE:
				break;		

		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			play.requestFocus();
			
			if (play_status == STAT_PLAY)
			{
				DTVpauseTimeShifting();
				play_status = STAT_PAUSE;
				play.setBackgroundResource(R.drawable.pause_button);
				TimeshiftingIcon.setImageResource(R.drawable.timeshifting_pause);
			}
			else if (play_status == STAT_PAUSE)
			{	
				DTVresumeTimeShifting();
				play_status = STAT_PLAY;
				play.setBackgroundResource(R.drawable.play_button);
				TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);
			}
			else if (play_status == STAT_FF)
			{
				DTVforwardTimeShifting(0);
				play_status = STAT_PLAY;
				speed = 0;
				play.setBackgroundResource(R.drawable.play_button);
				TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);

			}
			else if (play_status == STAT_FB)
			{
				DTVbackwardTimeShifting(0);
				play_status = STAT_PLAY;
				speed = 0;
				play.setBackgroundResource(R.drawable.play_button);
				TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);

			}
			else
			{
				DTVplayTimeShifting();
				play_status = STAT_PLAY;
				play.setBackgroundResource(R.drawable.play_button);
				TimeshiftingIcon.setImageResource(R.drawable.timeshifting_icon);

			}
			return true;
		case KeyEvent.KEYCODE_MEDIA_REWIND:
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS: //pre/next
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
			

			DTVbackwardTimeShifting(speed);

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
		case KeyEvent.KEYCODE_MEDIA_NEXT:
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
			
			DTVforwardTimeShifting(speed);

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
			

		}
		
		return super.onKeyDown(keyCode, event);
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


	private boolean inforbar_show_flag=true;
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

}

