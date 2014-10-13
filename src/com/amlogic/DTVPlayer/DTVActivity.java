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
import com.amlogic.tvutil.TVGroup;
import com.amlogic.tvutil.TVBooking;
import com.amlogic.tvutil.DTVPlaybackParams;
import com.amlogic.tvutil.DTVRecordParams;
import com.amlogic.widget.CheckUsbdevice;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.util.*;
import java.text.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.app.*;
import android.app.AlertDialog.*;
import android.content.*;
import android.graphics.*;
import android.view.ViewGroup.*;
import android.text.*;
import android.text.method.*;
import android.database.*;
import android.os.SystemProperties;
import android.os.*;
import java.lang.reflect.Field;



/**
 *DTV Activity
 */
abstract public class DTVActivity extends TVActivity{
    private static final String TAG="DTVActivity";

	final public static int KEYCODE_TTX=KeyEvent.KEYCODE_ZOOM_IN;
	
	final public static int KEYCODE_RED_BUTTON=KeyEvent.KEYCODE_C;
	final public static int KEYCODE_YELLOW_BUTTON=KeyEvent.KEYCODE_D;
	final public static int KEYCODE_BLUE_BUTTON=KeyEvent.KEYCODE_F3;
	final public static int KEYCODE_GREEN_BUTTON=KeyEvent.KEYCODE_F4;
	
	final public static int KEYCODE_GOTO_BUTTON=KeyEvent.KEYCODE_MEDIA_REWIND;
	final public static int KEYCODE_REC=KeyEvent.KEYCODE_ZOOM_OUT;
	
	final public static int KEYCODE_TIMESHIFTING=KeyEvent.KEYCODE_F8;
	final public static int KEYCODE_EPG=KeyEvent.KEYCODE_TV_SWITCH;
	
	final public static int KEYCODE_AUDIO_TRACK=KeyEvent.KEYCODE_MEDIA_NEXT;
	final public static int KEYCODE_AUDIO_LANGUAGE=KeyEvent.KEYCODE_TV_SHORTCUTKEY_VOICEMODE;
	
	final public static int KEYCODE_SUBTITLE=KeyEvent.KEYCODE_TV_SUBTITLE;
	final public static int KEYCODE_SUBTITLE_SWITCH=KeyEvent.KEYCODE_TV_REPEAT;	
	final public static int KEYCODE_RECALL_BUTTON=KeyEvent.KEYCODE_TV_SHORTCUTKEY_DISPAYMODE;
	final public static int KEYCODE_INFO=KeyEvent.KEYCODE_TAB;
	final public static int KEYCODE_FAV_BUTTON=KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;

	final public static int KEYCODE_AD_BUTTION=KeyEvent.KEYCODE_F5;
	final public static int KEYCODE_AD_VOLUME_UP = KeyEvent.KEYCODE_F6;
	final public static int KEYCODE_AD_VOLUME_DOWN = KeyEvent.KEYCODE_F7;
	

	private TVProgram TVProgram=null;
	private static int dtvactivity_actived_num = 0;
	//private static int dtvlayout_gravity = Gravity.CENTER;
	public boolean connected = false;
	private boolean delay_setinput_source = false;
	
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Context otherAppsContext = null;
		try{
			otherAppsContext = createPackageContext(
			"com.amlogic.DTVPlayer", Context.MODE_WORLD_WRITEABLE|Context.MODE_WORLD_READABLE);
		}
		catch (NameNotFoundException e){
		}
		mLast= PreferenceManager.getDefaultSharedPreferences(otherAppsContext);
		mLast= PreferenceManager.getDefaultSharedPreferences(otherAppsContext);

