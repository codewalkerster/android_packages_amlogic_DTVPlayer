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
	
	private Handler  waiting_handler;
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
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.dtvepg);
		
	}

	public void onStart(){
		super.onStart();
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		DTVEpgUIInit();

		//channel list
		mDTVSettings = new DTVSettings(this);
		DTVChannelList_UI_Init();
		myAdapter.notifyDataSetChanged();
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
			case TVMessage.TYPE_EVENT_UPDATE:
				eit_notify ++ ;
				break;	
			case TVMessage.TYPE_PROGRAM_START:	
				Log.d(TAG,"New program start!");
				cur_service_id = DTVEpgGetID();
		        db_id = cur_service_id;
				Log.d(TAG,"db_id="+db_id);
				new Thread(){     
		            public void run(){   
		         		Message msg;
		                try{
		                	Thread.sleep(1000);
		                	setup_db();
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
				myAdapter.notifyDataSetChanged();
				break;
			default:
				break;
	
		}
	}

	private int eit_list_cur_pos=0;
	
	private void DTVEpgUIInit(){	
		/*setup view*/
        
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
					case KeyEvent.KEYCODE_TAB: //info
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
		});
		
     	    
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
						refresh_Eitlistview(0);
	            	break;

					case 3:
	                    //EPG_parent_linearlayout.setVisibility(View.VISIBLE);
	                    progressDialog.dismiss();  
						if (TempFlag == 0){
							TempFlag = 1;
							moveto_currentevent();
						}
	            	break;
					case 4:
						update_new_eit();
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
	
	private void create_proganddate(){
		
	}
	
	/******************used after connect service*******************/

	private long get_current_datetime(){
		//Date date = new Date();
		//return date;
		return getUTCTime();
	}
	
	private void refresh_currenttime(){		
		
	}
	
	private void refresh_progname(){		
		
	}
	
	private void setup_db(){
	     
        mTVEvent[0] = DTVEpg_getDateEIT(get_current_datetime()
        						,get_firstmillisofcurrentday()+ 1 * 24 * 60 * 60*1000-get_current_datetime());
		Log.d("mTVEvent[0]:",""+mTVEvent[0].length);
		
        mTVEvent[1] = DTVEpg_getDateEIT(get_firstmillisofcurrentday()+ 1 * 24 * 60 * 60*1000,
        						 1 * 24 * 60 * 60*1000);
		Log.d("mTVEvent[1]:",""+mTVEvent[1].length);
        mTVEvent[2] = DTVEpg_getDateEIT(get_firstmillisofcurrentday()+ 2 * 24 * 60 * 60*1000,
        						1 * 24 * 60 * 60*1000);
		Log.d("mTVEvent[2]:",""+mTVEvent[2].length);
        mTVEvent[3] = DTVEpg_getDateEIT( get_firstmillisofcurrentday()+ 3 * 24 * 60 * 60*1000,
        						1 * 24 * 60 * 60*1000);
		Log.d("mTVEvent[3]:",""+mTVEvent[3].length);
        mTVEvent[4] = DTVEpg_getDateEIT( get_firstmillisofcurrentday()+ 4 * 24 * 60 * 60*1000,
        						1 * 24 * 60 * 60*1000);
		Log.d("mTVEvent[4]:",""+mTVEvent[4].length);
        mTVEvent[5] = DTVEpg_getDateEIT( get_firstmillisofcurrentday()+ 5 * 24 * 60 * 60*1000,
        						1 * 24 * 60 * 60*1000);
		Log.d("mTVEvent[5]:",""+mTVEvent[5].length);
        mTVEvent[6] = DTVEpg_getDateEIT( get_firstmillisofcurrentday()+ 6 * 24 * 60 * 60*1000,
        						1 * 24 * 60 * 60*1000);
		Log.d("mTVEvent[6]:",""+mTVEvent[6].length);
		
		//if (mTVEvent[0].length > 0)
			//HelpInfoTextView.setText(R.string.epg_operate_des01);
		//else
			//HelpInfoTextView.setText(R.string.epg_no_epg_info);
	}

	EitListDBAdapter mEitListDBAdapter =null;
	private void refresh_Eitlistview(int date){
		mEitListDBAdapter = new EitListDBAdapter(this,date);
        EitListView.setAdapter(mEitListDBAdapter); 
		mEitListDBAdapter.notifyDataSetChanged();
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
					update_new_eit();
				}
			}   
		};
	    currenttimer_handler.postDelayed(currenttimer_runnable, 1000);
	}
	
	private void create_eventnameanddetailinfo(){
		
	}
	
	

	
	
	private void create_dates(){
		
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
	
	private void create_hours(){
		
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
		
		
		class ViewHolder {
			TextView time;
			TextView eit_name;	
			ImageView icon_book;
			ImageView icon_pvr;
		}
		
		public EitListDBAdapter(Context context, int index) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);	
			date = index;
		}

		public int getCount() {
			if(mTVEvent[date]!=null){
				return mTVEvent[date].length;
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
			
			
			if(mTVEvent[date]!=null){
				if (mTVEvent[date].length>0){	
					/*
					if (
						(position == 0 ) && 
						(
						        mTVEvent[date][position].getStartTime() 
								> 
						        (get_firstmillisofcurrentday()/1000+ date * 24 * 60 * 60))
						)
					{
						
						holder.eit_name.setText("N/A");

					}
					else
					*/
						holder.eit_name.setText(mTVEvent[date][position].getName());

					Date dt_start =  new Date(mTVEvent[date][position].getStartTime()*1000);
		    		Date dt_end   =  new Date(mTVEvent[date][position].getEndTime()*1000);
		    		
		    		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
		    		String str_start = sdf.format(dt_start); 
		    		String str_end   = sdf.format(dt_end); 
					
					holder.time.setText(""+str_start + "--" + str_end);					
					
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
		
		eit_notify = 0;
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

		Text_title=(TextView)findViewById(R.id.prolist);
		Text_title.setTextColor(Color.YELLOW);
		
		class_total = getListProgramClass();
		if(service_type == TVProgram.TYPE_RADIO){
			getListData(1);
			Text_title.setText(R.string.radio);
		}	
		else{
			service_type = TVProgram.TYPE_TV;
			getListData(0);
			Text_title.setText(R.string.tv);
		}
		
		ListView_channel = (ListView) findViewById(R.id.programListView);
		myAdapter = new IconAdapter(this,null);
		ListView_channel.setOnItemSelectedListener(mOnSelectedListener);
		ListView_channel.setOnScrollListener(new listOnScroll()); 
		ListView_channel.setOnItemClickListener(mOnItemClickListener);
		ListView_channel.setAdapter(myAdapter);
		setFocusPosition();
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		//reset_timer();
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
				if(list_status==0){
					if(cur_select_item == ListView_channel.getCount()-1){
				    	ListView_channel.setSelection(0); 	
						return true;
					}	
				}
				else if(list_status==1){
					if(eit_list_cur_pos == EitListView.getCount()-1){
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
				break;
			case KeyEvent.KEYCODE_ZOOM_IN:
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
						ListView_channel.setSelection(cur_select_item-EitListView.getChildCount());
					}
					myAdapter.notifyDataSetChanged();
				}
				break;
			case KeyEvent.KEYCODE_ZOOM_OUT:
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
					if(p<EitListView.getCount())
						ListView_channel.setSelection(p-1);
					else{
						ListView_channel.setSelection(ListView_channel.getCount()-1);
					}
					myAdapter.notifyDataSetChanged();
				}
				
					
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


	

}

