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

import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.AdapterView.*;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.os.Process;
import android.text.*;
import android.text.method.*;
import android.graphics.*;
import java.io.*;
import java.util.*;


public class DTVDeviceBrowser extends Activity implements OnItemClickListener {
	private static final String TAG="DTVDeviceBrowser";

	private getDiskInfoThread  t=null;
	
	int dev_list_sel = 0;			
    final int DIALOG_INSERT_USB_MESSAGE = 1;
    final int DIALOG_YES_NO_MESSAGE = 2;
    ArrayList<DeviceItem> deviceList= null;
    private Context mContext	= null;
    
    private MountEventReceiver mount_receiver = null;

	private ImageButton returnButton;

	private static int widthPixels=0;
	private static int heightPixels=0;
	private static float density =0;	
    
    class MountEventReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        Uri uri = intent.getData();
	
	        if (uri.getScheme().equals("file")) {
	        	String path = uri.getPath();
	        	if (action.equals(Intent.ACTION_MEDIA_MOUNTED)){
  
	        		try{
	        			dismissDialog(DIALOG_INSERT_USB_MESSAGE);
	        		}
	        		catch(IllegalArgumentException e){
	        			
	        		}

	        	    refreshDevice();
    			}
	        	else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)){
    		
	        		try{
	        			dismissDialog(DIALOG_INSERT_USB_MESSAGE);
	        		}catch(IllegalArgumentException e){
	        			
	        		}
	        		refreshDevice();
    			}
	        }
		}
    }
	
	DeviceItemAdapter mDeviceItemAdapter;
	private class DeviceItemAdapter extends BaseAdapter {
	    private LayoutInflater mInflater;
	      
		public DeviceItemAdapter(Context context) {
	        mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return deviceList.size();
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}	   
	    
	    class ViewHolder {
	        TextView text;
			TextView total_info;
			TextView spare_info;
	        ImageView icon;
	    }
	    
	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ViewHolder holder;
	        
	        if (convertView == null) {  // if it's not recycled, initialize some attributes				
		        convertView = mInflater.inflate(R.layout.dtvdevice_grid_item, null);
				holder = new ViewHolder();
	            holder.text = (TextView) convertView.findViewById(R.id.device_grid_item_textview);
				holder.total_info = (TextView) convertView.findViewById(R.id.device_grid_item_total);
	            holder.spare_info = (TextView) convertView.findViewById(R.id.device_grid_item_spare);
				holder.icon = (ImageView) convertView.findViewById(R.id.device_grid_item_imageview);
	            
	            convertView.setTag(holder);
	            
	        } else {
	            holder = (ViewHolder) convertView.getTag();
	        }

			try {
				DeviceItem itm = deviceList.get(position);
				holder.text.setText(itm.VolumeName);
				holder.total_info.setText(mContext.getResources().getString(R.string.volume_total)+itm.total);
				holder.spare_info.setText(mContext.getResources().getString(R.string.volume_available)+itm.spare);
				holder.icon.setImageBitmap(itm.icon);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	        return convertView;
	   	}
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	  	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	  	WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.dtvdevice_browser);
		mContext = this;    
		DTVDeviceBrowerUIInit();
	}

	private void DTVDeviceBrowerUIInit(){
         
    	deviceList = new ArrayList<DeviceItem>();
    	getDevice();
        if(deviceList!=null&&!deviceList.isEmpty()&&deviceList.size()>0){	
		}
        else{			
        	showDia(DIALOG_INSERT_USB_MESSAGE);
		}
        
        GridView gridview = (GridView) findViewById(R.id.device_gridview);
        mDeviceItemAdapter = new DeviceItemAdapter(DTVDeviceBrowser.this);
        gridview.setAdapter(mDeviceItemAdapter);
        gridview.setOnItemClickListener(this);

        gridview.setOnItemSelectedListener(new OnItemSelectedListener(){

				public void onItemSelected(AdapterView<?> arg0, View v, int position, long id) {

					dev_list_sel = position;
					
				}

				public void onNothingSelected(AdapterView<?> arg0) {

					dev_list_sel = -1;
					
				}
	        	
	        }
        ); 

		t =new getDiskInfoThread();  
		t.start();  
		refreshDevice();	
	}
	
	void refreshDevice(){
		getDevice();
		mDeviceItemAdapter.notifyDataSetChanged();
	}

	AlertDialog mAlertDialog=null;
	private void showDia(int id){
		mAlertDialog = (AlertDialog)onCreateDialog(id);
		if(mAlertDialog!=null){

			showDialog(id);
			//mAlertDialog.getWindow().setLayout(500,100);
			WindowManager.LayoutParams lp=mAlertDialog.getWindow().getAttributes();
			Log.d(TAG,"x="+lp.x+"y="+lp.y);
			//lp.x=0;
			//lp.y=0;
			lp.dimAmount=0.0f;
			mAlertDialog.getWindow().setAttributes(lp);
			mAlertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		}
		
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	
        switch (id) {
	        case DIALOG_INSERT_USB_MESSAGE:
				builder.setTitle(R.string.download_sel_confirm);
		                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int whichButton) {                     	
		            			if((dev_list_sel>=0)&&(dev_list_sel<deviceList.size()))
		            			{
									DeviceItem item = deviceList.get(dev_list_sel);  
									//DVBPlayer.getConnect().setRecorderStorePath(item.Path);
									DTVDeviceBrowser.this.finish();
		            			}
		                    }
		                });

				mAlertDialog = builder.create();	
				return mAlertDialog;
	        case DIALOG_YES_NO_MESSAGE:
			
				builder.setTitle(R.string.download_sel_confirm);
	                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {                     	
	            			if((dev_list_sel>=0)&&(dev_list_sel<deviceList.size()))
	            			{
	            				DeviceItem item = deviceList.get(dev_list_sel);  
								//DVBPlayer.getConnect().setRecorderStorePath(item.Path);
	            				DTVDeviceBrowser.this.finish();
	            			}
	                    }
	                });
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int whichButton) {

		                    }
		                });
				mAlertDialog = builder.create();
				return mAlertDialog;
        }
        return null;
    }

	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		if(position<0){
			position = 0;
			dev_list_sel=-1;
		}
		else{
			dev_list_sel = position;
		}
		
	    Log.d("onItemClick","onItemClick******"+position);	
		if(deviceList.size()>0){
				//showDialog(DIALOG_YES_NO_MESSAGE);
				showDia(DIALOG_YES_NO_MESSAGE);
		}
	}
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
			case KeyEvent.KEYCODE_DPAD_CENTER:
			//case KeyEvent.KEYCODE_A:
				if(deviceList.size()>0){
					showDialog(DIALOG_YES_NO_MESSAGE);
				}
				return true;
			case KeyEvent.KEYCODE_BACK:
	            finish();
	            break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mount_receiver = new MountEventReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addDataScheme("file");
		registerReceiver(mount_receiver, filter);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(mount_receiver != null)
			unregisterReceiver(mount_receiver);
	}
	
	private class DeviceItem{
		String Path;
		String VolumeName;
		String format; //fat,ntfs,etc..
		/*
		long spare;//in MB
		long total; //in MB		
		*/
		String spare;
		String total; 
		Bitmap icon;//device icon
	}	

	private void getDevice() {
		deviceList.clear();
		File[] files = new File("/mnt").listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getPath().startsWith("/mnt/sd") && !(file.getPath().equals("/mnt/sdcard"))) {
					File myfile = file;
					Log.d("*******","device path: "+myfile.getName());

						DeviceItem item = getDeviceName(myfile.getName());								
						if((item!= null)&&(item.format!=null)){
							item.Path = myfile.getPath();
							//readUsbDevice(item.Path,item,0);
							item.VolumeName = item.VolumeName+" ["+myfile.getName()+"]";
							Log.d("*******","device path: "+item.Path+" device format: "+item.format+" name: "+item.VolumeName);
							deviceList.add(item);
						}
				}
			}	

		}

		sdcard_deal();

		if(t!=null)
			t.onGetDiskInfo();
	}

	private void getDeviceOnBack() {
		deviceList.clear();
		File[] files = new File("/mnt").listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getPath().startsWith("/mnt/sd") && !(file.getPath().equals("/mnt/sdcard"))) {
					File myfile = file;
					Log.d("*******","device path: "+myfile.getName());

						DeviceItem item = getDeviceName(myfile.getName());								
						if((item!= null)&&(item.format!=null)){
							item.Path = myfile.getPath();
							readUsbDevice(item.Path,item,0);
							item.VolumeName = item.VolumeName+" ["+myfile.getName()+"]";
							Log.d("*******","device path: "+item.Path+" device format: "+item.format+" name: "+item.VolumeName);
							deviceList.add(item);
	
						}
				}
			}	

		}

		sdcard_deal();
	}
	 
	private DeviceItem getDeviceName(String devname){
    	
    	
    	DeviceItem item = new DeviceItem();  
    	
    	devname = SystemProperties.get("volume.label."+devname, null);
		if(devname == null || devname.length() == 0){
			devname =mContext.getResources().getString(R.string.volume); 
		}
		else{			
			String[] byteStrings = devname.split(" "); 
			byte[] bytes = new byte[byteStrings.length-1];
			for(int i=1; i<byteStrings.length; i++)
			{
				bytes[i-1] = Integer.decode(byteStrings[i]).byteValue();
			} 
			if(byteStrings[0].equals("NTFS"))
			{ 
				item.format = "NTFS";
				try {
					devname = new String(bytes,"UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					devname =mContext.getResources().getString(R.string.volume); 
				} 
			}
			else if(byteStrings[0].equals("VFAT")){
				try {
					item.format = "VFAT";
					devname = new String(bytes, "GB2312");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					devname =mContext.getResources().getString(R.string.volume); 
				} 
			}
			else {
				try {
					item.format = "UNKOWN";
					devname = new String(bytes, "GB2312");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					devname =mContext.getResources().getString(R.string.volume); 
				} 
			}
		}
		item.VolumeName = devname;
		item.icon = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.list_disk_unsel);
		return item;
	 }


	private void readUsbDevice(String path,DeviceItem item) { 	
			Log.d(TAG,"path=="+path);	

			try{
				StatFs sf = new StatFs(path);
				long blockSize = sf.getBlockSize();
				long blockCount = sf.getBlockCount();
				long availCount = sf.getAvailableBlocks();
				item.spare = Long.toString(availCount*blockSize/1024/1024)+"MB";
				item.total =  Long.toString(blockSize*blockCount/1024/10240)+"MB";
				
				Log.d("", "block:"+ blockSize+",block:"+ blockCount+",total"+blockSize*blockCount/1024/1024+"MB");
				Log.d("", "availid"+ availCount+",reserved:"+ availCount*blockSize/1024/1024+"MB"); 	
			}
			catch(IllegalArgumentException e){
				e.printStackTrace();
				Log.d(TAG,"IllegalArgumentException");
			}
			
			
	}  


	private  String deleteExtraSpace(String str){  
	        if(str==null){  
	            return null;  
	        }  
	        if(str.length()==0 || str.equals(" ")){  
	            return new String();  
	        }  
	        char[] oldStr=str.toCharArray();  
	        int len=str.length();  
	        char[] tmpStr=new char[len];  
	        boolean keepSpace=false;  
	        int j=0;//the index of new string  
	        for(int i=0;i<len;i++){  
	            char tmpChar=oldStr[i];  
	            if(oldStr[i]!=' '){  
	                tmpStr[j++]=tmpChar;  
	                keepSpace=true;  
	            }else if(keepSpace){  
	                tmpStr[j++]=tmpChar;  
	                keepSpace=false;  
	            }  
	        }  
	         
	        int newLen=j;  
	        if(tmpStr[j-1]==' '){  
	            newLen--;  
	        }  
	        char[] newStr=new char[newLen];  
	        for(int i=0;i<newLen;i++){  
	            newStr[i]=tmpStr[i];  
	        }  
	        return new String(newStr);  
    }  


	private void readUsbDevice(String path,DeviceItem item,int mode) 	{ 	
		/*
			Runtime runtime = Runtime.getRuntime();  
            
			String cmd = "df "+path ;
            
    		try {
				Process proc = runtime.exec(cmd);
				InputStream input = proc.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(input));
				String strLine;
				while(null != (strLine = br.readLine())){
					Log.d(TAG,">>>"+strLine);
					///mnt/sda2: I/O error
					if(strLine.startsWith(path)){
						strLine = deleteExtraSpace(strLine);	
						String[] byteStrings = strLine.split(" "); 
						Log.d(TAG,"size="+byteStrings[1]+"free=="+byteStrings[3]);
						
						item.total= byteStrings[1];
						item.spare=byteStrings[3]; 	
						break;	
					}			
				}	
    		} 
    		catch (IOException e) {
    			e.printStackTrace();
    		} 
		*/	
	}  

	private void readSDCard(DeviceItem item) { 
		String state = Environment.getExternalStorageState(); 
		if(Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			long blockSize = sf.getBlockSize();
			long blockCount = sf.getBlockCount();
			long availCount = sf.getAvailableBlocks();

			item.spare = Long.toString(availCount*blockSize/1024/1024)+"MB";
			item.total =  Long.toString(blockSize*blockCount/1024/10240)+"MB";
			
			Log.d("", "block:"+ blockSize+",block:"+ blockCount+",total"+blockSize*blockCount/1024/1024+"MB");
			Log.d("", "availid"+ availCount+",reserved:"+ availCount*blockSize/1024/1024+"MB"); 
			}
	}  

	private void readSDCard(String path,DeviceItem item){
		readUsbDevice(path,item,0);
	}

	private boolean sdcard_deal(){
		String externalStorageState = Environment.getExternalStorageState();  
		String result = "";  


        if(externalStorageState.equals(Environment.MEDIA_MOUNTED)){  

            Log.d("SDcard",Environment.getExternalStorageDirectory().getAbsolutePath()); 

			DeviceItem item = getSdcardDevice(Environment.getExternalStorageDirectory().getAbsolutePath());

			if(deviceList!=null)
			{
				readSDCard("/mnt/sdcard",item);
				deviceList.add(item);

			}	
			/*
            File file = new File(Environment.getExternalStorageDirectory(),"chenzheng_java.txt");  
            result+="   sdcard absolute path:"+Environment.getExternalStorageDirectory().getAbsolutePath();  
            result+=" /n  sdcard path:"+Environment.getExternalStorageDirectory().getPath();  
            if(!file.exists()){  
                try {  
                    file.createNewFile();  
                    Log.d("result", "ok!!!");  
                } catch (IOException e) {  
                    Log.d("result", "fail!!!!!");  
                    e.printStackTrace();  
                }  
            } 
			*/

			return true;
        }  
		else
		{
			return false;
		}
	}

	 DeviceItem getSdcardDevice(String path){
    	DeviceItem item = new DeviceItem();  

		item.Path = path;
		item.VolumeName = "  SDCARD";
		item.icon = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.list_disk_unsel);
		return item;
	 }
	



	class returnSearchListener implements OnClickListener
    {
		public void onClick(View v) 
		{
			finish();
			//onDestroy();
		}
    }

	class ExitOnFocusChange  implements OnFocusChangeListener
	{
		public void onFocusChange(View v, boolean isFocused)
	    {		
	      if (isFocused==true)
	      {
	    	  //((ImageButton)v).setBackgroundResource(R.drawable.button_return_select);
	      }
	      else 
	      {
	    	  //((ImageButton)v).setBackgroundResource(R.drawable.button_return_unselect);
	      }
	    }	
	}	

	//------------------------------------------------        
  
	public class EventHandler extends Handler {  

		public EventHandler(Looper looper) {  
		     super(looper);  
		}  

		@Override  

		public void handleMessage(Message msg) {  
			   switch (msg.what) { 
					case 1: 
			   		 mDeviceItemAdapter.notifyDataSetChanged();
					break;
				}  
		}
	}  
  
	class getDiskInfoThread extends Thread {
		private Handler mHandler = null;
		
		public void run(){
			Looper.prepare();

			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					switch (msg.what) { 
						case 1: {							
								getDeviceOnBack();
								Message message=new Message();
								message.what=1;
								EventHandler ha =new EventHandler(Looper.getMainLooper());  
								ha.sendMessage(message);
							}
							break;
						default:
							break;
					}  
				}
			};

			Looper.loop();
			Log.d(TAG, "work thread will now exit.");
		}

		public void quitLoop() {
			if (mHandler != null && mHandler.getLooper() != null) {
				mHandler.getLooper().quit();
			}
		}

		public void onGetDiskInfo() {
			if (mHandler != null)
				mHandler.sendMessage(mHandler.obtainMessage(1));
		}

	}

	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		
		if (t != null) {
			/*wait the thread exit*/
			t.quitLoop();
			try {
				t.join();
				Log.d(TAG, "wait for work thread exit done!");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
