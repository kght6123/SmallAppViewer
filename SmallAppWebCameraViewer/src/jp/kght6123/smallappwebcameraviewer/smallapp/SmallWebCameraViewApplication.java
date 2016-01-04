package jp.kght6123.smallappwebcameraviewer.smallapp;

import java.util.Timer;
import java.util.TimerTask;

import jp.kght6123.smallappcommon.custom.GestureDetectView;
import jp.kght6123.smallappcommon.utils.ImageUtils;
import jp.kght6123.smallappcommon.utils.SmallApplicationUtils;
import jp.kght6123.smallappwebcameraviewer.R;
import jp.kght6123.smallappwebcameraviewer.asynctask.AWCCPrototypeAsyncTask;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallAppWindow.WindowState;
import com.sony.smallapp.SmallApplication;

/**
 * SmallImageViewApplicationの本体
 * @author Hirotaka
 *
 */
public class SmallWebCameraViewApplication extends SmallApplication
{
	private static final String TAG = SmallWebCameraViewApplication.class.getSimpleName();
	private View mMinimizedView;
	
	private int INIT_BITMAP_W_MAX_SIZE;
	private int INIT_BITMAP_H_MAX_SIZE;
	
	private int INIT_WINDOW_W_MAX_SIZE;
	private int INIT_WINDOW_H_MAX_SIZE;
	
	private int INIT_WINDOW_W_MIN_SIZE;
	private int INIT_WINDOW_H_MIN_SIZE;
	
	private Uri uri;
	
	private LoadStatus status = LoadStatus.LOADING;
	private enum LoadStatus {
		LOADING,
		LOADED
	}
	
	private Process process = Process.START;
	private enum Process {
		START,
		STOP
	}
	
	private static class OnClickResizeListener implements View.OnClickListener
	{
		private final SmallWebCameraViewApplication application;
		private final float scale;
		
		private OnClickResizeListener(final SmallWebCameraViewApplication application,
				final float scale) {
			super();
			this.application = application;
			this.scale = scale;
		}
		
		@Override
		public void onClick(View v)
		{
			final GestureDetectView view = ((GestureDetectView) this.application.findViewById(R.id.imageView));
			view.setTop(0);
			view.setLeft(0);
			view.setScaleFactor(this.scale);
		}
	};
	
	private AWCCPrototypeAsyncTask task = null;
	
	private Timer mTimer = null;
	private Handler mHandler = null;
	
	@SuppressLint("InflateParams")
	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "onCreate start.");
		
		INIT_WINDOW_W_MIN_SIZE = ImageUtils.dp2Px(100, this);
		INIT_WINDOW_H_MIN_SIZE = ImageUtils.dp2Px(50, this);
		
		/* Set the layout of the application */
//		final View window = 
				SmallApplicationUtils.setContentViewAndGetSmallApplicationWindowView(this, R.layout.smallapp_webcamera_view_main);
		
		{// 各種最大サイズを指定
			final WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
			// ディスプレイのインスタンス生成
			final Display disp = wm.getDefaultDisplay();
			final Point size = new Point();
			disp.getSize(size);
			
			this.INIT_BITMAP_W_MAX_SIZE = size.x;
			this.INIT_BITMAP_H_MAX_SIZE =  size.y;
			
			this.INIT_WINDOW_W_MAX_SIZE = size.x;
			this.INIT_WINDOW_H_MAX_SIZE =  size.y;
		}
		
		/*
		 * Set the layout displayed when the application is minimized.
		 */
		mMinimizedView = LayoutInflater.from(this).inflate(R.layout.smallapp_webcamera_view_minimized, null);
		setMinimizedView(mMinimizedView);
		
		/* Set the title of the application to be displayed in the titlebar */
		setTitle(R.string.app_webcamera_view_name);
		
		SmallAppWindow.Attributes attr = getWindow().getAttributes();
		
		/* Set the requested width of the application */
		attr.width = getResources().getDimensionPixelSize(R.dimen.width);
		/* Set the requested height of the application */
		attr.height = getResources().getDimensionPixelSize(R.dimen.height);
		
		/* Use this flag to make the application window resizable */
		attr.flags |= SmallAppWindow.Attributes.FLAG_RESIZABLE;
		/* Use this flag to remove the titlebar from the window */
		attr.flags |= SmallAppWindow.Attributes.FLAG_NO_TITLEBAR;
		/* Use this flag to enable hardware accelerated rendering */
		attr.flags |= SmallAppWindow.Attributes.FLAG_HARDWARE_ACCELERATED;
		
		/* Set the window attributes to apply the changes above */
		getWindow().setAttributes(attr);
		
		/**
		 * 全体のテーマ、DarkかLightが使える。
		 */
		getWindow().setWindowTheme(com.sony.smallapp.R.style.Theme.Dark);
		
