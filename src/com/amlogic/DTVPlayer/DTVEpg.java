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

import java.util.*;
import java.text.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.util.DisplayMetrics;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.app.*;
import android.app.AlertDialog.*;
import android.content.*;
import android.graphics.*;
import android.view.ViewGroup.*;
import android.text.*;
import android.text.method.*;
import android.database.*;
import android.os.*;
import java.lang.reflect.Field;
import android.graphics.Color;
import com.amlogic.widget.Rotate3D;
import com.amlogic.widget.CustomDialog;
import com.amlogic.widget.CustomDialog.ICustomDialog;
import com.amlogic.widget.SingleChoiseDialog;
import com.amlogic.widget.SureDialog;

public class DTVEpg extends DTVActivity{
	private static final String TAG="DTVEpg";


	private boolean dtv_standard_atsc=false;

    /*******eit check time **************/
	private final static int    EIT_NEW_CHECKTIME               = 55;
	/****ctrl constant *****/
	private final static int    EIT_ITEM_COUNT                  = 7;
	
	private final static int    EIT_ITEM_WIDTHOF_HOURBG         = 240;
	
	private final static int    EIT_ITEM_BOTTOMMARGINOF_DATEBG  = 1;

	private final static int    EIT_ITEM_CONTENT_MAXCOUNT       = 100;

	private Handler  currenttimer_handler;
	private Runnable currenttimer_runnable;
	private ProgressDialog progressDialog;  
	
	/********ctrl var *************/
	private LinearLayout EPG_parent_linearlayout;
	private ListView EitListView;

	private TextView HelpInfoTextView;
	
	/********db var*********/
	private TVEvent[][] mTVEvent=new TVEvent[7][];
	private int cur_service_id=-1;
	private int cur_source_id;

	/**********count var*******************/
	private int  eit_notify = 0;
	private long tickcount  = 1;
	private int  TempFlag   = 0;	
	private int  current_date_index=0;
 
	ImageButton date_button0 = null;
	ImageButton date_button1 = null;
	ImageButton date_button2 = null;
	ImageButton date_button3 = null;
	ImageButton date_button4 = null;
	ImageButton date_button5 = null;
	ImageButton date_button6 = null;

