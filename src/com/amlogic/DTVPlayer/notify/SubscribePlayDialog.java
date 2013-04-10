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

public class SubscribePlayDialog extends DTVActivity {

	private final String TAG = "PvrRecordDialog";
	private AlertDialog.Builder builder;
	private int event_db_id=-1;

	View view = null;
	int time=60;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = this.getIntent().getExtras();
		if(bundle!=null)
		{	
			event_db_id = bundle.getInt("evt_id");		
			Log.d(TAG,"event_db_id="+event_db_id);
		}	

		view = getLayoutInflater().inflate(R.layout.pvr_record_dialog, null);

		showPvrRecordDialog();

		timer_handler.postDelayed(timer_runnable, 1000);  
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


	@Override
	protected void onDestroy(){
		super.onDestroy();
		timer_handler.removeCallbacks(timer_runnable);	
	}
	
	private Handler timer_handler = new Handler();   
	private Runnable timer_runnable = new Runnable() {
	public void run() {
			TextView time_view = (TextView)view.findViewById(R.id.timer);
	
			if(time>0)
			{
		    	time_view.setText(Integer.toString(time));
				time--;
			}
			else
			{
				finish(); 
			}

			timer_handler.postDelayed(this,1000);  
		}   
	};	


	private void DealIgnore()
	{
		update_bookstatus(event_db_id,0);
	}

	private void deletePvrData(int db_id)
	{
		/*
		this.getContentResolver().delete(DVBClient.TABLE_RECORD ,"db_id="+db_id,null); 
		mDvb.syncDatabase(DVBClient.TABLE_RECORD,-1);
		Log.d(TAG,"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+db_id);
		*/
	}

	private void update_bookstatus(int eventid, int enablestate)
	{
		/*
		ContentValues cv = null;

		if (enablestate == 1)
		{
			cv = new ContentValues();	
			cv.put("sub_flag", 1);
			this.getContentResolver().update(DVBClient.TABLE_EVENT, cv, "db_id= "+eventid, null); 
		}
		else
		{
			cv = new ContentValues();	
			cv.put("sub_flag", 0);
			cv.put("sub_status", 0);
			this.getContentResolver().update(DVBClient.TABLE_EVENT, cv, "db_id= "+eventid, null); 
		}

		mDvb.syncDatabase(DVBClient.TABLE_SERVICE, eventid);
		*/
	}


	public void showPvrRecordDialog()
	{
		time=60;
		
		builder = new AlertDialog.Builder(this);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(SubscribePlayDialog.this); 


		builder.setView(view);	

		
		//builder.setMessage(R.string.subscribe_play_dialog)
		//.setCancelable(false)
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				finish();
			}        
		 })        
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					DealIgnore();
					dialog.cancel();  
					finish();
				}        
		 }); 
		 
		AlertDialog alert = builder.create();
		alert.show();	

		alert.getWindow().setLayout(500, 500);
		
		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
	               lp.dimAmount=0.0f;
	               alert.getWindow().setAttributes(lp);
	               alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}


 
}