//		final OnWindowFocusChangeTransportListener onTransport = 
//				new OnWindowFocusChangeTransportListener(findViewById(R.id.imageView), window, this);
//		getWindow().setOnWindowFocusChangeListener(onTransport);
//		getWindow().setOnWindowStateChangeListener(new OnWindowStateChangeTransportListener(onTransport, window, this));
		
		// メニューを設定
		setupOptionMenu();
		setupActionBar();
		
		Log.d(TAG, "onCreate end.");
	}
	
	private class AsyncUpdateBitmapTask extends AWCCPrototypeAsyncTask {
		public AsyncUpdateBitmapTask(final ProgressBar progressBar) {
			super(progressBar);
		}
		@Override
		public void onPostExecute(final Handler mHandler, final Bitmap result) {
			super.onPostExecute(mHandler, result);
			
			mHandler.post( new Runnable() {
				@Override
				public void run() {
					if(result != null)
						setBitmap(result, INIT_BITMAP_W_MAX_SIZE, INIT_BITMAP_H_MAX_SIZE);
					
					//Toast.makeText(SmallWebCameraViewApplication.this, R.string.image_view_help_message, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		Log.d(TAG, "onStart start.");
		
		if (SdkInfo.VERSION.API_LEVEL < 2) 
		{
			Toast.makeText(this, R.string.api_not_supported,
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		startup();
		
		getWindow().setWindowState(WindowState.MINIMIZED);
		showResizeDialog();
		
		Log.d(TAG, "onStart end.");
	}
	
	
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop start.");
		
		Log.d(TAG, "onStop end.");
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Log.d(TAG, "onDestroy start.");
		
		stop();
		
		Log.d(TAG, "onDestroy end.");
	}
	
	private void startup() {
		this.process = Process.START;
		this.status = LoadStatus.LOADING;
		
		final ProgressBar progressBarForImageView = 
				(ProgressBar)findViewById(R.id.progressBarForImageView);
		
		// タスク作成と実行 → TimerTaskを使って毎回タスクを作って定期実行。sessionIDは引き継ぎ、毎回ログインしない。
		this.task = new AsyncUpdateBitmapTask(progressBarForImageView);
		
		this.mHandler = new Handler();
		this.mTimer = new Timer(true);
		this.mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				
				task.onPreExecute(mHandler);
				final Bitmap result = task.doInBackground(mHandler, new String[]{});
				task.onPostExecute(mHandler, result);
				
				status = LoadStatus.LOADED;
			}
		}, 0L, 1L*250L);
	}
	
	private void stop(){
		this.process = Process.STOP;
		this.status = LoadStatus.LOADING;
		
		this.mTimer.cancel();
		this.mTimer.purge();
		this.mTimer = null;
		
		final AsyncTask<Object,Object,Object> asyncTask = new AsyncTask<Object,Object,Object>(){
			@Override
			protected Object doInBackground(Object... arg0) {
				
				if(task != null)
					task.onCancelled();
				
				status = LoadStatus.LOADED;
				
				return null;
			}
		};
		asyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Object[]{});
		asyncTask.cancel(false/*mayInterruptIfRunning*/);
	}
	
	private void setFaviconToMinimizedView(Bitmap icon)
	{
		final ImageView faviconView = (ImageView) mMinimizedView.findViewById(R.id.favicon_view);
		final TextView fallbackView = (TextView) mMinimizedView.findViewById(R.id.fallback_view);
		if (icon != null)
		{
			faviconView.setVisibility(View.VISIBLE);
			faviconView.setImageBitmap(icon);
			fallbackView.setVisibility(View.GONE);
		}
		else
		{
			faviconView.setVisibility(View.GONE);
			fallbackView.setVisibility(View.VISIBLE);
		}
	}
	
	@SuppressLint("InflateParams")
	private void setupOptionMenu()
	{
		final View header = 
				LayoutInflater.from(this).inflate(R.layout.smallapp_webcamera_view_header, null);// リソースからViewを取り出す
		
		final View optionMenu = header.findViewById(R.id.wc_option_menu);
		optionMenu.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(status == LoadStatus.LOADED) {
						final PopupMenu popup = new PopupMenu(SmallWebCameraViewApplication.this, optionMenu);
						popup.getMenuInflater().inflate(R.menu.smallapp_webcamera_view_menus, popup.getMenu());
						
						final Menu menu = popup.getMenu();
						menu.findItem(R.id.wc_startend).setVisible(true);
						
						switch(process) {
						case START :
							menu.findItem(R.id.wc_startend).setTitle(getString(R.string.stop));
							popup.setOnMenuItemClickListener
							(
								new PopupMenu.OnMenuItemClickListener()
								{
									@Override
									public boolean onMenuItemClick(final MenuItem item)
									{
										if(R.id.wc_startend == item.getItemId())
											stop();
										
										return true;
									}
								}
							);
							break;
						case STOP :
							menu.findItem(R.id.wc_startend).setTitle(R.string.start);
							popup.setOnMenuItemClickListener
							(
								new PopupMenu.OnMenuItemClickListener()
								{
									@Override
									public boolean onMenuItemClick(final MenuItem item)
									{
										if(R.id.wc_startend == item.getItemId())
											startup();
										
										return true;
									}
								}
							);
							break;
						}
						popup.show();
					}
				}
			}
		);
		
		final View shared = header.findViewById(R.id.shared);
		shared.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					sendUriApp(null);
				}
			}
		);
		
		/**
		 * タイトルビューの設定
		 * 
		 * 縦は48dpぐらい。AndoridのActionBarと同じサイズ。
		 * ActionButtonは48dp×48dpにすること。（UIのガイドラインレベル）
		 * Action button icons on header area should be 48x48 dp. 
		 * 
		 */
		/* Deploy the option menu in the header area of the titlebar */
		getWindow().setHeaderView(header);
	}
	
	private void setupActionBar()
	{
//		final View iv_save = findViewById(R.id.iv_btnSave);
//		iv_save.setOnClickListener(this.onClickSaveListener);
//		
		final View iv_btnActualSize = findViewById(R.id.wc_btnActualSize);
		iv_btnActualSize.setOnClickListener(new OnClickResizeListener(this, ResizeScale.x1.scale));
		
		final View iv_resize_x2 = findViewById(R.id.wc_btnResizeX2);
		iv_resize_x2.setOnClickListener(new OnClickResizeListener(this, ResizeScale.x2.scale));
		
		final View iv_resize_x4 = findViewById(R.id.wc_btnResizeX4);
		iv_resize_x4.setOnClickListener(new OnClickResizeListener(this, ResizeScale.x4.scale));
		
//		final View iv_refresh = findViewById(R.id.wc_btnRefresh);
//		iv_refresh.setOnClickListener
//		(
//			new View.OnClickListener()
//			{
//				@Override
//				public void onClick(final View v)
//				{
//					SmallWebCameraViewApplication.this.stop();
//					SmallWebCameraViewApplication.this.startup();
//				}
//				
//			}
//		);
		
		final View iv_close = findViewById(R.id.wc_btnClose);
		iv_close.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					SmallWebCameraViewApplication.this.finish();
				}
				
			}
		);
		
	}
	
	private enum ResizeScale {
		x1(1.0f),
		x2(2.0f),
		x3(3.0f),
		x4(4.0f),
		;
		public final float scale;
		private ResizeScale(float scale) {
			this.scale = scale;
		}
	}
	
	private void showResizeDialog()
	{
		final String[] items = new String[] {ResizeScale.x1.name(), ResizeScale.x2.name(), ResizeScale.x3.name(), ResizeScale.x4.name() };
		final AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog);
		builder.setTitle(getResources().getText(R.string.resize_title))
				.setItems(items, new OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						final OnClickResizeListener click = 
								new OnClickResizeListener(SmallWebCameraViewApplication.this, ResizeScale.valueOf(items[which]).scale);
						click.onClick(null);
						getWindow().setWindowState(WindowState.NORMAL);
					}
				});
		builder.show();
	}
	
	private void sendUriApp(final String packageName)
	{
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_STREAM, SmallWebCameraViewApplication.this.uri);
		if(packageName != null)
			intent.setPackage(packageName);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		startActivity(intent);
		
		getWindow().setWindowState(WindowState.MINIMIZED);
	}
	
	private Bitmap resize(Bitmap bmp, float scale)
	{
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
	}
	
	private void setBitmap(Bitmap bmp, int maxWidth, int maxHeight)
	{
		if (bmp != null)
		{
			if(maxWidth > 0 && maxHeight > 0)
			{
				if(bmp.getWidth() > maxWidth)
					bmp = resize(bmp, (float)((float)maxWidth / (float)bmp.getWidth()));
				
				else if(bmp.getHeight() > maxHeight)
					bmp = resize(bmp, (float)((float)maxHeight / (float)bmp.getHeight()));
			}
			setFaviconToMinimizedView(bmp);
			
			if(this.process == Process.STOP)
				return;
			
			final GestureDetectView view = ((GestureDetectView) findViewById(R.id.imageView));
			view.setOnResizeImageListener
			(
				new GestureDetectView.OnResizeImageListener()
				{
					@Override
					public void onResize(final float _scaleFactor, final int width, final int height)
					{
						final SmallAppWindow.Attributes attr = getWindow().getAttributes();
						
						attr.width = width > INIT_WINDOW_W_MAX_SIZE ? INIT_WINDOW_W_MAX_SIZE : (width < INIT_WINDOW_W_MIN_SIZE ? INIT_WINDOW_W_MIN_SIZE : width);
						attr.height = height > INIT_WINDOW_H_MAX_SIZE ? INIT_WINDOW_H_MAX_SIZE : (height < INIT_WINDOW_H_MIN_SIZE ? INIT_WINDOW_H_MIN_SIZE : height);
						
						getWindow().setAttributes(attr);
					}
				}
			);
			view.setImageBitmap(bmp);
		}
	}
}