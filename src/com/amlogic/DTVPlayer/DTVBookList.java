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
import com.amlogic.tvutil.TVBooking;

import java.util.*;
import java.text.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.app.*;
import android.app.AlertDialog.*;
import android.content.*;
import android.graphics.*;
import android.view.ViewGroup.*;
import android.text.*;
import android.text.method.*;
import android.database.*;
import android.os.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AbsListView.OnScrollListener;
import com.amlogic.widget.SureDialog;
import com.amlogic.widget.SingleChoiseDialog;

public class DTVBookList extends DTVActivity{
	private static final String TAG="DTVBookList";

	/********ctrl var *************/
    private TextView BookOrder;
    private TextView BookProgName;
    private TextView BookEventName;
    private TextView BookTime;
    private TextView BookOperation;
    
    private ListView listview;
    private MyAdapter myAdapter;
	private TVBooking[] mTVBooking=null;
	
    /********db var*********/
    private ArrayList<Object> list;
    private ArrayList<Object> serviceList;
    
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtv_book_list);
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		DTVBookListUIInit();
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

	private void DTVBookListUIInit(){
		setup_db();
        setup_views();
	}

	private void setup_views(){
		
		listview = (ListView)findViewById(R.id.list_content);
		myAdapter = new MyAdapter(DTVBookList.this, serviceList);
		listview.setOnItemClickListener(new listOnItemClick());
		listview.setAdapter(myAdapter);
	}
	
	private void setup_db(){
		int id = 0;
		serviceList = new ArrayList<Object>();             

		if (serviceList != null){
			serviceList.clear();
		}
		else{
			serviceList = new ArrayList<Object>();
		}

		//get all book list data
		mTVBooking = TVBooking.selectByStatus(this,TVBooking.ST_WAIT_START);

		//add to serviceList
		if(mTVBooking!=null){
			int count = mTVBooking.length;
			for(int i=0;i<count;i++){
				serviceInfo serviceinfo = new serviceInfo();
				
				serviceinfo.setId(mTVBooking[i].getID());
				serviceinfo.setProgramName(mTVBooking[i].getProgramName());
				serviceinfo.setEventName(mTVBooking[i].getEventName());
	    		Date dt_start =  new Date(mTVBooking[i].getStart());
	    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
	    		String str_start = sdf.format(dt_start); 
				serviceinfo.setTime(""+str_start);

				long dt_duration   = mTVBooking[i].getDuration()/60/1000;
				if(dt_duration==0)
					serviceinfo.setDuration("");
				else
	    			serviceinfo.setDuration(""+dt_duration+" MIN");
				
				serviceinfo.setMode(mTVBooking[i].getFlag());
				serviceinfo.setRepeat(mTVBooking[i].getRepeat());
				serviceinfo.setStatus(mTVBooking[i].getStatus());
				
				serviceList.add(serviceinfo);
				id++;
			}
		}

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
			this.getContentResolver().update(DVBClient.TABLE_EVENT, cv, "db_id= "+eventid, null); 
		}
		*/
	}
	
	
	
    class returnSearchListener implements OnClickListener
    {
		public void onClick(View v) 
		{
			finish();
		}
    }

	class ExitOnFocusChange  implements OnFocusChangeListener
	{
		public void onFocusChange(View v, boolean isFocused)
	    {		
	      if (isFocused==true)
	      {
	      }
	      else 
	      {
	      }
	    }	
	}

	private void  deleteBookItem( int cur_pos){
		serviceList.remove(cur_pos);
		if(mTVBooking[cur_pos]!=null)
			mTVBooking[cur_pos].delete();
		mTVBooking = TVBooking.selectByStatus(this,TVBooking.ST_WAIT_START);
		if(myAdapter!=null)
			myAdapter.notifyDataSetChanged();
	}

	private void  editBookItem( int cur_pos){
		if(mTVBooking[cur_pos].getEvent()!=null)
			showEventBookEditDialog(cur_pos);
		else
			showBookEditDialog(cur_pos);
	}

	private void showItemClickDialog(int position){
		final int item = position;
	
		int pos = 0;
		String items[] = new String[]{"delete", "Edit"};
		
		new SingleChoiseDialog(DTVBookList.this,items,pos){
			public void onSetMessage(View v){
				((TextView)v).setText("   ");
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				Log.d(TAG,"dialog choise="+which);

				switch(which){
					case 0:
						deleteBookItem(item);
						break;
					case 1:
						editBookItem(item);
						break;
				}
			}
		};
	}
	
    class listOnItemClick implements AdapterView.OnItemClickListener
    {
    	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
    	{   
			serviceInfo serviceinfo = getServiceInfoByPostion(arg2);
		  	
			showItemClickDialog(arg2);
		    myAdapter.notifyDataSetChanged();
        }      	
    }

   
    public serviceInfo getServiceInfoByPostion(int position)
    {
    	if (position > serviceList.size())
    	{
    		return null;
    	}
    	
    	return (serviceInfo) serviceList.get(position);
    }
   
    
	class MyAdapter extends BaseAdapter{
		 private Context context;
		 private List<Object> titles;    
		 private LayoutInflater lif;
		 private int selectItem;
		 
		 public MyAdapter(Context context, List<Object> titles)
		 {
		      this.context = context;
		      this.titles = titles;
		 }
		 
		 public int getCount() 
		 {
			 if(serviceList==null)
			 	return 0;
			 else
				return serviceList.size();
		 }
		 
		 public Object getItem(int position) 
		 {
			 return serviceList.get(position);
		 }
		
		 public long getItemId(int position) 
		 {
			 return position;
		 }
		 
		 public void setSelectItem(int position)
		 {
			 this.selectItem = position;
		 }
		 
		 public int getSelectItem()
		 {
			 return this.selectItem;
		 }

		 private void fillData(MyView mv, int position)
		{
			if (null != mv)
			{
				serviceInfo serviceinfo = getServiceInfoByPostion(position);
				if (null != serviceinfo)
				{
					mv.programName.setText(serviceinfo.getProgramName());
					mv.eventName.setText(serviceinfo.getEventName());
					mv.time.setText(serviceinfo.getTime());

					mv.duration.setText(serviceinfo.getDuration());

					if(serviceinfo.getMode()==1)
						mv.mode.setText(getString(R.string.view));
					else if(serviceinfo.getMode()==2)
						mv.mode.setText(getString(R.string.pvr));

					if(serviceinfo.getRepeat()==0)
						mv.repeat.setText(getString(R.string.once));
					else if(serviceinfo.getRepeat()==1)
						mv.repeat.setText(getString(R.string.daily));
					else if(serviceinfo.getRepeat()==2)
						mv.repeat.setText(getString(R.string.weekly));
					

					if(serviceinfo.getStatus()==0) // wait start
						mv.status.setText(getString(R.string.scheduled));
					else if(serviceinfo.getStatus()==1) //cannelled
						mv.status.setText(getString(R.string.scheduled));
					else if(serviceinfo.getStatus()==2) //started
						mv.status.setText(getString(R.string.recording));
					else if(serviceinfo.getStatus()==3)  //end
						mv.status.setText(getString(R.string.completed));

				}
			}
		}
	
	     public View getView(int position, View convertView, ViewGroup parent) 
	     {
			MyView mv;

			if(convertView == null)
			{
				mv = new MyView();
				lif = LayoutInflater.from(context);
				convertView = lif.inflate(R.layout.book_list_item, null);
			       
				mv.programName = (TextView)convertView.findViewById(R.id.bookProgramName) ;
				mv.eventName= (TextView)convertView.findViewById(R.id.bookEventName) ;
				mv.duration=(TextView)convertView.findViewById(R.id.duration);
				mv.time= (TextView)convertView.findViewById(R.id.booktime) ;
				mv.mode= (TextView)convertView.findViewById(R.id.mode) ;
				mv.repeat= (TextView)convertView.findViewById(R.id.repeat) ;
				mv.status= (TextView)convertView.findViewById(R.id.status) ;

				convertView.setTag(mv);
			}
			else
			{
			   mv = (MyView)convertView.getTag();
			}        

			fillData(mv, position);

			

			return convertView;
	     }

	     private class MyView{
		      TextView     programName;
		      TextView 	   eventName;
		      TextView     time;
			  TextView     duration;
			  TextView 	   mode;
			  TextView 	   repeat;
			  TextView 	   status;
	     }
	}

	public class serviceInfo {
		private int id;
		private String program_name;
		private String event_name;
		private String time;
		private String duration;
		private int    repeat;
		private int    mode;
		private int    status;

		public int getId() {
			return id;
		}
		
		public void setId(int id) {
			this.id = id;
		}

		public String getProgramName() {
			return program_name;
		}

		public void setProgramName(String program_name) {
			this.program_name = program_name;
		}

		public String getEventName() {
			return event_name;
		}
		
		public void setEventName(String event_name) {
			this.event_name = event_name;
		}

		public int getMode(){
			return this.mode;
		}

		public void setMode(int mode){
			this.mode=mode;
		}		

		public String getTime() {
			return time;
		}
		
		public void setTime(String time) {
			this.time = time;
		}

		public String getDuration() {
			return duration;
		}
		
		public void setDuration(String time) {
			this.duration = time;
		}

		public int getRepeat(){
			return this.repeat;
		}

		public void setRepeat(int repeat){
			this.repeat=repeat;
		}		

		public int getStatus(){
			return this.status;
		}

		public void setStatus(int status){
			this.status=status;
		}		
		
	}

	int mode=1;
	int repeat=0;
	private void showEventBookEditDialog(int position){		
		final int pos = position;
		TVEvent mTVEvent = mTVBooking[pos].getEvent();
		if(mTVEvent==null)
			return;
		
		final Dialog mDialog = new AlertDialog(this){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
							dismiss();
						break;
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
		
		lp.dimAmount=0.5f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		//title.setText(getString(R.string.scan_mode));
		title.setText("Event Edit");
	
		final TextView text_channel_name= (TextView) window.findViewById(R.id.text_channel_name);
		final TextView text_event_name = (TextView) window.findViewById(R.id.text_event_name);
		final TextView text_event_start_date = (TextView) window.findViewById(R.id.text_event_start_date);
		final TextView text_event_start_time = (TextView) window.findViewById(R.id.text_event_start_time);
		final TextView text_event_end_time = (TextView) window.findViewById(R.id.text_event_end_time);

		text_event_name.setText(mTVEvent.getName());

		Date dt_start =  new Date(mTVEvent.getStartTime());
		Date dt_end   =  new Date(mTVEvent.getEndTime());
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
		SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MMMM-dd"); 
		String str_start = sdf.format(dt_start); 
		String str_end   = sdf.format(dt_end); 
		String str_date  = sdf_date.format(dt_start);
		text_event_start_date.setText(str_date);
		text_event_start_time.setText(str_start);
		text_event_end_time.setText(str_end);
		
		text_channel_name.setText(mTVBooking[pos].getProgramName());
		
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
					Log.d(TAG,"mode="+mode + " ---repeat="+repeat);
					((serviceInfo)serviceList.get(pos)).setMode(mode);
					((serviceInfo)serviceList.get(pos)).setRepeat(repeat);
					
					mTVBooking[pos].updateFlag(mode);
					mTVBooking[pos].updateRepeat(repeat);
					if(myAdapter!=null)
							myAdapter.notifyDataSetChanged();
					
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

	class BookAdd{
		long start_date;
		long start_time;
		long duration;
		int repeat;
		int mode;
	}
	
    BookAdd mBookAdd =null;
	private void showBookEditDialog(int position){
		if(mBookAdd==null){
			mBookAdd = new BookAdd();	
		}	
	
		

		final int pos = position;
		if(mTVBooking[pos].getEvent()!=null)
			return;		

		mBookAdd.start_time=mTVBooking[pos].getStart();
		mBookAdd.duration=mTVBooking[pos].getDuration();
		mBookAdd.repeat=mTVBooking[pos].getRepeat();
		mBookAdd.mode=mTVBooking[pos].getFlag();

		final Dialog mDialog = new AlertDialog(this){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
							dismiss();
						break;
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
		mDialog.setContentView(R.layout.book_add_dialog);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.5f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText("Book Edit");
	
		final TextView text_channel_name= (TextView) window.findViewById(R.id.text_channel_name);
		
		Date dt_start =  new Date(mTVBooking[pos].getStart());
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
		SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MMMM-dd"); 
		String str_start = sdf.format(dt_start); 
		String str_date  = sdf_date.format(dt_start);
		
		text_channel_name.setText(mTVBooking[pos].getProgramName());
		
		final ListView LimitListView = (ListView)window.findViewById(R.id.set_list); 	
		LimitListView.setAdapter(new BookAddAdapter(this));
		
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
						showPvrDateSetDialog(text);
						break;
					case 1:
						showEditPvrTimeDialog(text);
						break;
					case 2:  
						if(text.getText().equals(getString((R.string.once)))){
							text.setText(getString(R.string.daily));
							if(mBookAdd!=null)
								mBookAdd.repeat=1;
						}
						else if(text.getText().equals(getString((R.string.daily)))){
							text.setText(getString(R.string.weekly));
							if(mBookAdd!=null)
								mBookAdd.repeat=2;
						}
						else{
							text.setText(getString(R.string.once));
							if(mBookAdd!=null)
								mBookAdd.repeat=0;
						}
						
						break;
					case 3:
						if(text.getText().equals(getString((R.string.view)))){
							text.setText(getString(R.string.pvr));
							if(mBookAdd!=null)
								mBookAdd.mode=1;
						}
						else{
							text.setText(getString(R.string.view));
							if(mBookAdd!=null)
								mBookAdd.mode=0;
						}
						break;					
				}
			}
        	    
        });

		no.setFocusable(true);     
     	no.setFocusableInTouchMode(true);   
		no.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {				  	 
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
		          }});	 
		yes.setOnClickListener(new OnClickListener(){
	          public void onClick(View v) {
					if(mBookAdd!=null){
						
						Date date =  new Date(mBookAdd.start_time);
						SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
						String str_date  = sdf_date.format(date);
						
						((serviceInfo)serviceList.get(pos)).setTime(str_date);	
						((serviceInfo)serviceList.get(pos)).setDuration(String.valueOf(mBookAdd.duration/1000/60));	

						((serviceInfo)serviceList.get(pos)).setMode(mBookAdd.mode);
						((serviceInfo)serviceList.get(pos)).setRepeat(mBookAdd.repeat);

						mTVBooking[pos].updateFlag(mBookAdd.mode);
						mTVBooking[pos].updateRepeat(mBookAdd.repeat);
						mTVBooking[pos].updateStartTime(mBookAdd.start_time);
						mTVBooking[pos].updateDuration(mBookAdd.duration);
						if(myAdapter!=null)
							myAdapter.notifyDataSetChanged();
						
					}
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

	private  class BookAddAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		
		private Context cont;
		private String[] DATA;

	 	class ViewHolder {
			
			TextView text;
			ImageView icon;
			TextView  info;
			ImageView icon1;
		}
	
		public BookAddAdapter(Context context) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);

			DATA = new String[4];
			//DATA[0]= cont.getResources().getString(R.string.event_start_date);			
			DATA[0]= cont.getResources().getString(R.string.event_start_time);
			DATA[1]= cont.getResources().getString(R.string.duration);		
			DATA[2]= cont.getResources().getString(R.string.repeat);			
			DATA[3]= cont.getResources().getString(R.string.mode);	
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

			Date dt_start =  new Date(mBookAdd.start_time);

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
			SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
			String str_start = sdf.format(dt_start); 
			String str_date  = sdf_date.format(dt_start);
	
			holder.text.setText(DATA[position]);
			holder.icon.setBackgroundResource(R.drawable.arrow_down2); 
			holder.info.setVisibility(View.VISIBLE);
			holder.icon.setVisibility(View.VISIBLE);
			holder.icon1.setVisibility(View.INVISIBLE);
			holder.info.setTextColor(Color.YELLOW);	
			switch(position){
				case 0:
					holder.info.setText(str_date);
					break;
				case 1:
					long duration = mBookAdd.duration/1000/60;
					holder.info.setText(String.valueOf(duration)+"  "+getString(R.string.dtvplayer_pvr_rec_min));
					break;
				case 2:
					holder.info.setText(cont.getResources().getString(R.string.once));
					break;
				case 3:
					holder.info.setText(cont.getResources().getString(R.string.view));
					break;	
			
			} 
			
			return convertView;
		}
	}

	private AlertDialog.Builder builder;
	Toast toast;
	public void showEditPvrTimeDialog(View v){
		builder = new AlertDialog.Builder(this);
		
		final EditText editText = new EditText(this);
		editText.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(4)});

		builder.setTitle(R.string.edit_title);
		final TextView text_time =(TextView) v;
		editText.setText("");
		builder.setView(editText); 

		AlertDialog alert = builder.create();

		alert.setOnKeyListener( new DialogInterface.OnKeyListener(){
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				switch(keyCode)
				{	
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						String time = editText.getText().toString();
						if(time==null||time.equals("")){
							editText.setText(null);
							toast = Toast.makeText(DTVBookList.this, 
						    	R.string.invalid_input,
						    	Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
						else{
							if(Integer.parseInt(time)==0){
								editText.setText(null);
								toast = Toast.makeText(DTVBookList.this, 
							    	R.string.invalid_input,
							    	Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
							}
							else{
								text_time.setText(editText.getText().toString()+getString(R.string.dtvplayer_pvr_rec_min));
								if(mBookAdd!=null)
									mBookAdd.duration=Long.valueOf(editText.getText().toString())*60*1000;	
								dialog.cancel();
							}	
						}
						return true;
					case  KeyEvent.KEYCODE_BACK:
						dialog.cancel();
						return true;
				}
				
				return false;
			}
		});	

		
		alert.setOnShowListener(new DialogInterface.OnShowListener(){
						public void onShow(DialogInterface dialog) {
			
							}         
							}); 	

		alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {	
						}         
						});	
		alert.show();	
		alert.getWindow().setLayout(500, -200);
		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.5f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}	

	private AlertDialog.Builder editBuilder=null;
	public void showPvrDateSetDialog(View v){
		final TextView text_info = (TextView)v;
		AlertDialog alert_password=null;	

		LinearLayout pvr_time_layout = null;
		if(editBuilder==null)
		editBuilder = new AlertDialog.Builder(this);

		pvr_time_layout=(LinearLayout) getLayoutInflater().inflate(R.layout.date_piker_dialog, null);

		DatePicker dp=(DatePicker)pvr_time_layout.findViewById(R.id.dPicker);
		pvr_time_init();
		dp.init(mYear, mMonth, mDay, new DatePicker.OnDateChangedListener()    {
		 	  
				public void onDateChanged(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) 
				{
					mYear=year;
					mMonth= monthOfYear;
					mDay=dayOfMonth;
				
				}
			});
		TimePicker tp=(TimePicker)pvr_time_layout.findViewById(R.id.tPicker);		
		tp=(TimePicker)pvr_time_layout.findViewById(R.id.tPicker);
		tp.setIs24HourView(true);
		tp.setCurrentHour(mHour); 
		tp.setCurrentMinute(mMinute); 
		tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
		  	public void onTimeChanged(TimePicker view,int hourOfDay,int minute)      {
		  	mHour=hourOfDay;
			mMinute=minute;
			  
			}    
		  });	 
			
		  editBuilder.setView(pvr_time_layout); 
		  editBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {	
			public void onClick(DialogInterface dialog, int which) {
				//int srv = DATA_DB_ID[cur_select_item];
				long now=0;	
				int end=0;

				now = time_test(mYear,mMonth,mDay,mHour,mMinute,0);
	 			Date date =  new Date(now);
	
				SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
				String str_date  = sdf_date.format(date);
			
				text_info.setText(str_date);
				if(mBookAdd!=null)
					mBookAdd.start_time=now;
			}
		});

		editBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				
			}        
		}); 
		 
		  alert_password = editBuilder.create();
		  alert_password.setOnShowListener(new DialogInterface.OnShowListener(){
								public void onShow(DialogInterface dialog) {

									}         
									}); 	

		   alert_password.setOnDismissListener(new DialogInterface.OnDismissListener(){
								public void onDismiss(DialogInterface dialog) {
								
								}         
								});	

		  
		  alert_password.show();

		  WindowManager m = getWindowManager();   
		  Display d = m.getDefaultDisplay();  	
		 	
		  WindowManager.LayoutParams lp=alert_password.getWindow().getAttributes();
		  lp.dimAmount=0.0f;
		  //lp.height = (int) (d.getHeight() * 0.95);  
		  //lp.width = (int) (d.getWidth() * 0.85);
		  alert_password.getWindow().setAttributes(lp);
		  alert_password.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

	}

	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute; 
	private void pvr_time_init(){				
		Date date = new Date(getUTCTime());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH mm ss"); 
		String today = sdf.format(date); 
		Log.d(TAG,"####Today"+today);
		
		String[] para = today.split(" ", 6);
		if (para.length >= 6){
			mYear = Integer.valueOf(para[0]).intValue();
			mMonth= Integer.valueOf(para[1]).intValue()-1;
			mDay = Integer.valueOf(para[2]).intValue();
			mHour = Integer.valueOf(para[3]).intValue();
			mMinute = Integer.valueOf(para[4]).intValue();
		}

		Log.d(TAG,"mYear="+mYear+"  mMonth="+mMonth+"  mDay="+mDay+"  mHour="+mHour+" mMinute="+mMinute);

	}

	long time_test(int y,int m, int d,int h,int min,int s){	
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT")); 
		cal.set(y, m, d, h, min, s); 
		return cal.getTime().getTime(); 
	}
	
}

