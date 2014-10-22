package com.amlogic.widget;

import java.io.*;
import java.util.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import android.widget.*;
import android.os.SystemProperties;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment; 
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import com.amlogic.DTVPlayer.R;

public class CheckUsbdevice
{
	public static final String TAG = "CheckUsbdevice";

	private Context mContext = null;
	
	public CheckUsbdevice(Context context)
	{
		this.mContext =  context;
	}

	private class DeviceItem{
		String Path;
		String VolumeName;
		String format; //fat,ntfs,etc..
		long spare;//in MB
		long total; //in MB		
		Bitmap icon;//device icon
	}	

	public String checkPvrFilePath(String file_path){
		String path=null;

		File[] files = new File("/storage/external_storage").listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getPath().startsWith("/storage/external_storage/sd") && !(file.getPath().startsWith("/storage/external_storage/sdcard"))) {
					/*File[] myfiles = new File(file.getPath()).listFiles();
					if (myfiles != null){
						for (File myfile : myfiles){*/
							File myfile = file;
							if(myfile.canRead()){
								DeviceItem item = new DeviceItem();//getDeviceName(myfile.getName());								
								//if((item!= null)&&(item.format!=null)){
									item.Path = myfile.getPath();		
									path = item.Path+"/"+file_path;
									File pvr_file = new File(path);
									if(pvr_file.canRead())
										return path;
										
								//}
							}
						/*}
					}*/
				}
			}	
		}

		if(sdcard_deal("/storage/external_storage/sdcard1"))
		{
			path = "/storage/external_storage/sdcard1/"+file_path;
			File pvr_file = new File(path);
			if(pvr_file.canRead())
				return path;
		}
		
		return null;
		
	}




	public String getDevice() {
		File[] files = new File("/storage/external_storage").listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getPath().startsWith("/storage/external_storage/sd") && !(file.getPath().startsWith("/storage/external_storage/sdcard"))) {
					/*File[] myfiles = new File(file.getPath()).listFiles();
					if (myfiles != null){
						for (File myfile : myfiles){*/
							File myfile = file;
							if(myfile.canRead()){
								DeviceItem item = new DeviceItem();//getDeviceName(myfile.getName());								
								//if((item!= null)&&(item.format!=null)){
								//if((item!= null)){
									item.Path = myfile.getPath();
									item.VolumeName = item.VolumeName+" ["+myfile.getName()+"]";
									Log.d("*******","device path: "+item.Path+" device format: "+item.format+" name: "+item.VolumeName);
									return item.Path;
									
								//}
							}
						/*}
					}*/
				}
			}	
		}

		if(sdcard_deal("/storage/external_storage/sdcard1"))
			return "/storage/external_storage/sdcard1";

		return null;
	}
	 
    DeviceItem getDeviceName(String devname){

    	DeviceItem item = new DeviceItem();  
		
    	devname = SystemProperties.get("volume.label."+devname, null);
		if(devname == null || devname.length() == 0){
			devname =mContext.getResources().getString(R.string.volume); 
		}
		else
		{			
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

	public boolean findSdcardString(String path){ 	
		 Runtime runtime = Runtime.getRuntime();  
            
            	String cmd = "mount";
		if(path.equals(" "))
			return false;
			
    		try {
			Process proc = runtime.exec(cmd);
			InputStream input = proc.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String strLine;
			while(null != (strLine = br.readLine())){
				Log.d(TAG,">>>"+strLine);
					for(int i=0;i<strLine.length();i++){
						if(strLine.regionMatches(i,path,0,path.length()))
							return true;
						
					}
			}	
    		}
	
    		catch (IOException e) {
    			e.printStackTrace();
    		} 

		return false;	
	}  

	private boolean sdcard_deal(String path)
	{
		if(findSdcardString(path)){
			return true;
		}
		else
			return false;
	}

	 private boolean sdcard_deal()
	{
		String externalStorageState = Environment.getExternalStorageState();  
		String result = "";  

        if(externalStorageState.equals(Environment.MEDIA_MOUNTED)){  

            Log.d("SDcard",Environment.getExternalStorageDirectory().getAbsolutePath()); 

	
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


	//for dvbs/s2 dbmanagement get all satellites.amdb files on external disk
	
	public List<String> getSatellitesDBFileList() {
		String externalStorageState = Environment.getExternalStorageState();  

		List<String> satellites_db_file_list = null;

		if(satellites_db_file_list == null)
			satellites_db_file_list =  new ArrayList<String>();
		
		File[] files = new File("/storage/external_storage").listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getPath().startsWith("/storage/external_storage/sd") && !(file.getPath().startsWith("/storage/external_storage/sdcard"))) {
					File[] myfiles = new File(file.getPath()).listFiles();
						if (myfiles != null){	
							File[] db_files = new File(file.getPath()).listFiles();
							for(File db_file : db_files){
							if(db_file.isDirectory()){

							}else if((db_file.getName().endsWith("xml"))&&(db_file.getName().startsWith("satellites"))){								
								satellites_db_file_list.add(file.getPath()+"/"+db_file.getName());
								Log.d("U disk","DB satellites file name:"+db_file.getName());
							}
						}
					}
				}
			}	

		}

		if(externalStorageState.equals(Environment.MEDIA_MOUNTED)){  
			//Log.d("SDcard",Environment.getExternalStorageDirectory().getAbsolutePath()); 
			File[] db_file_sds = new File("/storage/external_storage/sdcard1").listFiles();
			if(db_file_sds!=null){
				for(File db_file_sd : db_file_sds){
					if(db_file_sd.isDirectory()){

					}else{
						if((db_file_sd.getName().endsWith("xml"))&&(db_file_sd.getName().startsWith("satellites"))){
							satellites_db_file_list.add("/storage/external_storage/sdcard1/"+db_file_sd.getName());
							Log.d("SDCard","DB satellites file name:"+db_file_sd.getName());
							}
						}
					}
			}
		}  
	
		return satellites_db_file_list;
	}
	
	public List<File> getPvrFileList() {
		String externalStorageState = Environment.getExternalStorageState();  

		List<File> pvr_file_list = null;

		if(pvr_file_list == null)
			pvr_file_list =  new ArrayList<File>();
		
		File[] files = new File("/storage/external_storage").listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getPath().startsWith("/storage/external_storage/sd") && !(file.getPath().startsWith("/storage/external_storage/sdcard"))) {
					File[] myfiles = new File(file.getPath()).listFiles();
						if (myfiles != null){	
							for(File my_file : myfiles){
								if (my_file.getPath().endsWith("TVRecordFiles")){
									File[] mytsfiles = new File(my_file.getPath()).listFiles();
									if (mytsfiles != null){	
										for(File myts_file : mytsfiles){
											if(myts_file.isDirectory()){

											}else if(myts_file.getName().endsWith(".ts")){								
												//pvr_file_list.add(myts_file.getPath());
												pvr_file_list.add(myts_file);
												Log.d("U disk","DB satellites file name:"+myts_file.getPath());
											}
										}	
									}
								}
							}
					}
				}
			}	

		}

		if(externalStorageState.equals(Environment.MEDIA_MOUNTED)){  
			//Log.d("SDcard",Environment.getExternalStorageDirectory().getAbsolutePath()); 
			File[] ts_file_sds = new File("/storage/external_storage/sdcard1").listFiles();
			if(ts_file_sds!=null){
				for(File ts_file_sd : ts_file_sds){
					if (ts_file_sd.getPath().endsWith("TVRecordFiles")){
						File[] mytsfiles = new File(ts_file_sd.getPath()).listFiles();
						if (mytsfiles != null){	
							for(File myts_file : mytsfiles){
								if(myts_file.isDirectory()){

								}else{
									if(myts_file.getName().endsWith(".ts")){
										//pvr_file_list.add(myts_file.getPath());
										pvr_file_list.add(myts_file);
										Log.d("SDCard","DB satellites file name:"+myts_file.getName());
										}
								}
							}	
						}
					}
					

				}
			}	
	    }  
	
		return pvr_file_list;
	}

	public class FileOrder{
		//sort by size 
		public void orderByLength(List< File> files) {
		 //List< File> files = Arrays.asList(new File(fliePath).listFiles());
		 Collections.sort(files, new Comparator< File>() {
		     public int compare(File f1, File f2) {
			long diff = f1.length() - f2.length();
			if (diff > 0)
			  return 1;
			else if (diff == 0)
			  return 0;
			else
			  return -1;
		     }
		    public boolean equals(Object obj) {
			return true;
		    }
		   });
		   /*for (File f : files) {
		      if(f.isDirectory()) continue;
		      System.out.println(f.getName()+":"+f.length());
		   }*/
		}
		//sort by name
		public void orderByName(List< File> files) {
		 //List< File> files = Arrays.asList(new File(fliePath).listFiles());
		  Collections.sort(files, new Comparator< File>() {
		   @Override
		   public int compare(File o1, File o2) {
			if (o1.isDirectory() && o2.isFile())
		          return -1;
			if (o1.isFile() && o2.isDirectory())
		          return 1;
			return o1.getName().compareTo(o2.getName());
		   }
		  });
		  /*
		   for (File f : files) {
		     System.out.println(f.getName());
		    }*/
		  }
		//sort by Date
		public void orderByDate(List< File> files) {
		 //List< File> files = Arrays.asList(new File(fliePath).listFiles());
		   Collections.sort(files,new Comparator< File>(){
		     public int compare(File f1, File f2) {
			long diff = f1.lastModified() - f2.lastModified();
			if (diff > 0)
			  return 1;
			else if (diff == 0)
			  return 0;
			else
			  return -1;
		     }
		     public boolean equals(Object obj) {
			return true;
		     }
				
		     });
		   /*
		     for (int i = fs.length-1; i >-1; i--) {
			System.out.println(fs[i].getName());
			System.out.println(new Date(fs[i].lastModified()));
		      }
		      */
		}
		  
	}

	public  List<File> FindFile(List< File> list, String key_search)
	{
	     List<File> listFile = new ArrayList<File>();
			
	     for (int i = 0;i<list.size();i++) {
			if (list.get(i).getName().toLowerCase().lastIndexOf(key_search) > -1||list.get(i).getName().indexOf(key_search.toUpperCase()) > -1) {
				listFile.add(list.get(i));
			}
            }
	      
	    return listFile;
	}


}
