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
		listview.setOnItemClickListener(new listOnItemClick());
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

	

}