		this.getApplicationContext().addActivity(this);
	
    }

	public DTVPlayerApp getApplicationContext() {
		return ((DTVPlayerApp) super.getApplicationContext());
	}



	DTVSettings mDTVSettings=null;
	public void onConnected(){
		Log.d(TAG, "connected");
		connected = true;
		if(delay_setinput_source){
			delay_setinput_source = false;			
			setInputSource(TVConst.SourceInput.SOURCE_DTV);
		}
		mDTVSettings = new DTVSettings(this); 
		mDTVSettings.setTeltextBound();
	}

	public void onDisconnected(){
		Log.d(TAG, "disconnected");
		connected = false;
		delay_setinput_source = false;
	}	

	public void onMessage(TVMessage msg){
		Log.d(TAG, "message "+msg.getType());
		onDialogStatusRecord(msg);
	}

	public void onDialogStatusChanged(int status){
	}
	public final int STATUS_SIGNAL=0;
	public final int STATUS_SCRAMBLED=1;
	public final int STATUS_DATA=2;
	public final int STATUS_LOCKED=3;

	public static boolean signal=true;
	public static boolean scrambled=false;
	public static boolean has_data=true;
	public static boolean locked=false;

	public void RecordStatus(int status,boolean value){
		switch(status){
			case STATUS_SIGNAL:
				if(signal!=value){
					signal=value;
					onDialogStatusChanged(STATUS_SIGNAL);
				}
				break;
			case STATUS_SCRAMBLED:
				if(scrambled!=value){
					scrambled=value;
					onDialogStatusChanged(STATUS_SCRAMBLED);
				}	
				break;
			case STATUS_DATA:
				if(has_data!=value){
					has_data=value;
					onDialogStatusChanged(STATUS_DATA);
				}	
				break;
			case STATUS_LOCKED:
				if(locked!=value){
					locked=value;
					onDialogStatusChanged(STATUS_LOCKED);
				}

				break;
		}
	}
	
	public boolean getDTVSignalStatus(){
		return signal;
	}

	public boolean getDTVLockedStatus(){
		return locked;
	}
	
	public boolean getDTVScrambledStatus(){
		return scrambled;
	}

	public boolean getDTVAVDataStatus(){
		return has_data;
	}

	private void onDialogStatusRecord(TVMessage msg){
		switch(msg.getType()) {
			case TVMessage.TYPE_PROGRAM_BLOCK:
					Log.d(TAG,"BLOCK");
					
					switch(msg.getProgramBlockType()){
						case TVMessage.BLOCK_BY_LOCK:
							break;
						case TVMessage.BLOCK_BY_PARENTAL_CONTROL:
							break;
						case TVMessage.BLOCK_BY_VCHIP:
							break;
					}					
					mDTVSettings.setCheckProgramLock(true);
					RecordStatus(STATUS_LOCKED,true);
					break;
				case TVMessage.TYPE_PROGRAM_UNBLOCK:	
					RecordStatus(STATUS_LOCKED,false);
					mDTVSettings.setCheckProgramLock(false);
					break;
				case TVMessage.TYPE_SIGNAL_LOST:
					RecordStatus(STATUS_SIGNAL,false);
					break;
				case TVMessage.TYPE_SIGNAL_RESUME:
					RecordStatus(STATUS_SIGNAL,true);
					RecordStatus(STATUS_DATA,true);
					break;	
				case TVMessage.TYPE_DATA_LOST:
					RecordStatus(STATUS_DATA,false);
					break;
				case TVMessage.TYPE_DATA_RESUME:
					RecordStatus(STATUS_DATA,true);
					RecordStatus(STATUS_SCRAMBLED,false);
					break;
				case TVMessage.TYPE_PROGRAM_SWITCH:
					RecordStatus(STATUS_DATA,true);
					RecordStatus(STATUS_LOCKED,false);
					RecordStatus(STATUS_SCRAMBLED,false);
					mDTVSettings.setCheckProgramLock(false);
					break;
				case TVMessage.TYPE_PROGRAM_SCRAMBLED:
					RecordStatus(STATUS_SCRAMBLED,true);
					break;
			}

	}

	@Override
	public void setContentView (int layoutResID){
		//super.setContentView(layoutResID);
		setContentViewdtv(layoutResID);
	}
	private void setContentViewdtv (int layoutResID){
		/*reset layout by reproduction rate*/
		String outputmode = SystemProperties.get("ubootenv.var.outputmode");
		int x = 0, y = 0, w = 0, h = 0;
		String x_s = null, y_s = null, w_s = null, h_s = null;
		String not_set = "";
		
		Log.d(TAG, "setContentView " + outputmode);
		
		if(outputmode.contains("1080p") == true){
			x_s = SystemProperties.get("ubootenv.var.1080poutputx");
			y_s = SystemProperties.get("ubootenv.var.1080poutputy");
			w_s = SystemProperties.get("ubootenv.var.1080poutputwidth");
			h_s = SystemProperties.get("ubootenv.var.1080poutputheight");
		}
		else if(outputmode.contains("1080i") == true){
			x_s = SystemProperties.get("ubootenv.var.1080ioutputx");
			y_s = SystemProperties.get("ubootenv.var.1080ioutputy");
			w_s = SystemProperties.get("ubootenv.var.1080ioutputwidth");
			h_s = SystemProperties.get("ubootenv.var.1080ioutputheight");		
		}
		else if(outputmode.contains("720p") == true){
			x_s = SystemProperties.get("ubootenv.var.720poutputx");
			y_s = SystemProperties.get("ubootenv.var.720poutputy");
			w_s = SystemProperties.get("ubootenv.var.720poutputwidth");
			h_s = SystemProperties.get("ubootenv.var.720poutputheight");		
		}
		else if(outputmode.contains("576p") == true){
			x_s = SystemProperties.get("ubootenv.var.576poutputx");
			y_s = SystemProperties.get("ubootenv.var.576poutputy");
			w_s = SystemProperties.get("ubootenv.var.576poutputwidth");
			h_s = SystemProperties.get("ubootenv.var.576poutputheight");			
		}
		else if(outputmode.contains("576i") == true){
			x_s = SystemProperties.get("ubootenv.var.576ioutputx");
			y_s = SystemProperties.get("ubootenv.var.576ioutputy");
			w_s = SystemProperties.get("ubootenv.var.576ioutputwidth");
			h_s = SystemProperties.get("ubootenv.var.576ioutputheight");					
		}
		else if(outputmode.contains("480p") == true){
			x_s = SystemProperties.get("ubootenv.var.480poutputx");
			y_s = SystemProperties.get("ubootenv.var.480poutputy");
			w_s = SystemProperties.get("ubootenv.var.480poutputwidth");
			h_s = SystemProperties.get("ubootenv.var.480poutputheight");			
		}
		else if(outputmode.contains("480i") == true){
			x_s = SystemProperties.get("ubootenv.var.480ioutputx");
			y_s = SystemProperties.get("ubootenv.var.480ioutputy");
			w_s = SystemProperties.get("ubootenv.var.480ioutputwidth");
			h_s = SystemProperties.get("ubootenv.var.480ioutputheight");				
		}

		if((x_s != null) && (y_s != null) && (w_s != null) && (h_s != null)){
			if((x_s.equals("")) || (y_s.equals("")) || (w_s.equals("")) ||(h_s.equals(""))){
				Log.e(TAG, "not set reproduction");
			} else {
				x = Integer.parseInt(x_s);
				y = Integer.parseInt(y_s);
				w = Integer.parseInt(w_s);
				h = Integer.parseInt(h_s);

				if(outputmode.contains("1080p") == true){
					x=x*1280/1920;
					y=y*720/1080;
					w=w*1280/1920;
					h=h*720/1080;
				}
				else if(outputmode.contains("1080i") == true){
					x=x*1280/1920;
					y=y*720/1080;
					w=w*1280/1920;
					h=h*720/1080;	
				}
				else if(outputmode.contains("720p") == true){
					
				}
				else if(outputmode.contains("576p") == true){
					x=x*1280/720;
					y=y*720/576;
					w=w*1280/720;
					h=h*720/576;			
				}
				else if(outputmode.contains("576i") == true){
					x=x*1280/720;
					y=y*720/576;
					w=w*1280/720;
					h=h*720/576;				
				}
				else if(outputmode.contains("480p") == true){
					x=x*1280/720;
					y=y*720/480;
					w=w*1280/720;
					h=h*720/480;			
				}
				else if(outputmode.contains("480i") == true){
					x=x*1280/1920;
					y=y*720/1080;
					w=w*1280/1920;
					h=h*720/1080;			
				}
	
			}
		}


		
/*
		AbsoluteLayout root = new AbsoluteLayout(this);

		if(x == 0 && y == 0 && w == 0 && h == 0){
			Log.e(TAG, "screen error");
		}
		else{
			LayoutParams params = new AbsoluteLayout.LayoutParams(w, h, x, y);
			root.setLayoutParams(params);
		}

		LayoutInflater inflater = LayoutInflater.from(this);
		View reproduction_view = inflater.inflate(layoutResID, null); 

		if(x == 0 && y == 0 && w == 0 && h == 0){
			Log.e(TAG, "reproduction error");
		}
		else{
			LayoutParams reproduction_params = new AbsoluteLayout.LayoutParams(w, h, x, y);
			
			root.addView(reproduction_view);

			super.setContentView(root, reproduction_params);	
			
			Log.d(TAG, "DTVActivity_ResetLayoutbyReprodRate " + x + " " + y + " " + w + " " + h);
		}
*/		


		AbsoluteLayout root1 = new AbsoluteLayout(this);
		RelativeLayout root2 = new RelativeLayout(this);
		
		LayoutParams params1 = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		//LayoutParams params1 = new LayoutParams(1080, 720);
		//root1.setLayoutParams(params1);
		
		LayoutParams params2 = new LayoutParams(w, h);
		//root2.setLayoutParams(params2);
		//root2.setGravity(dtvlayout_gravity); 

		LayoutInflater inflater = LayoutInflater.from(this);
		View reproduction_view = inflater.inflate(layoutResID, null); 

		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		//Log.d(TAG, "########## w=" + params.width + "h=" + params.height);
		
		x=0;
		y=0;
		w=0;
		h=0;
		
		if(x == 0 && y == 0 && w == 0 && h == 0){
			Log.e(TAG, "reproduction error");
			super.setContentView(layoutResID);
		}
		else{

			super.setContentView(root1, params1);

			root1.addView(root2, params2);
			
			root2.setX(x);
			root2.setY(y);
			
			root2.addView(reproduction_view, params);

			/*
			AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams)root.getLayoutParams();
			params.x = x;
			params.y = y;
			params.width = w;
			params.height = h;
			root.setLayoutParams(params);
			*/
			
			Log.d(TAG, "DTVActivity_ResetLayoutbyReprodRate " + x + " " + y + " " + w + " " + h);
		}

			
	}	
	
	@Override
	protected void onStart(){
		Log.d(TAG, "onStart");
		DTVActivity_ActivedStateManage(true);
		super.onStart();
		
	}

	@Override
	protected void onStop(){
		Log.d(TAG, "onStop");
		DTVActivity_ActivedStateManage(false);
		super.onStop();
	}

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		// TODO Auto-generated method stub		
		return super.onKeyDown(keyCode, event);
	}


	private void DTVActivity_ActivedStateManage(boolean actived){
		Log.d(TAG, "DTVActivity_ActivedStateManage actived " + actived + " dtvactivity_actived_num " + dtvactivity_actived_num);
		if(actived){
			if(dtvactivity_actived_num == 0){
				/*stop music play when dtv actived*/
				Intent stopMusicIntent = new Intent();
				stopMusicIntent.setAction("com.android.music.musicservicecommand.pause");
				stopMusicIntent.putExtra("command", "stop");
				sendBroadcast(stopMusicIntent);

				Intent exitMusicIntent = new Intent();
				exitMusicIntent.setAction("com.android.music.musicservicecommand.pause");
				exitMusicIntent.putExtra("command", "exit");
				sendBroadcast(exitMusicIntent);

				/*set vpath*/
				if(connected){
					setInputSource(TVConst.SourceInput.SOURCE_DTV);
				}
				else{
					delay_setinput_source = true;
				}

				Log.d(TAG, "DTVActivity_ActivedStateManage actived stop music set vpath");
			}

			dtvactivity_actived_num++;
		}
		else{
			dtvactivity_actived_num--;

			if(dtvactivity_actived_num == 0){
				/*reset vpath, this is borrow SOURCE_ATV parameter*/
				setInputSource(TVConst.SourceInput.SOURCE_ATV);
			}			
		}
	}
	
	/***************DTVPlayer****************/
	public String secToTime(long i, Boolean isTotalTime){
		String retStr = null;
		long hour = 0;
		long minute = 0;
		long second = 0;
		if (i <= 0){
			if (isTotalTime && i<0)
				return "99:59:59";
			else
				return "00:00:00";
		}
		else{
			minute = i/60;
			if (minute < 60){
				second = i%60;
				retStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
			}
			else{
				hour = minute/60;
				if (hour > 99)
					return "99:59:59";
				minute = minute%60;
				second = i%60;
				retStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
			}
		}
		return retStr;
	}
    
    public String unitFormat(long i){
		String retStr = null;
		if (i >= 0 && i < 10)
			retStr = "0" + Long.toString(i);
		else
			retStr = Long.toString(i);
		return retStr;
    }

	public long DTVPlayerGetRecordDuration(){
		DTVRecordParams recPara = getRecordingParams();
		if (recPara != null) {
			//Log.d(TAG, "Record: time "+recPara.getCurrentRecordTime()/1000+" / "+
				//recPara.getTotalRecordTime()/1000+", size "+recPara.getCurrentRecordSize());
			return recPara.getCurrentRecordTime()/1000;
		} else {
			//Log.d(TAG, "Cannot get record params");
		}
		return 0;
	}

	public boolean DTVPlayerIsRecording(){
		DTVRecordParams recPara = getRecordingParams();
		if (recPara != null) 
			return true;
		return false;
	}

	public void DTVPlayerStartRecording(long duration){
		startRecording(duration);
	}

	public void DTVPlayerStartRecordingWithTime(long start,long duration){
		TVProgram mTVProgram = DTVPlayerGetDataByCurrentID();
			if(mTVProgram!=null)
				{
			try{
				TVBooking.bookProgram(this, mTVProgram, TVBooking.FL_RECORD, start,duration, TVBooking.RP_NONE, true);
			}catch(Exception e){
			}
		}
	}

	public void DTVPlayerAddBook(int id,int mode, long start,long duration,int repeat){
		//TVProgram mTVProgram = DTVPlayerGetDataByCurrentID();
		
		TVProgram mTVProgram = TVProgram.selectByID(this,id);
		
		int m_mode=0;
		int m_repeat=0;
			
		if(mTVProgram!=null)
		{
			if(mode==0)
				m_mode=TVBooking.FL_PLAY;
			else
				m_mode=TVBooking.FL_RECORD;

			if(repeat==0)
				m_repeat=TVBooking.RP_NONE;
			else if(repeat==1)
				m_repeat=TVBooking.RP_DAILY;
			else if(repeat==2)
				m_repeat=TVBooking.RP_WEEKLY;

			try{
				//Log.d(TAG,"m_mode="+m_mode+",start="+start+",duration="+duration+",m_repeat="+m_repeat);
				TVBooking.bookProgram(this, mTVProgram, m_mode, start,duration, m_repeat, true);
				
			}catch(Exception e){
			}
		}
	}

	public void DTVPlayerAddEvent(int id,int flag,int repeat){
		TVEvent mTVEvent = TVEvent.selectByID(this,id);
		
		int mFlag = 0;
		int m_repeat=0;
		
		if(mTVEvent!=null)
		{
			if(flag==1)
				mFlag=TVBooking.FL_PLAY;
			else if(flag==2)
				mFlag=TVBooking.FL_RECORD;
			
			if(repeat==0)
				m_repeat=TVBooking.RP_NONE;
			else if(repeat==1)
				m_repeat=TVBooking.RP_DAILY;
			else if(repeat==2)
				m_repeat=TVBooking.RP_WEEKLY;


			try{
				
				TVBooking.bookEvent(this, mTVEvent, mFlag ,repeat,true);
				
			}catch(Exception e){
			}
		}
	}
	
	public void DTVPlayerStopRecording(){
		stopRecording();
	}

	public void DTVPlayerPlayCurrentProgram(){

	}

	public void DTVPlayerPlayByProNo(int pronumber){
		TVProgramNumber mTVProgramNumber = new TVProgramNumber(pronumber);
		playProgram(mTVProgramNumber);
	}

	public void DTVPlayerPlayAtscByProNo(int major, int minor){
		TVProgramNumber mTVProgramNumber = new TVProgramNumber(major,minor);
		playProgram(mTVProgramNumber);
	}
	
	public void DTVPlayerPlayById(int db_id){
		playProgram(db_id);
		//DTVPlayerSetRecallList(db_id);
	}

	public void DTVPlayerPlayDown(){
		channelDown();
	}

	public void DTVPlayerPlayUp(){
		channelUp();
	}

	public void DTVPlayerStopPlay(){
		stopPlaying();
	}

	public boolean DTVPlayerSetFav(boolean f){
		int db_id=-1;
		db_id=DTVPlayerGetCurrentProgramID();
		TVProgram mTVProgram = TVProgram.selectByID(this,db_id);
		if(mTVProgram.getFavoriteFlag()!=f){
			mTVProgram.setFavoriteFlag(f);
			return true;
		}	
		else
			return false;
	}

	/*************TeleText**************/

	public void DTVTTShow(){
		Log.d(TAG,"DTVTTShow");
		ttShow();
	}

	public void DTVTTHide(){
		ttHide();
	}

	public void DTVTTGotoNextPage() {
        ttGotoNextPage();
    }
	
    public void DTVTTGotoPreviousPage() {
      ttGotoPreviousPage();
    }
	
    public void DTVTTGotoPage(int page) {
        ttGotoPage(page);
    }

    public void DTVTTGoHome() {
        ttGoHome();
    }
	
    public void DTVTTGotoColorLink(int color) {
        ttGotoColorLink(color);
    }


	/**********Timeshifting*************/
	public void DTVTimeShiftingForward(int speed){
		fastForward(speed);
	}

	public void DTVTimeShiftingBackward(int speed){
		fastBackward(speed);
	}

	public void DTVTimeShiftingPause(){
		pause();
	}

	public void DTVTimeShiftingResume(){
		resume();
	}
	
	public void DTVTimeShiftingSeek(int pos){
		seekTo(pos);
	}

	public void DTVTimeShiftingPlay(){
		
	}
	public void DTVTimeShiftingStop(){
		stopTimeshifting();
	}

	public void DTVRecordingStop(){
		stopRecording();
	}
	
	public void DTVSubtitleStop(){}
	public void DTVTeletextStop(){}

	/*REC pvr Player*/
	public void DTVPvrPlayerStop(){
		stopPlayback();
	}

	/*DTVProgramManager*/
	public void DTVProgramManagerDeletePro(int id){
		
	}

	public void DTVProgramManagerEditPro(int id){
		
	}

	public void DTVProgramManagerAddLock(int id){}

	public void DTVProgramManagerSkipPro(int id,boolean f){}
	public void DTVProgramManagerFavSet(int id,boolean f){}
	public void DTVProgramManagerAddGroup(String name){
		TVGroup.addGroup(this,name);
	}

	public void DTVProgramManagerDeleteGroup(int id){
		TVGroup.deleteGroup(this,id);
	}
	
	public void DTVProgramManagerEditGroupName(int id,String name){
		TVGroup.editGroup(this,id,name);
	}
	
	public TVGroup[] DTVProgramManagerGetGroupList(){
		TVGroup[] mProgramGroup=null;
		mProgramGroup = TVGroup.selectByGroup(this,true);
		return mProgramGroup;
	}

	public TVProgram[] DTVProgramManagerGetFav(){
		TVProgram[] mTVProgram=null;
		mTVProgram = TVProgram.selectByFavorite(this,true);
		return mTVProgram;
	}
	
	public TVProgram[] DTVProgramManagerGetProByGroup(int id){
		TVProgram[] mTVProgram=null;
		mTVProgram = TVProgram.selectByGroupMap(this,id,true);
		return mTVProgram;
	}

	/***********DTV EPG****************/
	public int DTVEpgGetID(){
		return getCurrentProgramID();
	}

	public String DTVEpg_get_currentprogname(){
		String value = " ";
		TVProgram mTVProgram=TVProgram.selectByID(this,DTVEpgGetID());
		value = mTVProgram.getName();
		return value; 
	}

	public TVEvent[] DTVEpg_getDateEIT(long start,long duration){

		TVProgram mTVProgram=TVProgram.selectByID(this,DTVEpgGetID());
		if(mTVProgram!=null)
			return  mTVProgram.getScheduleEvents(this,start,duration);
		else
			return null;
	}

	public String DTVEpg_get_detailinfo(int srv_id){
		String value = " ";
		TVEvent mTVEvent=TVEvent.selectByID(this,srv_id);
		value = mTVEvent.getEventDescr();
		return value; 
	}

	public void DTVEpg_updateEvent(int db_id){
	}

	public void DTVEpg_deleteRec(int eventid){
	}


	public void DTVEpg_subscribeEPGEvent(int eventid){

	}

	public void DTVEpg_recordByEventID(int eventid){

	}

	/*********settings************/
	
	public boolean DTVGetSubtitleStatus(){
		return getBooleanConfig("tv:subtitle:enable");
	}

	public void DTVSetSubtitleStatus(boolean value){
		setConfig("tv:subtitle:enable",value);
	}

	public boolean DTVGetATSCAtvEnable(){
		return getBooleanConfig("tv:atv:enable");
	}

	public int DTVGetScreenMode(){
		int mode = 0;
		if(mLast!=null)
			mode = mLast.getInt("screen_mode",1);
		return mode;
		//return getScreenType();
	}

	public void DTVSaveNitVersion(int v){
		if(mLast!=null)
			mLast.edit().putInt("nit_version",v).commit();
	}
	
	public int DTVgetNitVersion(){
		int v = -1;
		if(mLast!=null)
			v = mLast.getInt("nit_version",-1);
		return v;
		
	}

	public void DTVSetScreenMode(int mode){
		if(mLast!=null)
			mLast.edit().putInt("screen_mode",mode).commit();
		switchScreenType(mode);
	}

	public int DTVGetAudioTrack(){
		return getAudioTrack();
	}

	public void DTVSetAudioTrack(int mode){
		switchAudioTrack(mode);
	}

	public int DTVGetTimeShiftingDuration(){
		return 600;
	}

	public void  DTVSetTimeShiftingDuration(int value){
	}
	
	public int DTVGetParentalRating(){
		return 0;
	}

	public void DTVSetParentalRating(int value){
		return;
	}

	public int DTVGetTeletextRegion(){
		return 0;
	}

	public void DTVSetTeletextRegion(int value){

	}

	public int DTVPlayerGetCurrentProgramID(){
		return getCurrentProgramID();
	}

	public int DTVPlayerGetCurrentProgramType(){
		return getCurrentProgramType();
	}
	
	public TVProgram DTVPlayerGetDataByCurrentID(){
		int db_id=-1;
		db_id=DTVPlayerGetCurrentProgramID();
		Log.d(TAG,"current id="+db_id);
	
		return TVProgram.selectByID(this,db_id);
	}

	private static  SharedPreferences mLast = null;
	public void DTVPlayerSetRecallNumber(int value){
		if(mLast!=null)
			mLast.edit().putInt("recall_number",value).commit();
	}	

	public int DTVPlayergetRecallNumber(){
		int value = 1;
		if(mLast!=null)
			value = mLast.getInt("recall_number",1);
		return value;
	}	

	private ArrayList<Integer> recall_list = null;
	public void DTVPlayerSetRecallList(int id){
		if(recall_list==null){
			recall_list = new ArrayList<Integer>();
			recall_list.add(id);
		}
		else{
	
			int size = DTVPlayergetRecallNumber();

			while(recall_list.size()>=size+1){
				recall_list.remove(0);
			}

			boolean del=false;
			int pos = 0;
			for(int i=0;i<recall_list.size();i++){
				if(recall_list.get(i) == id){
					pos = i;
					del=true;
					break;
				}	
			}
			
			if(del)
				recall_list.remove(pos);
			
			recall_list.add(id);
		}
	}

	public TVProgram[] DTVPlayerGetRecallList(){
		if(recall_list==null){
			return null;
		}
		else{
			int i=0;
			for(i=0;i<recall_list.size();i++){
				Log.d(TAG,"--"+recall_list.get(i));
			}
		}

		TVProgram[] mTVProgram = null;
		TVProgram mTemp = null;
		int RecallNumber = DTVPlayergetRecallNumber();
		int len = recall_list.size();
	
		if(RecallNumber==1){
			mTemp = TVProgram.selectByID(this,recall_list.get(0));
			if(mTemp!=null){
				mTVProgram = new TVProgram[1];
				mTVProgram[0]= mTemp;
			}
		}	
		else if(recall_list.size() <= RecallNumber+1) {
			int pos = 0;
			if(recall_list.size()<=1){
				pos = 0;
			}
			else{
				pos = recall_list.size()-2;
			}
			int n=0;
			for(int i=pos;i>=0;i--){
				mTemp = TVProgram.selectByID(this,recall_list.get(i));
				if(mTemp!=null)
					n++;
			}
			mTVProgram = new TVProgram[n];
			n=0;
			for(int i=pos;i>=0;i--){
				
				mTemp = TVProgram.selectByID(this,recall_list.get(i));
				if(mTemp!=null){
					mTVProgram[n]=mTemp;
					n++;
				}	
			}
		}
		
		return mTVProgram;
	}


	public boolean isHavePragram(){
		TVProgram[]  mTVProgramList=null;
		mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_TV,0);
		if(mTVProgramList!=null){
			if(mTVProgramList.length!=0){
				return true;
			}	
			else{
				mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_RADIO,0);
				
				if(mTVProgramList==null){
					return false;
				}	
				else if(mTVProgramList.length!=0){
					return true;
				}
			}
			
		}
		else{
			mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_RADIO,0);
			if(mTVProgramList==null){
				return false;
			}	
			else if(mTVProgramList.length!=0){
				return true;
			}
		}
		
		return false;
	}

	public boolean isTopActivity(Context mContext){  
		boolean isTop = false;  
		ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);  
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;  
		
		Log.d(TAG, "isTopActivity = " + cn.getClassName());  
		
		if (cn.getClassName().contains(mContext.getClass().getName()))  {  
			isTop = true;  
		}  
		Log.d(TAG, "isTop = " + isTop);  
		return isTop;  
    } 

	public boolean isHaveExternalStorage(){
		
		CheckUsbdevice Usbdevice = new CheckUsbdevice(this);
		String path = getStringConfig("tv:dtv:record_storage_path");
		if(path!=null){
			if(Usbdevice.findSdcardString(path)==false){
				return false;
			}
			else{
				return true;
			}	
		}
		else
			return false;
	}
	
}
	
