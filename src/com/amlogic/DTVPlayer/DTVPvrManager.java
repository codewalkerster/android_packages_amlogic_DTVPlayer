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
import com.amlogic.tvutil.DTVRecordParams;

import java.util.*;
import java.text.*;
import java.io.File;

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
import com.amlogic.widget.CheckUsbdevice;

public class DTVPvrManager extends DTVActivity{
	private static final String TAG="DTVPvrManager";

	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtv_pvr_manager);
		VideoView video_view= (VideoView) findViewById(R.id.VideoView);
		openVideo(video_view,null);
	}

	protected void onRestart() {
		// TODO Auto-generated method stub
		Log.d(TAG,"-------onRestart--------");
		super.onRestart();
		
	}

	@Override
	protected void onStop(){
		Log.d(TAG, "onStop");
		super.onStop();
		
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
			case TVMessage.TYPE_PLAYBACK_START:
				Log.d(TAG, "Playback start, media info:");

				DTVRecordParams mediaInfo = msg.getPlaybackMediaInfo();
				TVProgram.Audio[] audios = mediaInfo.getAllAudio();
				if (audios != null){
					for (int i=0; i<audios.length; i++){
						Log.d(TAG, "Audio"+i+": pid "+audios[i].getPID()+", fmt "+
							audios[i].getFormat()+", lang "+audios[i].getLang());
					}
				}
				TVProgram.Subtitle[] subtitles = mediaInfo.getAllSubtitle();
				if (subtitles != null){
					for (int i=0; i<subtitles.length; i++){
						Log.d(TAG, "Subtitle"+i+": pid "+subtitles[i].getPID()+
							", type "+subtitles[i].getType()+
							", lang "+subtitles[i].getLang()+
							", "+subtitles[i].getCompositionPageID()+
							","+subtitles[i].getAncillaryPageID()+
							","+subtitles[i].getMagazineNumber()+
							","+subtitles[i].getMagazineNumber()+
							","+subtitles[i].getPageNumber());
					}
				}
				TVProgram.Teletext[] teletexts = mediaInfo.getAllTeletext();
				if (teletexts != null){
					for (int i=0; i<teletexts.length; i++){
						Log.d(TAG, "Teletext"+i+": pid "+teletexts[i].getPID()+
							", lang "+teletexts[i].getLang()+
							","+teletexts[i].getMagazineNumber()+
							","+teletexts[i].getPageNumber());
					}
				}
				break;
			
			default:
				break;
	
		}
	}


	private ListView list;
	private ArrayList<Object> serviceList;  
    private MyAdapter myAdapter;
  	private int cur_select_item = 0;

	int choise = 0;	

	private String filename=null;
	private int record_db_id;
	private Toast toast=null;

	private Handler  currenttimer_handler;
	private Runnable currenttimer_runnable;

	private List<String> filenameList=null;
	private void getFileList(){
		CheckUsbdevice Usbdevice = new CheckUsbdevice(this);

		if(Usbdevice.getDevice()==null)
		{
			toast = Toast.makeText(
			DTVPvrManager.this,
			R.string.check_usb_device,
			Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		else
		{
	
			filenameList = Usbdevice.getPvrFileList();
			if(filenameList == null){
			}			
		}	

	}

	private int getFileListCount(){
		if(filenameList == null){
			return 0;
		}		
		else 
			return filenameList.size();
	}
	
	private void DTVPvrManagerUIInit(){
		findViewById(R.id.return_icon).setOnClickListener(
			new View.OnClickListener(){	  
				public void onClick(View v) {		
					// TODO Auto-generated method stub		
					DTVPvrPlayerStop();
					Intent intent = new Intent();
					intent.setClass(DTVPvrManager.this, DTVSettingsMenu.class);
					startActivity(intent);
					DTVPvrManager.this.finish();
				}
			}
		);
		
		list = (ListView)findViewById(R.id.listview_recmanager);
		list.setOnItemSelectedListener(mOnSelectedListener);
		list.setOnItemClickListener(mOnItemClickListener);
		list.setOnScrollListener(new listOnScroll()); 
		getFileList();
		if (null == myAdapter){
			myAdapter = new MyAdapter(DTVPvrManager.this,filenameList);
			myAdapter.notifyDataSetChanged();
		}

		list.setAdapter(myAdapter);
		list.requestFocus();

		stopPlaying();
	}

	private AdapterView.OnItemSelectedListener mOnSelectedListener = new AdapterView.OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
	    	list = (ListView) findViewById(R.id.listview_recmanager);
	      	if(list.hasFocus() == true){	
	      	}

			cur_select_item = position;
			getPvrFileInfo(position);
		}
		public void onNothingSelected(AdapterView<?> parent){
			
		}
	};

	private String getIsRecordingFileName(){
		DTVRecordParams recPara = getRecordingParams();
		if (recPara != null) {	
		  return recPara.getRecordFilePath();
		}  
		else return null;
	}

	private void deletePvrData(int pos){
		String file_name = getServiceInfoByPostion(pos);
		if(file_name!=null){
			if(getIsRecordingFileName()!=null){
				if(getIsRecordingFileName().equals(file_name)){
					toast = Toast.makeText(
					DTVPvrManager.this,
					R.string.dtvplayer_pvr_is_running,
					Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return;
				}
			}
			
			
			File file = new File(file_name);
			if (file.exists()) { 
				if (file.isFile()) { 
					file.delete(); 
				} else if (file.isDirectory()) { 
					/*
					File files[] = file.listFiles(); 
					for (int i = 0; i < files.length; i++) { 
						//this.deleteFile(files[i].getPath());
					}
					*/
				}
				file.delete();
			} else {
				
			}	
		}

		if(filenameList!=null)
			filenameList.remove(pos);
	}

	private void getPvrFileInfo(int pos){
		String file_name = getServiceInfoByPostion(pos);
		if(file_name!=null){
			File file = new File(file_name);
			if (file.exists()) { 
				Date date=new Date(file.lastModified());

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				String time = sdf.format(date); 
				file.length(); 
				Log.d(TAG,"file size="+file.length()+"-----time = "+time);

				ListView detail= (ListView) findViewById(R.id.List_detail);
				detail.setVisibility(View.VISIBLE);
		        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();   

			    //float size = ((float)file.length())/1024/1024;
				HashMap<String, Object> map = new HashMap<String, Object>();   
				map.put("ItemTitle", getString(R.string.size)+":");   
				map.put("ItemText", file.length()+" B");   
				listItem.add(map);   

				HashMap<String, Object> map1 = new HashMap<String, Object>();   
				map1.put("ItemTitle", getString(R.string.recmanager_time)+":"); 
				map1.put("ItemText", time);   
				listItem.add(map1); 

				HashMap<String, Object> map2 = new HashMap<String, Object>();   
				map2.put("ItemTitle", getString(R.string.recmanager_filename)+":");
				map2.put("ItemText", file.getName());   
				listItem.add(map2); 
						       
		        SimpleAdapter listItemAdapter = new SimpleAdapter(DTVPvrManager.this,listItem,
					R.layout.simple_list_item,
					new String[] {"ItemTitle", "ItemText"},
					new int[] {R.id.ItemTitle,R.id.ItemText}   
		        );   

		        detail.setAdapter(listItemAdapter);   	
			} else {
				
			}	
		}

	}

	private void refresh_data(){
		list = (ListView)findViewById(R.id.listview_recmanager);
		list.setAdapter(myAdapter);
       	myAdapter.notifyDataSetChanged();

		ListView detail= (ListView) findViewById(R.id.List_detail);
		if(filenameList==null)
			detail.setVisibility(View.INVISIBLE);
		else if(filenameList.size()==0)
			detail.setVisibility(View.INVISIBLE);
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
		if(!connected){
			return true;
			}
	   switch (keyCode) {
			
			case KeyEvent.KEYCODE_DPAD_DOWN:			
				if(cur_select_item== list.getCount()-1)
			    	list.setSelection(0); 			
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
			  if(cur_select_item== 0)
				 list.setSelection(list.getCount()-1); 
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:			
						
				return true;	
			case DTVActivity.KEYCODE_YELLOW_BUTTON:
				if(getFileListCount()>0){
					filename = getServiceInfoByPostion(cur_select_item);
					Bundle bundle_pvr_player = new Bundle();										           
					bundle_pvr_player.putString("file_name", filename); 	
					Intent Intent_pvrplayer = new Intent();
					Intent_pvrplayer.setClass(DTVPvrManager.this, DTVPvrPlayer.class);
					Intent_pvrplayer.putExtras(bundle_pvr_player);
	                		startActivity(Intent_pvrplayer);
					DTVPvrManager.this.finish();
				}	
				return true;	
			case DTVActivity.KEYCODE_RED_BUTTON:
				if(getFileListCount()>0){
					stopPlaying();
					String file_name = getServiceInfoByPostion(cur_select_item);	
					if(file_name!=null){
						setBlackoutPolicy(1);
						startPlayback(file_name);
					}	
				}	
				return true;
			case DTVActivity.KEYCODE_BLUE_BUTTON:
				if(getFileListCount()>0){
					new SureDialog(DTVPvrManager.this){
						public void onSetMessage(View v){
							((TextView)v).setText(getString(R.string.sure_delete));
						}

						public void onSetNegativeButton(){
							
						}
						public void onSetPositiveButton(){
							deletePvrData(cur_select_item);
							refresh_data();
						}
					};	
				}	
				return true;		
			case KeyEvent.KEYCODE_BACK:
				DTVPvrPlayerStop();
				Intent intent = new Intent();
				intent.setClass(DTVPvrManager.this, DTVSettingsMenu.class);
				startActivity(intent);
				DTVPvrManager.this.finish();
				return true;	
		}
		
		return super.onKeyDown(keyCode, event);
	}	  
    
    public String getServiceInfoByPostion(int position){
		if(filenameList!=null)
    		return filenameList.get(position);
		else return null;
    }
    
    
    class MyAdapter extends BaseAdapter{
		private LayoutInflater mInflater;    			
		private Context cont;
		private int selectItem;
		private List<String> listItems=null;
		
		class ViewHolder {  
			TextView 	 filename;
			TextView     Time;
			ImageView icon;
		}
    		
		public MyAdapter(Context context, List<String> list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);			  
		}

		public int getCount() {
			if(listItems==null)
				return 0;
			else 
				return listItems.size();
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
				mv.filename.setText(getServiceInfoByPostion(position)); 
			 }
			 
		 }
		 
		public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;	
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.pvr_manager_item, null);
				holder = new ViewHolder();
				
		      	holder.Time=(TextView) convertView.findViewById(R.id.pvr_time);
				holder.filename=(TextView) convertView.findViewById(R.id.filename);			   
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
			String file_name = getServiceInfoByPostion(position);
			final int  pos = position;
			choise = 0;
				
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
							filename = getServiceInfoByPostion(pos);
							bundle_pvr_player.putString("file_name", filename); 	
							Intent Intent_pvrplayer = new Intent();
							Intent_pvrplayer.setClass(DTVPvrManager.this, DTVPvrPlayer.class);
							Intent_pvrplayer.putExtras(bundle_pvr_player);
		                    startActivity(Intent_pvrplayer);
							DTVPvrManager.this.finish();
							break;
						case 1:
							{
								new SureDialog(DTVPvrManager.this){
									public void onSetMessage(View v){
										((TextView)v).setText(getString(R.string.sure_delete));
									}

									public void onSetNegativeButton(){
										
									}
									public void onSetPositiveButton(){
										deletePvrData(pos);
										refresh_data();
									}
								};	
							}
							break;
					}
				}
			};			
		}
	};
	

}