	private EitUpdateThread t=null;
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.dtvepg);
		if(t==null){
			t =new EitUpdateThread();  
			t.start();
		}
	}

	public void onStart(){
		super.onStart();
	}

	public void onStop(){
		super.onStop();
		if(t!=null){
			t.quitLoop();
			t=null;
		}
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		DTVEpgUIInit();
		//channel list
		mDTVSettings = new DTVSettings(this);
		DTVChannelList_UI_Init();
	}

	public void onDisconnected(){
		Log.d(TAG, "disconnected");
		super.onDisconnected();
		if(t!=null){
			t.quitLoop();
			t=null;
		}
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
			case TVMessage.TYPE_EVENT_UPDATE:
				eit_notify ++ ;
				break;	
			case TVMessage.TYPE_PROGRAM_START:	
				if(isTopActivity(this)){
					Log.d(TAG,"New program start!");
					cur_service_id = DTVEpgGetID();
					db_id = cur_service_id;
					Log.d(TAG,"db_id="+db_id);
					if(mEitListDBAdapter!=null)
						mEitListDBAdapter.notifyDataSetChanged();
					if(t!=null){						
						t.onSetupCmd(EitUpdateThread.EIT_UPDATE,null);
						t.onSetupCmd(1,null);
					}
				}
				break;
			case TVMessage.TYPE_RECORD_CONFLICT:
				int recordConflict = msg.getRecordConflict();
				Log.d(TAG, "Record conflict:");
				if (recordConflict == TVMessage.REC_CFLT_START_NEW){
					Log.d(TAG, "Stop record for new recording");
				}else if (recordConflict == TVMessage.REC_CFLT_SWITCH_PROGRAM){
					Log.d(TAG, "Stop record for switching to new program");
				}else{
					break;
				}
				if(isTopActivity(this))
					showStopPVRDialog(recordConflict, msg.getProgramID());
				break;	
			default:
				break;
		}
	}

	private void showStopPVRDialog(){
		if(!isFinishing()){
			showStopPVRDialog(-1, -1);
		}
	}
	
	private void showStopPVRDialog(final int conflict, final int programID){
		new SureDialog(DTVEpg.this){
			public void onSetMessage(View v){
				String strMsg = "";
				
				if (conflict == TVMessage.REC_CFLT_SWITCH_PROGRAM){
					strMsg = getString(R.string.dtvplayer_change_channel);
				}else{
					strMsg = getString(R.string.dtvplayer_pvr_is_running);
				}
					
				((TextView)v).setText(strMsg);
			}
			public void onSetNegativeButton(){
				 
			}
			public void onSetPositiveButton(){
				DTVPlayerStopRecording();
				

				if (conflict == TVMessage.REC_CFLT_START_NEW){
					if (getCurrentProgramID() != programID){
						playProgram(programID);
					}

				}else if (conflict == TVMessage.REC_CFLT_SWITCH_PROGRAM){
					playProgram(programID);
				}
				
			}
			
			public void onShowEvent(){				
			}

			public void onDismissEvent(){
			}
		};
	}
	
	private int eit_list_cur_pos=0;
	private void DTVEpgUIInit(){	
		/*setup view*/
		findViewById(R.id.return_icon).setOnClickListener(
			new View.OnClickListener(){	  
				public void onClick(View v) {		
					// TODO Auto-generated method stub	
					Intent in = new Intent();
					in.setClass(DTVEpg.this, DTVPlayer.class);
					DTVEpg.this.startActivity(in);	
					DTVEpg.this.finish();
				}
			}
		);
		
	        EitListView  = (ListView)findViewById(R.id.EitListView);
	        EitListView.setItemsCanFocus(true);

		 EitListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
			int position, long id) {
				Log.d(TAG,"sat_list setOnItemSelectedListener " + position);
				eit_list_cur_pos = position;
				list_status=1;
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		EitListView.setOnKeyListener(new OnKeyListener() { 
			public boolean onKey(View v, int keyCode, KeyEvent event) {		
				switch(keyCode){
					case DTVActivity.KEYCODE_INFO: //info
						if (event.getAction() == KeyEvent.ACTION_DOWN) {
							showInfoDia(v,current_date_index,eit_list_cur_pos);	
						}
						return true;
					case KeyEvent.KEYCODE_PAGE_UP:
						if (event.getAction() == KeyEvent.ACTION_DOWN) {
							if(mEitListDBAdapter!=null){
								mEitListDBAdapter.setSelectItem(myAdapter.getSelectItem());
								mEitListDBAdapter.notifyDataSetChanged();
							}	
							Log.d(TAG, "press page up");
						}
						break;
					case KeyEvent.KEYCODE_PAGE_DOWN:	

						break;
				} 
				
				return false;
		}}); 
		
		EitListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				// TODO Auto-generated method stub				
				//showEventBookDialog(v, position);
				showEventAddDialog(v, current_date_index,position);
			}
		});
	
		mEitListDBAdapter = new EitListDBAdapter(this,mTVEvent,0);
		if(EitListView!=null)
        		EitListView.setAdapter(mEitListDBAdapter); 

		date_button0 = (ImageButton)findViewById(R.id.date_button0);
		date_button1 = (ImageButton)findViewById(R.id.date_button1); 
		date_button2 = (ImageButton)findViewById(R.id.date_button2);
		date_button3 = (ImageButton)findViewById(R.id.date_button3); 
		date_button4 = (ImageButton)findViewById(R.id.date_button4); 
		date_button5 = (ImageButton)findViewById(R.id.date_button5); 
		date_button6 = (ImageButton)findViewById(R.id.date_button6); 

		date_button0.setOnClickListener(new channelListButtonClick()); 	
		date_button1.setOnClickListener(new channelListButtonClick()); 	
		date_button2.setOnClickListener(new channelListButtonClick()); 	
		date_button3.setOnClickListener(new channelListButtonClick()); 
		date_button4.setOnClickListener(new channelListButtonClick()); 	
		date_button5.setOnClickListener(new channelListButtonClick()); 	
		date_button6.setOnClickListener(new channelListButtonClick()); 

		date_button0.setOnFocusChangeListener(new dateButtonFocusChange()); 	
		date_button1.setOnFocusChangeListener(new dateButtonFocusChange()); 	
		date_button2.setOnFocusChangeListener(new dateButtonFocusChange()); 	
		date_button3.setOnFocusChangeListener(new dateButtonFocusChange()); 
		date_button4.setOnFocusChangeListener(new dateButtonFocusChange()); 	
		date_button5.setOnFocusChangeListener(new dateButtonFocusChange()); 	
		date_button6.setOnFocusChangeListener(new dateButtonFocusChange()); 

		/*for touch mode*/
		
		ImageView red_button = (ImageView)findViewById(R.id.red_button);
		red_button.setFocusable(false);     
     		red_button.setFocusableInTouchMode(false);   
		red_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {				  	 
				Intent Intent_booklist = new Intent();
				Intent_booklist.setClass(DTVEpg.this, DTVBookList.class);
				startActivityForResult(Intent_booklist,22);			        	
		}});

		ImageView yellow_button = (ImageView)findViewById(R.id.yellow_button);
		yellow_button.setFocusable(false);     
     		yellow_button.setFocusableInTouchMode(false);   
		yellow_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {				  	 
				if(list_status==1){
					EitListView.requestFocus();
					if(EitListView.getChildCount()>eit_list_cur_pos)
						EitListView.setSelection(0);
					else{
						EitListView.setSelection(eit_list_cur_pos-EitListView.getChildCount());
					}
					mEitListDBAdapter.notifyDataSetChanged();
				}
				else if(list_status==0){
					ListView_channel.requestFocus();
					if(ListView_channel.getChildCount()>cur_select_item)
						ListView_channel.setSelection(0);
					else{
						ListView_channel.setSelection(cur_select_item-ListView_channel.getChildCount());
					}
					myAdapter.notifyDataSetChanged();
				}		        	
		}});	

		ImageView blue_button = (ImageView)findViewById(R.id.blue_button);
		blue_button.setFocusable(false);     
     		blue_button.setFocusableInTouchMode(false);   
		blue_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {				  	 
				int p=0;
				if(list_status==1){					
					p = eit_list_cur_pos+EitListView.getChildCount();
					if(p<EitListView.getCount())
						EitListView.setSelection(p-1);
					else{
						EitListView.setSelection(EitListView.getCount()-1);
					}
					mEitListDBAdapter.notifyDataSetChanged();
					EitListView.requestFocus();
				}
				else if(list_status==0){					
					p = cur_select_item+ListView_channel.getChildCount();
					if(p<ListView_channel.getCount())
						ListView_channel.setSelection(p-1);
					else{
						ListView_channel.setSelection(ListView_channel.getCount()-1);
					}
					myAdapter.notifyDataSetChanged();
					ListView_channel.requestFocus();
				}		        	
		}});	

		//refresh time
		refresh_time_thread();	
		if(t!=null){	
			t.onSetupCmd(1,null);
			t.onSetupCmd(2,null);
			t.onSetupCmd(EitUpdateThread.EIT_UPDATE,null);
		}
	}

	private void refresh_time_thread(){
		currenttimer_handler = new Handler();
		currenttimer_runnable = new Runnable() 
		{
			public void run() 
			{
				refresh_currenttime();
				currenttimer_handler.postDelayed(currenttimer_runnable, 1000);
			}   
		};
		currenttimer_handler.postDelayed(currenttimer_runnable, 1000);
	}

	private void refresh_currenttime()
	{		
		Date date = new Date(get_current_datetime()); 
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE,MM-dd-yyyy hh:mm:ss aa"); 
		String today = sdf.format(date); 
		
		TextView TempTexView; 
		TempTexView = (TextView)findViewById(R.id.current_time);
		TempTexView.setText((""+today));
	}

	private long get_firstmillisofcurrentday(){
		Date date1 = new Date(get_current_datetime());

		date1.setHours(0);
		date1.setMinutes(0);
		date1.setSeconds(0);
			
		return date1.getTime();
	}
	
	private int get_currentdb_srv_id()
	{
		if(dtv_standard_atsc){
			Log.d(TAG,"cur_source_id"+cur_source_id);
			return  cur_source_id;
		}
		else{
			cur_service_id = DTVEpgGetID();
			return cur_service_id; 
		}	
	}

	private String get_detailinfo(int db_evt_id){
		return DTVEpg_get_detailinfo(db_evt_id);
	}
	
	private int get_current_event_index(){
		int value = 0;
		TVEvent[]  PassedEit = DTVEpg_getDateEIT(get_firstmillisofcurrentday(), get_current_datetime()-get_firstmillisofcurrentday());

		if(PassedEit!=null){
			if (PassedEit.length>0){
			
				if((PassedEit[0].getStartTime())>  (get_firstmillisofcurrentday()))
					value = PassedEit.length+1;
				else
					value = PassedEit.length;
			}	
			else
				value = 0; 
		}
		Log.d(TAG,"value="+value);
		return value;

	}

	private void moveto_currentevent(){
		/*
        View TempButton = DTVEpg.this.getCurrentFocus();
        if (TempButton != null){
            Log.d("moveto_currentevent >>>", TempButton.toString());

            if (get_current_event_index()>=1){
				if(TempButton.toString().startsWith("android.widget.ListView")){

					TempButton = ((ListView)TempButton).getChildAt(0);
					if(TempButton !=null){
                        TempButton = ((View)TempButton).findViewById(1);
                        if(TempButton !=null){
							(TempButton).requestFocus();
                        }	
                        else{
                            for(int i = 0; i<get_current_event_index()-1; i++){
                                if (TempButton.focusSearch(View.FOCUS_RIGHT) != null){
                                        TempButton = TempButton.focusSearch(View.FOCUS_RIGHT);
                                        Log.d("moveto_currentevent >>>>---", TempButton.toString());
                                }
                                else{
                                        Log.d("moveto_currentevent>>>>", "null");
                                }
                                Log.d("moveto_currentevent>>>>>", ""+i);
                            }
                            (TempButton).requestFocus();
                        }
                	}
        		}
				else {	
					Log.d("moveto_currentevent>>>>>>>", TempButton.toString());			

					if (get_current_event_index()>=1){
			        	for(int i = 0; i<get_current_event_index(); i++){
			    			if (TempButton.focusSearch(View.FOCUS_RIGHT) != null){	
			        			TempButton = TempButton.focusSearch(View.FOCUS_RIGHT);
								Log.d("moveto_currentevent >>>>>>>>>", TempButton.toString());
							}
							else{
								Log.d("moveto_currentevent>>>>>>>>>", "null");
							}
							Log.d("moveto_currentevent>>>>>>>", ""+i);
						}
						
						(TempButton).requestFocus();
					}
				}
    		}
    	}
    	*/
	}
	
	/******************used after connect service*******************/

	private long get_current_datetime(){
		//Date date = new Date();
		//return date;
		return getUTCTime();
	}

	private TVEvent[][] mTempTVEvent=new TVEvent[7][];
	private void sync_date(){
		for(int i=0;i<7;i++){
			if(mTempTVEvent[i]!=null){
				mTVEvent[i]=new TVEvent[mTempTVEvent[i].length];
				System.arraycopy(mTempTVEvent[i],0,mTVEvent[i],0,mTempTVEvent[i].length);
			}
			else{
				mTVEvent[i]=null;
				continue;
			}	
		}
	}

	private static Object lock = new Object();
	private  void setup_db(){
	     synchronized(lock){

	        mTempTVEvent[0] = DTVEpg_getDateEIT(get_current_datetime()
	        						,get_firstmillisofcurrentday()+ 1 * 24 * 60 * 60*1000-get_current_datetime());
			if(mTempTVEvent[0]!=null)
			Log.d("mTVEvent[0]:",""+mTempTVEvent[0].length);

	        mTempTVEvent[1] = DTVEpg_getDateEIT(get_firstmillisofcurrentday()+ 1 * 24 * 60 * 60*1000,
	        						 1 * 24 * 60 * 60*1000);
			if(mTempTVEvent[1]!=null)
			Log.d("mTVEvent[1]:",""+mTempTVEvent[1].length);
	        mTempTVEvent[2] = DTVEpg_getDateEIT(get_firstmillisofcurrentday()+ 2 * 24 * 60 * 60*1000,
	        						1 * 24 * 60 * 60*1000);
			if(mTempTVEvent[2]!=null)
			Log.d("mTVEvent[2]:",""+mTempTVEvent[2].length);
	        mTempTVEvent[3] = DTVEpg_getDateEIT( get_firstmillisofcurrentday()+ 3 * 24 * 60 * 60*1000,
	        						1 * 24 * 60 * 60*1000);
			if(mTempTVEvent[3]!=null)
			Log.d("mTVEvent[3]:",""+mTempTVEvent[3].length);
	        mTempTVEvent[4] = DTVEpg_getDateEIT( get_firstmillisofcurrentday()+ 4 * 24 * 60 * 60*1000,
	        						1 * 24 * 60 * 60*1000);
			if(mTempTVEvent[4]!=null)
			Log.d("mTVEvent[4]:",""+mTempTVEvent[4].length);
	        mTempTVEvent[5] = DTVEpg_getDateEIT( get_firstmillisofcurrentday()+ 5 * 24 * 60 * 60*1000,
	        						1 * 24 * 60 * 60*1000);
			if(mTempTVEvent[5]!=null)
			Log.d("mTVEvent[5]:",""+mTempTVEvent[5].length);
	        mTempTVEvent[6] = DTVEpg_getDateEIT( get_firstmillisofcurrentday()+ 6 * 24 * 60 * 60*1000,
	        						1 * 24 * 60 * 60*1000);
			if(mTempTVEvent[6]!=null)
			Log.d("mTVEvent[6]:",""+mTempTVEvent[6].length);
			
		//if (mTVEvent[0].length > 0)
			//HelpInfoTextView.setText(R.string.epg_operate_des01);
		//else
			//HelpInfoTextView.setText(R.string.epg_no_epg_info);
	     	}	
	}

	EitListDBAdapter mEitListDBAdapter =null;
	private void refresh_Eitlistview(int date){
		if(EitListView!=null){
			EitListDBAdapter mAdapter = (EitListDBAdapter)EitListView.getAdapter();    
			if(mAdapter!=null){
				mAdapter.setDateIndex(date);			
				mAdapter.notifyDataSetChanged();
			}	
		}
		if(t!=null)
			t.onSetupCmd(3,null);
	}

	private  void SetAlarm(long time){
		//Intent intent = new Intent(this, AlarmReceiver.class);
		Intent intent = new Intent(this, AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent ,Intent.FLAG_ACTIVITY_NEW_TASK);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, time*1000, pi);		
		//am.set(AlarmManager.POWER_OFF_WAKEUP, time*1000, pi);		
	}

	private void update_bookstatus(int eventid, int bookflag){
		ContentValues cv = null;

		switch(bookflag){
			case 0:
			{
				cv = new ContentValues();	
				cv.put("sub_flag", 0);
				DTVEpg_updateEvent(eventid); 
				DTVEpg_deleteRec(eventid);
			}
			break;
			
			case 1:	
			{
				DTVEpg_deleteRec(eventid);
				DTVEpg_subscribeEPGEvent(eventid);
			}
			break;
			
			case 2:
			{
				DTVEpg_recordByEventID(eventid);
			}
			break;
		}
	}
	
	private void refresh_bookstatus(View view, int bookflag){
		/*
		if (bookflag == 0)
			((Button)view).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		else if (bookflag == 1)
			((Button)view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.epg_event_book_1, 0, 0, 0);
		else if (bookflag == 2)
			((Button)view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.epg_event_book_2, 0, 0, 0);
		*/
	}
	
	private void setup_timeupdatethread(){
	    currenttimer_handler = new Handler();
	    currenttimer_runnable = new Runnable() {
			public void run() {
				tickcount++;
				refresh_currenttime();
				currenttimer_handler.postDelayed(currenttimer_runnable, 1000);
				if (((tickcount % EIT_NEW_CHECKTIME) == 0)&&(eit_notify >0)){
					if(t!=null){
						t.onSetupCmd(EitUpdateThread.EIT_UPDATE,null);
					}
					eit_notify = 0;
				}
			}   
		};
	    currenttimer_handler.postDelayed(currenttimer_runnable, 1000);
	}

	private void refresh_dates(int focusindex){
		String Weeks[] = new String[]{
			getString(R.string.epg_sunday), 
			getString(R.string.epg_monday), 
			getString(R.string.epg_tuesday), 
			getString(R.string.epg_wednesday),
			getString(R.string.epg_thursday),
			getString(R.string.epg_friday),
			getString(R.string.epg_saturday)};
		Calendar calendar = Calendar.getInstance(); 

		for(int i=0; i<7; i++){
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd"); 
			calendar.setTime(new Date(get_firstmillisofcurrentday()+ i * 24 * 60 * 60 * 1000));
			String today = sdf.format(new Date(get_firstmillisofcurrentday()+ i * 24 * 60 * 60 * 1000)); 

			TextView TempTexView=null; 
			switch(i){
				case 0:
					TempTexView = (TextView)findViewById(R.id.date0);
					break;
				case 1:
					TempTexView = (TextView)findViewById(R.id.date1);
					break;
				case 2:
					TempTexView = (TextView)findViewById(R.id.date2);
					break;
				case 3:
					TempTexView = (TextView)findViewById(R.id.date3);
					break;
				case 4:
					TempTexView = (TextView)findViewById(R.id.date4);
					break;	
				case 5:
					TempTexView = (TextView)findViewById(R.id.date5);
					break;
				case 6:
					TempTexView = (TextView)findViewById(R.id.date6);
					break;

			}
			if(TempTexView!=null)
			TempTexView.setText(Weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
		}
	}
	
	/*disable HOME key*/
	@Override 
	public void onAttachedToWindow(){  
		//this.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_KEYGUARD);  
		super.onAttachedToWindow();  
	} 

	public class EitListDBAdapter extends BaseAdapter{

		private LayoutInflater mInflater;
		private Context cont;
		private int selectItem;
		private int date;
		private TVEvent[][] mEvent = null;
		
		
		class ViewHolder {
			TextView time;
			TextView eit_name;	
			ImageView icon_book;
			ImageView icon_pvr;
		}

		/*
		public EitListDBAdapter(Context context, int index) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);	
			date = index;
		}
		*/

		public EitListDBAdapter(Context context, TVEvent[][] event, int index) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);	
			mEvent = event;
			date = index;
		}

		public void setDateIndex(int index){
			this.date = index;
		}

		public int getCount() {
			if(mEvent==null){
				return 0;	
			}else if(mEvent[date]!=null){
				return mEvent[date].length;
			}	
			else
				return 0;
		}

		public Object getItem(int position) {
			return position;
		}
		
		public long getItemId(int position) {
			return position;
		}

		public void setSelectItem(int position){
			this.selectItem = position;
		}
        
	        public int getSelectItem(){
				return this.selectItem;
	        }
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;	
			if (convertView == null){    
				convertView = mInflater.inflate(R.layout.epg_eitlist_item, null);
				
				holder = new ViewHolder();
				holder.time = (TextView)convertView.findViewById(R.id.time);
				holder.eit_name = (TextView) convertView.findViewById(R.id.eit_name);
				holder.icon_book = (ImageView) convertView.findViewById(R.id.icon_1);
				holder.icon_pvr = (ImageView)convertView.findViewById(R.id.icon);
				convertView.setTag(holder);
			}
			else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
		
			// Bind the data efficiently with the holder.
			
			
			if(mEvent[date]!=null){
				if (mEvent[date].length>0){		
					holder.eit_name.setText(mEvent[date][position].getName());
					Date dt_start =  new Date(mEvent[date][position].getStartTime());
			    		Date dt_end   =  new Date(mEvent[date][position].getEndTime());
			    		
			    		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
			    		String str_start = sdf.format(dt_start); 
			    		String str_end   = sdf.format(dt_end); 
					holder.time.setText(""+str_start + "--" + str_end);	

					/*			
					switch(mTVEvent[date][position].getSubFlag()){
						case 0:
							holder.icon_book.setBackgroundResource(Color.TRANSPARENT);
							holder.icon_pvr.setBackgroundResource(Color.TRANSPARENT);
							break;
						case 1:
							holder.icon_book.setBackgroundResource(R.drawable.epg_event_book_1); 
							break;
						case 2:
							holder.icon_book.setBackgroundResource(R.drawable.epg_event_book_2); 
							break;
					}
					*/
					
				}
			}
		
			return convertView;
		}
		
	} 

	AlertDialog.Builder builder=null;
    private void showInfoDia(View v,int date,int pos){
		
		String message = mTVEvent[date][pos].getEventDescr()+"\n"+mTVEvent[date][pos].getEventExtDescr();
				
		builder = new AlertDialog.Builder(DTVEpg.this) ;
		builder.setTitle(mTVEvent[date][pos].getName());
		//builder.setMessage((mTVEvent[pos].getString(mTVEvent[pos].getColumnIndex("ext_descr"))!=null&&!(mTVEvent[pos].getString(mTVEvent[pos].getColumnIndex("ext_descr")).equals("")))?
		builder.setMessage(message);
		builder.setPositiveButton("ok", null);

		AlertDialog alert = builder.create();
		alert.show();

		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
	}	

	private void showEventBookDialog(View v,int position){
		final int item = position;
		final View TempView = v;
		int pos = 0;
		String items[] = new String[]{"Cancel Book", "BookPlay", "BookRecord"};
		
		new SingleChoiseDialog(DTVEpg.this,items,pos){
			public void onSetMessage(View v){
				((TextView)v).setText("   ");
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				Log.d(TAG,"dialog choise="+which);
				//refresh_bookstatus(TempItemView, which);
				final ImageView icon = (ImageView)TempView.findViewById(R.id.icon_1);
				switch(which){
					case 0:
						update_bookstatus(mTVEvent[current_date_index][item].getID(),0);
						mTVEvent[current_date_index][item].setSubFlag(0);
						icon.setBackgroundResource(0);
					break;
					case 1:
						update_bookstatus(mTVEvent[current_date_index][item].getID(),1);
						mTVEvent[current_date_index][item].setSubFlag(1);
						icon.setBackgroundResource(R.drawable.epg_event_book_1);
					break;
					case 2:
						update_bookstatus(mTVEvent[current_date_index][item].getID(),2);
						mTVEvent[current_date_index][item].setSubFlag(2);
						icon.setBackgroundResource(R.drawable.epg_event_book_2);
						//if((long)(mTVEvent[current_date_index][item].getStartTime())>System.currentTimeMillis()/1000)
							//SetAlarm(mTVEvent[current_date_index][item].getStartTime()/1000-100);
						Calendar cal = Calendar.getInstance();
						SetAlarm(cal.getTimeInMillis()/1000+60);	
					break;
				}
			}
		};
	}

	/*private void showEventAddDialog(View v,int position){
		
		final int item = position;
		final View TempView = v;
		int pos = 0;
		String items[] = new String[]{"Cancel Book", "BookPlay", "BookRecord"};
		
		new SingleChoiseDialog(DTVEpg.this,items,pos){
			public void onSetMessage(View v){
				((TextView)v).setText("   ");
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				Log.d(TAG,"dialog choise="+which);
				//refresh_bookstatus(TempItemView, which);
				final ImageView icon = (ImageView)TempView.findViewById(R.id.icon_1);
				switch(which){
					case 0:
						update_bookstatus(mTVEvent[current_date_index][item].getID(),0);
						mTVEvent[current_date_index][item].setSubFlag(0);
						icon.setBackgroundResource(0);
					break;
					case 1:
						update_bookstatus(mTVEvent[current_date_index][item].getID(),1);
						mTVEvent[current_date_index][item].setSubFlag(1);
						icon.setBackgroundResource(R.drawable.epg_event_book_1);
					break;
					case 2:
						update_bookstatus(mTVEvent[current_date_index][item].getID(),2);
						mTVEvent[current_date_index][item].setSubFlag(2);
						icon.setBackgroundResource(R.drawable.epg_event_book_2);
						//if((long)(mTVEvent[current_date_index][item].getStartTime())>System.currentTimeMillis()/1000)
							//SetAlarm(mTVEvent[current_date_index][item].getStartTime()-100);
						Calendar cal = Calendar.getInstance();
						SetAlarm(cal.getTimeInMillis()/1000+60);	
					break;
				}
			}
		};
		
	}
	*/

	private  class EventAddAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		
		private Context cont;
		private String[] DATA;

	 	class ViewHolder {
			
			TextView text;
			ImageView icon;
			TextView  info;
			ImageView icon1;
		}
	
		public EventAddAdapter(Context context) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);
			
			DATA = new String[2];
			DATA[0]= cont.getResources().getString(R.string.repeat);			
			DATA[1]= cont.getResources().getString(R.string.mode);
				
		}

		public int getCount() {	
			return DATA.length;
		}

		public Object getItem(int position) {

			return position;
		}
	
		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder=null;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.epg_event_add_list, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.info = (TextView)convertView.findViewById(R.id.info);
				holder.icon = (ImageView)convertView.findViewById(R.id.icon);
				holder.icon1 = (ImageView)convertView.findViewById(R.id.icon1);
				convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			
			holder.text.setText(DATA[position]);
			holder.icon.setBackgroundResource(R.drawable.arrow_down2); 
			holder.info.setVisibility(View.VISIBLE);
			holder.icon.setVisibility(View.VISIBLE);
			holder.icon1.setVisibility(View.INVISIBLE);
			holder.info.setTextColor(Color.YELLOW);	
			switch(position){
				case 0:
					holder.info.setText(cont.getResources().getString(R.string.once));
					break;
				case 1:
					holder.info.setText(cont.getResources().getString(R.string.view));
					break;
			
			} 
			
			return convertView;
		}
	}
	
	int mode=1;
	int repeat=0;
	private void showEventAddDialog(View v,int date,int position){
		String message = mTVEvent[date][position].getEventDescr()+"\n"+mTVEvent[date][position].getEventExtDescr();
		final int pos = position;
		
		final Dialog mDialog = new AlertDialog(this){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						//if(mDialog!=null&& mDialog.isShowing()){
							dismiss();
						//}
						return true;
				}
				return super.onKeyDown(keyCode, event);
			}
			
		};
		
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);

		if(mDialog == null){
			return;
		}

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				
			}         
		}); 	
		mDialog.show();
		mDialog.setContentView(R.layout.event_add_dialog);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.0f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		//title.setText(getString(R.string.scan_mode));
		title.setText(getString(R.string.epg_event_add));
	
		final TextView text_channel_name= (TextView) window.findViewById(R.id.text_channel_name);
		final TextView text_event_name = (TextView) window.findViewById(R.id.text_event_name);
		final TextView text_event_start_date = (TextView) window.findViewById(R.id.text_event_start_date);
		final TextView text_event_start_time = (TextView) window.findViewById(R.id.text_event_start_time);
		final TextView text_event_end_time = (TextView) window.findViewById(R.id.text_event_end_time);

		text_event_name.setText(mTVEvent[date][position].getName());

		Log.d(TAG,"start time------"+mTVEvent[date][position].getStartTime());
		Date dt_start =  new Date(mTVEvent[date][position].getStartTime());
		Date dt_end   =  new Date(mTVEvent[date][position].getEndTime());
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
		SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd"); 
		String str_start = sdf.format(dt_start); 
		String str_end   = sdf.format(dt_end); 
		String str_date  = sdf_date.format(dt_start);
		text_event_start_date.setText(str_date);
		text_event_start_time.setText(str_start);
		text_event_end_time.setText(str_end);
		
		text_channel_name.setText(mTVProgramList[cur_select_item].getName());
		
		final ListView LimitListView = (ListView)window.findViewById(R.id.set_list); 	
		LimitListView.setAdapter(new EventAddAdapter(this));

		
		LimitListView.setOnItemSelectedListener(new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view,
			int position, long id) {
				Log.d(TAG,"sat_list setOnItemSelectedListener " + position);
				//SetLimitItemSelected = position;
			}

			public void onNothingSelected(AdapterView<?> parent) {
				Log.d(TAG,"<<sat_list onNothingSelected>> ");
			}
		});
		LimitListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				final TextView text =(TextView) arg1.findViewById(R.id.info);
				final ImageView icon=(ImageView)arg1.findViewById(R.id.icon);	
				final ImageView icon1=(ImageView)arg1.findViewById(R.id.icon1);
				
				// TODO Auto-generated method stub
				System.out.println("onItemSelected arg0 " + arg0);
				System.out.println("onItemSelected arg1 " + arg1);
				System.out.println("onItemSelected arg2 " + arg2);
				System.out.println("onItemSelected arg3 " + arg3);
				
				switch(arg2){
					case 0:  
						if(text.getText().equals(getString((R.string.once)))){
							text.setText(getString(R.string.daily));
							repeat=1;
						}
						else if(text.getText().equals(getString((R.string.daily)))){
							text.setText(getString(R.string.weekly));
							repeat=2;
						}
						else{
							text.setText(getString(R.string.once));
							repeat=0;
						}
						break;
					case 1:
						if(text.getText().equals(getString((R.string.view)))){
							text.setText(getString(R.string.pvr));
							mode=2;
						}
						else{
							text.setText(getString(R.string.view));
							mode=1;
						}
						break;					
				}
			}
        	    
        });

		no.setFocusable(true);     
     	no.setFocusableInTouchMode(true);   
		no.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {				  	 
		        	 //onSetNegativeButton();
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
		          }});	 
		yes.setOnClickListener(new OnClickListener(){
	          public void onClick(View v) {
			  		/*
			  		if(mode==1){
						//update_bookstatus(mTVEvent[current_date_index][pos].getID(),1);
						//mTVEvent[current_date_index][pos].setSubFlag(1);
						//icon.setBackgroundResource(R.drawable.epg_event_book_1);
						DTVPlayerAddEvent(mTVEvent[current_date_index][pos],1);
			  		}else if((mode==2)){
						//update_bookstatus(mTVEvent[current_date_index][pos].getID(),2);
						//mTVEvent[current_date_index][pos].setSubFlag(2);
						//icon.setBackgroundResource(R.drawable.epg_event_book_2);
						DTVPlayerAddEvent(mTVEvent[current_date_index][pos],2);
					}
					*/
					Log.d(TAG,"mode="+mode + " ---repeat="+repeat);
					DTVPlayerAddEvent(mTVEvent[current_date_index][pos].getID(),mode,repeat);	
					
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
			}});
		

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
						public void onShow(DialogInterface dialog) {
								
							}         
							}); 	

		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {
							
						}         
						});	
	
	}
	

	/*
	private AlertDialog.Builder diaBuilder;
	private AlertDialog alert; 
	private int SetLimitItemSelected = 0;
	private View dvbs_set_limit_list;
	private void showBookAddDia(){

		diaBuilder = new AlertDialog.Builder(this);
	    LayoutInflater layoutInflater = LayoutInflater.from(this);  
		
 	 	dvbs_set_limit_list = layoutInflater.inflate(R.layout.dvbs_set_limit_dia, null); 
		//diaBuilder.setTitle(R.string.dish_setup_conf_button_des7_info);

		final ListView LimitListView = (ListView)dvbs_set_limit_list.findViewById(R.id.set_limit_list); 

	
		final TextView edittext_frequency= (TextView) dvbs_set_limit_list.findViewById(R.id.edittext_frequency);
		final TextView edittext_symbol = (TextView) dvbs_set_limit_list.findViewById(R.id.edittext_symbol);
		final TextView polarization = (TextView) dvbs_set_limit_list.findViewById(R.id.polarization);
		
		LimitListView.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
					Log.d(TAG,"sat_list setOnItemSelectedListener " + position);

					SetLimitItemSelected = position;
					
				}

				public void onNothingSelected(AdapterView<?> parent) {
					Log.d(TAG,"<<sat_list onNothingSelected>> ");
				}
			}
		);
		LimitListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
	        {

				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
					final TextView text =(TextView) arg1.findViewById(R.id.info);
					final ImageView icon=(ImageView)arg1.findViewById(R.id.icon);	
					final ImageView icon1=(ImageView)arg1.findViewById(R.id.icon1);
					
					
					// TODO Auto-generated method stub
					System.out.println("onItemSelected arg0 " + arg0);
					System.out.println("onItemSelected arg1 " + arg1);
					System.out.println("onItemSelected arg2 " + arg2);
					System.out.println("onItemSelected arg3 " + arg3);

					
					switch(arg2){
						case 0:  //move continue
							text.setText("Stop");	
							hideMoveIcon(icon);
							hideMoveIcon(icon1);
							//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_STOP_MOVING,null);
							 t.onSetupCmd(t.ROTOR_CMD_STOP_MOVING,null);
							break;
						
					}
				}
	        	    
	        });

		LimitListView.setOnKeyListener( new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				int pos=0;	
					TextView text=null;	
					ImageView icon =null;
					ImageView icon1=null;
					ListView listView = (ListView) v;
					DvbsSetLimitAdapter adapter = null;
					if (listView.getSelectedView() != null) {
					        // (cast if necessary) and use selected view
						//pos = listView.getSelected();
						View view = listView.getChildAt(SetLimitItemSelected);
						text =(TextView) view.findViewById(R.id.info);
						icon = (ImageView)  view.findViewById(R.id.icon);
						icon1 = (ImageView)  view.findViewById(R.id.icon1);
						adapter = (DvbsSetLimitAdapter)listView.getAdapter();
				}
				
 				if (event.getAction() == KeyEvent.ACTION_UP) {
 	
	        			
		
					// TODO Auto-generated method stub
					switch(keyCode)
					{	
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if(SetLimitItemSelected==0){
								Log.d(TAG,"KEYCODE_DPAD_RIGHT"+SetLimitItemSelected);
								my_cmdParams.unit=0;
								t.onSetupCmd(t.ROTOR_CMD_MOVE_EAST,(Object)my_cmdParams);
								text.setText("East");
								hideMoveIcon(icon);
								showMoveIcon(icon1);
								adapter.notifyDataSetChanged();
								resetrotorstatuscache = true;
							}
							else if(SetLimitItemSelected==1) {
								Log.d(TAG,"KEYCODE_DPAD_RIGHT"+SetLimitItemSelected);
								my_cmdParams.unit=1;	
								t.onSetupCmd(t.ROTOR_CMD_MOVE_EAST,(Object)my_cmdParams);
								text.setText("East");
								showMoveIcon1(icon1,text);	
								adapter.notifyDataSetChanged();
								resetrotorstatuscache = true;
							}
							return true;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if(SetLimitItemSelected==0){
								Log.d(TAG,"KEYCODE_DPAD_LEFT"+SetLimitItemSelected);
								my_cmdParams.unit=0;	
								t.onSetupCmd(t.ROTOR_CMD_MOVE_WEST,(Object)my_cmdParams);
								text.setText("West");
								hideMoveIcon(icon1);
								showMoveIcon(icon);
								adapter.notifyDataSetChanged();
								resetrotorstatuscache = true;
							}
							else if(SetLimitItemSelected==1) {
								Log.d(TAG,"KEYCODE_DPAD_LEFT"+SetLimitItemSelected);
								my_cmdParams.unit=1;	
								t.onSetupCmd(t.ROTOR_CMD_MOVE_WEST,(Object)my_cmdParams);
								text.setText("West");
								showMoveIcon1(icon,text);
								adapter.notifyDataSetChanged();
								resetrotorstatuscache = true;
								
							}
							return true;
					}
 				}	
				else if(event.getAction() == KeyEvent.ACTION_DOWN){
					switch(keyCode)
					{
						case KeyEvent.KEYCODE_DPAD_DOWN:
							Log.d(TAG,"----continue is stop-----"+SetLimitItemSelected);
							if(SetLimitItemSelected==0)
							{
								hideMoveIcon(icon);
								hideMoveIcon(icon1);
								text.setText("Stop");
								t.onSetupCmd(t.ROTOR_CMD_STOP_MOVING,null);						
							}
							break;
					}	
				}	
				return false;
	            }
			
		});	
		
				
		diaBuilder.setView(dvbs_set_limit_list);
	
		alert = diaBuilder.create();

		alert.setOnShowListener(new DialogInterface.OnShowListener(){
							public void onShow(DialogInterface dialog) {
									t.onSetupCmd(50,null);
								}         
								}); 	

		alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
							public void onDismiss(DialogInterface dialog) {
							t.quitLoop();	
							timer_position_adjust_ts_signal_info_handler.removeCallbacks(timer_position_adjust_ts_signal_info_runnable);  
							if(resetrotorstatuscache){
								resetrotorstatuscache = false;
								//mLockDvb.resetrotorstatuscache();
							}
							}         
							});	

		alert.show();	
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		//alert.getWindow().setLayout(displayMetrics.widthPixels / 2, -1);

		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.00f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
		timer_position_adjust_ts_signal_info_handler.postDelayed(timer_position_adjust_ts_signal_info_runnable, 500);
	}
	*/
	
	
    private class ChoiceOnClickListener implements DialogInterface.OnClickListener 
    {  
        private int which = 0;  
		
        public void onClick(DialogInterface dialogInterface, int which) 
        {  
            this.which = which;  
        }  
          
        public int getWhich() 
        {  
            return which;  
        }  
    }  

	class EitItemOnClick  implements OnClickListener{
		public void onClick(View v){
			/*
       		final int i=Integer.valueOf(((Button)v).getHint().toString());
        	if (i != -1){	
        		try{
		        	int TempInt = EitListView.getPositionForView(v);

		    		Date dt_start =  new Date(mTVEvent[TempInt][i].getStartTime()*1000);
		    		Date dt_end   =  new Date((mTVEvent[TempInt][i].getEndTime())*1000);
		    		
		    		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
		    		String str_start = sdf.format(dt_start); 
		    		String str_end   = sdf.format(dt_end); 
		        	
				    refresh_eventnameanddetailinfo(((Button)v).getText().toString() + "[" + str_start + "--" + str_end +"]", 
				    		get_detailinfo(mTVEvent[TempInt][i].getID()));
        		}
				catch(Exception e){
				    Log.d(TAG, e.getMessage());
				}
				
				TempItemView = v;
				if(TempItemView  == null)
					Log.d(TAG,"TempItemView is null");
				else{
					final int TempInt = EitListView.getPositionForView(TempItemView);
					int pos = 0;
					String items[] = new String[]{"Cancel Book", "BookPlay", "BookRecord"};
					
					new SingleChoiseDialog(DTVEpg.this,items,pos){
						public void onSetMessage(View v){
							((TextView)v).setText("   ");
						}

						public void onSetNegativeButton(){
							
						}
						public void onSetPositiveButton(int which){
							Log.d(TAG,"dialog choise="+which);
							refresh_bookstatus(TempItemView, which);
							switch(which){
								case 0:
									update_bookstatus(mTVEvent[TempInt][i].getID(),0);
								break;
								case 1:
									update_bookstatus(mTVEvent[TempInt][i].getID(),1);
								break;
								case 2:
									update_bookstatus(mTVEvent[TempInt][i].getID(),2);
									if((long)(mTVEvent[TempInt][i].getStartTime())>System.currentTimeMillis()/1000)
										SetAlarm(mTVEvent[TempInt][i].getStartTime()-100);
								break;
							}
						}
					};
				}	
		    }
			else{
			clear_eventnameanddetailinfo();
			}
			*/
        }
	}
	
	private void update_new_eit(){
		/*
		if(!isFinishing()){
			progressDialog.show();
			WindowManager.LayoutParams lp=progressDialog.getWindow().getAttributes();
			lp.dimAmount=0.0f;
			progressDialog.getWindow().setAttributes(lp);
			progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		}
		*/
	}

	private void showUpdateDialog(){
		if(!isFinishing()){	
			progressDialog = ProgressDialog.show(DTVEpg.this, getString(R.string.epg_loading), getString(R.string.epg_waiting), true, false);   
			WindowManager.LayoutParams lp=progressDialog.getWindow().getAttributes();
			lp.dimAmount=0.0f;
			progressDialog.getWindow().setAttributes(lp);
			progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);	
		}
	}

	private void dismissUpdateDialog(){
		if(progressDialog!=null)
			progressDialog.dismiss();  
	}
	
	
