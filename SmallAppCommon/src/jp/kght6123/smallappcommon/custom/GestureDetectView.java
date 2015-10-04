package jp.kght6123.smallappcommon.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.widget.FrameLayout;

/**
 * �J�X�^���C���[�W�r���[
 *  �� �s���`�C���E�A�E�g�Ŋg��k��
 *  �� �X���C�h�ŉ摜�̈ړ�
 *  �� �w��g�嗦�ւ̊g��k��
 * @author Hirotaka
 *
 */
public class GestureDetectView extends View
{
	private static final String TAG = "GestureDetectView";
	private ScaleGestureDetector _gestureDetector;
	
	private Drawable _image;
	private float _scaleFactor = 1.0f;
	
	private int originalIntrinsicWidth = 0;
	private int originalIntrinsicHeight = 0;
	
	private OnResizeImageListener _resizeListener = null;
	
	private boolean _onScaling = false;
	
	private SimpleOnScaleGestureListener _simpleListener = 
		new ScaleGestureDetector.SimpleOnScaleGestureListener()
		{
			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector) {
				Log.d(TAG, "onScaleBegin : "+ detector.getScaleFactor());
				GestureDetectView.this._onScaling = true;
				return super.onScaleBegin(detector);
			}

			@Override
			public void onScaleEnd(ScaleGestureDetector detector) {
				Log.d(TAG, "onScaleEnd : "+ detector.getScaleFactor());
				GestureDetectView.this._scaleFactor *= detector.getScaleFactor();
				GestureDetectView.this._onScaling = false;
				super.onScaleEnd(detector);
			}

			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				Log.d(TAG, "onScale : "+ detector.getScaleFactor());
				GestureDetectView.this._scaleFactor *= detector.getScaleFactor();
				return true;
			};
		};
	
	private OnTouchListener _onTouchListener = 
		new OnTouchListener()
		{
			private int oldx;
			private int oldy;
			
			@Override
			public boolean onTouch(final View v, final MotionEvent event)
			{
				GestureDetectView.this._gestureDetector.onTouchEvent(event);
				
				// �^�b�`���Ă���ʒu�擾
				final int x = (int) event.getRawX();
				final int y = (int) event.getRawY();
				
				final int left;
				final int top;
				
				switch (event.getAction())
				{
					case MotionEvent.ACTION_MOVE:
						if(!GestureDetectView.this._onScaling)
						{// ����C�x���g�ł�View�ړ���̈ʒu
							left = GestureDetectView.this.getLeft() + (x - this.oldx);
							top = GestureDetectView.this.getTop() + (y - this.oldy);
						}
						else
						{// �s���`�C���E�A�E�g�i�X�P�[���ύX���͈ړ����Ȃ��E�ʒu���������E�ړ����邪�I���ł���Ɨǂ������A���L�͈ړ��ʂ�����������Łj
							left = GestureDetectView.this.getLeft() + (int)((x - this.oldx)*GestureDetectView.this._scaleFactor);
							top = GestureDetectView.this.getTop() + (int)((y - this.oldy)*GestureDetectView.this._scaleFactor);
							
							//�ʒu��������
							//left = 0;
							//top = 0;
							
							//�ʒu�͂��̂܂�
							//left = GestureDetectView.this.getLeft();
							//top = GestureDetectView.this.getTop();
							
							//�ʒu���ړ�����
							//left = GestureDetectView.this.getLeft() + (x - this.oldx);
							//top = GestureDetectView.this.getTop() + (y - this.oldy);
							
						}
					break;
					
					default:
						left = GestureDetectView.this.getLeft();
						top = GestureDetectView.this.getTop();
					break;
				}
				// ����̃^�b�`�ʒu��ێ�
				this.oldx = x;
				this.oldy = y;
				
				Log.d(TAG, "x,y : "+  x + "," + y);
				
				GestureDetectView.this.refresh(left, top);
				
				// �C�x���g��������
				return true;
			}
		
		};
	
	public GestureDetectView(Context context)
	{
		super(context);
		this._gestureDetector = new ScaleGestureDetector(context, this._simpleListener);
		this.setOnTouchListener(this._onTouchListener);
	}
	
	public GestureDetectView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this._gestureDetector = new ScaleGestureDetector(context, this._simpleListener);
		this.setOnTouchListener(this._onTouchListener);
	}
	
	public GestureDetectView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this._gestureDetector = new ScaleGestureDetector(context, this._simpleListener);
		this.setOnTouchListener(this._onTouchListener);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		canvas.save();
		canvas.scale(this._scaleFactor, this._scaleFactor);
		
		if(this._image != null)
			this._image.draw(canvas);
		
		canvas.restore();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		return true;
	}
	
	public void setImageBitmap(final Bitmap bitmap)
	{
		this._image = new BitmapDrawable(getResources(), bitmap);
		this.originalIntrinsicWidth = this._image.getIntrinsicWidth();
		this.originalIntrinsicHeight = this._image.getIntrinsicHeight();
		
		this._image.setBounds(0, 0, this.originalIntrinsicWidth, this.originalIntrinsicHeight);
		this.requestLayout();
		
		refresh(GestureDetectView.this.getLeft(), GestureDetectView.this.getTop());
	}
	
	public void setOnResizeImageListener(OnResizeImageListener onResizeImageListener) {
		this._resizeListener = onResizeImageListener;
	}
	
	private void refresh(final int left, final int top)
	{
		final int width = (int)((float)GestureDetectView.this.originalIntrinsicWidth * (float)GestureDetectView.this._scaleFactor);
		final int height = (int)((float)GestureDetectView.this.originalIntrinsicHeight * (float)GestureDetectView.this._scaleFactor);
		
		// �摜�̃T�C�Y�ύX��View�̈ړ��𓯎��Ɏw�肷��
		final FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(width, height);
		layoutParam.leftMargin = left;
		layoutParam.topMargin = top;
		layoutParam.rightMargin = left + width;
		layoutParam.bottomMargin = top + height;
		GestureDetectView.this.setLayoutParams(layoutParam);
		
		// ���X�i�[�̒ǉ����������{
		if(GestureDetectView.this._resizeListener != null)
			GestureDetectView.this._resizeListener.onResize(GestureDetectView.this._scaleFactor, width, height);
		
		GestureDetectView.this.invalidate();
	}
	
	public void setScaleFactor(final float _scaleFactor)
	{
		this._scaleFactor = _scaleFactor;
		refresh(GestureDetectView.this.getLeft(), GestureDetectView.this.getTop());
	}
	
	public OnResizeImageListener getOnResizeImageListener() {
		return _resizeListener;
	}
	
	public interface OnResizeImageListener
	{
		public void onResize(final float _scaleFactor, final int width, final int height);
	}
}