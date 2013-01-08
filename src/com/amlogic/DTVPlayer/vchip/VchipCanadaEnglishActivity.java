package com.amlogic.DTVPlayer;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.util.Log;
import android.content.Intent;
import android.content.SharedPreferences;

import android.view.WindowManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.KeyEvent;

public class VchipCanadaEnglishActivity extends Activity{
    private static final String TAG = "VchipCanadaEnglishActivity";
    private static SharedPreferences mLast = null;
    static private int Rating_status[]={0,0,0,0,1,1};
    ListView list_mpaa;
    RatingAdapter list_mpaa_adapter=null;
  
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.vchip_canada_english);

		Window w = getWindow();
		WindowManager.LayoutParams wl = w.getAttributes();
		wl.x = 80;
		wl.y = 0;
		w.setAttributes(wl);
        
        list_mpaa = (ListView) findViewById(R.id.list_mpaa);
        list_mpaa.setItemsCanFocus(false);
        list_mpaa.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list_mpaa_adapter = new RatingAdapter(this);
        list_mpaa.setOnItemClickListener(new listOnItemClick());
        list_mpaa.setAdapter(list_mpaa_adapter);
        
    }
    
    protected void onStart(){
		super.onStart();
		/*
		VchipProvider.attach(this);
		
		VchipProvider.addDimension(getContentResolver(), 0, getString(R.string.parent_control_Canada_English), Rating_status);

		VchipProvider.query(getContentResolver(), 0, getString(R.string.parent_control_Canada_English), Rating_status);
		*/
	}
	
	protected void onStop()
	{
		super.onStop();
		/*
		VchipProvider.update(getContentResolver(), 0, getString(R.string.parent_control_Canada_English), Rating_status, true);
		VchipProvider.detach();
		*/
	}

    private class listOnItemClick implements OnItemClickListener{
    	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long position) {   
    		System.out.println("id---------" + arg2);
        	Log.d("#####","#####################"+position);
        	int p=(int)position;
        	
        	if(Rating_status[p]==0)
        	{
        		for(int i=p;i<Rating_status.length;i++)
        		{
        			Rating_status[i]=1;
        		}	
        	}	
        	else
        	{
        		for(int i=0;i<=p;i++)
        		{
        			Rating_status[i]=0;
        		}	
        	}	
        	list_mpaa_adapter.notifyDataSetChanged();	
        }      	
    }
    
    
 
    
    	
    void InitMpaaMap(int rating)
    {
 
    }
   
	private static class RatingAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
		private Bitmap mIcon2;

		private Context cont;
		private List<Map<String, Object>> listItems;

		static class ViewHolder {
	    ImageView image;
		}

		public RatingAdapter(Context context) {
		super();
		cont = context;
		mInflater=LayoutInflater.from(context);
		
		}

		
		public int getCount() {
		
		return Rating_status.length;
		}

		
		public Object getItem(int position) {
		
		return position;
		}
		
		
		public long getItemId(int position) {
		return position;
		}
		
		
		public boolean isEnabled(int position) {
		
			    return super.isEnabled(position);
		}


		
		public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
		   convertView = mInflater.inflate(R.layout.rating_list_item, null);
		   holder = new ViewHolder();
		   holder.image = (ImageView) convertView.findViewById(R.id.image);
		   //holder.iboolean = (ImageButton)convertView.findViewById(R.id.iboolean);
		   //holder.icon1 = (ImageView)convertView.findViewById(R.id.icon1);
		   convertView.setTag(holder);
		}else {
		  // Get the ViewHolder back to get fast access to the TextView
		  // and the ImageView.
		  holder = (ViewHolder) convertView.getTag();
		  }
		
		  // Bind the data efficiently with the holder.
		 if(Rating_status[position]==1)
			 holder.image.setImageResource(R.drawable.rating_locked);
		 else
			 holder.image.setImageResource(R.drawable.rating_unlocked);

		  
		  switch(position)
		  {
		  	case 1: 
		  	
			break;
	
		  }
		  
		  return convertView;
		}
	  }	

	 public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

	   switch (keyCode) {
		
		case KeyEvent.KEYCODE_A:		
    		for(int i=0;i<Rating_status.length;i++)
    		{
    			Rating_status[i]=1;
    		}	
        	list_mpaa_adapter.notifyDataSetChanged();	
			break;
		case KeyEvent.KEYCODE_D:
    		for(int i=0;i<Rating_status.length;i++)
    		{
    			Rating_status[i]=0;
    		}	
        	list_mpaa_adapter.notifyDataSetChanged();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}	
	
}
