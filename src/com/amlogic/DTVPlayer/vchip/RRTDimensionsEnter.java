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
import android.database.Cursor;

public class RRTDimensionsEnter extends Activity{
    private static final String TAG = "RRTDimensionsEnter";
    private static final int MPAA_TOTAL_RATINGS = 7;
    private static SharedPreferences mLast = null;
    static private int Rating_status[]=null;
    private static String[] mAbbrev= null;
    private static String mValue[] = null;
    private static String mDimension = null;
    ListView list_mpaa;
	
    RatingAdapter list_mpaa_adapter=null;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

       
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.dimensions_rating);

		Window w = getWindow();
		WindowManager.LayoutParams wl = w.getAttributes();
		wl.x = 450;
		wl.y = 0;
		w.setAttributes(wl);
		
        Bundle bundle = this.getIntent().getExtras();
		/*
		if(bundle!=null)
		{
			mDimension = bundle.getString("Dimension");
			int valueCount = VchipProvider.getValueCount(getContentResolver(), RRTDimensions.RRT_REGION, mDimension);
			if (valueCount > 0) {
				Rating_status = new int[valueCount];
				mAbbrev = new String[valueCount];
				mValue = new String[valueCount];
				//get the abbrev & value text
				VchipProvider.query(getContentResolver(), RRTDimensions.RRT_REGION, mDimension, Rating_status, mAbbrev, mValue);
				for(int i=0;i<valueCount;i++)
				{
					Log.d(TAG, "#######"+mAbbrev[i]);
				}
			}
		}
		*/
		if (Rating_status == null || mAbbrev == null || mValue == null) {
			Log.d(TAG, "No value data of this dimension, will not display!");
			finish();
		}
		
		list_mpaa = (ListView) findViewById(R.id.list_dimensions_rating);
        list_mpaa.setItemsCanFocus(false);
        list_mpaa.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list_mpaa_adapter = new RatingAdapter(this);
        list_mpaa.setOnItemClickListener(new listOnItemClick());
        list_mpaa.setAdapter(list_mpaa_adapter);

		TextView title = (TextView)findViewById(R.id.title);
		title.setText(mDimension);
    }
    
    protected void onStart() {
    	super.onStart();
    	
    	//VchipProvider.attach(this);
    }
    
    protected void onStop() {
    	super.onStop();
    	/*
    	if (mDimension != null && Rating_status != null)
    		VchipProvider.update(getContentResolver(), RRTDimensions.RRT_REGION, mDimension, Rating_status, true);
    		
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
    
	private static class RatingAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
		private Bitmap mIcon2;

		private Context cont;
		private List<Map<String, Object>> listItems;

		static class ViewHolder {
		TextView  text0;
		TextView  text1;	
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
		   convertView = mInflater.inflate(R.layout.dimensions_rating_list_item, null);
		   holder = new ViewHolder();
		   holder.text0	= (TextView) convertView.findViewById(R.id.dimensiongs_content0);
		   holder.text1	= (TextView) convertView.findViewById(R.id.dimensiongs_content1);
		   holder.image = (ImageView) convertView.findViewById(R.id.image);
		   convertView.setTag(holder);
		}else {
		  // Get the ViewHolder back to get fast access to the TextView
		  // and the ImageView.
		  holder = (ViewHolder) convertView.getTag();
		  }

		 holder.text1.setText(mValue[position]);
		 holder.text0.setText(mAbbrev[position]);

		 Log.d(TAG,"!!!!!!!!!!!!!"+mValue[position]+"######"+mAbbrev[position]);	

		 
		  // Bind the data efficiently with the holder.
		 if(Rating_status[position]==1)
		 {
			
			 holder.image.setImageResource(R.drawable.rating_locked);
			 //holder.image.setVisibility(View.VISIBLE);
		 } 	 
		 else
		 {
			 holder.image.setImageResource(R.drawable.rating_unlocked);
			 
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
