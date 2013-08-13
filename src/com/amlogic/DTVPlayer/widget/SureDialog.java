package com.amlogic.widget;

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
import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.graphics.Color;
import com.amlogic.DTVPlayer.R;
import com.amlogic.DTVPlayer.DTVActivity;
	
abstract public class SureDialog {
	private static final String TAG="SureDialog";
	Dialog mDialog = null;
	private Context mContext = null;

	TextView content;
	Button no ;
    Button yes ;
   
	
	public SureDialog(Context context) {
		mContext = context;
		mDialog = new AlertDialog(mContext){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						//dismissDialog();
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
		mDialog.setContentView(R.layout.sure_dialog);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		lp.dimAmount=0.5f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		dialogInit(window);

	}

	public SureDialog(Context context,boolean b) {
		mContext = context;
		mDialog = new AlertDialog(mContext){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						//dismissDialog();
						break;
				}
				return super.onKeyDown(keyCode, event);
			}
			
		};
		
		mDialog.setCancelable(b);
		mDialog.setCanceledOnTouchOutside(false);

		if(mDialog == null){
			return;
		}

		mDialog.show();
		mDialog.setContentView(R.layout.sure_dialog);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		lp.dimAmount=0.5f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		dialogInit(window);

	}

	private void dialogInit(Window window){
		no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);

		content = (TextView)window.findViewById(R.id.content);
		onSetMessage(content);
		
     	yes.setFocusable(true);   
     	yes.requestFocus();   
     	yes.setFocusableInTouchMode(true);   
 
		no.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {				  	 
		        	 onSetNegativeButton();
					 dismissDialog();
		          }});	 
       
		yes.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {
					 onSetPositiveButton();
					 dismissDialog();
		          }});	    
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

	abstract public void onSetMessage(View v);
	abstract public void onSetNegativeButton();
	abstract public void onSetPositiveButton();
}

 
