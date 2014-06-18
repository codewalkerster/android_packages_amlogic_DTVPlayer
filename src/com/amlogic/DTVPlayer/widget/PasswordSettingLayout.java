package com.amlogic.widget;

import android.content.Context;
import android.widget.LinearLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.util.Log;

public class PasswordSettingLayout extends LinearLayout {
	private static final String TAG = "PasswordSettingLayout";
	private Context mContext = null;
	private boolean mEnterPreActionDown = false;
	private boolean mConfirmEnter = false;
	private PasswordSettingDialog mPasswordSettingDialog = null;

	public PasswordSettingLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public void setPasswordSettingDialog(PasswordSettingDialog dialog) {
		mPasswordSettingDialog = dialog;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
			if (action == KeyEvent.ACTION_DOWN)
				mEnterPreActionDown = true;
			if (action == KeyEvent.ACTION_UP && mEnterPreActionDown) {
				mEnterPreActionDown = false;
				if (mPasswordSettingDialog != null && mConfirmEnter)
					mPasswordSettingDialog.checkPassword();
				mConfirmEnter = !mConfirmEnter;
			}
		}
		return super.dispatchKeyEvent(event);
	}
}
