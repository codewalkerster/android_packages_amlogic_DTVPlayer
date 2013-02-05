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

import com.amlogic.tvutil.TVDimension;

public class RRTDimensionsEnter extends Activity{
    private static final String TAG = "RRTDimensionsEnter";
    private static final int MPAA_TOTAL_RATINGS = 7;
    private static SharedPreferences mLast = null;
	private int index=0;
    static private int Rating_status[]=null;
    private static String[] mAbbrev= null;
    private static String mValue[] = null;
    private static String mDimension = null;
    ListView mListView=null;
    RatingAdapter mListView_adapter=null;
    private TVDimension TVDimensionItem=null;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.dtvsettings);
		
        Bundle bundle = this.getIntent().getExtras();
		if(bundle!=null){
			mDimension = bundle.getString("Dimension");
			index = bundle.getInt("Index");
			TVDimension[] mTVDimension =  RRTDimensions.getTVDimension();
			TVDimensionItem = mTVDimension[index];
			mAbbrev = TVDimensionItem.getAbbrev();
			mValue = TVDimensionItem.getText();
			Rating_status = TVDimensionItem.getLockStatus();
		}
		
		if (Rating_status == null || mAbbrev == null || mValue == null) {
			Log.d(TAG, "No value data of this dimension, will not display!");
			finish();
		}
		
		mListView = (ListView) findViewById(R.id.settings_list);
        mListView.setItemsCanFocus(false);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView_adapter = new RatingAdapter(this);
        mListView.setOnItemClickListener(new listOnItemClick());
        mListView.setAdapter(mListView_adapter);

		TextView title = (TextView)findViewById(R.id.title);
		title.setText(mDimension);
    }
    
    protected void onStart() {
    	super.onStart();
    }
    
    protected void onStop() {
    	super.onStop();
    }

	private void setupData(){
		TVDimensionItem.setLockStatus(Rating_status);
	}
    
    private class listOnItemClick implements OnItemClickListener{
    	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long position) {   
    		System.out.println("id---------" + arg2);
        	int p=(int)position;
        	
        	if(Rating_status[p]==0){
        		for(int i=p;i<Rating_status.length;i++){
        			Rating_status[i]=1;
        		}	
        	}	
        	else{
        		for(int i=0;i<=p;i++){
        			Rating_status[i]=0;
        		}	
        	}	
        	mListView_adapter.notifyDataSetChanged();
			setupData();
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
			holder.text0.setTextColor(Color.WHITE);
			holder.text1.setTextColor(Color.WHITE);

			Log.d(TAG,"!!!!!!!!!!!!!"+mValue[position]+"######"+mAbbrev[position]);	
			// Bind the data efficiently with the holder.
			if(Rating_status[position]==1){
				holder.image.setImageResource(R.drawable.rating_locked);
				//holder.image.setVisibility(View.VISIBLE);
			} 	 
			else{
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
				mListView_adapter.notifyDataSetChanged();	
				break;
			case KeyEvent.KEYCODE_D:
				for(int i=0;i<Rating_status.length;i++)
				{
					Rating_status[i]=0;
				}	
				mListView_adapter.notifyDataSetChanged();
				break;
			case KeyEvent.KEYCODE_BACK:	
				setResult(RESULT_OK,null);
				break;	
		}
		return super.onKeyDown(keyCode, event);
	}	

}
