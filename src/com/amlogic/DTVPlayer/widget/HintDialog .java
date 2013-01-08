package com.amlogic.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;
	
public class HintDialog {
	Dialog mDialog = null;
	private Context mContext = null;
	private IHintDialog mIDialogInstance = null;
	
	public HintDialog(Context context) {
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
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);
	}

	public void showDialog(int iLayoutResId,IHintDialog interfaceInstance){
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

	public interface IHintDialog{
		public boolean onKeyDown(int keyCode, KeyEvent event);
		public void showWindowDetail(Window window);
	}
}

 
