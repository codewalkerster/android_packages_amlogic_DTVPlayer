package com.amlogic.widget;

import java.util.*;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;


import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.graphics.Color;
import com.amlogic.DTVPlayer.R;
import com.amlogic.DTVPlayer.DTVActivity;

	
public class CustomDialog {
	Dialog mDialog = null;
	private Context mContext = null;
	private ICustomDialog mIDialogInstance = null;
	
	public CustomDialog(Context context) {
		mContext = context;
		
		mDialog = new AlertDialog(mContext){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				if(keyCode == KeyEvent.KEYCODE_BACK&&mIDialogInstance!=null){
					mIDialogInstance.onKeyDown(keyCode,event);
					return true;
				}
				return super.onKeyDown(keyCode,event);
			}
			
		};

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				onShowEvent();
			}         
		}); 	

		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {
							onDismissEvent();
						}         
						});	
		
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
	}

	public CustomDialog(Context context,int theme) {
		mContext = context;
		
		mDialog = new Dialog(mContext,theme){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				if(keyCode == KeyEvent.KEYCODE_BACK&&mIDialogInstance!=null){
					mIDialogInstance.onKeyDown(keyCode,event);
					return true;
				}
				return super.onKeyDown(keyCode,event);
			}
			
		};

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				onShowEvent();
			}         
		}); 	

		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {
							onDismissEvent();
						}         
						});	
		
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);
	}

	public void onShowEvent(){}
	public void onDismissEvent(){}
	
	public void showDialog(int iLayoutResId,ICustomDialog interfaceInstance){
		if(mDialog == null||iLayoutResId == 0){
			return;
		}

		mIDialogInstance = interfaceInstance;
		mDialog.show();
		mDialog.setContentView(iLayoutResId);
		Window window = mDialog.getWindow();
		if(mIDialogInstance!= null){
			mIDialogInstance.showWindowDetail(window);
		}
	}

	public void dismissDialog(){
		if(mDialog!=null&& mDialog.isShowing()){
			mDialog.dismiss();
		}
	}

	public boolean isShowing(){
		if(mDialog!=null&&mDialog.isShowing()){
			return mDialog.isShowing();
		}
		return false;
	}

	public interface ICustomDialog{
		public boolean onKeyDown(int keyCode, KeyEvent event);
		public void showWindowDetail(Window window);
	}
}

 
