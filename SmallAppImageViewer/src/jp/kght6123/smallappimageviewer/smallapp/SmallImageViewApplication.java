package jp.kght6123.smallappimageviewer.smallapp;

import java.io.File;

import jp.kght6123.smallappcommon.custom.GestureDetectView;
import jp.kght6123.smallappcommon.listener.OnWindowFocusChangeTransportListener;
import jp.kght6123.smallappcommon.listener.OnWindowStateChangeTransportListener;
import jp.kght6123.smallappcommon.service.DownloadService;
import jp.kght6123.smallappcommon.utils.FileUtils;
import jp.kght6123.smallappcommon.utils.ImageUtils;
import jp.kght6123.smallappcommon.utils.SmallApplicationUtils;
import jp.kght6123.smallappimageviewer.R;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
public class SmallImageViewApplication extends SmallApplication
{
	private static final String TAG = SmallImageViewApplication.class.getSimpleName();
	private View mMinimizedView;
	
	private int INIT_BITMAP_W_MAX_SIZE;
	private int INIT_BITMAP_H_MAX_SIZE;
	
	private int INIT_WINDOW_W_MAX_SIZE;
	private int INIT_WINDOW_H_MAX_SIZE;
	
	private int INIT_WINDOW_W_MIN_SIZE;
	private int INIT_WINDOW_H_MIN_SIZE;
	
	private Uri uri;
//	private boolean downlodHttp = false;
	
	private static class OnClickResizeListener implements View.OnClickListener
	{
		private final SmallImageViewApplication application;
		private final float scale;
		
		private OnClickResizeListener(final SmallImageViewApplication application,
				final float scale) {
			super();
			this.application = application;
			this.scale = scale;
		}
		
		@Override
		public void onClick(View v)
		{
			if(SmallImageViewApplication.isHttpUri(this.application.uri))
			{// http:などから始まるURI
				Toast.makeText(this.application, this.application.getString(R.string.save_error_for_not_local), Toast.LENGTH_SHORT).show();
				return;
			}
			
			final GestureDetectView view = ((GestureDetectView) this.application.findViewById(R.id.imageView));
			view.setTop(0);
			view.setLeft(0);
			view.setScaleFactor(this.scale);
		}
	};
	
//	private final View.OnClickListener onClickSaveListener = new View.OnClickListener()
//	{
//		@Override
//		public void onClick(View v)
//		{
//			if(isHttpUri(SmallImageViewApplication.this.uri))
//			{// http:などから始まるURI
//				Toast.makeText(SmallImageViewApplication.this, getString(R.string.save_error_for_not_local), Toast.LENGTH_SHORT).show();
//				return;
//			}
//			
//			final File saveDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SmallAppViewer");
//			if(!saveDir.exists())
//				saveDir.mkdirs();
//			
//			final File saveFile = new File(saveDir, uri.getLastPathSegment());
//			final File downloadFile = FileUtils.toFile(SmallImageViewApplication.this.uri, SmallImageViewApplication.this);
//			
//			FileUtils.copyFile(downloadFile, saveFile, SmallImageViewApplication.this, true);
//			
//			SmallImageViewApplication.this.uri = Uri.fromFile(saveFile);
//		}
//	};
	
	private static boolean isHttpUri(Uri uri)
	{
		if(uri.isAbsolute() && uri.getScheme().equals("http"))
		{// http:などから始まるURI
			return true;
		}
		else
			return false;
	}
	
	@SuppressLint("InflateParams")
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		INIT_WINDOW_W_MIN_SIZE = ImageUtils.dp2Px(300, this);
		INIT_WINDOW_H_MIN_SIZE = ImageUtils.dp2Px(300, this);
		
		/* Set the layout of the application */
		final View window = 
				SmallApplicationUtils.setContentViewAndGetSmallApplicationWindowView(this, R.layout.smallapp_image_view_main);
		
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
		mMinimizedView = LayoutInflater.from(this).inflate(R.layout.smallapp_image_view_minimized, null);
		setMinimizedView(mMinimizedView);
		
		/* Set the title of the application to be displayed in the titlebar */
		setTitle(R.string.app_image_view_name);
		
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
		
