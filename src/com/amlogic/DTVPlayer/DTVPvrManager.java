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

public class DTVPvrManager extends DTVActivity{
	private static final String TAG="DTVPvrManager";

	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtv_pvr_manager);
		VideoView video_view= (VideoView) findViewById(R.id.VideoView);
		openVideo(video_view,true);
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		DTVPvrManagerUIInit();
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
			/*case DVBEvent.EVENT_RECORDS_CHANGED:
				refresh_data();
			*/	
			default:
				break;
	
		}
	}


	TVBooking[] mTVBookingList=null;
	private ListView list;
	private ArrayList<Object> serviceList;
	private TabHost tabHost;    
    private MyAdapter myAdapter;
  	private int cur_select_item = 0;
    private int[] cur_status=null;
	final static int PVR_STATUS_ALL = -1;
    
    final static int PVR_STATUS_REC_WAIT = 0; //does not notify, just wait
    final static int PVR_STATUS_NOTIFIED = 1; //has notified, will start record shortly
	final static int PVR_STATUS_RECORDING = 2;
    final static int PVR_STATUS_REC_OVER = 3;
	
    AlertDialog.Builder builder;

    private TabWidget tabWidget;

	int choise = 0;	
	private String proname=null;
	private String filename=null;
	private int record_db_id;
	private Toast toast=null;

	private Handler  currenttimer_handler;
	private Runnable currenttimer_runnable;
	
	private void DTVPvrManagerUIInit(){
		
		//TextView tv2 = (TextView)findViewById(R.id.editProgramDescription);
        //tv2.setTextColor(Color.WHITE);
        //tv2.setText(R.string.recmanager_help_info);
        
        list = (ListView)findViewById(R.id.listview_recmanager);
	    list.setOnItemSelectedListener(mOnSelectedListener);
		list.setOnItemClickListener(mOnItemClickListener);
	    list.setOnScrollListener(new listOnScroll()); 

		getPvrData(PVR_STATUS_ALL);
		if (null == myAdapter){
			myAdapter = new MyAdapter(DTVPvrManager.this,null);
			myAdapter.notifyDataSetChanged();
		}

		list.setAdapter(myAdapter);
	   
		try{
			tabHostInit();		
			tabWidget = (TabWidget)findViewById(android.R.id.tabs);
			int count = tabWidget.getChildCount();

			for (int i = 0; i < count; i++) {
				View view = tabWidget.getChildTabViewAt(i);   
				//view.getLayoutParams().height = 80;
				final TextView tv = (TextView) view.findViewById(android.R.id.title);
				if(android.os.Build.VERSION.RELEASE.equals("2.2.1")){
					tv.setTextSize(28);
					//tabWidget.getChildAt(i).getLayoutParams().height = 70;  
					//tabWidget.getChildAt(i).getLayoutParams().width = 65;
				}
				else{
					Locale l = Locale.getDefault();  
					String lan = String.format("%s-%s", l.getLanguage(), l.getCountry());  
					if(lan.equals("zh-CN")||lan.equals("zh-TW")){
						tv.setTextSize(15);
					}	
					else
						tv.setTextSize(22);
				}
				tv.setTextColor(this.getResources().getColorStateList(android.R.color.white));
			}
		}
		catch(Exception ex){   
            ex.printStackTrace();               
        }
		tabHost.setCurrentTab(2);
		tabHost.setCurrentTab(0);
		
	}

	private void setup_timeupdatethread(){
	    currenttimer_handler = new Handler();
	    currenttimer_runnable = new Runnable() {
			public void run() {
				refresh_currenttime();
				currenttimer_handler.postDelayed(currenttimer_runnable, 1000);
			}   
		};
	    currenttimer_handler.postDelayed(currenttimer_runnable, 1000);
	}

	private void refresh_currenttime(){		
		Date date = new Date(); 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		String today = sdf.format(date); 
		
		//TextView TempTexView; 
		//TempTexView = (TextView)findViewById(R.id.editProgramDescription);
		//TempTexView.setText((""+today));
	}

	private AdapterView.OnItemSelectedListener mOnSelectedListener = new AdapterView.OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
	    	list = (ListView) findViewById(R.id.listview_recmanager);
	      	if(list.hasFocus() == true){	
	      	}

			cur_select_item = position;
			TVBooking mTVBooking = getServiceInfoByPostion(position);
			if(mTVBooking!=null){
				int item_status = mTVBooking.getStatus();
				if(item_status==TVBooking.ST_END)
					record_db_id = mTVBooking.getID();
				else
					record_db_id = -1;

				long int_start = mTVBooking.getStart();
				Date dt_start =  new Date(int_start);
				Date dt_end   =  new Date(int_start+mTVBooking.getDuration());
				SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm"); 
				String str_start = sdf.format(dt_start); 
				String str_end   = sdf.format(dt_end);

				final TextView programName = (TextView)findViewById(R.id.programName);
				final TextView evt_name = (TextView)findViewById(R.id.evt_name);
				final TextView time = (TextView)findViewById(R.id.time);
				final TextView pvrfilename = (TextView)findViewById(R.id.filename);
 				
				programName.setText(mTVBooking.getProgramName());
				evt_name.setText(mTVBooking.getEventName());
				time.setText(""+str_start+" ~ "+str_end);
				pvrfilename.setText(mTVBooking.getRecordStoragePath() + "/" + mTVBooking.getRecordFilePath());
			}

			
		}
		public void onNothingSelected(AdapterView<?> parent){
		}
	};

	private int getPvrData(int status){
	    int n=0;

		if(status == PVR_STATUS_ALL){		
			mTVBookingList = TVBooking.selectAllPlayBookings(this);
		}
		else if(status == PVR_STATUS_REC_OVER){
			mTVBookingList = TVBooking.selectRecordBookingsByStatus(this,status);
		}	
		else{ 
			mTVBookingList = TVBooking.selectRecordBookingsByStatus(this,status);
		}	
		if(mTVBookingList!=null)
			n = mTVBookingList.length;
		else
			n=0;
		
		return n;
	 }  


	private void deletePvrData(int pos){
		TVBooking mTVBooking = getServiceInfoByPostion(pos);
		mTVBooking.delete();	
	}

	private void tabHostInit(){    

        if (null == tabHost){       	
        	tabHost = (TabHost) this.findViewById(R.id.TabHost01); 
            tabHost.setup();
        }
        
        if(android.os.Build.VERSION.RELEASE.equals("2.2.1")){
	    	tabHost.addTab(tabHost.newTabSpec("all")   
	                .setContent(R.id.LinearLayout1)   
	                .setIndicator(this.getResources().getString(R.string.all))); 
	                     
	        tabHost.addTab(tabHost.newTabSpec("recording")   
	                .setContent(R.id.LinearLayout1)
	                .setIndicator(this.getResources().getString(R.string.recording)));  
	                
	        tabHost.addTab(tabHost.newTabSpec("Complete")   
	                .setContent(R.id.LinearLayout1)
	               .setIndicator(this.getResources().getString(R.string.completed)));
	        
	        tabHost.addTab(tabHost.newTabSpec("waiting")   
	                .setContent(R.id.LinearLayout1)
	                .setIndicator(this.getResources().getString(R.string.scheduled)));
        		
        }
		else{
			tabHost.addTab(tabHost.newTabSpec("all")   
	                .setContent(R.id.LinearLayout1)   
	                .setIndicator(this.getResources().getString(R.string.all), this.getResources().getDrawable(R.drawable.recmanager_all))); 
	                     
	        tabHost.addTab(tabHost.newTabSpec("recording")   
	                .setContent(R.id.LinearLayout1)
	                .setIndicator(this.getResources().getString(R.string.recording), this.getResources().getDrawable(R.drawable.recmanager_recording_icon)));  
	                
	        tabHost.addTab(tabHost.newTabSpec("Complete")   
	                .setContent(R.id.LinearLayout1)
	                //.setIndicator(R.string.completed));  
	               .setIndicator(this.getResources().getString(R.string.completed), this.getResources().getDrawable(R.drawable.recmanager_complete)));
	        
	        tabHost.addTab(tabHost.newTabSpec("waiting")   
	                .setContent(R.id.LinearLayout1)
	                .setIndicator(this.getResources().getString(R.string.scheduled), this.getResources().getDrawable(R.drawable.recmanager_wait)));
        		
		}
           
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener(){

			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				System.out.println("tabId is " + tabId);
				if (tabId.equalsIgnoreCase("all"))
				{
					getPvrData(PVR_STATUS_ALL);
		        	myAdapter.notifyDataSetChanged();
				}
				else if (tabId.equalsIgnoreCase("recording"))
				{
					getPvrData(PVR_STATUS_RECORDING);
					
					myAdapter.notifyDataSetChanged();
				}
				else if (tabId.equalsIgnoreCase("Complete"))
				{
					getPvrData(PVR_STATUS_REC_OVER);
					myAdapter.notifyDataSetChanged();
				}
				else if (tabId.equalsIgnoreCase("waiting"))
				{
					getPvrData(PVR_STATUS_REC_WAIT);
					myAdapter.notifyDataSetChanged();				
				}
			}
        	
        });
             
    }

	private void refresh_data(){
		if(tabHost.getCurrentTabTag().equals("all"))
			getPvrData(PVR_STATUS_ALL);
		else if(tabHost.getCurrentTabTag().equals("recording")) 
			getPvrData(PVR_STATUS_RECORDING);
		else if(tabHost.getCurrentTabTag().equals("Complete")) 
			getPvrData(PVR_STATUS_REC_OVER);
		else if(tabHost.getCurrentTabTag().equals("waiting")) 
			getPvrData(PVR_STATUS_REC_WAIT);
		//list.setAdapter(myAdapter);         
       	myAdapter.notifyDataSetChanged();	
	}

    class programOnKeyListener implements OnKeyListener{
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			
			//System.out.println("key2---------" + arg2);
        	//System.out.println("key1--------" + arg1);
        	//System.out.println("arg0--------" + arg0);   	
			return false;
		}
    	 
    }

	/*disable HOME key*/
	@Override 
	public void onAttachedToWindow(){  
		//this.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_KEYGUARD);  
		super.onAttachedToWindow();  
	} 

   @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
	   switch (keyCode) {
			
			case KeyEvent.KEYCODE_DPAD_DOWN:			
				if(cur_select_item== list.getCount()-1)
			    	list.setSelection(0); 			
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
			  if(cur_select_item== 0)
				 list.setSelection(list.getCount()-1); 
				break;
			case KeyEvent.KEYCODE_ZOOM_IN:
				Intent pickerIntent = new Intent();
                pickerIntent.setClass(DTVPvrManager.this, DTVChannelList.class);
                startActivity(pickerIntent);
				return true;	
			case KeyEvent.KEYCODE_ZOOM_OUT:
				stopPlaying();
				if(record_db_id!=-1)
					startPlayback(record_db_id);
				return true;
			case KeyEvent.KEYCODE_BACK:
				DTVPvrPlayerStop();
				Intent intent = new Intent();
				intent.setClass(DTVPvrManager.this, DTVPlayer.class);
				startActivity(intent);
				DTVPvrManager.this.finish();
				break;
		}
		
		return super.onKeyDown(keyCode, event);
	}	  
    
    public TVBooking getServiceInfoByPostion(int position){
    	if (position > mTVBookingList.length){
    		return null;
    	}
    	
    	return (TVBooking) mTVBookingList[position];
    }
    
    
    class MyAdapter extends BaseAdapter{
		private LayoutInflater mInflater;    			
		private Context cont;
		private int selectItem;

		class ViewHolder {
			TextView     pvr_no;
			TextView     tvName;
			TextView     tvEvtName;		      		
			TextView     tvTime;
			TextView 	 filename;
			ImageView icon;
		}
    		
		public MyAdapter(Context context, List<Object> list) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);			  
		}

		public int getCount() {
			if(mTVBookingList==null)
				return 0;
			else
				return mTVBookingList.length;
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
		
		private void fillData(ViewHolder mv, int position){
			if (null != mv){
				TVBooking mTVBooking= getServiceInfoByPostion(position);
				if (null != mTVBooking){					  									   
					mv.tvName.setText(mTVBooking.getProgramName()); 
					mv.tvEvtName.setText(mTVBooking.getEventName());  

					long int_start = mTVBooking.getStart();
					Date dt_start =  new Date(int_start);
					Date dt_end   =  new Date(int_start+mTVBooking.getDuration());
					SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm"); 
					String str_start = sdf.format(dt_start); 
					String str_end   = sdf.format(dt_end);

					mv.tvTime.setText(""+str_start+" ~ "+str_end);  
					mv.filename.setText(mTVBooking.getRecordStoragePath() + "/" + mTVBooking.getRecordFilePath());
			   }
			 }
		 }
		 
		public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;	
			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.dtvrecmanager_list_item, null);
				holder = new ViewHolder();
				
			    holder.pvr_no = (TextView) convertView.findViewById(R.id.ItemText);
				holder.tvName= (TextView) convertView.findViewById(R.id.ItemText);
				holder.tvEvtName =(TextView) convertView.findViewById(R.id.evtname);		      		
		      	holder.tvTime=(TextView) convertView.findViewById(R.id.pvr_time);
				holder.filename=(TextView) convertView.findViewById(R.id.filename);			   
			    holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			    convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
			// Bind the data efficiently with the holder.
			fillData(holder, position);

			return convertView;
		}
	}	

    class listOnScroll implements OnScrollListener{
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			//System.out.println("view**********"  + view);
			//System.out.println("getSelectedItemPosition--------" + view.getSelectedItemPosition());
		}
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			//System.out.println("scrollState---------" + scrollState);
		}
    }	
	
	private AdapterView.OnItemClickListener mOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			TVBooking mTVBooking = getServiceInfoByPostion(position);
			int item_status = mTVBooking.getStatus();	
			record_db_id = mTVBooking.getID();
			proname = mTVBooking.getProgramName();
			final int  pos = position;
			choise = 0;
			builder = new AlertDialog.Builder(DTVPvrManager.this);
			switch(item_status){
				case PVR_STATUS_NOTIFIED:
				case PVR_STATUS_REC_WAIT:
					new SureDialog(DTVPvrManager.this){
						public void onSetMessage(View v){
							((TextView)v).setText(getString(R.string.sure_delete));
						}

						public void onSetNegativeButton(){
							
						}
						public void onSetPositiveButton(){
							deletePvrData(pos);
							getPvrData(PVR_STATUS_REC_WAIT);      
							myAdapter.notifyDataSetChanged();
							refresh_data();
						}
					};	
					break;
				case PVR_STATUS_REC_OVER:
					new SingleChoiseDialog(DTVPvrManager.this,new String[]  { "Play", "Delete" }, 0){
						public void onSetMessage(View v){
							((TextView)v).setText(getString(R.string.sure_factory_set));
						}

						public void onSetNegativeButton(){
							
						}
						public void onSetPositiveButton(int which){
							switch(which){
								case 0:														
									Bundle bundle_pvr_player = new Bundle();										           
									bundle_pvr_player.putInt("booking_id", record_db_id); 	
									bundle_pvr_player.putString("program_name", proname); 
									Intent Intent_pvrplayer = new Intent();
									Intent_pvrplayer.setClass(DTVPvrManager.this, DTVPvrPlayer.class);
									Intent_pvrplayer.putExtras(bundle_pvr_player);
				                    startActivity(Intent_pvrplayer);
									DTVPvrManager.this.finish();
									break;
								case 1:
									deletePvrData(pos);
									refresh_data();
									myAdapter.notifyDataSetChanged();
									break;
							}
						}
					};	
						
					break;
				case PVR_STATUS_RECORDING:	
					new SingleChoiseDialog(DTVPvrManager.this,new String[]  { "Stop", "Delete" }, 0){
						public void onSetMessage(View v){
							((TextView)v).setText(getString(R.string.sure_factory_set));
						}

						public void onSetNegativeButton(){
							
						}
						public void onSetPositiveButton(int which){
							switch(which){
								case 0:
									DTVPlayerStopRecording();
									break;
								case 1:
									DTVPlayerStopRecording();
									deletePvrData(pos);
									refresh_data();
									myAdapter.notifyDataSetChanged();
									break;
							}
						}
					};				
			}
		}
	};
	

	@Override
	protected void onStart(){
		Log.d(TAG, "onStart");
		super.onStart();
		setup_timeupdatethread();
		currenttimer_handler.postDelayed(currenttimer_runnable, 1000);
	}

	@Override
	protected void onStop(){
		Log.d(TAG, "onStop");
		super.onStop();
		currenttimer_handler.removeCallbacks(currenttimer_runnable); 
	}
	
}
