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

import java.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.util.DisplayMetrics;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.app.*;
import android.content.*;
import android.graphics.Color;

public class DTVProgramManager extends DTVActivity{
	private static final String TAG="DTVProgramManager";
	
	ListView ListView_channel=null;
	TextView Text_title=null;
	private int class_total=0;
	private int cur_class_no=-1;
	private String[] class_name=null;
	private int cur_select_item=0;
	private IconAdapter myAdapter=null;
	private TVProgram[]  mTVProgramList=null;

	int db_id=-1;
	private int service_type=TVProgram.TYPE_TV;
	private boolean favor=false;

	private void getListData(int type){
		if(type==0)
			mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_TV,true);
		else if(type==1)
			mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_RADIO,true);
	}

	private void getListFavorite(){
		mTVProgramList=null;
		
	}

	private int getListProgramClass(){
		return 0;
	}

	private void getClassData(int class_no){

	}

	private LinearLayout LinearLayoutListView=null;
	private void DTVChannelListUIInit(){
	    Bundle bundle = this.getIntent().getExtras();
		if(bundle!=null){
	    	db_id = bundle.getInt("db_id");
			service_type=getCurrentProgramType();
		}	

		Text_title=(TextView)findViewById(R.id.Text_title);
		Text_title.setTextColor(Color.YELLOW);
		class_total = getListProgramClass();
		if(service_type == TVProgram.TYPE_RADIO){
			getListData(1);
			Text_title.setText(R.string.radio);
		}	
		else{
			service_type = TVProgram.TYPE_TV;
			getListData(0);
			Text_title.setText(R.string.tv);
		}
		
		ListView_channel = (ListView) findViewById(R.id.ListView_channel);
		myAdapter = new IconAdapter(DTVProgramManager.this,null);
		ListView_channel.setOnItemSelectedListener(mOnSelectedListener);
		ListView_channel.setOnScrollListener(new listOnScroll()); 
		ListView_channel.setOnItemClickListener(mOnItemClickListener);

		ListView_channel.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG,"long Click");
				createMenuChoiceDialog(position);
				mMenuChoiceDialog.show();
				WindowManager.LayoutParams lp=mMenuChoiceDialog.getWindow().getAttributes();
				lp.dimAmount=0.0f;
				mMenuChoiceDialog.getWindow().setAttributes(lp);
				mMenuChoiceDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
				
				return false;
			}
		});
		
		ListView_channel.setAdapter(myAdapter);
		setFocusPosition();
	}


	private void DTVProgramManagerUIInit(){
		DTVProgramManagerInitData();
	}

	private String[] GroupName=null;
	private void DTVProgramManagerInitData(){
		ProgramGroup[] mProgramGroup=null;
		mProgramGroup = DTVProgramManagerGetGroupList();
		int len = 0;
		if(mProgramGroup!=null){
			len = mProgramGroup.length;
		}

		GroupName = new String[len+3];
		GroupName[0] = getString(R.string.tv);
		GroupName[1] = getString(R.string.radio);
		GroupName[2] = getString(R.string.favorite);
		for(int i=0;i<len;i++){
			GroupName[i+3] = mProgramGroup[i].name;
		}

		for(int j=0;j<GroupName.length;j++){
			Log.d(TAG,"GroupName="+GroupName[j]);
		}

		//get tv list
		
					
	}

	

	public void setFocusPosition(){
		int i = 0;
		if(mTVProgramList!=null){
			for(i=0;i<mTVProgramList.length;i++){
				if(db_id == mTVProgramList[i].getID()){
					ListView_channel.setFocusableInTouchMode(true);
	   			  	ListView_channel.requestFocus();
	   			  	ListView_channel.requestFocusFromTouch();
	        		ListView_channel.setSelection(i);
					cur_select_item = i;
					break;
				}
			}	
		}
	}
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvchannellist); 
		/*get list data*/
		DTVChannelListUIInit();
	}

	public void onConnected(){
		Log.d(TAG, "connected");
	}

	public void onDisconnected(){
		Log.d(TAG, "disconnected");
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
  
  	private AdapterView.OnItemSelectedListener mOnSelectedListener = new AdapterView.OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
			ListView_channel = (ListView) findViewById(R.id.ListView_channel);
			if(ListView_channel.hasFocus() == true){
			}
			cur_select_item = position;
		}
		public void onNothingSelected(AdapterView<?> parent){
		}
	};

	private AdapterView.OnItemClickListener mOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){

				int db_id=mTVProgramList[position].getID();
				DTVPlayerPlayById(db_id);
				DTVProgramManager.this.finish();
		}
	};

	private class IconAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context cont;
		private List<String> listItems;
		private int selectItem;
		
		class ViewHolder {
			TextView prono;
			TextView text;	
			ImageView icon_scrambled;
			ImageView icon_fav;
			ImageView icon;
		}
		
		public IconAdapter(Context context, List<String> list) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);			  
		}

		public int getCount() {
			if(mTVProgramList==null)
				return 0;
			else
				return mTVProgramList.length;
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
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;	
			if (convertView == null){    
				convertView = mInflater.inflate(R.layout.dtvchannellist_item, null);
				
				holder = new ViewHolder();
				holder.prono = (TextView)convertView.findViewById(R.id.prono);
				holder.text = (TextView) convertView.findViewById(R.id.ItemText);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.icon_scrambled = (ImageView)convertView.findViewById(R.id.icon_scrambled);
				holder.icon_fav = (ImageView)convertView.findViewById(R.id.icon_fav);
				convertView.setTag(holder);
			}
			else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
		
			// Bind the data efficiently with the holder.
			holder.prono.setText(Integer.toString(mTVProgramList[position].getNumber().getNumber()));
			holder.text.setText(mTVProgramList[position].getName());

			if(db_id == mTVProgramList[position].getID()){  
				//convertView.setBackgroundColor(Color.RED);  
				holder.text.setTextColor(Color.YELLOW);
			}	
			else{
				//convertView.setBackgroundColor(Color.TRANSPARENT); 
				holder.text.setTextColor(Color.WHITE);
			}	
		
			if(mTVProgramList[position].getLockFlag()){
				holder.icon.setBackgroundResource(R.drawable.dtvplayer_icon_lock); 
			}	
			else{
				holder.icon.setBackgroundResource(Color.TRANSPARENT);
			}

			if(mTVProgramList[position].getFavoriteFlag()){
				holder.icon_fav.setBackgroundResource(R.drawable.dtvplayer_icon_fav); 
			}	
			else{
				holder.icon_fav.setBackgroundResource(Color.TRANSPARENT);
			}	

			if(mTVProgramList[position].getScrambledFlag()){
				holder.icon_scrambled.setBackgroundResource(R.drawable.dtvplayer_icon_scrambled); 
			}	
			else{
				holder.icon_scrambled.setBackgroundResource(Color.TRANSPARENT);
			}			  
			return convertView;
		}
	}	
		
	class listOnScroll implements OnScrollListener{
		public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
			//reset_timer();	
		}
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			//reset_timer();
		}
    }	
	
	class MouseClick implements OnClickListener{    
		public void onClick(View v) {
				// TODO Auto-generated method stub	
		    	//reset_timer();
				switch (v.getId()) {
					case R.id.arrow_left:
						break;
				}
			}
	     }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				DTVListDealLeftAndRightKey(0);
				break;		
			case KeyEvent.KEYCODE_DPAD_RIGHT:	
				DTVListDealLeftAndRightKey(1);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:			
				if(cur_select_item== ListView_channel.getCount()-1)
			    	ListView_channel.setSelection(0); 			
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				if(cur_select_item== 0)
					ListView_channel.setSelection(ListView_channel.getCount()-1); 
				break;
			case KeyEvent.KEYCODE_ZOOM_IN:
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}	  
	
	private void DTVListDealLeftAndRightKey(int mode){
		switch(mode){
			case 0:  //left
				if((service_type == TVProgram.TYPE_RADIO)&&(favor!=true)){
					getListData(0);
					Text_title.setText(R.string.tv);
					service_type = TVProgram.TYPE_TV;
					myAdapter.notifyDataSetChanged();
				}
				else if((service_type == TVProgram.TYPE_TV)&&(favor!=true)){
					Log.d(TAG,"##########"+class_total);
					if(class_total>0)
					{	
						service_type = -1;
					   	cur_class_no = class_total-1;
						Text_title.setText(class_name[cur_class_no]);
						getClassData(cur_class_no);
						myAdapter.notifyDataSetChanged();
					}
					else
					{					
						Text_title.setText(R.string.favorite);
						getListFavorite();
						myAdapter.notifyDataSetChanged();
						favor=true;
					}	
				}	
				else if((favor!=true)&&(service_type != TVProgram.TYPE_TV)&&(service_type != TVProgram.TYPE_RADIO)){
					if(cur_class_no>0&&class_total>0)
					{
						service_type = -1;
						cur_class_no --;
						Text_title.setText(class_name[cur_class_no]);
						getClassData(cur_class_no);
						myAdapter.notifyDataSetChanged();
						
					}
					else
					{
						Text_title.setText(R.string.favorite);
						getListFavorite();
						myAdapter.notifyDataSetChanged();
						favor=true;
					}
				}
				else if(favor==true)
				{
					getListData(1);
					Text_title.setText(R.string.radio);
					service_type = TVProgram.TYPE_RADIO;
					myAdapter.notifyDataSetChanged();
					favor=false;
				}	
				setFocusPosition();
				break;	
			case 1:
				if(service_type == TVProgram.TYPE_TV)
				{
					getListData(1);
					Text_title.setText(R.string.radio);
					service_type = TVProgram.TYPE_RADIO;
					myAdapter.notifyDataSetChanged();
				}
				else if((service_type == TVProgram.TYPE_RADIO)&&(favor==false)){
					Text_title.setText(R.string.favorite);
					getListFavorite();
					myAdapter.notifyDataSetChanged();
					favor=true;
				}
				else if(favor==true)
				{
					Log.d(TAG,"##########"+class_total);
					if(class_total>0)
					{	
						service_type = -1;
					    cur_class_no = 0;
						Text_title.setText(class_name[cur_class_no]);
						getClassData(cur_class_no);
						myAdapter.notifyDataSetChanged();
					}
					else
					{
						
						getListData(0);
						Text_title.setText(R.string.tv);
						service_type = TVProgram.TYPE_TV;
						myAdapter.notifyDataSetChanged();
					}	
					favor=false;
				}	
				else
				{
					if(cur_class_no<(class_total-1))
					{
						service_type = -1;
						cur_class_no ++;
						Text_title.setText(class_name[cur_class_no]);
						getClassData(cur_class_no);
						myAdapter.notifyDataSetChanged();

					}
					else
					{
						getListData(0);
						Text_title.setText(R.string.tv);
						service_type = TVProgram.TYPE_TV;
						myAdapter.notifyDataSetChanged();
					}
					
				}	
				setFocusPosition();
				break;
		}
	}

	AlertDialog mMenuChoiceDialog=null;
	AlertDialog mEditDialog=null;
	void createMenuChoiceDialog(int pos){
		
		String[] itemChoices = {
			getString(R.string.edit),
			getString(R.string.delete),
			getString(R.string.move),
			getString(R.string.add_fav),
			getString(R.string.del_fav) ,
			getString(R.string.add_skip),
			getString(R.string.del_skip)
		};
		
		mMenuChoiceDialog = new AlertDialog.Builder(this).setItems(itemChoices, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case 0:
					mEditDialog = new AlertDialog.Builder(DTVProgramManager.this)
					.setTitle(R.string.edit)
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					})
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							EditText edtText  = (EditText)(mEditDialog.findViewById(0x10000001));
							/*
							mServiceInfoItem.setName(edtText.getText().toString());
						    ContentValues value = new ContentValues();
						    value.put("name", mServiceInfoItem.getName());
						    Log.d(TAG,"The TS Id is "+mServiceInfoItem.getTsId());
						    Log.d(TAG,"The Service Id is "+mServiceInfoItem.getServiceId());
							ProgramManager.this.getContentResolver().update(DVBClient.TABLE_SERVICE,value,"db_ts_id=" + mServiceInfoItem.getTsId() + " and service_id=" + mServiceInfoItem.getServiceId(),null);
							serviceList.set(myAdapter.getSelectItem(),mServiceInfoItem);
							*/
							myAdapter.notifyDataSetChanged();
						}
					}).create();
				
					EditText editText = new EditText(DTVProgramManager.this);
					//editText.setText(mServiceInfoItem.getName());
					//editText.setSelection(mServiceInfoItem.getName().length());
					editText.setGravity(Gravity.LEFT);
					editText.setId(0x10000001);
					mEditDialog.setView(editText);
					mEditDialog.show();
					WindowManager.LayoutParams lp=mEditDialog.getWindow().getAttributes();
					lp.dimAmount=0.0f;
					mEditDialog.getWindow().setAttributes(lp);
					mEditDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
					mEditDialog.getWindow().setLayout(500, -1); 
					break;
				case 1:
				
					break;
				case 2:
					mEditDialog = new AlertDialog.Builder(DTVProgramManager.this)
					.setTitle(R.string.move)
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							/*
							setMoveMode(true);		
							changeOpDesc();
							setMoveItemPos(myAdapter.getSelectItem());
							myAdapter.notifyDataSetChanged();
							*/
						}
					})
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							EditText edtText  = (EditText)(mEditDialog.findViewById(0x10000002));

						}
					}).create();

					EditText editText1 = new EditText(DTVProgramManager.this);
					//editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
					editText1.setGravity(Gravity.LEFT);
					editText1.setId(0x10000002);
					mEditDialog.setView(editText1);
					mEditDialog.show();
					WindowManager.LayoutParams lp1=mEditDialog.getWindow().getAttributes();
					lp1.dimAmount=0.0f;
					mEditDialog.getWindow().setAttributes(lp1);
					mEditDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
					mEditDialog.getWindow().setLayout(500, -1); 
					break;
				case 3:
					break;
				case 4:
					break;
				case 5:
					break;
				default:
					break;
				}
			}
		}).create();
	}

}

