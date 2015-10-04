package jp.kght6123.smallappcommon.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.sony.smallapp.SmallAppWindow;

/**
 * 【未完成】ジェスチャー操作可能なWebView
 * @author Hirotaka
 *
 */
public class GestureSmallAppWebView extends WebView
{
	private static final String TAG = GestureSmallAppWebView.class.getSimpleName();
	
	private GestureDetector gd;
	//private OnMotionGestureListener onMotionGestureListener;
	
	private SmallAppWindow smallAppWindow;
	
	public GestureSmallAppWebView(final Context context, final AttributeSet attrs,
			final int defStyle)
	{
		super(context, attrs, defStyle);
		this.gd = new GestureDetector(context, this.onGestureListener);
	}
	public GestureSmallAppWebView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		this.gd = new GestureDetector(context, this.onGestureListener);
	}
	public GestureSmallAppWebView(final Context context)
	{
		super(context);
		this.gd = new GestureDetector(context, this.onGestureListener);
	}
	
//	public void setOnMotionGestureListener(final OnMotionGestureListener onMotionGestureListener)
//	{
//		this.onMotionGestureListener = onMotionGestureListener;
//	}
	
	@Override
	public boolean onTouchEvent(final MotionEvent event)
	{
		return (this.gd.onTouchEvent(event) || super.onTouchEvent(event));
	}
	
	private final SimpleOnGestureListener onGestureListener = new SimpleOnGestureListener()
	{
		@Override
		public boolean onDoubleTap(final MotionEvent e)
		{
			Log.d(TAG, "onDoubleTap");log();
			return super.onDoubleTap(e);
		}
		@Override
		public boolean onDoubleTapEvent(final MotionEvent e)
		{
			Log.d(TAG, "onDoubleTapEvent");log();
			return super.onDoubleTapEvent(e);
		}
		@Override
		public boolean onDown(final MotionEvent e)
		{
			Log.d(TAG, "onDown");log();
			return super.onDown(e);
		}
		@Override
		public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY)
		{
			Log.d(TAG, "onFling");log();
			return super.onFling(e1, e2, velocityX, velocityY);
		}
		@Override
		public void onLongPress(final MotionEvent e)
		{
			Log.d(TAG, "onLongPress");log();
			super.onLongPress(e);
		}
		@Override
		public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY)
		{
			Log.d(TAG, "onScroll");log();
			return super.onScroll(e1, e2, distanceX, distanceY);
		}
		@Override
		public void onShowPress(final MotionEvent e)
		{
			Log.d(TAG, "onShowPress");log();
			super.onShowPress(e);
		}
		@Override
		public boolean onSingleTapConfirmed(final MotionEvent e)
		{
			Log.d(TAG, "onSingleTapConfirmed");log();
			return super.onSingleTapConfirmed(e);
		}
		@Override
		public boolean onSingleTapUp(final MotionEvent e)
		{
			Log.d(TAG, "onSingleTapUp");log();
			return super.onSingleTapUp(e);
		}
	};
	
//	public static interface OnMotionGestureListener
//	{
//		public boolean onUpMotion();
//		public boolean onDownMotion();
//		public boolean onRightMotion();
//		public boolean onLeftMotion();
//	}

	public void setSmallAppWindow(SmallAppWindow window)
	{
		this.smallAppWindow = window;
	}
	
	private void log()
	{
		Log.d(TAG, "window.width="+this.smallAppWindow.getAttributes().width);
		Log.d(TAG, "window.height="+this.smallAppWindow.getAttributes().height);
	}
}
