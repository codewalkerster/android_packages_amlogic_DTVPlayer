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

import android.os.SystemProperties;

/**
 *DTV Activity
 */
abstract public class DTVActivity extends TVActivity{
    private static final String TAG="DTVActivity";
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
		SystemProperties.set("vplayer.hideStatusBar.enable", "true");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
		SystemProperties.set("vplayer.hideStatusBar.enable", "false");
        super.onDestroy();
    }	
}
	