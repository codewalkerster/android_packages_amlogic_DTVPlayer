package com.amlogic.DTVPlayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

public class DvbserviceNotifyReceiver extends BroadcastReceiver {  
	private DvbserviceNotifyReceiver receiver=null;
	static final String ACTION = "android.intent.action.pvr.notify"; 
	static final String ACTION1 = "android.intent.action.pvr.start";

	static final String ACTION_SUBSCRIBEPLAY_NOTIFY = "android.intent.action.subscribeplay.notify";
	static final String ACTION_SUBSCRIBEPLAY_START = "android.intent.action.subscribeplay.start";

	 
	 @Override  
	 public void onReceive(Context context, Intent intent) {
		
		if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
		{
			  //context.startService(new Intent("com.amlogic.dvb.DVB_SERVICE"));
		}
		else if (intent.getAction().equals(ACTION)){  	
			Bundle bundle_receive = intent.getExtras();         	      
			int record_db_id = bundle_receive.getInt("db_id");
			int srv_id = bundle_receive.getInt("srv_id");

			Intent Intent_progmanage = new Intent();
			ComponentName comp_progmanage = new ComponentName("com.amlogic.DTVPlayer","com.amlogic.DTVPlayer.PvrRecordDialog");
			Intent_progmanage.setComponent(comp_progmanage);
			Intent_progmanage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 

			Bundle bundle = new Bundle();
			bundle.putInt("db_id", record_db_id);	            	      
			bundle.putInt("srv_id",srv_id);

			Intent_progmanage.putExtras(bundle);

			context.startActivity(Intent_progmanage);			 
		}
		else if(intent.getAction().equals(ACTION1))
		{
			
		}
		else if(intent.getAction().equals(ACTION_SUBSCRIBEPLAY_NOTIFY))
		{
			Bundle bundle_receive = intent.getExtras();         	      
		    int db_evt_id = bundle_receive.getInt("evt_id");
		    Intent Intent_progmanage = new Intent();
			ComponentName comp_progmanage = new ComponentName("com.amlogic.DTVPlayer","com.amlogic.DTVPlayer.SubscribePlayDialog");
			Intent_progmanage.setComponent(comp_progmanage);
			Intent_progmanage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			Bundle bundle = new Bundle();
			bundle.putInt("evt_id", db_evt_id);	            	      
			Intent_progmanage.putExtras(bundle);
			context.startActivity(Intent_progmanage);
		}
		else if(intent.getAction().equals(ACTION_SUBSCRIBEPLAY_START))
		{
		    Intent Intent_dvbplayer = new Intent();
			ComponentName comp_dvbplayer = new ComponentName("com.amlogic.DTVPlayer","com.amlogic.DTVPlayer.DTVPlayer");
			Intent_dvbplayer.setComponent(comp_dvbplayer);
			Intent_dvbplayer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			context.startActivity(Intent_dvbplayer);
		}
	 }  
	  
	}
