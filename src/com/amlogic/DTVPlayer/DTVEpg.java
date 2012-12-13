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
import android.os.*;
import java.lang.reflect.Field;

public class DTVEpg extends DTVActivity{
	private static final String TAG="DTVEpg";


	private boolean dtv_standard_atsc=false;

    /*******eit check time **************/
	private final static int    EIT_NEW_CHECKTIME               = 55;
	/****ctrl constant *****/
	private final static int    EIT_ITEM_COUNT                  = 7;
	
	private final static int    EIT_ITEM_WIDTHOF_HOURBG         = 240;
	
	private final static int    EIT_ITEM_LEFTMARGINOF_HOURBG    = 1;
	private final static int    EIT_ITEM_COLOROF_HOURBG         = Color.argb(200, 0, 0, 0);
	
	private final static int    EIT_ITEM_HEIGHTOF_DATEBG        = 67;
	private final static int    EIT_ITEM_BOTTOMMARGINOF_DATEBG  = 1;
	private final static int    EIT_ITEM_LEFTMARGINOF_DATEBG    = 10;
	private final static int    EIT_ITEM_COLOROF_DATEBG         = Color.argb(200, 0, 0, 0);

	private final static int    EIT_ITEM_CONTENT_MAXCOUNT       = 100;
	private final static float  EIT_ITEM_CONTENT_TEXTSIZE       = 20F;
	
	private final static int    EIT_ITEM_CONTENT_LEFTMARGIN     = 1;
	private final static int    EIT_ITEM_CONTENT_BOTTOMMARGIN   = 1;
	
	private final static int    EIT_TIEM_FOCUSCOLOR             = Color.argb(200, 255, 180, 0);
	private final static int    EIT_TIEM_UNFOCUSCOLOR_ODD       = Color.argb(200, 75, 75, 75);
	private final static int    EIT_TIEM_UNFOCUSCOLOR_EVEN      = Color.argb(200, 42, 42, 42);
	
	
	/*********************ctrl id*************************************/
	private final static int    EIT_TITLE_PROGNAME_ID           = 0;
	private final static int    EIT_TITLE_CURRENTTIME_ID        = EIT_TITLE_PROGNAME_ID+1;
	private final static int    EIT_TITLE_EVENTNAME_ID          = EIT_TITLE_PROGNAME_ID+2;
	private final static int    EIT_TITLE_EVENTDETAIL_ID        = EIT_TITLE_PROGNAME_ID+3;
	private final static int    EIT_FIRSTDAY_TEXTVIEW_ID        = EIT_TITLE_PROGNAME_ID+4;

	/*********************layout size*************************************/
	private final int LAYOUT_SMALL = 0;
	private final int LAYOUT_MID   = 1;
	private final int LAYOUT_LARGE = 2;
	private final int LAYOUT_SMALL576 = 3;
	
	private int Layout_size = LAYOUT_MID;

    private int EIT_ITEM_HEIGHTOF_HOURBG = 39;
	private int EIT_ITEM_CONTENT_HEIGHT  = EIT_ITEM_HEIGHTOF_DATEBG;
	private int EIT_ITEMWIDTH_PERMINUTE  = EIT_ITEM_WIDTHOF_HOURBG/60; // (EIT_ITEM_WIDTHOF_HOURBG/60);
	
	
	private Handler  currenttimer_handler;
	private Runnable currenttimer_runnable;
	
	private Handler  waiting_handler;
	private ProgressDialog progressDialog;  
	
	/********ctrl var *************/
	private View TempItemView;
	
	private LinearLayout EPG_parent_linearlayout;
	private ListView EitListView;
	private Button   ExitButton;
	private TextView HelpInfoTextView;
	private int Eit_ItemColor[];
	
	/********db var*********/
	private Cursor srv_Cursor;  
	private Cursor eit_Cursor[];
	private int cur_service_id=-1;
	private int cur_source_id;

	/**********count var*******************/
	private int  eit_notify = 0;
	private long tickcount  = 1;

