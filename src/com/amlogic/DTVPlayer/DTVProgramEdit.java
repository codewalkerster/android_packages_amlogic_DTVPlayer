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
import com.amlogic.tvutil.TVSatellite;
import com.amlogic.tvutil.TVConst;
import com.amlogic.tvutil.TVGroup;
import com.amlogic.tvutil.TVConfigResolver;

import java.util.*;
import java.text.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.util.DisplayMetrics;
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
import android.widget.SimpleAdapter;
import java.lang.reflect.Field;

import com.amlogic.tvutil.DTVRecordParams;
import com.amlogic.tvsubtitle.TVSubtitleView;
import com.amlogic.widget.PasswordDialog;
import com.amlogic.widget.SureDialog;
import com.amlogic.widget.SingleChoiseDialog;
import com.amlogic.widget.MutipleChoiseDialog;
import com.amlogic.widget.CustomDialog;
import com.amlogic.widget.CustomDialog.ICustomDialog;
import com.amlogic.widget.SingleChoiseDialog;

public class DTVProgramEdit extends DTVActivity{
	private static final String TAG="DTVProgramEdit";

	private DTVSettings mDTVSettings=null;
	private TextView mTextview=null;
	private TextView mTextInfo=null;
	ListView ListView_programmanager=null;
	TextView Text_title=null;
	private int cur_select_item=0;
	private IconAdapter myAdapter=null;
	private TVProgram[]  mTVProgramList=null;

	int db_id=-1;
	private int service_type=TVProgram.TYPE_TV;
	private int TVProgramCurrentId = -1;
	private int TabIndex = TVProgramCurrentId;

	private Button mButtonTv=null;
	private Button mButtonRadio=null;
	private Button mButtonFav=null;

	private Button getGroupButtonById(int id){
		Button temp=null;
		if(id==-1)
			temp=mButtonTv;
		else if(id==-2)
			temp=mButtonRadio;
		else if(id==-3)
			temp=mButtonFav;
		else {
			LinearLayout mLinearLayout = (LinearLayout)findViewById(R.id.LinearLayoutGroupButton) ;
			temp = (Button) mLinearLayout.findViewById(id);
		}
		return temp;
	}

