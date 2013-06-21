package com.amlogic.DTVPlayer;

import java.util.Calendar;
import android.content.ComponentName;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.util.Log;


import android.os.PowerManager;



public class AlarmReceiver extends BroadcastReceiver {
	private static PowerManager.WakeLock wakeLock;
	@Override
	public void onReceive(Context context, Intent arg1) {
		//DVBControl mControl = new DVBControl("/sys/power/state");
			//mControl.setValue("on");
		//Toast.makeText(context, "PVR or booked play will start,please wait...", Toast.LENGTH_SHORT).show();
		Log.d("AlarmReceiver","PVR or booked play will start,please wait..");

		this.acquire(context);
		
	}

	public static void acquire(Context ctx) {
        if (wakeLock != null) wakeLock.release();

        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "AlarmReceiver");
        wakeLock.acquire();
    }

    public static void release() {
        if (wakeLock != null) wakeLock.release(); wakeLock = null;
    }

}