		final OnWindowFocusChangeTransportListener onTransport = 
				new OnWindowFocusChangeTransportListener(findViewById(R.id.imageView), window, this);
		getWindow().setOnWindowFocusChangeListener(onTransport);
		getWindow().setOnWindowStateChangeListener(new OnWindowStateChangeTransportListener(onTransport, window, this));
		
		// メニューを設定
		setupOptionMenu();
		setupActionBar();
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		if (SdkInfo.VERSION.API_LEVEL < 2) 
		{
			Toast.makeText(this, R.string.api_not_supported,
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		final Uri imageUri;
		try
		{
			if(getIntent() == null)
			{
				Toast.makeText(this, R.string.image_notfound_error, Toast.LENGTH_SHORT).show();
				return;
			}
			else if(getIntent().getExtras() != null
					&& getIntent().getExtras().get(Intent.EXTRA_STREAM) != null)
				this.uri = Uri.parse(getIntent().getExtras().get(Intent.EXTRA_STREAM).toString());
			else if(getIntent().getData() != null)
				this.uri = getIntent().getData();
			else
			{
				Toast.makeText(this, R.string.image_notfound_error, Toast.LENGTH_SHORT).show();
				return;
			}
			imageUri = this.uri;
		}
		catch(Exception e)
		{
			Log.w(TAG, "not found "+Intent.EXTRA_STREAM+".", e);
			Toast.makeText(this, e.getClass().getSimpleName()+", "+e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (imageUri != null) 
		{
			Log.v(TAG, "暗黙的intentから起動");
			
			final String name = imageUri.getPath();
			if(name != null)
				setTitle(name);
			
			//Toast.makeText(this, "imageUri.isAbsolute() = "+Boolean.valueOf(imageUri.isAbsolute()), Toast.LENGTH_SHORT).show();
			//Toast.makeText(this, "imageUri.getScheme() = "+imageUri.getScheme(), Toast.LENGTH_SHORT).show();
			
			final ProgressBar progressBarForImageView = 
					(ProgressBar)findViewById(R.id.progressBarForImageView);
			
			if(isHttpUri(imageUri))
			{// http:などから始まるURI
//				this.downlodHttp = true;
				
				progressBarForImageView.setVisibility(View.VISIBLE);
				
				registerDownloadBroadcastReceiver();
				startDownload(this.uri.toString());
			}
			else
			{// ファイルを遅延処理で読み込んで、リサイズ（SmallAppのウィンドウが遅延処理じゃないとリサイズされない）
//				this.downlodHttp = false;
				
				new Handler().postDelayed
				(
					new Runnable()
					{
						@Override
						public void run()
						{
							//try
							//{
								progressBarForImageView.setVisibility(View.GONE);
								
								final Bitmap bmp = ImageUtils.getResizeBitmap(FileUtils.toFile(imageUri, SmallImageViewApplication.this), 1980)/*Media.getBitmap(getContentResolver(), imageUri)*/;
								
								if(bmp != null)
									setBitmap(bmp, INIT_BITMAP_W_MAX_SIZE, INIT_BITMAP_H_MAX_SIZE);
								
								Toast.makeText(SmallImageViewApplication.this, R.string.image_view_help_message, Toast.LENGTH_SHORT).show();
							//}
							//catch (FileNotFoundException e)
							//{
							//	Log.w(TAG, "not found image file.", e);
							//	Toast.makeText(SmallImageViewApplication.this, e.getClass().getSimpleName()+", "+e.getMessage(), Toast.LENGTH_SHORT).show();
							//}
							//catch (IOException e)
							//{
							//	Log.w(TAG, "io exception.", e);
							//	Toast.makeText(SmallImageViewApplication.this, e.getClass().getSimpleName()+", "+e.getMessage(), Toast.LENGTH_SHORT).show();
							//}
						}
					},
					1
				);
			}
		}
		else
		{
			Log.v(TAG, "普通に起動");
			
			Toast.makeText(this, R.string.image_notfound_error, Toast.LENGTH_SHORT).show();
			return;
		}
	}
	
	@Override
	protected void onDestroy()
	{
		if(this.progressReceiver != null)
			unregisterReceiver(this.progressReceiver);
		
		super.onDestroy();
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
				LayoutInflater.from(this).inflate(R.layout.smallapp_image_view_header, null);// リソースからViewを取り出す
		
		final View optionMenu = header.findViewById(R.id.iv_option_menu);
		optionMenu.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					final PopupMenu popup = new PopupMenu(SmallImageViewApplication.this, optionMenu);
					popup.getMenuInflater().inflate(R.menu.smallapp_image_view_menus, popup.getMenu());
					
					final Menu menu = popup.getMenu();
					switch(getWindow().getWindowState())
					{
					case NORMAL:
						menu.findItem(R.id.iv_returned).setVisible(false);
						menu.findItem(R.id.iv_maximization).setVisible(true);
						menu.findItem(R.id.iv_minimization).setVisible(true);
						break;
					case FITTED:
						menu.findItem(R.id.iv_returned).setVisible(true);
						menu.findItem(R.id.iv_maximization).setVisible(false);
						menu.findItem(R.id.iv_minimization).setVisible(true);
						break;
					case MINIMIZED:
						menu.findItem(R.id.iv_returned).setVisible(true);
						menu.findItem(R.id.iv_maximization).setVisible(true);
						menu.findItem(R.id.iv_minimization).setVisible(false);
						break;
					}
					
					popup.setOnMenuItemClickListener
					(
						new PopupMenu.OnMenuItemClickListener()
						{
							@Override
							public boolean onMenuItemClick(final MenuItem item)
							{
								if(R.id.iv_minimization == item.getItemId())
								{
									getWindow().setWindowState(WindowState.MINIMIZED);
								}
								else if(R.id.iv_maximization == item.getItemId())
								{
									getWindow().setWindowState(WindowState.FITTED);
								}
								else if(R.id.iv_returned == item.getItemId())
								{
									getWindow().setWindowState(WindowState.NORMAL);
								}
								else if(R.id.iv_shared == item.getItemId())
								{
									sendUriApp(null);
								}
								return true;
							}
						}
					);
					popup.show();
				}
			}
		);
		
//		final View iv_save = header.findViewById(R.id.iv_save);
//		iv_save.setOnClickListener(this.onClickSaveListener);
		
//		/**
//		 * 06-01 10:52:45.870: I/ActivityManager(1016):
//		 * START u0 {
//		 * act=android.intent.action.SEND
//		 * typ=image/jpeg
//		 * flg=0x80001
//		 * cmp=jp.co.sony.tablet.PersonalSpace/com.sony.pmo.pmoa.startup.StartupActivity (has clip) (has extras)
//		 * } from pid 5436
//		 */
//		final View playmemoriesonline = header.findViewById(R.id.playmemoriesonline);
//		playmemoriesonline.setOnClickListener
//		(
//			new View.OnClickListener()
//			{
//				@Override
//				public void onClick(View v)
//				{
//					sendUriApp("jp.co.sony.tablet.PersonalSpace");
//				}
//			}
//		);
//		
//		/**
//		 * 06-01 10:45:33.040: I/ActivityManager(1016):
//		 * START u0 {
//		 * act=android.intent.action.SEND
//		 * typ=image/jpeg
//		 * flg=0x2880001
//		 * cmp=com.google.android.apps.docs/.shareitem.UploadSharedItemActivityDelegate (has clip) (has extras)
//		 * } from pid 3303
//		 */
//		final View googledrive = header.findViewById(R.id.googledrive);
//		googledrive.setOnClickListener
//		(
//			new View.OnClickListener()
//			{
//				@Override
//				public void onClick(View v)
//				{
//					sendUriApp("com.google.android.apps.docs");
//				}
//			}
//		);
//		
//		/**
//		 * 06-01 10:41:51.400: I/ActivityManager(1016):
//		 * START u0 {
//		 * act=android.intent.action.SEND
//		 * typ=image/jpeg
//		 * flg=0x80001
//		 * cmp=com.facebook.orca/com.facebook.messenger.activity.ShareLauncherActivity (has clip) (has extras)
//		 * } from pid 1612
//		 */
//		final View facebookmessenger = header.findViewById(R.id.facebookmessenger);
//		facebookmessenger.setOnClickListener
//		(
//			new View.OnClickListener()
//			{
//				@Override
//				public void onClick(View v)
//				{
//					sendUriApp("com.facebook.orca");
//				}
//			}
//		);
		
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
		final View iv_btnActualSize = findViewById(R.id.iv_btnActualSize);
		iv_btnActualSize.setOnClickListener(new OnClickResizeListener(this, 1.0f));
		
		final View iv_resize_x2 = findViewById(R.id.iv_btnResizeX2);
		iv_resize_x2.setOnClickListener(new OnClickResizeListener(this, 2.0f));
		
		final View iv_resize_x4 = findViewById(R.id.iv_btnResizeX4);
		iv_resize_x4.setOnClickListener(new OnClickResizeListener(this, 4.0f));
		
		final View iv_close = findViewById(R.id.iv_btnClose);
		iv_close.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					SmallImageViewApplication.this.finish();
				}
				
			}
		);
		
	}
	
	private void sendUriApp(final String packageName)
	{
//		if(this.downlodHttp)
//			this.onClickSaveListener.onClick(null);
//		
		//final PackageManager pm = getPackageManager();
		//final Intent intent = pm.getLaunchIntentForPackage(packageName);
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//intent.setAction();
		//intent.setDataAndType(SmallImageViewApplication.this.uri, "image/*");
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_STREAM, SmallImageViewApplication.this.uri);
		if(packageName != null)
			intent.setPackage(packageName);
		//intent.setComponent(new ComponentName(packageName, activityName));
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
	
	private DownloadProgressBroadcastReceiver progressReceiver;
	
	private void registerDownloadBroadcastReceiver()
	{
		this.progressReceiver = new DownloadProgressBroadcastReceiver();
		
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("DOWNLOAD_PROGRESS_ACTION");
		
		registerReceiver(this.progressReceiver, intentFilter);
	}
	
	private void startDownload(final String url)
	{
		Intent intent = new Intent(getBaseContext(), DownloadService.class);
		intent.putExtra("url", url);
		startService(intent);
	}
	
	class DownloadProgressBroadcastReceiver extends BroadcastReceiver
	{
		
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// Show Progress
			final Bundle bundle = intent.getExtras();
			final int completePercent = bundle.getInt("completePercent");
			final int totalByte = bundle.getInt("totalByte");
			final String progressString = totalByte + " byte read."
					+ (0 < completePercent ? "[" + completePercent + "%]" : "[0%]");
			
			final ProgressBar progressBarForImageView = 
					(ProgressBar)findViewById(R.id.progressBarForImageView);
			progressBarForImageView.setProgress(completePercent);
			
			//progressTextView.setText(progressString);
			//Toast.makeText(context, progressString, Toast.LENGTH_SHORT).show();
			setTitle(progressString);
			
			// If completed, show the picture.
			if(completePercent == 100)
			{
				final String fileName = bundle.getString("filename");
				//new File(getExternalCacheDir()/*getExternalFilesDir(Environment.DIRECTORY_PICTURES)*/, fileName).getPath();
				
				final File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SmallAppViewer");
				downloadDir.mkdirs();
				
				SmallImageViewApplication.this.uri = Uri.fromFile(new File(downloadDir, fileName));
				Log.d(TAG, SmallImageViewApplication.this.uri.toString());
				
				final Bitmap bitmap = 
						BitmapFactory.decodeFile
						(
							//new File(context.getPackageResourcePath(), fileName).getPath()
							FileUtils.toFile(SmallImageViewApplication.this.uri, context).getPath()
						);
				if(bitmap != null)
				{
					setBitmap(bitmap, INIT_BITMAP_W_MAX_SIZE, INIT_BITMAP_H_MAX_SIZE);
					progressBarForImageView.setVisibility(View.GONE);
					
					Toast.makeText(SmallImageViewApplication.this, R.string.image_view_help_message, Toast.LENGTH_SHORT).show();
					
//					// メディアスキャン実行
//					final String[] paths = {downloadDir.toString()};
//					final String[] mimeTypes = {"image/jpeg","image/jpg","image/png","image/bmp","image/bitmap"};
//					MediaScannerConnection.scanFile
//					(
//						getApplicationContext(),
//						paths,
//						mimeTypes,
//						new OnScanCompletedListener()
//						{
//							@Override
//							public void onScanCompleted(String path, Uri uri)
//							{
//								Log.v("OnScanCompletedListener", "Scan completed: path = " + paths[0]);
//								
//								Toast.makeText(SmallImageViewApplication.this, R.string.image_view_help_message, Toast.LENGTH_SHORT).show();
//							}
//						}
//					);
				}
			}
		}
	}
	
}