/****************************Channel List****************************/

	ListView ListView_channel=null;
	TextView Text_title=null;
	private int class_total=0;
	private int cur_class_no=-1;
	private int cur_select_item=0;
	private IconAdapter myAdapter=null;
	private TVProgram[]  mTVProgramList=null;
	private DTVSettings mDTVSettings=null;
	private int list_status=-1;

	int db_id=-1;
	private int service_type=TVProgram.TYPE_TV;
	private boolean favor=false;

	private void getListData(int type){
		if(type==0)
			mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_TV,true);
		else if(type==1)
			mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_RADIO,true);
	}

	private void getListFavorite(){
		mTVProgramList=TVProgram.selectByFavorite(this,true);	
	}

	TVGroup[] mTVGroup=null;
	private int getListProgramClass(){
		mTVGroup=DTVProgramManagerGetGroupList();
		if(mTVGroup!=null)
			return mTVGroup.length;
		else
			return 0;
	}
	
	private void getClassData(int class_no){
		mTVProgramList=DTVProgramManagerGetProByGroup(class_no);
	}

	private LinearLayout LinearLayoutListView=null;
	private void DTVChannelList_UI_Init(){

		Bundle bundle = this.getIntent().getExtras();
		if(bundle!=null){
	    	db_id = bundle.getInt("db_id");
			service_type=getCurrentProgramType();
		}	

		ImageButton prolist_button = (ImageButton)findViewById(R.id.prolist_button);
		prolist_button.setOnClickListener(new channelListButtonClick()); 
		prolist_button.setOnFocusChangeListener(new dateButtonFocusChange());

		Text_title=(TextView)findViewById(R.id.prolist);
		Text_title.setTextColor(Color.YELLOW);
		
		class_total = getListProgramClass();
		if(service_type == TVProgram.TYPE_RADIO){

			if(t!=null){
				t.onSetupCmd(EitUpdateThread.DATA_RADIO,null);
			}
			Text_title.setText(R.string.radio);
		}	
		else{
			service_type = TVProgram.TYPE_TV;
			if(t!=null){
				t.onSetupCmd(EitUpdateThread.DATA_TV,null);
			}
			Text_title.setText(R.string.tv);
		}
		
		ListView_channel = (ListView) findViewById(R.id.programListView);
		myAdapter = new IconAdapter(this,null);
		ListView_channel.setOnItemSelectedListener(mOnSelectedListener);
		ListView_channel.setOnScrollListener(new listOnScroll()); 
		ListView_channel.setOnItemClickListener(mOnItemClickListener);
		ListView_channel.setAdapter(myAdapter);
		setFocusPosition();
		myAdapter.notifyDataSetChanged();
	}

	public void setFocusPosition(){
		int i = 0;
		if(mTVProgramList!=null){
			for(i=0;i<mTVProgramList.length;i++){
				if(db_id == mTVProgramList[i].getID()){
					ListView_channel.setFocusableInTouchMode(true);
	   			  	ListView_channel.requestFocus();
	   			  	ListView_channel.requestFocusFromTouch();
	        		ListView_channel.setSelection(i);
					cur_select_item = i;
					break;
				}
			}	
		}
	}
	
  	private AdapterView.OnItemSelectedListener mOnSelectedListener = new AdapterView.OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
			ListView_channel = (ListView) findViewById(R.id.programListView);
			if(ListView_channel.hasFocus() == true){
			}
			cur_select_item = position;
			list_status=0;
		}
		public void onNothingSelected(AdapterView<?> parent){
		}
	};

	private AdapterView.OnItemClickListener mOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			int db_id=mTVProgramList[position].getID();	
			int serviceType = mTVProgramList[position].getType();
			if(DTVPlayerGetCurrentProgramType()!=serviceType){
				if(serviceType==TVProgram.TYPE_RADIO){
					setProgramType(TVProgram.TYPE_RADIO);
				}	
				else{
					setProgramType(TVProgram.TYPE_TV);
				}
			}	
			
			if(DTVPlayerGetCurrentProgramID()!=db_id){
				
				DTVPlayerPlayById(db_id);
				current_date_index=0;
				date_button0.setBackgroundResource(R.drawable.epg_date_button_press);	
				date_button1.setBackgroundResource(R.drawable.epg_date_button);
				date_button2.setBackgroundResource(R.drawable.epg_date_button);		
				date_button3.setBackgroundResource(R.drawable.epg_date_button);
				date_button4.setBackgroundResource(R.drawable.epg_date_button);		
				date_button5.setBackgroundResource(R.drawable.epg_date_button);
				date_button6.setBackgroundResource(R.drawable.epg_date_button);
			}
		}
	};

	private Button.OnFocusChangeListener mOnFocusChangeListener = new Button.OnFocusChangeListener(){
        public void onFocusChange(View v, boolean hasFocus) {
           // TODO Auto-generated method stub
			if(hasFocus == true){
				v.setBackgroundResource(R.drawable.epg_date_button);
			}
			else{
				switch(current_date_index){
						case 0:
							date_button0.setBackgroundResource(R.drawable.epg_date_button_press);
							break;
						case 1:
							date_button1.setBackgroundResource(R.drawable.epg_date_button_press);
							break;
						case 2:
							date_button2.setBackgroundResource(R.drawable.epg_date_button_press);
							break;
						case 3:
							date_button3.setBackgroundResource(R.drawable.epg_date_button_press);
							break;
						case 4:
							date_button4.setBackgroundResource(R.drawable.epg_date_button_press);
							break;
						case 5:
							date_button5.setBackgroundResource(R.drawable.epg_date_button_press);
							break;	
						case 6:
							date_button6.setBackgroundResource(R.drawable.epg_date_button_press);
							break;	
					}
			}	
           
        }
    };

	private class IconAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context cont;
		private List<String> listItems;
		private int selectItem;
		
		class ViewHolder {
			TextView prono;
			TextView text;	
			ImageView icon_scrambled;
			ImageView icon_fav;
			ImageView icon;
		}
		
		public IconAdapter(Context context, List<String> list) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);			  
		}

		public int getCount() {
			if(mTVProgramList==null)
				return 0;
			else
				return mTVProgramList.length;
		}

		public Object getItem(int position) {
			return position;
		}
		
		public long getItemId(int position) {
			return position;
		}

		public void setSelectItem(int position){
			this.selectItem = position;
		}
        
        public int getSelectItem(){
			return this.selectItem;
        }
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;	
			if (convertView == null){    
				convertView = mInflater.inflate(R.layout.epg_channellist_item, null);
				
				holder = new ViewHolder();
				holder.prono = (TextView)convertView.findViewById(R.id.prono);
				holder.text = (TextView) convertView.findViewById(R.id.ItemText);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.icon_scrambled = (ImageView)convertView.findViewById(R.id.icon_scrambled);
				holder.icon_fav = (ImageView)convertView.findViewById(R.id.icon_fav);
				convertView.setTag(holder);
			}
			else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
		
			// Bind the data efficiently with the holder.

			if(mDTVSettings.getScanRegion().contains("ATSC")==false){
				holder.prono.setText(Integer.toString(mTVProgramList[position].getNumber().getNumber()));
			}
			else{
				holder.prono.setText(Integer.toString(mTVProgramList[position].getNumber().getNumber())+"-"+Integer.toString(mTVProgramList[position].getNumber().getMinor()));
			}
			
			holder.text.setText(mTVProgramList[position].getName());
			
			Log.d(TAG,"db_id="+db_id);
			Log.d(TAG,"mTVProgramList[position].getID()="+mTVProgramList[position].getID());
			if(db_id == mTVProgramList[position].getID()){  
				//convertView.setBackgroundColor(Color.RED);  
				holder.text.setTextColor(Color.YELLOW);
			}	
			else{
				//convertView.setBackgroundColor(Color.TRANSPARENT); 
				holder.text.setTextColor(Color.WHITE);
			}	
		
			if(mTVProgramList[position].getLockFlag()){
				holder.icon.setBackgroundResource(R.drawable.dtvplayer_icon_lock); 
			}	
			else{
				holder.icon.setBackgroundResource(Color.TRANSPARENT);
			}

			if(mTVProgramList[position].getFavoriteFlag()){
				holder.icon_fav.setBackgroundResource(R.drawable.dtvplayer_icon_fav); 
			}	
			else{
				holder.icon_fav.setBackgroundResource(Color.TRANSPARENT);
			}	

			if(mTVProgramList[position].getScrambledFlag()){
				holder.icon_scrambled.setBackgroundResource(R.drawable.dtvplayer_icon_scrambled); 
			}	
			else{
				holder.icon_scrambled.setBackgroundResource(Color.TRANSPARENT);
			}			  
			return convertView;
		}
	}	
		
	class listOnScroll implements OnScrollListener{
		public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
			//reset_timer();	
		}
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			//reset_timer();
		}
    }	
	
	class MouseClick implements OnClickListener{    
		public void onClick(View v) {
			// TODO Auto-generated method stub	
	    	//reset_timer();
			switch (v.getId()) {
				case R.id.arrow_left:
					break;
			}
		}
     }

	@Override
       public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		//reset_timer();
		if(!connected){
			return true;
			}
		switch (keyCode) {
			/*
			case KeyEvent.KEYCODE_DPAD_LEFT:
				DTVListDealLeftAndRightKey(0);
				break;		
			case KeyEvent.KEYCODE_DPAD_RIGHT:	
				DTVListDealLeftAndRightKey(1);
				break;	
			*/	
			case KeyEvent.KEYCODE_DPAD_DOWN:
				Log.d(TAG,"list_status="+list_status+"---cur_select_item="+cur_select_item);
				if(list_status==0){
					if(cur_select_item == ListView_channel.getCount()-1){
				    	ListView_channel.setSelection(0); 	
						return true;
					}	
				}
				else if(list_status==1){
					Log.d(TAG,"eit_list_cur_pos="+eit_list_cur_pos+"---EitListView.getCount()="+EitListView.getCount());
					if(eit_list_cur_pos == EitListView.getCount()-1){
						EitListView.requestFocus();
						EitListView.setSelection(0); 	
						return true;
					}	
				}
				else if(list_status==-1){
					if(EitListView.getCount()>0){
						EitListView.requestFocus();
						EitListView.setSelection(0); 
						return true;
					}	
				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				if(list_status==1){
					switch(current_date_index){
						case 0:
							date_button0.requestFocus();
							break;
						case 1:
							date_button1.requestFocus();
							break;
						case 2:
							date_button2.requestFocus();
							break;
						case 3:
							date_button3.requestFocus();
							break;
						case 4:
							date_button4.requestFocus();
							break;
						case 5:
							date_button5.requestFocus();
							break;	
						case 6:
							date_button6.requestFocus();
							break;	
					}
				}	
				else if(list_status==0){
					/*
					if(cur_select_item == 0){
						ListView_channel.setSelection(ListView_channel.getCount()-1); 
						return true;
					}	
					*/
				}
				break;
			case KeyEvent.KEYCODE_BACK:	
				DTVEpg.this.finish();
				return true;
			case DTVActivity.KEYCODE_YELLOW_BUTTON:
				if(list_status==1){
					if(EitListView.getChildCount()>eit_list_cur_pos)
						EitListView.setSelection(0);
					else{
						EitListView.setSelection(eit_list_cur_pos-EitListView.getChildCount());
					}
					mEitListDBAdapter.notifyDataSetChanged();
				}
				else if(list_status==0){
					if(ListView_channel.getChildCount()>cur_select_item)
						ListView_channel.setSelection(0);
					else{
						ListView_channel.setSelection(cur_select_item-ListView_channel.getChildCount());
					}
					myAdapter.notifyDataSetChanged();
				}
				break;
			case DTVActivity.KEYCODE_BLUE_BUTTON:
				int p=0;
				if(list_status==1){
					p = eit_list_cur_pos+EitListView.getChildCount();
					if(p<EitListView.getCount())
						EitListView.setSelection(p-1);
					else{
						EitListView.setSelection(EitListView.getCount()-1);
					}
					mEitListDBAdapter.notifyDataSetChanged();
				}
				else if(list_status==0){
					p = cur_select_item+ListView_channel.getChildCount();
					if(p<ListView_channel.getCount())
						ListView_channel.setSelection(p-1);
					else{
						ListView_channel.setSelection(ListView_channel.getCount()-1);
					}
					myAdapter.notifyDataSetChanged();
				}
									
				break;	
			case DTVActivity.KEYCODE_RED_BUTTON:	
				Intent Intent_booklist = new Intent();
				Intent_booklist.setClass(DTVEpg.this, DTVBookList.class);
				startActivityForResult(Intent_booklist,22);
				break;
		}
		return super.onKeyDown(keyCode, event);
	}	  

	
	Rotate3D lQuest1Animation=null;		
	Rotate3D lQuest2Animation =null;	
 	Rotate3D rQuest1Animation=null;	
    Rotate3D rQuest2Animation=null;	
	public void initAnimation() {
		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		int mCenterX = dm.widthPixels / 2;
		int mCenterY = dm.heightPixels / 2;
		
		int duration = 300;
		lQuest1Animation = new Rotate3D(0, -90, mCenterX, mCenterY);	
		lQuest1Animation.setFillAfter(true);
		lQuest1Animation.setDuration(duration);

		lQuest2Animation = new Rotate3D(90, 0, mCenterX, mCenterY);		
		lQuest2Animation.setFillAfter(true);
		lQuest2Animation.setDuration(duration);

		rQuest1Animation = new Rotate3D(0, 90, mCenterX, mCenterY);		
		rQuest1Animation.setFillAfter(true);
		rQuest1Animation.setDuration(duration);

		rQuest2Animation = new Rotate3D(-90, 0, mCenterX, mCenterY);	
		rQuest2Animation.setFillAfter(true);
		rQuest2Animation.setDuration(duration);
	}

	class channelListButtonClick  implements android.view.View.OnClickListener{	  
		public void onClick(View v) {		
			// TODO Auto-generated method stub		
			switch (v.getId()) {			
				case R.id.prolist_button:		
					if((service_type == TVProgram.TYPE_RADIO)&&(favor!=true)){
						getListData(0);
						Text_title.setText(R.string.tv);
						service_type = TVProgram.TYPE_TV;
						myAdapter.notifyDataSetChanged();
					}
					else if((service_type == TVProgram.TYPE_TV)&&(favor!=true)){
						Log.d(TAG,"##########"+class_total);
						if(class_total>0){	
							service_type = -1;
						   	cur_class_no = class_total-1;
							Text_title.setText(mTVGroup[cur_class_no].getName());
							getClassData(mTVGroup[cur_class_no].getID());
							myAdapter.notifyDataSetChanged();
						}
						else{					
							Text_title.setText(R.string.favorite);
							getListFavorite();
							myAdapter.notifyDataSetChanged();
							favor=true;
						}	
					}	
					else if((favor!=true)&&(service_type != TVProgram.TYPE_TV)&&(service_type != TVProgram.TYPE_RADIO)){
						if(cur_class_no>0&&class_total>0)
						{
							service_type = -1;
							cur_class_no --;
							Text_title.setText(mTVGroup[cur_class_no].getName());
							getClassData(mTVGroup[cur_class_no].getID());
							myAdapter.notifyDataSetChanged();
							
						}
						else
						{
							Text_title.setText(R.string.favorite);
							getListFavorite();
							myAdapter.notifyDataSetChanged();
							favor=true;
						}
					}
					else if(favor==true)
					{
						getListData(1);
						Text_title.setText(R.string.radio);
						service_type = TVProgram.TYPE_RADIO;
						myAdapter.notifyDataSetChanged();
						favor=false;
					}	
					setFocusPosition();							
					break;
				case R.id.date_button0:
					date_button0.setBackgroundResource(R.drawable.epg_date_button_press);		
					date_button1.setBackgroundResource(R.drawable.epg_date_button);
					date_button2.setBackgroundResource(R.drawable.epg_date_button);		
					date_button3.setBackgroundResource(R.drawable.epg_date_button);
					date_button4.setBackgroundResource(R.drawable.epg_date_button);		
					date_button5.setBackgroundResource(R.drawable.epg_date_button);
					date_button6.setBackgroundResource(R.drawable.epg_date_button);
					
					refresh_Eitlistview(0);
					current_date_index = 0;
					break;
				case R.id.date_button1:
					date_button0.setBackgroundResource(R.drawable.epg_date_button);		
					date_button1.setBackgroundResource(R.drawable.epg_date_button_press);
					date_button2.setBackgroundResource(R.drawable.epg_date_button);		
					date_button3.setBackgroundResource(R.drawable.epg_date_button);
					date_button4.setBackgroundResource(R.drawable.epg_date_button);		
					date_button5.setBackgroundResource(R.drawable.epg_date_button);
					date_button6.setBackgroundResource(R.drawable.epg_date_button);
					refresh_Eitlistview(1);
					current_date_index = 1;
					break;
				case R.id.date_button2:
					date_button0.setBackgroundResource(R.drawable.epg_date_button);		
					date_button1.setBackgroundResource(R.drawable.epg_date_button);
					date_button2.setBackgroundResource(R.drawable.epg_date_button_press);		
					date_button3.setBackgroundResource(R.drawable.epg_date_button);
					date_button4.setBackgroundResource(R.drawable.epg_date_button);		
					date_button5.setBackgroundResource(R.drawable.epg_date_button);
					date_button6.setBackgroundResource(R.drawable.epg_date_button);
					refresh_Eitlistview(2);
					current_date_index = 2;
					break;	
				case R.id.date_button3:
					date_button0.setBackgroundResource(R.drawable.epg_date_button);		
					date_button1.setBackgroundResource(R.drawable.epg_date_button);
					date_button2.setBackgroundResource(R.drawable.epg_date_button);		
					date_button3.setBackgroundResource(R.drawable.epg_date_button_press);
					date_button4.setBackgroundResource(R.drawable.epg_date_button);		
					date_button5.setBackgroundResource(R.drawable.epg_date_button);
					date_button6.setBackgroundResource(R.drawable.epg_date_button);
					refresh_Eitlistview(3);
					current_date_index = 3;
					break;
				case R.id.date_button4:
					date_button0.setBackgroundResource(R.drawable.epg_date_button);		
					date_button1.setBackgroundResource(R.drawable.epg_date_button);
					date_button2.setBackgroundResource(R.drawable.epg_date_button);		
					date_button3.setBackgroundResource(R.drawable.epg_date_button);
					date_button4.setBackgroundResource(R.drawable.epg_date_button_press);		
					date_button5.setBackgroundResource(R.drawable.epg_date_button);
					date_button6.setBackgroundResource(R.drawable.epg_date_button);
					refresh_Eitlistview(4);
					current_date_index = 4;
					break;
				case R.id.date_button5:
					date_button0.setBackgroundResource(R.drawable.epg_date_button);		
					date_button1.setBackgroundResource(R.drawable.epg_date_button);
					date_button2.setBackgroundResource(R.drawable.epg_date_button);		
					date_button3.setBackgroundResource(R.drawable.epg_date_button);
					date_button4.setBackgroundResource(R.drawable.epg_date_button);		
					date_button5.setBackgroundResource(R.drawable.epg_date_button_press);
					date_button6.setBackgroundResource(R.drawable.epg_date_button);
					refresh_Eitlistview(5);
					current_date_index = 5;
					break;
				case R.id.date_button6:
					date_button0.setBackgroundResource(R.drawable.epg_date_button);		
					date_button1.setBackgroundResource(R.drawable.epg_date_button);
					date_button2.setBackgroundResource(R.drawable.epg_date_button);		
					date_button3.setBackgroundResource(R.drawable.epg_date_button);
					date_button4.setBackgroundResource(R.drawable.epg_date_button);		
					date_button5.setBackgroundResource(R.drawable.epg_date_button);
					date_button6.setBackgroundResource(R.drawable.epg_date_button_press);
					refresh_Eitlistview(6);
					current_date_index = 6;
					break;
			}		

		}	
	}
	
	class dateButtonFocusChange  implements android.view.View.OnFocusChangeListener{	  
		public void onFocusChange(View v, boolean isFocused){	
			// TODO Auto-generated method stub		
			if(isFocused){
				switch (v.getId()) {	
					case R.id.prolist_button:
						list_status=0;
						break;
					case R.id.date_button0:
				
						date_button0.setBackgroundResource(R.drawable.epg_date_button_press);		
						date_button1.setBackgroundResource(R.drawable.epg_date_button);
						date_button2.setBackgroundResource(R.drawable.epg_date_button);		
						date_button3.setBackgroundResource(R.drawable.epg_date_button);
						date_button4.setBackgroundResource(R.drawable.epg_date_button);		
						date_button5.setBackgroundResource(R.drawable.epg_date_button);
						date_button6.setBackgroundResource(R.drawable.epg_date_button);
						
						refresh_Eitlistview(0);
						current_date_index = 0;
						break;
					case R.id.date_button1:
						date_button0.setBackgroundResource(R.drawable.epg_date_button);		
						date_button1.setBackgroundResource(R.drawable.epg_date_button_press);
						date_button2.setBackgroundResource(R.drawable.epg_date_button);		
						date_button3.setBackgroundResource(R.drawable.epg_date_button);
						date_button4.setBackgroundResource(R.drawable.epg_date_button);		
						date_button5.setBackgroundResource(R.drawable.epg_date_button);
						date_button6.setBackgroundResource(R.drawable.epg_date_button);
						refresh_Eitlistview(1);
						current_date_index = 1;
						list_status=-1;
						break;
					case R.id.date_button2:
						date_button0.setBackgroundResource(R.drawable.epg_date_button);		
						date_button1.setBackgroundResource(R.drawable.epg_date_button);
						date_button2.setBackgroundResource(R.drawable.epg_date_button_press);		
						date_button3.setBackgroundResource(R.drawable.epg_date_button);
						date_button4.setBackgroundResource(R.drawable.epg_date_button);		
						date_button5.setBackgroundResource(R.drawable.epg_date_button);
						date_button6.setBackgroundResource(R.drawable.epg_date_button);
						refresh_Eitlistview(2);
						current_date_index = 2;
						list_status=-1;
						break;	
					case R.id.date_button3:
						date_button0.setBackgroundResource(R.drawable.epg_date_button);		
						date_button1.setBackgroundResource(R.drawable.epg_date_button);
						date_button2.setBackgroundResource(R.drawable.epg_date_button);		
						date_button3.setBackgroundResource(R.drawable.epg_date_button_press);
						date_button4.setBackgroundResource(R.drawable.epg_date_button);		
						date_button5.setBackgroundResource(R.drawable.epg_date_button);
						date_button6.setBackgroundResource(R.drawable.epg_date_button);
						refresh_Eitlistview(3);
						current_date_index = 3;
						list_status=-1;
						break;
					case R.id.date_button4:
						date_button0.setBackgroundResource(R.drawable.epg_date_button);		
						date_button1.setBackgroundResource(R.drawable.epg_date_button);
						date_button2.setBackgroundResource(R.drawable.epg_date_button);		
						date_button3.setBackgroundResource(R.drawable.epg_date_button);
						date_button4.setBackgroundResource(R.drawable.epg_date_button_press);		
						date_button5.setBackgroundResource(R.drawable.epg_date_button);
						date_button6.setBackgroundResource(R.drawable.epg_date_button);
						refresh_Eitlistview(4);
						current_date_index = 4;
						list_status=-1;
						break;
					case R.id.date_button5:
						date_button0.setBackgroundResource(R.drawable.epg_date_button);		
						date_button1.setBackgroundResource(R.drawable.epg_date_button);
						date_button2.setBackgroundResource(R.drawable.epg_date_button);		
						date_button3.setBackgroundResource(R.drawable.epg_date_button);
						date_button4.setBackgroundResource(R.drawable.epg_date_button);		
						date_button5.setBackgroundResource(R.drawable.epg_date_button_press);
						date_button6.setBackgroundResource(R.drawable.epg_date_button);
						refresh_Eitlistview(5);
						current_date_index = 5;
						list_status=-1;
						break;
					case R.id.date_button6:
						date_button0.setBackgroundResource(R.drawable.epg_date_button);		
						date_button1.setBackgroundResource(R.drawable.epg_date_button);
						date_button2.setBackgroundResource(R.drawable.epg_date_button);		
						date_button3.setBackgroundResource(R.drawable.epg_date_button);
						date_button4.setBackgroundResource(R.drawable.epg_date_button);		
						date_button5.setBackgroundResource(R.drawable.epg_date_button);
						date_button6.setBackgroundResource(R.drawable.epg_date_button_press);
						refresh_Eitlistview(6);
						current_date_index = 6;
						list_status=-1;
						break;
				}		
			}
		}	
	}

	public class EventHandler extends Handler {  
		public EventHandler(Looper looper) {  
		     super(looper);  
		}  

		@Override  
		public void handleMessage(Message msg) {  
			   switch (msg.what) {
			   	case 0:
					sync_date();
					refresh_Eitlistview(0);
					if(myAdapter!=null)
						myAdapter.notifyDataSetChanged();
					break;
				case 1:
					setup_timeupdatethread();
					refresh_currenttime();
					refresh_dates(0);
					break;
				case 2:
					showUpdateDialog();
					break;
				case 3:				        
					dismissUpdateDialog();
					break;
				case 4:
				case 5:
					if(myAdapter!=null)
						myAdapter.notifyDataSetChanged();
					if(ListView_channel!=null)
						ListView_channel.requestFocus();
					break;
					
			}
		}
	}  
	
	class EitUpdateThread extends Thread {
		private Handler mHandler = null;
		private static final int EIT_UPDATE = 0;
		private static final int DATA_RADIO = 4;
		private static final int DATA_TV = 5;
		public void run() {
			Looper.prepare();

			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					//Log.d(TAG,"------------------------EitUpdateThread---------------");
					Message message=new Message();
					EventHandler ha =new EventHandler(Looper.getMainLooper());
					switch (msg.what) { 
						case DATA_RADIO:
							getListData(1);
							message.what=4;
							ha.sendMessage(message);
							break;
						case DATA_TV:
							getListData(0);
							message.what=5;
							ha.sendMessage(message);
							break;	
						case EIT_UPDATE:
							{		
								setup_db();
								message.what=0;
								ha.sendMessage(message);
							}
							break;
						case 1:
								message.what=1;
								ha.sendMessage(message);
							break;
						case 2:
								message.what=2;
								ha.sendMessage(message);
							break;
						case 3:
								message.what=3;
								ha.sendMessage(message);
							break;	
						default:
							break;
					}  
				}
			};

			Looper.loop();
			//Log.d(TAG, "work thread will now exit.");
		}

		public void quitLoop() {
			if (mHandler != null && mHandler.getLooper() != null) {
				mHandler.getLooper().quit();
			}
		}
		
		public void onSetupCmd(int cmd, Object para ) {
			if (mHandler != null){
				mHandler.sendMessage(mHandler.obtainMessage(cmd,para));	
			}	
		}

		

	}
	
}