	private int  TempFlag   = 0;	
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.dtvepg);
	}

	public void onStart(){
		super.onStart();
		DTVEpgUIInit();
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
			case TVMessage.TYPE_EVENT_UPDATE:
				eit_notify ++ ;
				break;	
			default:
				break;
	
		}
	}

	private void DTVEpgUIInit(){
		Layout_size = LAYOUT_MID;
		EIT_ITEM_CONTENT_HEIGHT = EIT_ITEM_HEIGHTOF_DATEBG;
		EIT_ITEM_HEIGHTOF_HOURBG = 39;
		EIT_ITEMWIDTH_PERMINUTE  = EIT_ITEM_WIDTHOF_HOURBG/60;
        
		cur_service_id = DTVEpgGetID();
        Eit_ItemColor = new int[2];
        Eit_ItemColor[0] = EIT_TIEM_UNFOCUSCOLOR_ODD;
        Eit_ItemColor[1] = EIT_TIEM_UNFOCUSCOLOR_EVEN;

		/*setup view*/
        EPG_parent_linearlayout = (LinearLayout)findViewById(R.id.epg_layout_parent);
		EPG_parent_linearlayout.setVisibility(View.INVISIBLE);
        EitListView  = (ListView)findViewById(R.id.EitListView);
        EitListView.setItemsCanFocus(true);
       	HelpInfoTextView   = (TextView)findViewById(R.id.epg_tv_helpinfo);
		    
	    progressDialog = ProgressDialog.show(DTVEpg.this, getString(R.string.epg_loading), getString(R.string.epg_waiting), true, false);   
		WindowManager.LayoutParams lp=progressDialog.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		progressDialog.getWindow().setAttributes(lp);
		progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);	

        waiting_handler = new Handler(){     
            public void handleMessage(Message msg) {   
            	switch (msg.arg1)
            	{
					case 0:
						try{
							create_proganddate();
		                }
		                catch (Exception e){
		                }

						try{
							create_eventnameanddetailinfo();
		                }
		                catch (Exception e){
		                }  
						
						try{
							create_hours();
		                }
		                catch (Exception e){
		                }  
			                    
						try{
							create_dates();
		                }
		                catch (Exception e){
		                }  
	            	break;
	            	
	            	case 1:
	                    refresh_progname();
        				refresh_currenttime();
	                    refresh_dates(0);
	                    setup_timeupdatethread();
	            	break;
	            	
	            	case 2:
						refresh_Eitlistview();
	            	break;

					case 3:
	                    EPG_parent_linearlayout.setVisibility(View.VISIBLE);
	                    progressDialog.dismiss();  
						if (TempFlag == 0){
							TempFlag = 1;
							moveto_currentevent();
						}
	            	break;
            	}
            }
        };  

        new Thread(){     
            public void run() {   
         		Message msg;
                try{
                	Thread.sleep(1000);
                	setup_db();
                }
                catch (Exception e){
                }
                
                try{
                    msg = waiting_handler.obtainMessage();
                    msg.arg1 = 0;
                	waiting_handler.sendMessage(msg); 
                	Thread.sleep(200);
                }
                catch (Exception e){
                }

                try{
                    msg = waiting_handler.obtainMessage();
                    msg.arg1 = 1;
                	waiting_handler.sendMessage(msg);
                	Thread.sleep(200);
                }
                catch (Exception e){
                }    	
                
                try{
                    msg = waiting_handler.obtainMessage();
                    msg.arg1 = 2;
                	waiting_handler.sendMessage(msg);
                	Thread.sleep(1000);
                }
                catch (Exception e){
                } 

                try{
                    msg = waiting_handler.obtainMessage();
                    msg.arg1 = 3;
                	waiting_handler.sendMessage(msg);
                	//Thread.sleep(1000);
                }
                catch (Exception e){
                } 
				
            }
         }.start(); 
	}



	private long get_firstmillisofcurrentday(){
		Date date1 = new Date();
		Date date2 = new Date(date1.getTime());

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
		Cursor PassedEit_Cursor = DTVEpg_select_passedeit(get_currentdb_srv_id(), 
			get_firstmillisofcurrentday()/1000, get_current_datetime().getTime()/1000);

		if(PassedEit_Cursor!=null){
			if (PassedEit_Cursor.getCount()>0){
				PassedEit_Cursor.moveToFirst();
				if(PassedEit_Cursor.getLong(PassedEit_Cursor.getColumnIndex("start")) >  (get_firstmillisofcurrentday()/1000))
					value = PassedEit_Cursor.getCount()+1;
				else
					value = PassedEit_Cursor.getCount();
			}	
			else
				value = 0; 
			
			PassedEit_Cursor.close();
		}	
		value = 0;
		return value;

	}

	private void moveto_currentevent(){
		View TempButton = DTVEpg.this.getCurrentFocus();
    	if (TempButton == null)
			Log.d("moveto_currentevent >>>>>>>", "TempButton is null");
		else{	
			Log.d("moveto_currentevent>>>>>>>", TempButton.toString());			

			if (get_current_event_index()>1){
	        	for(int i = 0; i<get_current_event_index()-1; i++)
	    		{
	    			if (TempButton.focusSearch(View.FOCUS_RIGHT) != null)
	        		{	
	        			TempButton = TempButton.focusSearch(View.FOCUS_RIGHT);
						Log.d("moveto_currentevent >>>>>>>>>", TempButton.toString());
					}
					else
					{
						Log.d("moveto_currentevent>>>>>>>>>", "null");
					}
					Log.d("moveto_currentevent>>>>>>>", ""+i);
				}
				
				(TempButton).requestFocus();
			}
		}
    }
	
	private void create_proganddate(){
		LinearLayout proganddate_view  = (LinearLayout)findViewById(R.id.epg_layout_ProgNameAndDate);
		TextView TempTexView;
		TempTexView = new TextView(DTVEpg.this);
		TempTexView.setId(EIT_TITLE_PROGNAME_ID);
		TempTexView.setText("");

		TempTexView.setTextSize(24F);
		switch(Layout_size){
			case LAYOUT_SMALL:
				TempTexView.setTextSize(18F);
			break;
			case LAYOUT_SMALL576:
				TempTexView.setTextSize(20F);
			break;
			case LAYOUT_MID:
				TempTexView.setTextSize(24F);
			break;
			
			case LAYOUT_LARGE:
				TempTexView.setTextSize(28F);
			break;
		}
		
		TextPaint tp = TempTexView.getPaint();
        tp.setFakeBoldText(true);
		TempTexView.setTextColor(Color.WHITE);
		TempTexView.setGravity(Gravity.LEFT);

		TempTexView.setHeight(40);
		switch(Layout_size){
			case LAYOUT_SMALL:
				TempTexView.setHeight(35);
			break;
			case LAYOUT_SMALL576:
				TempTexView.setHeight(38);
			break;
			
			case LAYOUT_MID:
				TempTexView.setHeight(40);
			break;
			
			case LAYOUT_LARGE:
				TempTexView.setHeight(60);
			break;
		}
		
		((LinearLayout)proganddate_view).addView(TempTexView); 
		TempTexView = new TextView(DTVEpg.this);
		TempTexView.setId(EIT_TITLE_CURRENTTIME_ID);
		TempTexView.setText("");
		TempTexView.setTextSize(20F);
		switch(Layout_size)
		{
			case LAYOUT_SMALL:
				TempTexView.setTextSize(15F);
			break;
			case LAYOUT_SMALL576:
				TempTexView.setTextSize(18F);
			break;
			case LAYOUT_MID:
				TempTexView.setTextSize(20F);
			break;
			
			case LAYOUT_LARGE:
				TempTexView.setTextSize(20F);
			break;
		}
		
		TempTexView.setTextColor(Color.WHITE);
		TempTexView.setGravity(Gravity.LEFT);
		TempTexView.setHeight(36);

		switch(Layout_size)
		{
			case LAYOUT_SMALL:
				TempTexView.setHeight(30);
			break;
			case LAYOUT_SMALL576:
				TempTexView.setHeight(33);
			break;
			case LAYOUT_MID:
				TempTexView.setHeight(36);
			break;
			case LAYOUT_LARGE:
				TempTexView.setHeight(80);
			break;
		}

		((LinearLayout)proganddate_view).addView(TempTexView); 

	}
	
	/******************used after connect service*******************/

	private Date get_current_datetime(){
		Date date = new Date();
		return date;
	}
	
	private void refresh_currenttime(){		
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		String today = sdf.format(date); 
		TextView TempTexView; 
		TempTexView = (TextView)findViewById(EIT_TITLE_CURRENTTIME_ID);
		TempTexView.setText((""+today));
	}
	
	private void refresh_progname(){		
		TextView TempTexView; 
		TempTexView = (TextView)findViewById(EIT_TITLE_PROGNAME_ID);
		TempTexView.setText(DTVEpg_get_currentprogname());
	}
	
	private void setup_db(){        
		eit_Cursor = new Cursor[7];
        
        eit_Cursor[0] = DTVEpg_getCurrentDateEIT(get_currentdb_srv_id(), 
        						get_current_datetime().getTime()/1000,get_firstmillisofcurrentday()/1000);
		Log.d("eit_Cursor[0]:",""+eit_Cursor[0].getCount());
        eit_Cursor[1] = DTVEpg_getDateEIT(get_currentdb_srv_id(), 
        						get_firstmillisofcurrentday()/1000+ 1 * 24 * 60 * 60);
		Log.d("eit_Cursor[1]:",""+eit_Cursor[1].getCount());
        eit_Cursor[2] = DTVEpg_getDateEIT(get_currentdb_srv_id(), 
        						get_firstmillisofcurrentday()/1000+ 2 * 24 * 60 * 60);
		Log.d("eit_Cursor[2]:",""+eit_Cursor[2].getCount());
        eit_Cursor[3] = DTVEpg_getDateEIT(get_currentdb_srv_id(), 
        						get_firstmillisofcurrentday()/1000+ 3 * 24 * 60 * 60);
		Log.d("eit_Cursor[3]:",""+eit_Cursor[3].getCount());
        eit_Cursor[4] = DTVEpg_getDateEIT(get_currentdb_srv_id(), 
        						get_firstmillisofcurrentday()/1000+ 4 * 24 * 60 * 60);
		Log.d("eit_Cursor[4]:",""+eit_Cursor[4].getCount());
        eit_Cursor[5] = DTVEpg_getDateEIT(get_currentdb_srv_id(), 
        						get_firstmillisofcurrentday()/1000+ 5 * 24 * 60 * 60);
		Log.d("eit_Cursor[5]:",""+eit_Cursor[5].getCount());
        eit_Cursor[6] = DTVEpg_getDateEIT(get_currentdb_srv_id(), 
        						get_firstmillisofcurrentday()/1000+ 6 * 24 * 60 * 60);
		Log.d("eit_Cursor[6]:",""+eit_Cursor[6].getCount());

		if (eit_Cursor[0].getCount() >0)
			HelpInfoTextView.setText(R.string.epg_operate_des01);
		else
			HelpInfoTextView.setText(R.string.epg_no_epg_info);
	}
	
	private void refresh_Eitlistview(){
        EitListView.setAdapter(new EitListDBAdapter(this)); 
	}

	private  void SetAlarm(long time){
		/*		
		Intent intent = new Intent(this, AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent,0);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, time*1000, pi);	
		*/						
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
				//cv.put("sub_flag", 1);
				//this.getContentResolver().update(DVBClient.TABLE_EVENT, cv, "db_id= "+eventid, null); 
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
		if (bookflag == 0)
			((Button)view).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		else if (bookflag == 1)
			((Button)view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.epg_event_book_1, 0, 0, 0);
		else if (bookflag == 2)
			((Button)view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.epg_event_book_2, 0, 0, 0);
	}
	
	private void setup_timeupdatethread(){
	    currenttimer_handler = new Handler();
	    currenttimer_runnable = new Runnable() {
			public void run() {
				tickcount++;
				refresh_currenttime();
				currenttimer_handler.postDelayed(currenttimer_runnable, 1000);
				if (((tickcount % EIT_NEW_CHECKTIME) == 0)&&(eit_notify >0)){
					update_new_eit();
				}
			}   
		};
	    currenttimer_handler.postDelayed(currenttimer_runnable, 1000);
	}
	
	
	private void create_eventnameanddetailinfo(){
		LinearLayout eventname_detailinfo_view  = (LinearLayout)findViewById(R.id.epg_layout_EventNameAndDetailInfo);
		LinearLayout.LayoutParams TempLP; 
	
		TextView TempTexView;
		
		TempTexView = new TextView(DTVEpg.this);
		TempLP= new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);					
			
		TempTexView.setSingleLine(true);
		TempTexView.setLayoutParams(TempLP);
		TempTexView.setId(EIT_TITLE_EVENTNAME_ID);
		TempTexView.setText("                                  ");

		TempTexView.setTextSize(20F);
		switch(Layout_size)
		{
			case LAYOUT_SMALL:		
				TempTexView.setTextSize(15F);
			break;
			case LAYOUT_SMALL576:		
				TempTexView.setTextSize(17F);
			break;
			case LAYOUT_MID:
				TempTexView.setTextSize(20F);
			break;
			
			case LAYOUT_LARGE:
				TempTexView.setTextSize(22F);
			break;
		}
	
		TempTexView.setTextColor(Color.WHITE);
		TempTexView.setGravity(Gravity.CENTER_VERTICAL);
		
		/*sth wrong if set WRAP_CONTENT
		 * TempTexView.setBackgroundResource(R.drawable.eventname_bg_mid);
		TempTexView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.eventname_bg_left, 0, R.drawable.eventname_bg_right, 0);
		*/

		((LinearLayout)eventname_detailinfo_view).addView(TempTexView); 
		
		TempTexView = new TextView(DTVEpg.this);
		TempLP= new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 1);					
		TempTexView.setLayoutParams(TempLP);
		TempTexView.setBackgroundResource(R.drawable.epg_event_name_divider);
		((LinearLayout)eventname_detailinfo_view).addView(TempTexView); 

		
		TempTexView = new TextView(DTVEpg.this);
		TempLP= new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);					
		TempLP.topMargin = 1;
		TempTexView.setLayoutParams(TempLP);
		TempTexView.setId(EIT_TITLE_EVENTDETAIL_ID);
		TempTexView.setText("");
		TempTexView.setTextColor(Color.WHITE);

		TempTexView.setTextSize(15F);
		TempTexView.setOnClickListener(new DetailOnClick());

		switch(Layout_size)
		{
			case LAYOUT_SMALL:
				TempTexView.setTextSize(12F);
			break;
			case LAYOUT_SMALL576:		
				TempTexView.setTextSize(13F);
			break;
			case LAYOUT_MID:
				TempTexView.setTextSize(15F);
			break;
			
			case LAYOUT_LARGE:
				TempTexView.setTextSize(18F);
			break;
		}
		
		TempTexView.setGravity(Gravity.LEFT);
		((LinearLayout)eventname_detailinfo_view).addView(TempTexView); 
		((LinearLayout)eventname_detailinfo_view).setVisibility(View.INVISIBLE);
	}
	
	private void refresh_eventnameanddetailinfo(String progname, String detailinfo){
		TextView TempTexView; 
		TempTexView = (TextView)findViewById(EIT_TITLE_EVENTNAME_ID);
		TempTexView.setText(progname);

		TempTexView = (TextView)findViewById(EIT_TITLE_EVENTDETAIL_ID);
		TempTexView.setText(detailinfo);
		
		LinearLayout eventname_detailinfo_view  = (LinearLayout)findViewById(R.id.epg_layout_EventNameAndDetailInfo);
		eventname_detailinfo_view.setVisibility(View.VISIBLE);
	}

	private void clear_eventnameanddetailinfo(){
		TextView TempTexView; 
		TempTexView = (TextView)findViewById(EIT_TITLE_EVENTNAME_ID);
		TempTexView.setText("");

		TempTexView = (TextView)findViewById(EIT_TITLE_EVENTDETAIL_ID);
		TempTexView.setText("");
		
		LinearLayout eventname_detailinfo_view  = (LinearLayout)findViewById(R.id.epg_layout_EventNameAndDetailInfo);
		eventname_detailinfo_view.setVisibility(View.INVISIBLE);
	}
	
	private void create_dates(){
		LinearLayout datesView  = (LinearLayout)findViewById(R.id.epg_layout_Date);

		for(int i=0; i<7; i++)
		{
			LinearLayout.LayoutParams TempLP = 
			new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);					
			if (i == 0)
			{	
				TempLP.topMargin = EIT_ITEM_HEIGHTOF_HOURBG;

				switch(Layout_size)
				{
				case LAYOUT_SMALL:
					TempLP.topMargin = 23;
				break;
				case LAYOUT_SMALL576:		
					TempLP.topMargin = 31;
				break;
				case LAYOUT_MID:
					TempLP.topMargin = EIT_ITEM_HEIGHTOF_HOURBG;
				break;
				
				case LAYOUT_LARGE:
					TempLP.topMargin = EIT_ITEM_HEIGHTOF_HOURBG-1;
				break;
				}
			}
			TempLP.bottomMargin = EIT_ITEM_BOTTOMMARGINOF_DATEBG;
			TempLP.leftMargin   = EIT_ITEM_LEFTMARGINOF_DATEBG;
			
			TextView TempTexView;
			TempTexView = new TextView(DTVEpg.this);
			
			TempTexView.setLayoutParams(TempLP);
			TempTexView.setTextColor(Color.WHITE);

			TempTexView.setId(EIT_FIRSTDAY_TEXTVIEW_ID+i);
			
			TextPaint tp = TempTexView.getPaint();
            tp.setFakeBoldText(true);
			TempTexView.setText("");
			TempTexView.setGravity(Gravity.CENTER);


			TempTexView.setTextSize(17F);
			TempTexView.setHeight(EIT_ITEM_HEIGHTOF_DATEBG+1);
			switch(Layout_size)
			{
			case LAYOUT_SMALL:		
				TempTexView.setHeight(42+1);
				TempTexView.setTextSize(13F);
			break;

			case LAYOUT_SMALL576:		
				TempTexView.setHeight(52+1);
				TempTexView.setTextSize(15F);
			break;
			case LAYOUT_MID:
				TempTexView.setHeight(EIT_ITEM_HEIGHTOF_DATEBG+1);
				TempTexView.setTextSize(17F);
			break;
			
			case LAYOUT_LARGE:
				TempTexView.setHeight(101+1);
				TempTexView.setTextSize(20F);
			break;
			}
			((LinearLayout)datesView).addView(TempTexView); 
		}
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

			TextView TempTexView; 
			TempTexView = (TextView)findViewById(EIT_FIRSTDAY_TEXTVIEW_ID+i);
			TempTexView.setText((""+today +"\n "+ Weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]));

			TempTexView.setBackgroundColor(EIT_TIEM_UNFOCUSCOLOR_ODD);
		}
	}
	
	private void create_hours(){
		LinearLayout HoursView  = (LinearLayout)findViewById(R.id.EitHourBglinearLayout);
		HoursView.setBackgroundColor(EIT_ITEM_COLOROF_HOURBG);
		
		for(int i=0; i<24; i++)
		{
			LinearLayout.LayoutParams TempLP = 
			new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);					
			TempLP.leftMargin = 0;//EIT_ITEM_LEFTMARGINOF_HOURBG;
			
			TextView TempTexView;
			TempTexView = new TextView(DTVEpg.this);
			
			TempTexView.setLayoutParams(TempLP);
			TempTexView.setText(""+i+":00");
			TempTexView.setTextColor(Color.WHITE);

			TempTexView.setTextSize(20F);
			switch(Layout_size)
			{
				case LAYOUT_SMALL:		
					TempTexView.setTextSize(15F);
				break;
				case LAYOUT_SMALL576:		
					TempTexView.setTextSize(17F);
				break;
				case LAYOUT_MID:
					TempTexView.setTextSize(20F);
				break;
				
				case LAYOUT_LARGE:
					TempTexView.setTextSize(22F);
				break;
			}

			TempTexView.setGravity(Gravity.LEFT);
			
			TempTexView.setWidth(EIT_ITEM_WIDTHOF_HOURBG);
			TempTexView.setHeight(EIT_ITEM_HEIGHTOF_HOURBG);
			switch(Layout_size)
			{
			case LAYOUT_SMALL:
			case LAYOUT_SMALL576:		
				TempTexView.setHeight(24);
			break;
			
			case LAYOUT_MID:
				TempTexView.setHeight(EIT_ITEM_HEIGHTOF_HOURBG);
			break;
			
			case LAYOUT_LARGE:
				TempTexView.setHeight(59);//EIT_ITEM_HEIGHTOF_HOURBG*1.5
			break;
			}
				
		
			TempTexView.setBackgroundResource(R.drawable.epg_hour_bg);
			((LinearLayout)HoursView).addView(TempTexView); 
		}
	}
	
	/*disable HOME key*/
	@Override 
	public void onAttachedToWindow(){  
		//this.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_KEYGUARD);  
		super.onAttachedToWindow();  
	} 
	
	public class EitListDBAdapter extends BaseAdapter{
		private Context _Context; 
		private View create_eit_content() {
			View convertView = getLayoutInflater().inflate(R.layout.dtvepg_eitlistitem, null);
			for(int i=0; i<EIT_ITEM_CONTENT_MAXCOUNT; i++){
				LinearLayout.LayoutParams TempLP = 
					new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);					
				TempLP.leftMargin = EIT_ITEM_CONTENT_LEFTMARGIN;
				TempLP.bottomMargin = EIT_ITEM_CONTENT_BOTTOMMARGIN;
				
				Button TempButton;
				TempButton = new Button(_Context);
				
				TempButton.setId(i);
				TempButton.setTextColor(Color.WHITE);

				TempButton.setTextSize(18F);

				switch(Layout_size)
				{
					case LAYOUT_SMALL:	
						TempButton.setTextSize(13F);
					break;
					case LAYOUT_SMALL576:	
						TempButton.setTextSize(15F);
					break;
					case LAYOUT_MID:
						TempButton.setTextSize(18F);
					break;
					
					case LAYOUT_LARGE:
						TempButton.setTextSize(20);
					break;
				}

				TempButton.setLayoutParams(TempLP);
				TempButton.setVisibility(View.GONE);

				TempButton.setFocusableInTouchMode(true);
				TempButton.setOnFocusChangeListener(new EitItemOnFocusChange());
				TempButton.setOnClickListener(new EitItemOnClick());
				TempButton.setSingleLine(true);
				TempButton.setEllipsize(TextUtils.TruncateAt.valueOf("MARQUEE"));
				
				((LinearLayout)convertView).addView(TempButton); 
			}
			return convertView;
		}
		
		public EitListDBAdapter(Context context)
		{
			_Context = context;
		}
		
		public int getCount() 
		{
			return EIT_ITEM_COUNT;
		}
		
		public Object getItem(int arg0) 
		{
			return null;
		}
		
		public long getItemId(int arg0) 
		{
			return arg0;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) 
		{			
			if (convertView == null)
			{	
				convertView = create_eit_content();
			}

			if(eit_Cursor[position]!=null){
				if (eit_Cursor[position].getCount()>0){	

					int TempInt = 0;
					for(int i=0;((i<EIT_ITEM_CONTENT_MAXCOUNT)&&(i<eit_Cursor[position].getCount()));i++)
					{
						Button TempButton;
						long TempLong;
						
						eit_Cursor[position].moveToPosition(i);
						if (
							(i == 0 ) && 
							(
							        eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start")) 
									> 
							        (get_firstmillisofcurrentday()/1000+ position * 24 * 60 * 60))
							)
						{
							TempButton = (Button)((LinearLayout)convertView).findViewById(0);
							TempInt = 1;
							
							TempLong = eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start")) 
							                - (get_firstmillisofcurrentday()/1000+ position * 24 * 60 * 60);
							
							TempButton.setHeight(EIT_ITEM_CONTENT_HEIGHT);
							TempButton.setHint("-1");
							TempButton.setWidth(((int)TempLong/60)*EIT_ITEMWIDTH_PERMINUTE - 1);
							TempButton.setText("N/A");
							
							TempButton.setBackgroundColor(Eit_ItemColor[1 - (position%2)]);	
							TempButton.setVisibility(View.VISIBLE);
						}

						 
						TempButton = (Button)((LinearLayout)convertView).findViewById(i+TempInt);
						if(TempButton==null){
							return convertView;
						}

						if ((i == (eit_Cursor[position].getCount() - 1) )||(i == 0))
						{
						
							if (eit_Cursor[position].getCount() == 1)
							{

								if (eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start")) 
									< (get_firstmillisofcurrentday()/1000+ position * 24 * 60 * 60) )
								{

									if ( eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("end")) 
										< (get_firstmillisofcurrentday()/1000+ (position+1) * 24 * 60 * 60)
										)
										{
											TempLong = eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("end")) 
										           - (get_firstmillisofcurrentday()/1000+ position * 24 * 60 * 60);
										}

									else
										TempLong = 24 * 60 * 60;
										
								}

								else
								{

									if(	eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("end")) 
											>
							           (get_firstmillisofcurrentday()/1000+ (position+1) * 24 * 60 * 60) )
									{
										TempLong = (get_firstmillisofcurrentday()/1000+ (position+1) * 24 * 60 * 60)
									       - eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start"));	
									}

									else
									{
										TempLong = eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("end")) 
								          - eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start"));
									}
								}
							}	

							else
							{
								if ((i == 0 ) && 
								(
								        eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start")) 
										<
								        (get_firstmillisofcurrentday()/1000+ position * 24 * 60 * 60))
								)	
								{
									TempLong = eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("end")) 
								           - (get_firstmillisofcurrentday()/1000+ position * 24 * 60 * 60);
								}
								else
								if((i == (eit_Cursor[position].getCount() - 1) ) && 
								(
								        eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("end")) 
										>
								        (get_firstmillisofcurrentday()/1000+ (position+1) * 24 * 60 * 60))
								)	
								{
									TempLong = (get_firstmillisofcurrentday()/1000+ (position+1) * 24 * 60 * 60)
									       - eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start"));
								}
								else{

									
									
									
									if((i < (eit_Cursor[position].getCount() - 1) ))
									{
										long start = eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start"));
										eit_Cursor[position].moveToPosition(i+1);
										long end =  eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start")); 
										eit_Cursor[position].moveToPosition(i);
										TempLong = end-start;	
									}
									else{
										TempLong = eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("end")) 
							          - eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start"));

									}
								}	
							}
							
						}
						else
						{
							long start = eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start"));
							eit_Cursor[position].moveToPosition(i+1);
							long end =  eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start")); 
							eit_Cursor[position].moveToPosition(i);
							TempLong = end-start;	
							/*
							TempLong = eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("end")) 
							          - eit_Cursor[position].getLong(eit_Cursor[position].getColumnIndex("start"));
							*/          
						}

						LinearLayout.LayoutParams TempLP = 
						new LinearLayout.LayoutParams(((int)TempLong/60)*EIT_ITEMWIDTH_PERMINUTE - 1, EIT_ITEM_CONTENT_HEIGHT);					
							TempLP.leftMargin = EIT_ITEM_CONTENT_LEFTMARGIN;
							TempLP.bottomMargin = EIT_ITEM_CONTENT_BOTTOMMARGIN;
						TempButton.setLayoutParams(TempLP);	
						TempButton.setHeight(EIT_ITEM_CONTENT_HEIGHT);
						TempButton.setWidth(((int)TempLong/60)*EIT_ITEMWIDTH_PERMINUTE - 1);
						TempButton.setHint(""+i);
						TempButton.setText(""+eit_Cursor[position].getString(eit_Cursor[position].getColumnIndex("name")));	

						//TempButton.setWidth(((int)TempLong/60)*EIT_ITEMWIDTH_PERMINUTE - 1);	

						TempButton.setBackgroundColor(Eit_ItemColor[1 - (position%2)]);	
						TempButton.setVisibility(View.VISIBLE);

						final int pos = position;
						TempButton.setOnKeyListener(new OnKeyListener() { 
						public boolean onKey(View v, int keyCode, KeyEvent event) {		
					
							switch(keyCode)
							{
								case KeyEvent.KEYCODE_TAB: //info
									if (event.getAction() == KeyEvent.ACTION_DOWN) {
										showInfoDia(v,pos);	
																		}
									return true;	
							} 
							return false;
						}}); 

						

						switch(eit_Cursor[position].getInt(eit_Cursor[position].getColumnIndex("sub_flag")))
						{
							case 0:
								refresh_bookstatus(TempButton, 0);
							break;
							case 1:
								refresh_bookstatus(TempButton, 1);
							break;
							case 2:
								refresh_bookstatus(TempButton, 2);
							break;
						}
					}
				}
			}	
			return convertView;	
		}
	} 

	AlertDialog.Builder builder=null;
    	private void showInfoDia(View v,int pos){

		String message = eit_Cursor[pos].getString(eit_Cursor[pos].getColumnIndex("descr"))+"\n"+eit_Cursor[pos].getString(eit_Cursor[pos].getColumnIndex("ext_descr"));
				
		builder = new AlertDialog.Builder(DTVEpg.this) ;
		builder.setTitle((((Button)v).getText().toString()!=null&&!(((Button)v).getText().toString().equals("")))?((Button)v).getText().toString():"--");
		//builder.setMessage((eit_Cursor[pos].getString(eit_Cursor[pos].getColumnIndex("ext_descr"))!=null&&!(eit_Cursor[pos].getString(eit_Cursor[pos].getColumnIndex("ext_descr")).equals("")))?
					//eit_Cursor[pos].getString(eit_Cursor[pos].getColumnIndex("ext_descr")):eit_Cursor[pos].getString(eit_Cursor[pos].getColumnIndex("descr")));
		builder.setMessage(message);
		builder.setPositiveButton("ok", null);

		AlertDialog alert = builder.create();
		alert.show();

		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
	}	
	
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

	class EitItemOnClick  implements OnClickListener
    {
	        public void onClick(View v) 
	       {
	        	if (Integer.valueOf(((Button)v).getHint().toString()) != -1)
	        	{	
	        		try 
					{
			        	int TempInt = EitListView.getPositionForView(v);

			        	eit_Cursor[TempInt].moveToPosition(Integer.valueOf(((Button)v).getHint().toString()));

			    		Date dt_start =  new Date(eit_Cursor[TempInt].getLong(eit_Cursor[TempInt].getColumnIndex("start"))*1000);
			    		Date dt_end   =  new Date(eit_Cursor[TempInt].getLong(eit_Cursor[TempInt].getColumnIndex("end"))*1000);
			    		
			    		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
			    		String str_start = sdf.format(dt_start); 
			    		String str_end   = sdf.format(dt_end); 
			        	
					    refresh_eventnameanddetailinfo(((Button)v).getText().toString() + "[" + str_start + "--" + str_end +"]", 
					    		get_detailinfo(eit_Cursor[TempInt].getInt(eit_Cursor[TempInt].getColumnIndex("db_id"))));
						

	        		}
					catch(Exception e)
					{
					    Log.d("EitItemOnClick Exception#################################################", e.getMessage());
					}

					try
					{
						TempItemView = v;
						final CharSequence[] book_items = {"No Book", "BookPlay", "BookRecord"};
						Dialog dialog = null;  
					    Builder builder = new AlertDialog.Builder(DTVEpg.this);  
					    builder.setTitle("   ");  
					    final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();  
					    builder.setSingleChoiceItems(book_items, 0, choiceListener);  
					      
					    DialogInterface.OnClickListener btnListener =   
					        new DialogInterface.OnClickListener() 
					    	{  
					            public void onClick(DialogInterface dialogInterface, int which) 
					            {  
					            	if (TempItemView  == null)
										Log.d("Error #################################################", "TempItemView is null");
									else
					            	{
						            	int TempInt = EitListView.getPositionForView(TempItemView);
						                int choiceWhich   = choiceListener.getWhich();  
										refresh_bookstatus(TempItemView, choiceWhich);
										try
										{
											switch(choiceWhich)
											{

													case 0:
														update_bookstatus(eit_Cursor[TempInt].getInt(eit_Cursor[TempInt].getColumnIndex("db_id")),0);
													break;
													
													case 1:
														update_bookstatus(eit_Cursor[TempInt].getInt(eit_Cursor[TempInt].getColumnIndex("db_id")),1);
													break;
													
													case 2:
														update_bookstatus(eit_Cursor[TempInt].getInt(eit_Cursor[TempInt].getColumnIndex("db_id")),2);
														
														Log.d("EPG","start="+(eit_Cursor[TempInt].getLong(eit_Cursor[TempInt].getColumnIndex("start")))+"System.currentTimeMillis()"+System.currentTimeMillis());
														if((long)(eit_Cursor[TempInt].getLong(eit_Cursor[TempInt].getColumnIndex("start")))>System.currentTimeMillis()/1000)
															SetAlarm(eit_Cursor[TempInt].getLong(eit_Cursor[TempInt].getColumnIndex("start"))-100);
													break;
											}
										}
										catch(Exception e)
										{
										    Log.d("update_bookstatus Exception#################################################", e.getMessage());
										}
									}
					            }  
					        };  
					    builder.setPositiveButton("Ok", btnListener);  
					    builder.setNegativeButton("Cancel", null);
					    dialog = builder.create(); 
					    dialog.show();
					    dialog.getWindow().setLayout(450, 450);

					    AlertDialog alert = builder.create();
 					dialog.show();
 
 					WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
 					lp.dimAmount=0.0f;
 					dialog.getWindow().setAttributes(lp);
 					dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);		
						
					}
					catch(Exception e)
					{
					    Log.d("EitItemOnClick Exception#################################################", e.getMessage());
					}
			    }
				else
				{
					clear_eventnameanddetailinfo();
				}

	        }
	}

	class EitItemOnFocusChange  implements OnFocusChangeListener
	{
		public void onFocusChange(View v, boolean isFocused)
	    {		
	    	  refresh_dates(EitListView.getPositionForView(v));
			  if (Integer.valueOf(((Button)v).getHint().toString()) != -1)
			  {
			  
					try 
					{
						int TempInt = EitListView.getPositionForView(v);
						eit_Cursor[TempInt].moveToPosition(Integer.valueOf(((Button)v).getHint().toString()));

						Date dt_start =  new Date(eit_Cursor[TempInt].getLong(eit_Cursor[TempInt].getColumnIndex("start"))*1000);
				    		Date dt_end   =  new Date(eit_Cursor[TempInt].getLong(eit_Cursor[TempInt].getColumnIndex("end"))*1000);
				    		
				    		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
				    		String str_start = sdf.format(dt_start); 
				    		String str_end   = sdf.format(dt_end); 
						
				  		refresh_eventnameanddetailinfo(((Button)v).getText().toString()+ "[" + str_start + "--" + str_end +"]",
			         		get_detailinfo(eit_Cursor[TempInt].getInt(eit_Cursor[TempInt].getColumnIndex("db_id"))));
					}
					catch(Exception e)
					{
					    Log.d("EitItemOnFocusChange Exception#################################################",e.getMessage());
					}
			  }
			  else
			  {
					clear_eventnameanddetailinfo();
			  }
			  
		      if (isFocused==true)
		      {
		    	  ((Button)v).setBackgroundColor(EIT_TIEM_FOCUSCOLOR);
		      }
		      else 
		      {
		    	  if(EitListView.getPositionForView(v)%2 == 1)
		    		  ((Button)v).setBackgroundColor(EIT_TIEM_UNFOCUSCOLOR_ODD);
					else
						((Button)v).setBackgroundColor(EIT_TIEM_UNFOCUSCOLOR_EVEN);	
		      }
	    }	
	}
	
	class ExitOnClick  implements OnClickListener
    {
	        public void onClick(View v) 
	        { 
			    finish();
	        }
    }

	class DetailOnClick  implements OnClickListener
    {
	        public void onClick(View v) 
	        { 
            	new AlertDialog.Builder(DTVEpg.this) 
            	.setTitle("--")
            	.setMessage(((TextView)v).getText())
            	.setPositiveButton("ok", null)
            	.show();
	        }
    }	
	
	private void update_new_eit(){
		progressDialog.show();
		WindowManager.LayoutParams lp=progressDialog.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		progressDialog.getWindow().setAttributes(lp);
		progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		new Thread(){     
            public void run(){   
         		Message msg;
                try{
                	Thread.sleep(1000);
                	setup_db();
                }
                catch (Exception e){
                 	Log.d("update_new_eit Exception 1", e.getMessage());	
                }

                try{
                    msg = waiting_handler.obtainMessage();
                    msg.arg1 = 2;
                	waiting_handler.sendMessage(msg);
                	Thread.sleep(1000);
                }
                catch (Exception e){
                 	Log.d("update_new_eit Exception 2", e.getMessage());	
                } 

                try{
                    msg = waiting_handler.obtainMessage();
                    msg.arg1 = 3;
                	waiting_handler.sendMessage(msg);
                	//Thread.sleep(1000);
                }
                catch (Exception e){
                 	Log.d("update_new_eit Exception 3", e.getMessage());	
				} 
			}
		}.start(); 
		
		eit_notify = 0;
	}
	

}