	private int getProgramCurrentIndex(){
		if(TVProgramCurrentId!=-1){
			for(int i=0;i<mProgramGroup.length;i++){
				if(TVProgramCurrentId==mProgramGroup[i].getID())
					return i;
			}
		}

		return -1;
	}
	private void getListData(int type){
		if(type==0)
			mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_TV,true);
		else if(type==1)
			mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_RADIO,true);
	}

	private void getListFavorite(){
		mTVProgramList=DTVProgramManagerGetFav();
	}

	private void getListGroupById(int id){
		mTVProgramList=DTVProgramManagerGetProByGroup(id);
	}

	private void getProgBySatIdAndType(int sat_id,int type){
		mTVProgramList=TVProgram.selectBySatIDAndType(this,sat_id,type);
	}

	private void deleteCurrentGroup(){
		if(TVProgramCurrentId!=-1)
			DTVProgramManagerDeleteGroup(TVProgramCurrentId);
	}

	private void editCurrentGroupName(String name){
		if(TVProgramCurrentId!=-1)
			DTVProgramManagerEditGroupName(TVProgramCurrentId,name);
	}

	private void dealFav(int pos){
		if(mTVProgramList[pos].getFavoriteFlag()){
			mTVProgramList[pos].setFavoriteFlag(false);
			if(TabIndex==-3){
				mTVProgramList=removeProgramFromList(mTVProgramList,pos);
			}
		}
		else
			mTVProgramList[pos].setFavoriteFlag(true);
	}

	private void dealLock(int pos){
		if(mTVProgramList[pos].getLockFlag())
			mTVProgramList[pos].setLockFlag(false);
		else
			mTVProgramList[pos].setLockFlag(true);
	}

	private void deleteProgramFromDB(int index){
		int cur_db_id = -1;
		cur_db_id = mTVProgramList[index].getID();
		if(getIsRecordingProgramID()==cur_db_id){
		    Toast toast = Toast.makeText(
					DTVProgramEdit.this,
					R.string.dtvplayer_pvr_is_running,
					Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
		}
		else{
			mTVProgramList[index].deleteFromDb();
			mTVProgramList = removeProgramFromList(mTVProgramList,index);
			if(isHavePragram()==false){
				switchScreenType(0);
			}else{
				if(TVConfigResolver.getConfig(DTVProgramEdit.this,"tv:dtv:dvbt:lcn",false)==false){
					Log.d(TAG, "LCN is off, reset program info");
					for(int i=0;i<mTVProgramList.length;i++){
						 mTVProgramList[i].modifyChanOrder(i);
						 mTVProgramList[i].setProgramNumber(i+1);
					}
				} else {
					Log.d(TAG, "LCN is ON, don't reset program info");
				}

			}

			if(db_id==cur_db_id)
				stopPlaying();
		}
	}

	private TVProgram[] removeProgramFromList(TVProgram[] a,int index){
	    int len=a.length;
	    if(index<0||index>=len){
	        return a;
	    }
	    TVProgram[] result=new TVProgram[len-1];
	    System.arraycopy(a,0,result,0,index);
	    System.arraycopy(a,index+1,result,index,len-index-1);
	    return result;
	}

	private void addIntoGroup(int pos,int group_id){
		mTVProgramList[pos].addProgramToGroup(group_id);
	}

	private void deleteProgramFromGroup(int pos,int group_id){
		mTVProgramList[pos].deleteFromGroup(group_id);
	}


	private TVGroup[] mProgramGroup=null;
	private int TVGroupCount = 0;
	private void DTVProgramEditGroupButtonData(){
		TVGroup[] group = DTVProgramManagerGetGroupList();
		int len = 0;
		if(group!=null){
			len = group.length;
		}

		TVGroupCount = len+3;
		mProgramGroup = new TVGroup[TVGroupCount];
		for(int m=0;m<TVGroupCount;m++){
			mProgramGroup[m]=new TVGroup();
		}
		mProgramGroup[0].setID(-1);
		mProgramGroup[0].setName(getString(R.string.tv));
		mProgramGroup[1].setID(-1);
		mProgramGroup[1].setName(getString(R.string.radio));
		mProgramGroup[2].setID(-1);
		mProgramGroup[2].setName(getString(R.string.favorite));
		for(int i=0;i<len;i++){
			mProgramGroup[i+3] = group[i];
		}

		for(int j=0;j<mProgramGroup.length;j++){
			Log.d(TAG,"mProgramGroup="+mProgramGroup[j].getName());
		}

	}

	private UpdateThread t=null;
	private String video_axis = null;
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtv_program_edit);
		if(t==null){
			t =new UpdateThread();
			t.start();
		}
		video_axis = getVideoWindowSize();
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		VideoView video_view= (VideoView) findViewById(R.id.VideoView);
		TVSubtitleView mSubtitleView= (TVSubtitleView) findViewById(R.id.mSubtitleView);
		openVideo(video_view,mSubtitleView);
		mDTVSettings = new DTVSettings(this);
		DTVChannelList_UI_Init();
		myAdapter.notifyDataSetChanged();
	}

	public void onDisconnected(){
		Log.d(TAG, "disconnected");
		super.onDisconnected();
		if(t!=null){
			t.quitLoop();
			t=null;
		}
	}

	public void onMessage(TVMessage msg){
		super.onMessage(msg);
		Log.d(TAG, "message "+msg.getType());
		switch (msg.getType()) {
			case TVMessage.TYPE_PROGRAM_START:
				if(getCurrentProgramType()==TVProgram.TYPE_RADIO)
					showRadioBg();
				else
					hideRadioBg();

				db_id = getCurrentProgramID();
				myAdapter.notifyDataSetChanged();
				break;
			case TVMessage.TYPE_RECORD_CONFLICT:
				int recordConflict = msg.getRecordConflict();
				Log.d(TAG, "Record conflict:");
				if (recordConflict == TVMessage.REC_CFLT_START_NEW){
					Log.d(TAG, "Stop record for new recording");
				}else if (recordConflict == TVMessage.REC_CFLT_SWITCH_PROGRAM){
					Log.d(TAG, "Stop record for switching to new program");
				}else{
					break;
				}
				if(isTopActivity(this))
					showStopPVRDialog(recordConflict, msg.getProgramID());
				break;
			default:
				break;
		}
	}

	private void showStopPVRDialog(){
		if(!isFinishing()){
			showStopPVRDialog(-1, -1);
		}
	}

	private void showStopPVRDialog(final int conflict, final int programID){
		new SureDialog(DTVProgramEdit.this){
			public void onSetMessage(View v){
				String strMsg = "";

				if (conflict == TVMessage.REC_CFLT_SWITCH_PROGRAM){
					strMsg = getString(R.string.dtvplayer_change_channel);
				}else{
					strMsg = getString(R.string.dtvplayer_pvr_is_running);
				}

				((TextView)v).setText(strMsg);
			}
			public void onSetNegativeButton(){

			}
			public void onSetPositiveButton(){
				DTVPlayerStopRecording();


				if (conflict == TVMessage.REC_CFLT_START_NEW){
					if (getCurrentProgramID() != programID){
						playProgram(programID);
					}

				}else if (conflict == TVMessage.REC_CFLT_SWITCH_PROGRAM){
					playProgram(programID);
				}

			}

			public void onShowEvent(){
			}

			public void onDismissEvent(){
			}
		};
	}

	public void onDialogStatusChanged(int status){
		super.onDialogStatusChanged(status);
		switch(status){
			case STATUS_LOCKED:
				if(getDTVLockedStatus()){
					mTextInfo.setVisibility(View.INVISIBLE);
					//mTextInfo.setText("Locked");
				}
				else if(getDTVSignalStatus()==false){
					mTextInfo.setVisibility(View.VISIBLE);
					mTextInfo.setText("No Signal");
				}
				else if(getDTVScrambledStatus()==true){
					mTextInfo.setVisibility(View.VISIBLE);
					mTextInfo.setText("Scrmbled");
				}
				else{
					mTextInfo.setVisibility(View.INVISIBLE);
				}
			break;
			case STATUS_SIGNAL:
				if(getDTVSignalStatus()==false){
					mTextInfo.setVisibility(View.VISIBLE);
					mTextInfo.setText("No Signal");
				}
				else if(getDTVScrambledStatus()==true){
					mTextInfo.setVisibility(View.VISIBLE);
					mTextInfo.setText("Scrmbled");
				}
				else if(getDTVLockedStatus()){
					mTextInfo.setVisibility(View.VISIBLE);
					mTextInfo.setText("Locked");
				}
				else {
					mTextInfo.setVisibility(View.INVISIBLE);
				}
				break;
			case STATUS_SCRAMBLED:
				if(getDTVScrambledStatus()==true){
					mTextInfo.setVisibility(View.VISIBLE);
					mTextInfo.setText("Scrmbled");
				}
				else if(getDTVSignalStatus()==false){
					mTextInfo.setVisibility(View.VISIBLE);
					mTextInfo.setText("No Signal");
				}
				else if(getDTVLockedStatus()){
					mTextInfo.setVisibility(View.VISIBLE);
					mTextInfo.setText("Locked");
				}
				else {
					mTextInfo.setVisibility(View.INVISIBLE);
				}

				break;
		}
	}

	private AdapterView.OnItemSelectedListener mOnSelectedListener = new AdapterView.OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
			ListView List_detail = (ListView) findViewById(R.id.List_detail);
			if(ListView_channel.hasFocus() == true){
				if(mDTVSettings.getScanRegion().contains("DVBS")==true){
					ListView detail= (ListView) findViewById(R.id.List_detail);
					if(mTVProgramList[position].getChannel()!=null){
						int fre = mTVProgramList[position].getChannel().getParams().getFrequency();
						int sym = mTVProgramList[position].getChannel().getParams().getSymbolRate();
						String pol="";
						if(mTVProgramList[position].getChannel().getParams().getPolarisation()==0)
							pol="V";
						else
							pol="H";
						int sat_id = mTVProgramList[position].getChannel().getParams().getSatId();
						String sat_name = TVSatellite.tvSatelliteSelect(DTVProgramEdit.this,sat_id).getSatelliteName();

						//info.setText(String.valueOf(fre/1000)+" "+pol+" "+String.valueOf(sym/1000)+"  "+sat_name);
	 					/*
				        ArrayAdapter<String> adapter = new ArrayAdapter<String>(DTVProgramEdit.this,R.layout.simple_list_item);
				        adapter.add("Satellite Name:	"+sat_name);
				        adapter.add("Fre:	"+String.valueOf(fre/1000));
				        adapter.add("Sym:	"+String.valueOf(sym/1000));
						adapter.add("Pol:	"+pol);
				        List_detail.setAdapter(adapter);
				        */

				        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("ItemTitle", "Satellite Name:");
						map.put("ItemText", sat_name);
						listItem.add(map);

						HashMap<String, Object> map1 = new HashMap<String, Object>();
						map1.put("ItemTitle", "Fre:");
						map1.put("ItemText", String.valueOf(fre/1000)+"MHz");
						listItem.add(map1);

						HashMap<String, Object> map2 = new HashMap<String, Object>();
						map2.put("ItemTitle", "Sym:");
						map2.put("ItemText", String.valueOf(sym/1000)+"KS/s");
						listItem.add(map2);

						HashMap<String, Object> map3 = new HashMap<String, Object>();
						map3.put("ItemTitle", "Pol:");
						map3.put("ItemText", pol);
						listItem.add(map3);

				        SimpleAdapter listItemAdapter = new SimpleAdapter(DTVProgramEdit.this,listItem,
							R.layout.simple_list_item,
							new String[] {"ItemTitle", "ItemText"},
							new int[] {R.id.ItemTitle,R.id.ItemText}
				        );

				        List_detail.setAdapter(listItemAdapter);
					}
				}
				else if(mDTVSettings.getScanRegion().contains("DVB-C")==true){
					ListView detail= (ListView) findViewById(R.id.List_detail);
					if(mTVProgramList[position].getChannel()!=null){
						int fre = mTVProgramList[position].getChannel().getParams().getFrequency();
						int sym = mTVProgramList[position].getChannel().getParams().getSymbolRate();
						int modulation = mTVProgramList[position].getChannel().getParams().getModulation();
						String mod = null;
						switch(modulation){
							case TVChannelParams.MODULATION_QAM_16:
								mod = "16QAM";
								break;
							case TVChannelParams.MODULATION_QAM_32:
								mod = "32QAM";
								break;
							case TVChannelParams.MODULATION_QAM_64:
								mod = "64QAM";
								break;
							case TVChannelParams.MODULATION_QAM_128:
								mod = "128QAM";
								break;
							case TVChannelParams.MODULATION_QAM_256:
								mod = "256QAM";
								break;
							default:
								mod = "Auto";
								break;
						}

				        	ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

						HashMap<String, Object> map1 = new HashMap<String, Object>();
						map1.put("ItemTitle", "Fre:");
						map1.put("ItemText", String.valueOf(fre/1000)+"MHz");
						listItem.add(map1);

						HashMap<String, Object> map2 = new HashMap<String, Object>();
						map2.put("ItemTitle", "Sym:");
						map2.put("ItemText", String.valueOf(sym/1000)+"Kbd/s");
						listItem.add(map2);

						HashMap<String, Object> map3 = new HashMap<String, Object>();
						map3.put("ItemTitle", "Modulation:");
						map3.put("ItemText", mod);
						listItem.add(map3);

				        SimpleAdapter listItemAdapter = new SimpleAdapter(DTVProgramEdit.this,listItem,
							R.layout.simple_list_item,
							new String[] {"ItemTitle", "ItemText"},
							new int[] {R.id.ItemTitle,R.id.ItemText}
				        );

				        List_detail.setAdapter(listItemAdapter);
					}
				}
				else if((mDTVSettings.getScanRegion().contains("DVB-T")==true)||(mDTVSettings.getScanRegion().contains("ISDBT"))==true){
					ListView detail= (ListView) findViewById(R.id.List_detail);
					if(mTVProgramList[position].getChannel()!=null){
						int fre = mTVProgramList[position].getChannel().getParams().getFrequency();
						int bandwidth=mDTVSettings.getDvbtScanBandwidth();
						String bw = null;
					switch(bandwidth){
						case TVChannelParams.BANDWIDTH_8_MHZ:
							bw = "8M";
							break;
						case TVChannelParams.BANDWIDTH_7_MHZ:
							bw = "7M";
							break;
						case TVChannelParams.BANDWIDTH_6_MHZ:
							bw = "6M";
							break;
						case TVChannelParams.BANDWIDTH_AUTO:
							bw = "Auto";
							break;
						default:
							bw = "Auto";
							break;
					}

			        	ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

					HashMap<String, Object> map1 = new HashMap<String, Object>();
					map1.put("ItemTitle", "Fre:");
					map1.put("ItemText", String.valueOf(fre/1000)+"MHz");
					listItem.add(map1);

					HashMap<String, Object> map2 = new HashMap<String, Object>();
					map2.put("ItemTitle", "BandWidth:");
					map2.put("ItemText", bw);
					listItem.add(map2);


				        SimpleAdapter listItemAdapter = new SimpleAdapter(DTVProgramEdit.this,listItem,
							R.layout.simple_list_item,
							new String[] {"ItemTitle", "ItemText"},
							new int[] {R.id.ItemTitle,R.id.ItemText}
				        );

				        List_detail.setAdapter(listItemAdapter);
					}
				}
			}
			cur_select_item = position;
		}
		public void onNothingSelected(AdapterView<?> parent){
		}
	};


	private boolean move_mode=false;
	private int moveItemPos = -1;
	private int cur_pos = -1;
	private int temp_pos = 0;
	private AdapterView.OnItemClickListener mOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
				int db_id=mTVProgramList[position].getID();
				int type = mTVProgramList[position].getType();
				if(DTVPlayerGetCurrentProgramType()!=type){
					if(type==TVProgram.TYPE_RADIO){
						setProgramType(TVProgram.TYPE_RADIO);
					}
					else{
						setProgramType(TVProgram.TYPE_TV);
					}
				}

				if(DTVPlayerGetCurrentProgramID()!=db_id){
					Log.d(TAG,"mOnItemClickListener pos="+position);
					int serviceType = mTVProgramList[position].getType();
					DTVPlayerPlayById(db_id);
					if(mTVProgramList[position].getLockFlag())
						unblock();
				}

		}
	};

	public void setMoveMode(boolean mode){
		this.move_mode = mode;
	}

	public boolean getMoveMode(){
		return this.move_mode;
	}

	public void setMoveItemPos(int pos){
		this.moveItemPos = pos;
	}

	public int getMoveItemPos(){
		return this.moveItemPos;
	}

	public void exchageItem(int first, int second){
		Log.d(TAG,"cur_pos="+first+"-----"+"temp_pos="+second);
		TVProgram mTemp;

		if(first<second){
			mTemp = mTVProgramList[first];
			System.arraycopy(mTVProgramList,first+1,mTVProgramList,first,second-first);
			mTVProgramList[second]=mTemp;
		}
		else if(first>second) {
			mTemp = mTVProgramList[first];
			System.arraycopy(mTVProgramList,second,mTVProgramList,second+1,first-second);
			mTVProgramList[second]=mTemp;
		}

		for(int i=0;i<mTVProgramList.length;i++){
			 mTVProgramList[i].modifyChanOrder(i);
			 mTVProgramList[i].setProgramNumber(i+1);
		}
	}

	class listOnKeyListener implements OnKeyListener{
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			Log.d(TAG, "enter key press");

			if (arg2.getAction() == KeyEvent.ACTION_DOWN){
				switch(arg1)
				{
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						if (getMoveMode()){
							Log.d(TAG,"switch itme " + getMoveItemPos() + "   "+cur_select_item);
							//saveChange();

							exchageItem(getMoveItemPos(), cur_select_item);
							setMoveItemPos(cur_select_item);
							//setMoveItemPos(myAdapter.getSelectItem());
							//myAdapter.setSelectItem(getMoveItemPos());
							//setMoveMode(false);
							myAdapter.notifyDataSetChanged();
							return true;
						}
						else{

						}
						break;
					case KeyEvent.KEYCODE_DPAD_UP:

						break;
					case KeyEvent.KEYCODE_DPAD_DOWN:

						break;
				}
			}

			if (arg2.getAction() == KeyEvent.ACTION_UP){
				switch(arg1)
				{
					case KeyEvent.KEYCODE_ZOOM_IN:
						if (getMoveMode()){
							for (int i = getMoveItemPos(); i > myAdapter.getSelectItem(); i--){
								exchageItem(i, i - 1);
							}
							setMoveItemPos(myAdapter.getSelectItem());
							myAdapter.setSelectItem(myAdapter.getSelectItem());
							myAdapter.notifyDataSetChanged();
							Log.d(TAG, "press page up");
						}
						break;
					case KeyEvent.KEYCODE_ZOOM_OUT:
						if (getMoveMode())
						{
							for (int i = getMoveItemPos(); i < myAdapter.getSelectItem(); i++)
							{
								exchageItem(i, i + 1);
							}
							setMoveItemPos(myAdapter.getSelectItem());
							myAdapter.setSelectItem(myAdapter.getSelectItem());
							myAdapter.notifyDataSetChanged();
							Log.d(TAG, "press page down");
						}
						break;
						/*
					case KeyEvent.KEYCODE_DPAD_UP:

						 break;
					case KeyEvent.KEYCODE_DPAD_DOWN:

						break;
						*/
				   }
			}

			return false;

		}

    }

	private void DTVListDealLeftAndRightKey(int mode){
		switch(mode){
			case 0://left
			case 1://right
				Button mButton=getGroupButtonById(TabIndex);
				if(mButton!=null)
					mButton.requestFocus();
				break;
		}

	}

	private void DTVListDealUpAndDownKey(int mode){
		switch(mode){
			case 0://up

				cur_pos=temp_pos;
				myAdapter.notifyDataSetChanged();
				break;
			case 1://down

				break;
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
		if(!connected){
			return true;
			}
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				DTVListDealLeftAndRightKey(0);
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				DTVListDealLeftAndRightKey(1);
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if(cur_select_item == ListView_channel.getCount()-1){
					ListView_channel.setSelection(0);
					return true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				if(cur_select_item== 0){
					Button mButton=getGroupButtonById(TabIndex);
					if(mButton!=null)
						mButton.requestFocus();
				}
				break;
			case DTVActivity.KEYCODE_YELLOW_BUTTON:

				if(ListView_channel.getChildCount()>cur_select_item)
					ListView_channel.setSelection(0);
				else{
					ListView_channel.setSelection(cur_select_item-ListView_channel.getChildCount());
				}
				myAdapter.notifyDataSetChanged();

				break;
			case DTVActivity.KEYCODE_BLUE_BUTTON:
				dealLock(cur_select_item);
				myAdapter.notifyDataSetChanged();
				break;
			case DTVActivity.KEYCODE_RED_BUTTON:
				if(mTVProgramList!=null&&mTVProgramList.length>0){
					new SureDialog(DTVProgramEdit.this,true){
						public void onSetMessage(View v){
							((TextView)v).setText(getString(R.string.sure_delete));
						}

						public void onSetNegativeButton(){

						}
						public void onSetPositiveButton(){
							deleteProgramFromDB(cur_select_item);
							myAdapter.notifyDataSetChanged();
						}
					};
				}
				break;
			case DTVActivity.KEYCODE_GREEN_BUTTON:
				/*
				if(mTVProgramList!=null&&mTVProgramList.length>0){
					setMoveMode(true);
					setMoveItemPos(cur_select_item);
					myAdapter.notifyDataSetChanged();
				}
				*/
				int p=0;
				p = cur_select_item+ListView_channel.getChildCount();
				if(p<ListView_channel.getCount())
					ListView_channel.setSelection(p-1);
				else{
					ListView_channel.setSelection(ListView_channel.getCount()-1);
				}
				myAdapter.notifyDataSetChanged();
				break;
			case DTVActivity.KEYCODE_GOTO_BUTTON:
				showSatellitesList();
				break;
			case DTVActivity.KEYCODE_INFO:
				if(mTVProgramList!=null&&mTVProgramList.length>0){
					final CustomDialog mEditCustomDialog = new CustomDialog(DTVProgramEdit.this,R.style.MyDialog);
					mEditCustomDialog.showDialog(R.layout.edit_dialog, new ICustomDialog(){
						public boolean onKeyDown(int keyCode, KeyEvent event){
							if(keyCode == KeyEvent.KEYCODE_BACK)
								mEditCustomDialog.dismissDialog();
							return false;
						}
						public void showWindowDetail(Window window){
							TextView title = (TextView)window.findViewById(R.id.title);
							title.setText(R.string.rename);
							final EditText mEditText = (EditText)window.findViewById(R.id.edit);
							mEditText.setText(mTVProgramList[cur_select_item].getName());
							Button no = (Button)window.findViewById(R.id.no);
							no.setText(R.string.no);
							Button yes = (Button)window.findViewById(R.id.yes);
							yes.setText(R.string.yes);
							no.setOnClickListener(new OnClickListener(){
								public void onClick(View v) {
									mEditCustomDialog.dismissDialog();
								}
							});
							yes.setOnClickListener(new OnClickListener(){
								public void onClick(View v) {
									mTVProgramList[cur_select_item].setProgramName(mEditText.getText().toString());
									myAdapter.notifyDataSetChanged();
									mEditCustomDialog.dismissDialog();
								}
							});
						}
					});
				}
				break;
			case DTVActivity.KEYCODE_FAV_BUTTON:
				if(mTVProgramList!=null&&mTVProgramList.length>0){
					dealFav(cur_select_item);
					myAdapter.notifyDataSetChanged();
				}
				break;
			case KeyEvent.KEYCODE_BACK:
				if(move_mode){
					move_mode=false;
					setMoveItemPos(-1);
					myAdapter.notifyDataSetChanged();
					return true;
				}
				else{
					//setVideoWindowSize(video_axis);
					Intent in = new Intent();
					in.setClass(DTVProgramEdit.this, DTVPlayer.class);
					DTVProgramEdit.this.startActivity(in);
					DTVProgramEdit.this.finish();
				}
				break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean [] b=null;
	public void programGroupOperate(int pos){
		final int p = pos;

		final TVGroup[] group = DTVProgramManagerGetGroupList();
		if(group==null)
			return;
		if(group.length<=0)
			return;

		Log.d(TAG,"group.length="+group.length);
		String[] items = new String[group.length];
		b = new boolean[group.length];

		for (int j = 0; j < group.length; j++){
			items[j] = group[j].getName();
			b[j]= mTVProgramList[pos].checkGroup(group[j].getID());
			Log.d(TAG,">>>"+j+"item="+items[j]+"----"+b[j]);
		}

		new MutipleChoiseDialog(DTVProgramEdit.this,items,b,0){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.add));
			}
			public void onSetNegativeButton(){

			}
			public void onSetPositiveButton(int which,boolean[] b){
				if(b!=null)
				for(int index = 0;index < group.length;index++){
					Log.d(TAG,"position: "+index+"is "+ b[index]);
					if(b[index])
						addIntoGroup(p,group[index].getID());
				}
			}
		};
	}

	void createMenuChoiceDialogForGroup(Context context){
		final Context mContext = context;
		final String[] itemChoices = {
			getString(R.string.add),
			getString(R.string.edit),
			getString(R.string.delete)
		};

		final CustomDialog mCustomDialog = new CustomDialog(mContext);
		mCustomDialog.showDialog(R.layout.list_menu, new ICustomDialog(){
				public boolean onKeyDown(int keyCode, KeyEvent event){
					if(keyCode == KeyEvent.KEYCODE_BACK)
						mCustomDialog.dismissDialog();
					return false;
				}
				public void showWindowDetail(Window window){
					TextView title = (TextView)window.findViewById(R.id.title);
					title.setTextColor(Color.YELLOW);
					title.setText(getString(R.string.operations));

					ListView list_item = (ListView)window.findViewById(R.id.list_item);
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,R.layout.menu_list_item,itemChoices);
					list_item.setAdapter(adapter);
					list_item.setOnItemClickListener(new AdapterView.OnItemClickListener(){
						public void onItemClick(AdapterView<?> parent, View v, int position, long id){
							switch(position){
								case 0: //add
									final CustomDialog mAddCustomDialog = new CustomDialog(mContext,R.style.MyDialog);
										mAddCustomDialog.showDialog(R.layout.edit_dialog, new ICustomDialog(){
											public boolean onKeyDown(int keyCode, KeyEvent event){
												if(keyCode == KeyEvent.KEYCODE_BACK)
													mAddCustomDialog.dismissDialog();
												return false;
											}
											public void showWindowDetail(Window window){
												TextView title = (TextView)window.findViewById(R.id.title);
												title.setText(R.string.add);
												final EditText mAddText = (EditText)window.findViewById(R.id.edit);
												mAddText.setText(null);
												Button no = (Button)window.findViewById(R.id.no);
												no.setText(R.string.no);
												Button yes = (Button)window.findViewById(R.id.yes);
												yes.setText(R.string.yes);
												no.setOnClickListener(new OnClickListener(){
													public void onClick(View v) {
														mAddCustomDialog.dismissDialog();
													}
												});
												yes.setOnClickListener(new OnClickListener(){
													public void onClick(View v) {
														DTVProgramManagerAddGroup(mAddText.getText().toString());
														DTVProgramEditGroupButtonData();
														refreshGroupButton();
														mAddCustomDialog.dismissDialog();
													}
												});
											}
										});
									break;
								case 1: //edit
									if(TVProgramCurrentId!=-1){
										final CustomDialog mEditCustomDialog = new CustomDialog(mContext);
										mEditCustomDialog.showDialog(R.layout.edit_dialog, new ICustomDialog(){
											public boolean onKeyDown(int keyCode, KeyEvent event){
												if(keyCode == KeyEvent.KEYCODE_BACK)
													mEditCustomDialog.dismissDialog();
												return false;
											}
											public void showWindowDetail(Window window){
												TextView title = (TextView)window.findViewById(R.id.title);
												title.setText(R.string.edit);
												final EditText mEditText = (EditText)window.findViewById(R.id.edit);
												mEditText.setText(mProgramGroup[getProgramCurrentIndex()].getName());
												Button no = (Button)window.findViewById(R.id.no);
												no.setText(R.string.no);
												Button yes = (Button)window.findViewById(R.id.yes);
												yes.setText(R.string.yes);
												no.setOnClickListener(new OnClickListener(){
													public void onClick(View v) {
														mEditCustomDialog.dismissDialog();
													}
												});
												yes.setOnClickListener(new OnClickListener(){
													public void onClick(View v) {
														editCurrentGroupName(mEditText.getText().toString());
														DTVProgramEditGroupButtonData();
														mEditCustomDialog.dismissDialog();
													}
												});
											}
										});
									}
									break;
								case 2: //delete
									deleteCurrentGroup();
									DTVProgramEditGroupButtonData();
									refreshGroupButton();
									break;
							}
						}
					}
					);
				}
			}
		);
	}

	void createMenuChoiceDialog(Context context, int position){
		final Context mContext = context;
		final int pos = position;
		boolean fav = false;
		boolean lock = false;
		boolean skip = false;

		if(mTVProgramList!=null){
			fav = mTVProgramList[position].getFavoriteFlag();
			lock = mTVProgramList[position].getLockFlag();
			//skip = mTVProgramList[pos].get
		}

		final String[] itemChoices = {
			getString(R.string.rename),
			getString(R.string.delete),
			(fav==false)?getString(R.string.add_fav):getString(R.string.del_fav),
			//(skip==false)?getString(R.string.add_skip):getString(R.string.del_skip),
			(lock==false)?getString(R.string.add_lock):getString(R.string.del_lock),
			getString(R.string.move),
			//getString(R.string.add_into_group)
		};

		final CustomDialog mCustomDialog = new CustomDialog(mContext);
		mCustomDialog.showDialog(R.layout.list_menu, new ICustomDialog(){
				public boolean onKeyDown(int keyCode, KeyEvent event){
					if(keyCode == KeyEvent.KEYCODE_BACK)
						mCustomDialog.dismissDialog();
					return false;
				}
				public void showWindowDetail(Window window){
					TextView title = (TextView)window.findViewById(R.id.title);
					title.setTextColor(Color.YELLOW);
					title.setText(R.string.program_operation);

					ListView list_item = (ListView)window.findViewById(R.id.list_item);
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,R.layout.menu_list_item,itemChoices);
					//ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,R.layout.dtvsettings_list_item,itemChoices);
					list_item.setAdapter(adapter);
					list_item.setOnItemClickListener(new AdapterView.OnItemClickListener(){
						public void onItemClick(AdapterView<?> parent, View v, int position, long id){
							switch(position){
								case 0: //edit
									final CustomDialog mEditCustomDialog = new CustomDialog(mContext,R.style.MyDialog);
									mEditCustomDialog.showDialog(R.layout.edit_dialog, new ICustomDialog(){
										public boolean onKeyDown(int keyCode, KeyEvent event){
											if(keyCode == KeyEvent.KEYCODE_BACK)
												mEditCustomDialog.dismissDialog();
											return false;
										}
										public void showWindowDetail(Window window){
											TextView title = (TextView)window.findViewById(R.id.title);
											title.setText(R.string.rename);
											final EditText mEditText = (EditText)window.findViewById(R.id.edit);
											mEditText.setText(mTVProgramList[pos].getName());
											Button no = (Button)window.findViewById(R.id.no);
											no.setText(R.string.no);
											Button yes = (Button)window.findViewById(R.id.yes);
											yes.setText(R.string.yes);
											no.setOnClickListener(new OnClickListener(){
												public void onClick(View v) {
													mEditCustomDialog.dismissDialog();
												}
											});
											yes.setOnClickListener(new OnClickListener(){
												public void onClick(View v) {
													mTVProgramList[pos].setProgramName(mEditText.getText().toString());
													myAdapter.notifyDataSetChanged();
													mEditCustomDialog.dismissDialog();
													mCustomDialog.dismissDialog();
												}
											});
										}
									});
									break;
								case 1: //delete
									new SureDialog(DTVProgramEdit.this,true){
										public void onSetMessage(View v){
											((TextView)v).setText(getString(R.string.sure_delete));
										}

										public void onSetNegativeButton(){

										}
										public void onSetPositiveButton(){
											deleteProgramFromDB(pos);
											myAdapter.notifyDataSetChanged();
											mCustomDialog.dismissDialog();
										}
									};
									break;
								case 2: //fav
									dealFav(pos);
									myAdapter.notifyDataSetChanged();
									mCustomDialog.dismissDialog();
									break;
								case 3: //lock
									dealLock(pos);
									myAdapter.notifyDataSetChanged();
									mCustomDialog.dismissDialog();
									break;

								case 4: //move
									setMoveMode(true);
									setMoveItemPos(pos);
									myAdapter.notifyDataSetChanged();
									mCustomDialog.dismissDialog();
									break;
								/*
								case 5: //add into group
									programGroupOperate(pos);
									//mCustomDialog.dismissDialog();
									break;
								*/
								default:
									break;
							}
						}
					}
					);
				}
			}
		);
	}

	private void create_group_button() {
		LinearLayout mLinearLayout = (LinearLayout)findViewById(R.id.LinearLayoutGroupButton) ;
		for(int i=0; i<TVGroupCount; i++){
			Log.d(TAG,"create_group_button="+i);
			LinearLayout.LayoutParams TempLP =
				new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			TempLP.leftMargin = 2;
			TempLP.bottomMargin = 1;

			Button TempButton;
			TempButton = new Button(this);

			switch(i){
				case 0:
					mButtonTv = TempButton;
					break;
				case 1:
					mButtonRadio= TempButton;
					break;
				case 2:
					mButtonFav= TempButton;
					break;
			}

			TempButton.setId(mProgramGroup[i].getID());
			TempButton.setTextColor(Color.WHITE);
			TempButton.setTextSize(22F);
			TempButton.setLayoutParams(TempLP);
			//TempButton.setVisibility(View.GONE);

			TempButton.setFocusableInTouchMode(true);
			TempButton.setOnFocusChangeListener(new GroupButtonItemOnFocusChange());
			TempButton.setOnClickListener(new GroupButtonItemOnClick());
			TempButton.setOnLongClickListener(new OnLongClickListener() {
				public boolean onLongClick(View view) {
					Log.d(TAG,"long Click");
					createMenuChoiceDialogForGroup(DTVProgramEdit.this);
					return false;
				}
			});

			TempButton.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					switch(keyCode)
					{
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (event.getAction() == KeyEvent.ACTION_DOWN) {
								ListView_programmanager.requestFocus();
							}
							break;
					}
					return false;
				}

			});

			TempButton.setSingleLine(true);
			TempButton.setEllipsize(TextUtils.TruncateAt.valueOf("MARQUEE"));

			TempButton.setWidth(128);
			TempButton.setHeight(50);
			TempButton.setHint(" "+mProgramGroup[i].getID());
			TempButton.setBackgroundColor(GROUP_TIEM_UNFOCUSCOLOR_EVEN);
			TempButton.setText(mProgramGroup[i].getName());
			TempButton.setVisibility(View.VISIBLE);

			((LinearLayout)mLinearLayout).addView(TempButton);
		}
	}

	private void refreshGroupButton(){
		LinearLayout mLinearLayout = (LinearLayout)findViewById(R.id.LinearLayoutGroupButton) ;
		mLinearLayout.removeAllViews();
		for(int i=0; i<TVGroupCount; i++){
			Log.d(TAG,"create_group_button="+i);
			LinearLayout.LayoutParams TempLP =
				new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			TempLP.leftMargin = 2;
			TempLP.bottomMargin = 1;

			Button TempButton;
			TempButton = new Button(this);

			switch(i){
				case 0:
					mButtonTv = TempButton;
					break;
				case 1:
					mButtonRadio= TempButton;
					break;
				case 2:
					mButtonFav= TempButton;
					break;
			}

			TempButton.setId(mProgramGroup[i].getID());
			TempButton.setTextColor(Color.WHITE);
			TempButton.setTextSize(22F);
			TempButton.setLayoutParams(TempLP);
			//TempButton.setVisibility(View.GONE);

			TempButton.setFocusableInTouchMode(true);
			TempButton.setOnFocusChangeListener(new GroupButtonItemOnFocusChange());
			TempButton.setOnClickListener(new GroupButtonItemOnClick());
			TempButton.setOnLongClickListener(new OnLongClickListener() {
				public boolean onLongClick(View view) {
					Log.d(TAG,"long Click");
					createMenuChoiceDialogForGroup(DTVProgramEdit.this);

					return false;
				}
			});

			TempButton.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					switch(keyCode)
					{
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (event.getAction() == KeyEvent.ACTION_DOWN) {
								ListView_programmanager.requestFocus();
								ListView_programmanager.setSelection(0);
								return true;
							}
							break;
					}
					return false;
				}

			});

			TempButton.setSingleLine(true);
			TempButton.setEllipsize(TextUtils.TruncateAt.valueOf("MARQUEE"));

			TempButton.setWidth(128);
			TempButton.setHeight(50);
			TempButton.setHint(" "+mProgramGroup[i].getID());
			TempButton.setBackgroundColor(GROUP_TIEM_UNFOCUSCOLOR_EVEN);
			TempButton.setText(mProgramGroup[i].getName());
			TempButton.setVisibility(View.VISIBLE);

			((LinearLayout)mLinearLayout).addView(TempButton);
		}
	}


	class GroupButtonItemOnClick  implements OnClickListener{
		public void onClick(View v) {
			//final int i=Integer.valueOf(((Button)v).getHint().toString());
		}
	}

	private final static int    GROUP_TIEM_FOCUSCOLOR             = Color.argb(200, 255, 180, 0);
	private final static int    GROUP_TIEM_UNFOCUSCOLOR_ODD       = Color.argb(200, 75, 75, 75);
	private final static int    GROUP_TIEM_UNFOCUSCOLOR_EVEN      = Color.argb(200, 42, 42, 42);
	class GroupButtonItemOnFocusChange  implements OnFocusChangeListener{
		public void onFocusChange(View v, boolean isFocused){
			if (isFocused==true){
				((Button)v).setBackgroundColor(GROUP_TIEM_FOCUSCOLOR);
				TVProgramCurrentId = ((Button)v).getId();
				mTextview.setText(((Button)v).getText());
				if(TVProgramCurrentId!=-1){
					getListGroupById(TVProgramCurrentId);
					TabIndex = TVProgramCurrentId;
				}
				else{
					String name = ((Button)v).getText().toString();
					if(name.equals(getString(R.string.tv))){
						getListData(0);
						TabIndex = -1;
					}
					else if(name.equals(getString(R.string.radio))){
						getListData(1);
						TabIndex = -2;
					}
					else if(name.equals(getString(R.string.favorite))){
						getListFavorite();
						TabIndex = -3;
					}
				}

				myAdapter.notifyDataSetChanged();
			}
			else {
				((Button)v).setBackgroundColor(GROUP_TIEM_UNFOCUSCOLOR_EVEN);
			}
	    }
	}

	/****************************Channel List****************************/

	ListView ListView_channel=null;

	private int class_total=0;
	private int cur_class_no=-1;
	private int list_status=-1;
	private boolean favor=false;

	TVGroup[] mTVGroup=null;
	private int getListProgramClass(){
		mTVGroup=DTVProgramManagerGetGroupList();
		if(mTVGroup!=null)
			return mTVGroup.length;
		else
			return 0;
	}

	private void getClassData(int class_no){
		mTVProgramList=DTVProgramManagerGetProByGroup(class_no);
	}

	private LinearLayout LinearLayoutListView=null;
	private void DTVChannelList_UI_Init(){

		Bundle bundle = this.getIntent().getExtras();
		if(bundle!=null){
	    	db_id = bundle.getInt("db_id");
			service_type=getCurrentProgramType();
		}

		ImageButton prolist_button = (ImageButton)findViewById(R.id.prolist_button);
		prolist_button.setOnClickListener(new channelListButtonClick());

		Text_title=(TextView)findViewById(R.id.prolist);
		Text_title.setTextColor(Color.YELLOW);

		class_total = getListProgramClass();
		RelativeLayout_radio_bg = (RelativeLayout)findViewById(R.id.RelativeLayoutRadioBg);
		if(service_type == TVProgram.TYPE_RADIO){
			if(t!=null){
				t.onSetupCmd(1,null);
			}
			Text_title.setText(R.string.radio);
			showRadioBg();
		}
		else{
			service_type = TVProgram.TYPE_TV;
			if(t!=null){
				t.onSetupCmd(0,null);
			}
			Text_title.setText(R.string.tv);
			hideRadioBg();
		}

		mTextInfo = (TextView)findViewById(R.id.text_info);

		ListView_channel = (ListView) findViewById(R.id.programListView);
		myAdapter = new IconAdapter(this,null);
		ListView_channel.setOnItemSelectedListener(mOnSelectedListener);
		ListView_channel.setOnScrollListener(new listOnScroll());
		ListView_channel.setOnItemClickListener(mOnItemClickListener);
		ListView_channel.setOnKeyListener(new listOnKeyListener());
		ListView_channel.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
				Log.d(TAG,"long Click");
				createMenuChoiceDialog(DTVProgramEdit.this ,position);
				return false;
			}
		});

		ListView_channel.setAdapter(myAdapter);
		setFocusPosition();

		findViewById(R.id.return_icon).setOnClickListener(
			new View.OnClickListener(){
				public void onClick(View v) {
					// TODO Auto-generated method stub
					 finish();
				}
			}
		);

		findViewById(R.id.return_icon).setOnClickListener(
			new View.OnClickListener(){
				public void onClick(View v) {
					// TODO Auto-generated method stub
					 finish();
				}
			}
		);

		if(getDTVSignalStatus()==false){
			mTextInfo.setVisibility(View.VISIBLE);
			mTextInfo.setText("No Signal");
		}
		else if(getDTVScrambledStatus()==true){
			mTextInfo.setVisibility(View.VISIBLE);
			mTextInfo.setText("Scrmbled");
		}
		else{
			mTextInfo.setVisibility(View.INVISIBLE);
		}


	}

	private RelativeLayout RelativeLayout_radio_bg=null;
	private boolean radio_bg_flag=false;
	private void showRadioBg(){
		if(radio_bg_flag==false){
			RelativeLayout_radio_bg.setVisibility(View.VISIBLE);
			radio_bg_flag = true;
		}
	}

	private void hideRadioBg(){
		if(radio_bg_flag){
			RelativeLayout_radio_bg.setVisibility(View.INVISIBLE);
			radio_bg_flag = false;
		}
	}

	class channelListButtonClick  implements android.view.View.OnClickListener{
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
				case R.id.prolist_button:
					if((service_type == TVProgram.TYPE_RADIO)&&(favor!=true)){
						getListData(0);
						Text_title.setText(R.string.tv);
						service_type = TVProgram.TYPE_TV;
						myAdapter.notifyDataSetChanged();
					}
					else if((service_type == TVProgram.TYPE_TV)&&(favor!=true)){
						Log.d(TAG,"##########"+class_total);
						if(class_total>0){
							service_type = -1;
						   	cur_class_no = class_total-1;
							Text_title.setText(mTVGroup[cur_class_no].getName());
							getClassData(mTVGroup[cur_class_no].getID());
							myAdapter.notifyDataSetChanged();
						}
						else{
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
							Text_title.setText(mTVGroup[cur_class_no].getName());
							getClassData(mTVGroup[cur_class_no].getID());
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

			}

		}
	}

	public void setFocusPosition(){
		int i = 0;
		if(mTVProgramList!=null){
			for(i=0;i<mTVProgramList.length;i++){
				if(db_id == mTVProgramList[i].getID()){
					ListView_channel.setFocusableInTouchMode(true);

	        			ListView_channel.setSelection(i);
					cur_select_item = i;
					if (mTVProgramList[i].getLockFlag())
						unblock();
					break;
				}
			}
			ListView_channel.requestFocus();
	   		ListView_channel.requestFocusFromTouch();
		}
	}

	private class IconAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context cont;
		private List<String> listItems;
		private int selectItem;

		class ViewHolder {
			ImageView move_icon;
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
				convertView = mInflater.inflate(R.layout.program_edit_list, null);

				holder = new ViewHolder();
				holder.move_icon = (ImageView)convertView.findViewById(R.id.move_icon);
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

			if(mDTVSettings.getScanRegion().contains("ATSC")==false){
				//if(mDTVSettings.getLCNStatus()){
					holder.prono.setText(Integer.toString(mTVProgramList[position].getNumber().getNumber()));
				//}
				//else{
					//holder.prono.setText(Integer.toString(position+1));
				//}
			}
			else{
				holder.prono.setText(Integer.toString(mTVProgramList[position].getNumber().getNumber())+"-"+Integer.toString(mTVProgramList[position].getNumber().getMinor()));
			}

			holder.text.setText(mTVProgramList[position].getName());

			Log.d(TAG,"db_id="+db_id);
			Log.d(TAG,"mTVProgramList[position].getID()="+mTVProgramList[position].getID());
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

			if(getMoveItemPos()==position){
				holder.move_icon.setBackgroundResource(R.drawable.move_flag);
			}
			else{
				holder.move_icon.setBackgroundResource(Color.TRANSPARENT);
			}


			return convertView;
		}
	}

	private static class mySatListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context cont;
		private List<String> listItems;
		private int selectItem;

		static class ViewHolder {
			TextView text;
			ImageView icon;
		}

		public mySatListAdapter(Context context, List<String> list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);
		}

		public int getCount() {
			if(list_sat==null)
				return 0;
			else
				return list_sat.length;
		}

		public Object getItem(int position) {

			return position;
		}

		public long getItemId(int position) {
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


		public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {

		   convertView = mInflater.inflate(R.layout.listitem, null);

		   holder = new ViewHolder();
		   holder.text = (TextView) convertView.findViewById(R.id.ItemText);
		   holder.icon = (ImageView) convertView.findViewById(R.id.icon);
		   convertView.setTag(holder);
		}else {
		  // Get the ViewHolder back to get fast access to the TextView
		  // and the ImageView.
		  holder = (ViewHolder) convertView.getTag();
		  }

		  // Bind the data efficiently with the holder.
		  if(list_sat!=null)
		  	holder.text.setText(list_sat[position].getSatelliteName());

		  return convertView;
		}
	  }

	private AlertDialog.Builder diaBuilder;
	private void showSatellitesList(){

		getSatellitesListData();

		final Dialog mDialog = new AlertDialog(this){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:
						dismiss();
						break;
				}
				return super.onKeyDown(keyCode, event);
			}

		};

		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);

		if(mDialog == null){
			return;
		}

		mDialog.show();
		mDialog.setContentView(R.layout.dvbs_show_sat_dia);

		Window window = mDialog.getWindow();
		window.setGravity(Gravity.CENTER);
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		//WindowManager m = getWindowManager();
		//Display d = m.getDefaultDisplay();
		lp.dimAmount=0.0f;
		lp.x=600;

		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(getString(R.string.satellites_info_list));

		ListView LimitListView =(ListView)window.findViewById(R.id.set_list);

		LimitListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				System.out.println("onItemSelected arg0 " + arg0);
				System.out.println("onItemSelected arg1 " + arg1);
				System.out.println("onItemSelected arg2 " + arg2);
				System.out.println("onItemSelected arg3 " + arg3);
				Log.d(TAG,"id=="+list_sat[arg2].getSatelliteId());
				getProgBySatIdAndType(list_sat[arg2].getSatelliteId(),getCurrentProgramType());
				Text_title.setText(list_sat[arg2].getSatelliteName());
				myAdapter.notifyDataSetChanged();
				mDialog.dismiss();
			}

        });
		LimitListView.setAdapter(new mySatListAdapter(this,null));
	}


	private static TVSatellite[] list_sat=null;
	private  void  getSatellitesListData(){
	 	list_sat = TVSatellite.tvSatelliteList(this);
	}

	private void getProgBySatellites(int sat_id){
		mTVProgramList=TVProgram.selectBySatID(this,sat_id);
	}

	private int getIsRecordingProgramID(){
		DTVRecordParams recPara = getRecordingParams();
		if (recPara != null) {
		  return recPara.getProgramID();
		}
		else return -1;
	}


	public class UIHandler extends Handler {
		public UIHandler(Looper looper) {
		     super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			   switch (msg.what) {
			   	case 0:
				case 1:
					if(myAdapter!=null)
						myAdapter.notifyDataSetChanged();
					setFocusPosition();
					break;
				case 2:

					break;
				case 3:

					break;
			}
		}
	}

	class UpdateThread extends Thread {
		private Handler mHandler = null;
		public void run() {
			Looper.prepare();

			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					Message message=new Message();
					UIHandler ha =new UIHandler(Looper.getMainLooper());
					switch (msg.what) {
						case 0:
							{
								getListData(0);
								message.what=0;
								ha.sendMessage(message);
							}
							break;
						case 1:
								getListData(1);
								message.what=1;
								ha.sendMessage(message);
							break;
						case 2:
								message.what=2;
								ha.sendMessage(message);
							break;
						case 3:
								message.what=3;
								ha.sendMessage(message);
							break;
						default:
							break;
					}
				}
			};

			Looper.loop();
		}

		public void quitLoop() {
			if (mHandler != null && mHandler.getLooper() != null) {
				mHandler.getLooper().quit();
			}
		}

		public void onSetupCmd(int cmd, Object para ) {
			if (mHandler != null){
				mHandler.sendMessage(mHandler.obtainMessage(cmd,para));
			}
		}



	}


}

