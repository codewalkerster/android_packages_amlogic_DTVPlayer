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

import android.os.Bundle;
import android.os.SystemProperties;
import android.os.Environment; 
import android.os.StatFs;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;
import android.text.*;

import android.text.method.*;
import android.graphics.*;
import java.io.*;
import java.util.*;

import com.amlogic.widget.PasswordDialog;
import com.amlogic.widget.SureDialog;
import com.amlogic.widget.SingleChoiseDialog;
import com.amlogic.widget.MutipleChoiseDialog;
import com.amlogic.widget.PasswordSettingDialog;

public class DTVRecordDevice extends DTVActivity {
	private static final String TAG="DTVRecordDevice";

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
	private String sd_path="/storage/external_storage/sdcard1";
	private TextView disk_type=null;
	private TextView disk_total=null;
	private TextView disk_space=null;
	private boolean mMidUi = false;
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	  	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	  	WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		if (SystemProperties.get("ro.product.brand", "").equals("MID")) {
			mMidUi = true;
			setContentView(R.layout.mid_dtv_record_device);
		} else {
			mMidUi = false;
			setContentView(R.layout.dtv_record_device);
		}
		
		mContext = this;  	
	}

	DTVSettings mDTVSettings; 
	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		mDTVSettings = new DTVSettings(DTVRecordDevice.this);
		DTVRecordDeviceUIInit();
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
	private void DTVRecordDeviceUIInit(){

		findViewById(R.id.return_icon).setOnClickListener(
			new View.OnClickListener(){	  
				public void onClick(View v) {		
					// TODO Auto-generated method stub	
					 finish();
				}
			}
		);
		
		deviceList = new ArrayList<DeviceItem>();
		getDevice();

		ListView itemListView = (ListView) findViewById(R.id.itemListView);
		String[] DATA = getResources().getStringArray(R.array.record_device_content);
		mDeviceItemAdapter = new ItemAdapter(DTVRecordDevice.this,DATA);
		itemListView.setAdapter(mDeviceItemAdapter);

		itemListView.setOnItemClickListener(mOnItemClickListener); 

		disk_type = (TextView) findViewById(R.id.type);
		disk_total = (TextView) findViewById(R.id.total);
		disk_space = (TextView) findViewById(R.id.space);

		t =new getDiskInfoThread();  
		t.start();  
		//refreshDevice();	
	}

	private ItemAdapter mDeviceItemAdapter=null;
	private class ItemAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context cont;
		private String[] listItems;

	 	class ViewHolder {
			TextView text;
			ImageView icon;
		    TextView   info; 
		    ImageButton  iboolean;
		    ImageView icon1;
		}
	
		public ItemAdapter(Context context, String[] list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);
		}

		public int getCount() {
			return listItems.length;
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
				convertView = mInflater.inflate(R.layout.dtvsettings_list_item, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.info = (TextView)convertView.findViewById(R.id.info);
				holder.icon1 = (ImageView)convertView.findViewById(R.id.icon1);
				convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data efficiently with the holder.
			holder.text.setText(listItems[position]);
			holder.icon.setVisibility(View.INVISIBLE);
			holder.info.setTextColor(Color.YELLOW);
			switch(position){
			
		     case 0://device select:
				holder.info.setVisibility(View.VISIBLE);
				holder.icon1.setVisibility(View.INVISIBLE);
				if(deviceList==null)
					Log.d(TAG,"-----deviceList is null");
				else
					Log.d(TAG,"deviceList.isEmpty="+deviceList.isEmpty()+"----deviceList.size()="+deviceList.size());
				
				if(deviceList!=null&&!deviceList.isEmpty()&&deviceList.size()>0){
					DeviceItem item = deviceList.get(dev_list_sel);  
					Log.d(TAG,"path="+item.Path);
					mDTVSettings.setRecordStoragePath(item.Path);
					holder.info.setText(item.Path);

					//disk_type.setText(item.format);
					disk_total.setText(item.total);
					disk_space.setText(item.spare);
				}
				else{			
					holder.info.setText(R.string.no_usb_device);
					disk_type.setText(null);
					disk_total.setText(null);
					disk_space.setText(null);
				}
				
		    	break;
		     case 1://timeshifting settings:
				holder.info.setVisibility(View.VISIBLE);
				holder.icon1.setVisibility(View.INVISIBLE);
				holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 
				int time = mDTVSettings.getTimeShiftingDuration();
				switch(time){
					case 600:
						holder.info.setText("10 Min");
						break;
					case 30*60:
						holder.info.setText("30 Min");
						break;
					case 60*60:
						holder.info.setText("60 Min");
						break;
					default:
						holder.info.setText("10 Min");
						break;
				}
				break;
		  }

		  return convertView;
		}
	}	
	
	void refreshDevice(){
		if(t!=null)
			t.onGetDiskInfo();
		mDeviceItemAdapter.notifyDataSetChanged();
	}


	private void showPromptDialog(int id){
		switch (id) {
	        case DIALOG_INSERT_USB_MESSAGE:
				new SureDialog(DTVRecordDevice.this){
					public void onSetMessage(View v){
						((TextView)v).setText(getString(R.string.download_sel_confirm));
					}

					public void onSetNegativeButton(){
						
					}
					public void onSetPositiveButton(){
						Log.d(TAG,"dev_list_sel="+dev_list_sel);
						if((dev_list_sel>=0)&&(dev_list_sel<deviceList.size())){
							DeviceItem item = deviceList.get(dev_list_sel);  
							Log.d(TAG,"path="+item.Path);
							mDTVSettings.setRecordStoragePath(item.Path);
							DTVRecordDevice.this.finish();
            			}
					}
				};

				break;
	        case DIALOG_YES_NO_MESSAGE:		
				new SureDialog(DTVRecordDevice.this){
					public void onSetMessage(View v){
						((TextView)v).setText(getString(R.string.download_sel_confirm));
					}

					public void onSetNegativeButton(){
						
					}
					public void onSetPositiveButton(){
						Log.d(TAG,"dev_list_sel="+dev_list_sel);
						if((dev_list_sel>=0)&&(dev_list_sel<deviceList.size())){
            				DeviceItem item = deviceList.get(dev_list_sel);
							Log.d(TAG,"path="+item.Path);
							mDTVSettings.setRecordStoragePath(item.Path);
            				DTVRecordDevice.this.finish();
            			}
					}
				};

				break;
        }
		
		return;
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
									DTVRecordDevice.this.finish();
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
	            				DTVRecordDevice.this.finish();
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


	private AdapterView.OnItemClickListener mOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){			
			ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);

			final TextView info_cur = (TextView)v.findViewById(R.id.info);
			
			switch(position){
				case 0:
					showUsbDeviceListDialog(info_cur);
					break;
				case 1:
					showTimeshiftingTimeSettingDialog(info_cur);
				break;
			}
		}
	};

	SingleChoiseDialog myUsbDeviceSingleChoiseDialog=null;
	private void showUsbDeviceListDialog(TextView v){
		final TextView info_cur = v;
		int pos = 0;
		pos = dev_list_sel;
		
		String[] list_content=null;
		if(deviceList!=null){
			list_content=new String[deviceList.size()];
			for(int i=0;i<deviceList.size();i++){
				DeviceItem item = deviceList.get(i);  
				list_content[i]=item.Path;
			}
		}
		myUsbDeviceSingleChoiseDialog=new SingleChoiseDialog(DTVRecordDevice.this,list_content,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.pvr_storage_select));
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				if(deviceList!=null&&deviceList.size()>0){
					dev_list_sel = which;
					DeviceItem item = deviceList.get(which);  
					Log.d(TAG,"path="+item.Path);
					info_cur.setText(item.Path);
					mDTVSettings.setRecordStoragePath(item.Path);
					//disk_type.setText(item.format);
					disk_total.setText(item.total);
					disk_space.setText(item.spare);
				}	
			}
		};				
	}

	private void updateUsbDeviceList(){
		if(myUsbDeviceSingleChoiseDialog!=null){
			String[] list_content=null;
			if(deviceList!=null){
				list_content=new String[deviceList.size()];
				for(int i=0;i<deviceList.size();i++){
					DeviceItem item = deviceList.get(i);  
					list_content[i]=item.Path;
				}
			}
			myUsbDeviceSingleChoiseDialog.updateDialog(list_content,0);
		}
	}

	private void showTimeshiftingTimeSettingDialog(TextView v){
		final TextView info_cur = v;
		int pos = 0;
		int time = mDTVSettings.getTimeShiftingDuration();
		switch(time){
			case 600:
				pos=0;
				break;
			case 30*60:
				pos=1;
				break;
			case 60*60:
				pos=2;
				break;	
			default:
				pos=0;
				break;
		}

		new SingleChoiseDialog(DTVRecordDevice.this,new String[]{ "10 Min","30 Min","60 Min"},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.timeshift_time_set));
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
					//pos = which;
					case 0:
						info_cur.setText("10 Min");
						mDTVSettings.setTimeShiftingDuration(10*60);
						break;
					case 1:
						info_cur.setText("30 Min");
						mDTVSettings.setTimeShiftingDuration(30*60);
						break;
					case 2:
						info_cur.setText("60 Min");
						mDTVSettings.setTimeShiftingDuration(60*60);
						break;	
				}
			}
		};				
	}

		
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(!connected){
			return true;
			}
		switch(keyCode){
		
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
		File[] files = new File("/storage/external_storage").listFiles();
		if (files != null) {
			for (File file : files) {
				if (((file.getPath().startsWith("/storage/external_storage/sd"))||(file.getPath().startsWith("/storage/external_storage/udisk"))) && !(file.getPath().startsWith("/storage/external_storage/sdcard"))) {
	
					File myfile = file;
					Log.d(TAG,"device path: "+myfile.getName());
					DeviceItem item = new DeviceItem();//getDeviceName(myfile.getName());								
					//if((item!= null)&&(item.format!=null)){
						item.Path = myfile.getPath();
						readUsbDevice(item.Path,item,0);
						Log.d(TAG,"item.total: "+item.total+"item.spare: "+ item.spare);
						if(item.total.equals("0.0K") && item.spare.equals("0.0K")) {
							Log.d(TAG,"external storage device is invalid");
						} else {
							item.VolumeName = item.VolumeName+" ["+myfile.getName()+"]";
							Log.d(TAG,"device path: "+item.Path+" device format: "+item.format+" name: "+item.VolumeName);
							deviceList.add(item);
						}
					//}
				}
			}	
		}

		sdcard_deal(sd_path);

		String cur_record_path=mDTVSettings.getRecordStoragePath();
		if(deviceList!=null){
			for(int i=0;i<deviceList.size();i++){
				DeviceItem item = deviceList.get(i);  
				if(cur_record_path!=null)
				  if(cur_record_path.equals(item.Path)){
					 dev_list_sel=i;
					 break;
				  }
			}
		}

		//if(t!=null)
			//t.onGetDiskInfo();
	}

	private void getDeviceOnBack() {
		deviceList.clear();
		File[] files = new File("/storage/external_storage").listFiles();
		if (files != null) {
			for (File file : files) {
				if (((file.getPath().startsWith("/storage/external_storage/sd"))||(file.getPath().startsWith("/storage/external_storage/udisk"))) && !(file.getPath().startsWith("/storage/external_storage/sdcard"))) {
					File myfile = file;
					Log.d(TAG,"device path: "+myfile.getName());

					DeviceItem item = new DeviceItem();//getDeviceName(myfile.getName());								
					//if((item!= null)&&(item.format!=null)){
						item.Path = myfile.getPath();
						readUsbDevice(item.Path,item,0);
						item.VolumeName = item.VolumeName+" ["+myfile.getName()+"]";
						Log.d(TAG,"device path: "+item.Path+" device format: "+item.format+" name: "+item.VolumeName);
						deviceList.add(item);

					//}
				}
			}	

		}

		sdcard_deal(sd_path);
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
		//item.icon = BitmapFactory.decodeResource(mContext.getResources(),	R.drawable.list_disk_unsel);
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
				readSDCard("/storage/external_storage/sdcard1",item);
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

	private boolean findSdcardString(String path){ 	
		 Runtime runtime = Runtime.getRuntime();  
            
            	String cmd = "mount";
		
    		try {
			File dir = new File(path);
			String tmp = dir.getCanonicalPath();				

			Process proc = runtime.exec(cmd);
			InputStream input = proc.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String strLine;
			while(null != (strLine = br.readLine())){
				Log.d(TAG,">>>"+strLine);
					for(int i=0;i<strLine.length();i++){
						if(strLine.regionMatches(i,tmp,0,tmp.length()))
							return true;
						
					}
			}	
    		}
	
    		catch (IOException e) {
    			e.printStackTrace();
			return false;
    		} 

		return false;	
	}  

	private boolean sdcard_deal(String path)
	{
		if(findSdcardString(path)){
			DeviceItem item = getSdcardDevice(path);

			if(deviceList!=null){
				readSDCard(sd_path,item);
				deviceList.add(item);
			}
			return true;
		}
		else
			return false;
	}


	 DeviceItem getSdcardDevice(String path){
    	DeviceItem item = new DeviceItem();  

		item.Path = path;
		item.VolumeName = "  SDCARD";
		//item.icon = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.list_disk_unsel);
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
			   		 updateUsbDeviceList();
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

