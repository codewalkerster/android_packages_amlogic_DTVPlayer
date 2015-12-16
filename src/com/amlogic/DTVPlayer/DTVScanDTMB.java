package com.amlogic.DTVPlayer;

import android.os.Bundle;
import android.util.Log;
import com.amlogic.tvutil.TVMessage;

public class DTVScanDTMB extends DTVActivity{
	private static final String TAG="DTVScanDTMB";
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");

		super.onCreate(savedInstanceState);

		setContentView(R.layout.test);

	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
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
}

