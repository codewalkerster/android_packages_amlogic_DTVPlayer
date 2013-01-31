package com.amlogic.widget;

import java.lang.reflect.Method;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;
import com.amlogic.DTVPlayer.R;

public class FocusScrollListView extends ListView {
	private final static String TAG="FocusScrollListView";
	private final byte FOCUS_MIDDLE = 0;
	private final byte FOCUS_BOTTOM = 1;
	private final byte FOCUS_TOP = 2;

	private int itemWidth;
	private int itemHeight;
	
	private int listHeight;
	
	private int top;
	
	private byte mFocusState = FOCUS_MIDDLE;
	
	private Bitmap mBitmap;
	
	private boolean isSetSelection;

	private boolean upAndDownKeyFlag=false;
	private int lastItemCordinatesY=0;
	private int cordinatesY;
	
	private boolean isPageScroll;
	
	private boolean hadHeight;

	private Scroller mScroller;
	
	private Matrix m;
	
	private float sy;
	private float sx;
	
	private int sDuration = 200;
	
	private Method method_pageScroll;
	
	private Method method_arrowScrollImpl;
	
	private boolean isScroll;
	
	private int tmpSelection;

	public FocusScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScroller = new Scroller(context);
		
		setVerticalFadingEdgeEnabled(false);
		m = new Matrix();
		
		mBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.transparent);
		
		initPrivateMethods();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		itemWidth = getWidth();
		listHeight = getHeight();
		
		if (getChildCount() > 0) {
			if (!hadHeight) {
				itemHeight = getChildAt(0).getHeight();
				hadHeight = true;
			}
			
			sx = (float) itemWidth / mBitmap.getWidth();
			sy = (float) itemHeight / mBitmap.getHeight();
			m.setScale(sx, sy);
			
			mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(),
					mBitmap.getHeight(), m, true);
		}
		updateFocus();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		if (isSetSelection&&isScroll==false) {
			
			if (null != getSelectedView()) {
				//canvas.drawBitmap(mBitmap, 0, getSelectedView().getTop(), null);
				setScroller(getSelectedView().getTop());
				isSetSelection = false;
				//return;
			}
		}
		
		if(upAndDownKeyFlag){
			if (null != getSelectedView()) {
				mScroller.startScroll(0, lastItemCordinatesY, 0,
									getSelectedView().getTop(), sDuration);
				setScroller(getSelectedView().getTop());
			}	
			upAndDownKeyFlag=false;
		}
		
		if (mScroller.computeScrollOffset()) {
			invalidate();
		} else {
			if (isScroll) {
				isScroll = false;
			}
		}

		if (isPageScroll) {
			if (null != getSelectedView()) {
				cordinatesY = getSelectedView().getTop();
				setScroller(getSelectedView().getTop());
				isPageScroll = false;
			}
		} else {
			cordinatesY = mScroller.getCurrY();
		}

		canvas.drawBitmap(mBitmap, 0, cordinatesY, null);
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if(gainFocus){
			setFocusBitmap(R.drawable.channel_item_light);
		}
		else{
			setFocusBitmap(R.drawable.transparent);
		}
		
		if (getChildCount() > 0) {
			if (!gainFocus) {
				tmpSelection = getSelectedItemPosition();
			} else {
				if (null != getSelectedView()) {
					setSelectionFromTop(tmpSelection, getSelectedView()
							.getTop());
				}
			}
		}
	}

	@Override
	public void setSelection(int position) {
		Log.d(TAG,"setSelection");
		setTmpSelection(position);
		super.setSelection(position);
		updateFocus();
		//isPageScroll = true;
	}

	/**
	 * The method of setMSelection() instead of setSelection(), so please call
	 * setMSelection to set position of item
	 * 
	 * @param position
	 */
	public void setMSelection(int position) {
		setTmpSelection(position);
		setSelection(position);
	}

	private void updateFocus() {
		isSetSelection = true;
	}

	private void setTmpSelection(int position) {
		tmpSelection = position;
	}

	/**
	 * return the number of items at present
	 * 
	 * @return the number of items at present
	 */
	public int getItemNum() {
		return getChildCount();
	}

	private void setScroller(int newY) {
		mScroller.setFinalY(newY);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		//setMSelection(0);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
			View view = getSelectedView();
		
			if (null != view) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_DOWN:
					upAndDownKeyFlag = true;
					lastItemCordinatesY = view.getTop();
					if (getLastVisiblePosition() == getAdapter().getCount() - 1
							&& getSelectedItemPosition() == getLastVisiblePosition() - 1
							&& mFocusState == FOCUS_MIDDLE) {
						//top = view.getTop() + itemHeight + getDividerHeight();
						//mScroller.startScroll(0, view.getTop(), 0,
								//top - view.getTop(), sDuration);
						isScroll = true;
						mFocusState = FOCUS_MIDDLE;
						break;
					}
					if (getSelectedItemPosition() < getLastVisiblePosition() - 1) {
						//top = view.getTop() + itemHeight + getDividerHeight();
						//mScroller.startScroll(0, view.getTop(), 0,
								//top - view.getTop(), sDuration);
						isScroll = true;
						mFocusState = FOCUS_MIDDLE;
					} 
					else if (getSelectedItemPosition() == getLastVisiblePosition() - 1) {
						if (mFocusState != FOCUS_BOTTOM) {
							//top = listHeight - itemHeight
									//- getVerticalFadingEdgeLength()
									//- getDividerHeight();
							//mScroller.startScroll(0, view.getTop(), 0, top
									//- view.getTop(), sDuration);
							mFocusState = FOCUS_BOTTOM;
						}
					}
					break;
				case KeyEvent.KEYCODE_DPAD_UP:
					upAndDownKeyFlag = true;
					lastItemCordinatesY = view.getTop();
					if (getSelectedItemPosition() == getFirstVisiblePosition() + 1) {
						if (mFocusState != FOCUS_TOP) {
							
							//top = 0 + getDividerHeight()
								//	+ getVerticalFadingEdgeLength();
							//mScroller.startScroll(0, view.getTop(), 0, top
								//	- view.getTop(), sDuration);
							mFocusState = FOCUS_TOP;
						}
						break;
					}

					if (getSelectedItemPosition() > getFirstVisiblePosition()) {
						//top = view.getTop() - itemHeight - getDividerHeight();
						//mScroller.startScroll(0, view.getTop(), 0,
								//top - view.getTop(), sDuration);
						mFocusState = FOCUS_MIDDLE;
					}				
					break;
				}
			}
		Log.d(TAG,
				String.valueOf("listHeight " + listHeight + " itemHeight "
						+ itemHeight + " top " + top));
		return super.onKeyDown(keyCode, event);
	}

	private void initPrivateMethods() {
		try {
			method_pageScroll = ListView.class.getDeclaredMethod("pageScroll",
					int.class);
			method_arrowScrollImpl = ListView.class.getDeclaredMethod(
					"arrowScrollImpl", int.class);
			method_pageScroll.setAccessible(true);
			method_arrowScrollImpl.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setFocusBitmap(int resourceId) {
		mBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
	}

}
