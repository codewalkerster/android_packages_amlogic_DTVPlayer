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
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.dtvepg);
		DTVEpgUIInit();
	}

	public void onStart(){
		super.onStart();
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
			case TVMessage.TYPE_EVENT_UPDATE:
				eit_notify ++ ;
				break;	
			default:
				break;
	
		}
	}

	private void DTVEpgUIInit(){
		
		cur_service_id = DTVEpgGetID();
        
		/*setup view*/
        
        EitListView  = (ListView)findViewById(R.id.EitListView);
        EitListView.setItemsCanFocus(true);
     	    
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
	
	private void refresh_Eitlistview(int date){
        EitListView.setAdapter(new EitListDBAdapter(this,date)); 
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
				convertView = mInflater.inflate(R.layout.dtvchannellist_item, null);
				
				holder = new ViewHolder();
				holder.time = (TextView)convertView.findViewById(R.id.prono);
				holder.eit_name = (TextView) convertView.findViewById(R.id.ItemText);
				holder.icon_book = (ImageView) convertView.findViewById(R.id.icon);
				holder.icon_pvr = (ImageView)convertView.findViewById(R.id.icon_scrambled);
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
						holder.eit_name.setText(mTVEvent[date][position].getName());

					holder.time.setText(""+mTVEvent[date][position].getStartTime());					
					
					switch(mTVEvent[date][position].getSubFlag()){
						case 0:
							holder.icon_book.setBackgroundResource(Color.TRANSPARENT);
							holder.icon_pvr.setBackgroundResource(Color.TRANSPARENT);
							
						break;
						case 1:
							holder.icon_book.setBackgroundResource(R.drawable.dtvplayer_icon_lock); 
						break;
						case 2:
							holder.icon_pvr.setBackgroundResource(R.drawable.dtvplayer_icon_fav); 
						break;
					}


					/*	
					final int pos = position;
					final int item = i;
					TempButton.setOnKeyListener(new OnKeyListener() { 
					public boolean onKey(View v, int keyCode, KeyEvent event) {		
				
						switch(keyCode)
						{
							case KeyEvent.KEYCODE_TAB: //info
								if (event.getAction() == KeyEvent.ACTION_DOWN) {
									showInfoDia(v,pos,item);	
																	}
								return true;	
						} 
						return false;
					}}); 
					*/
					
				}
			}
		
			return convertView;
		}
		
	} 

	AlertDialog.Builder builder=null;
    	private void showInfoDia(View v,int pos,int i){
		/*
		String message = mTVEvent[pos][i].getEventDescr()+"\n"+mTVEvent[pos][i].getEventExtDescr();
				
		builder = new AlertDialog.Builder(DTVEpg.this) ;
		builder.setTitle((((Button)v).getText().toString()!=null&&!(((Button)v).getText().toString().equals("")))?((Button)v).getText().toString():"--");
		//builder.setMessage((mTVEvent[pos].getString(mTVEvent[pos].getColumnIndex("ext_descr"))!=null&&!(mTVEvent[pos].getString(mTVEvent[pos].getColumnIndex("ext_descr")).equals("")))?
		builder.setMessage(message);
		builder.setPositiveButton("ok", null);

		AlertDialog alert = builder.create();
		alert.show();

		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		*/
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

