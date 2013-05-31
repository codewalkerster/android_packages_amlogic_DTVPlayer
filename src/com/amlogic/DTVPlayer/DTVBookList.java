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
		mTVBooking = TVBooking.selectAllPlayBookings(this);
		
		//add to serviceList
		if(mTVBooking!=null){
			int count = mTVBooking.length;
			for(int i=0;i<count;i++){
				serviceInfo serviceinfo = new serviceInfo();
				
				serviceinfo.setId(mTVBooking[i].getID());
				serviceinfo.setProgramName(mTVBooking[i].getProgramName());
				serviceinfo.setEventName(mTVBooking[i].getEventName());
				
	    		Date dt_start =  new Date(mTVBooking[i].getStart()*1000);
	    		Date dt_end   =  new Date((mTVBooking[i].getStart()+mTVBooking[i].getDuration())*1000);
	    		
	    		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm"); 
	    		String str_start = sdf.format(dt_start); 
	    		String str_end   = sdf.format(dt_end); 

				serviceinfo.setTime(""+str_start+" ~ "+str_end);
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
    
    class listOnItemClick implements AdapterView.OnItemClickListener
    {
    	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
    	{   
			serviceInfo serviceinfo = getServiceInfoByPostion(arg2);
		  	

		    myAdapter.setSelectItem(arg2);
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
			 return titles.size();
		 }
		 
		 public Object getItem(int position) 
		 {
			 return titles.get(position);
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
					mv.mode.setText(String.valueOf(serviceinfo.getMode()));
					mv.repeat.setText(String.valueOf(serviceinfo.getRepeat()));
					mv.status.setText(String.valueOf(serviceinfo.getStatus()));
					   
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

	

}

