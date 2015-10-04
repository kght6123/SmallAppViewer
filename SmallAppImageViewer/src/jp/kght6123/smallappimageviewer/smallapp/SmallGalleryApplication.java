package jp.kght6123.smallappimageviewer.smallapp;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.kght6123.smallappcommon.listener.OnWindowFocusChangeTransportListener;
import jp.kght6123.smallappcommon.listener.OnWindowStateChangeTransportListener;
import jp.kght6123.smallappcommon.utils.ImageUtils;
import jp.kght6123.smallappcommon.utils.SmallApplicationUtils;
import jp.kght6123.smallappimageviewer.R;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppNotFoundException;
import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallAppWindow.WindowState;
import com.sony.smallapp.SmallApplication;
import com.sony.smallapp.SmallApplicationManager;

/**
 * SmallGalleryApplicationの本体（まだまだ負荷が大きい）
 * @author Hirotaka
 *
 */
public class SmallGalleryApplication extends SmallApplication
{
	private static final String TAG = SmallGalleryApplication.class.getSimpleName();
	private View mMinimizedView;
	
	private final Handler mHandler = new Handler();
	
	private OnWindowFocusChangeTransportListener onTransport;
	
	private FileFilter galleryFileFilter = new FileFilter()
	{
		public boolean accept(final File file)
		{
			final String[] fileNameTemp = file.getName().split("\\.");
			
			if(fileNameTemp.length > 1)
			{
				final String ex = fileNameTemp[fileNameTemp.length-1];
				return ex.equalsIgnoreCase("jpg")
						|| ex.equalsIgnoreCase("jpeg")
						|| ex.equalsIgnoreCase("png")
						|| ex.equalsIgnoreCase("gif")
						|| ex.equalsIgnoreCase("bmp");
			}
			else
				return false;
		}
	};
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		Log.d(TAG, "test application.");
		
		/* Set the layout of the application */
		final View window = 
				SmallApplicationUtils.setContentViewAndGetSmallApplicationWindowView(this, R.layout.smallapp_gallery_main);
		
		/*
		 * Set the layout displayed when the application is minimized.
		 */
		mMinimizedView = LayoutInflater.from(this).inflate(R.layout.smallapp_gallery_minimized, null);
		setMinimizedView(mMinimizedView);
		
		/* Set the title of the application to be displayed in the titlebar */
		setTitle(R.string.app_name);
		
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
		
		this.onTransport = new OnWindowFocusChangeTransportListener(window, this);
		getWindow().setOnWindowFocusChangeListener(this.onTransport);
		getWindow().setOnWindowStateChangeListener(new OnWindowStateChangeTransportListener(this.onTransport, window, this));
		
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
		
		// 読み込む画像ファイル一覧作成
		final List<BitmapFileInfo> targetFileList = new ArrayList<BitmapFileInfo>();
		Log.d(TAG, "内部ストレージ画像一覧");
		targetFileList.addAll(getStorageImageList(MediaStore.Images.Media.INTERNAL_CONTENT_URI));
		Log.d(TAG, "外部ストレージ画像一覧");
		targetFileList.addAll(getStorageImageList(MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
		
		Log.d(TAG, "アプリ内画像一覧");
		targetFileList.addAll(getDummyBitmapFileInfoList(getFilesDir().listFiles(this.galleryFileFilter)));
		Log.d(TAG, "アプリ外部画像一覧");
		targetFileList.addAll(getDummyBitmapFileInfoList(getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles(this.galleryFileFilter)));
		Log.d(TAG, "アプリ外部共有ダウンロード画像一覧");
		targetFileList.addAll(getDummyBitmapFileInfoList(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SmallAppViewer").listFiles(this.galleryFileFilter)));
		Log.d(TAG, "一覧作成完了");
		
		Collections.sort(targetFileList, BITMAP_FILE_INFO_COMPARATOR);
		Log.d(TAG, "一覧ソート完了");
		
		// Gallery作成
		final GridLayout gridLayoutGallery = (GridLayout) findViewById(R.id.gridLayoutGallery);
		
		final int rowMax = (int)(Math.ceil(Double.valueOf(targetFileList.size()) / 6d/*5*/));// 切り上げ
		final int columnMax = 6/*5*/;
		
		gridLayoutGallery.setRowCount(rowMax);
		gridLayoutGallery.setColumnCount(columnMax);
		
		final ContentResolver cr = getContentResolver();
		
		final Runnable updateGalleryRunnable = new Runnable()
		{
			@Override
			public void run()
			{// 遅延で読み込み
				final int px = ImageUtils.dp2Px(87/*192*/, SmallGalleryApplication.this);
				final int paddingPx = ImageUtils.dp2Px(5, SmallGalleryApplication.this);
				
				int fileIndex = 0;
				for(int rowIdx = 0; rowIdx < rowMax; rowIdx++)
				{
					final int rowIdxTmp = rowIdx;
					
					for(int colIdx = 0; colIdx < columnMax; colIdx++)
					{
						if(fileIndex >= targetFileList.size())
							continue;
						
						final BitmapFileInfo fileInfo = targetFileList.get(fileIndex);
						final File imageFile = fileInfo.file;
						
						final int colIdxTmp = colIdx;
						
						final Bitmap resizeBitmap;
						{
//							final Bitmap bitmap = 
//									BitmapFactory.decodeFile
//									(
//										imageFile.getPath()
//									);
							
							final Bitmap bitmap;
							if(fileInfo.id == -1)
								bitmap = ImageUtils.getResizeBitmap(fileInfo.file, px);
							else
								bitmap = MediaStore.Images.Thumbnails.getThumbnail(cr, fileInfo.id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
							
							if(bitmap == null)
								return;
							
							Log.d(TAG, "imageFile="+imageFile.getPath());
							Log.d(TAG, "px="+px);
							
							resizeBitmap = ImageUtils.getSquareBitmap(px, bitmap);
						}
						
						/**
						 * Handlerのpostメソッドを使ってUIスレッドに処理をdispatchします
						 */
						mHandler.post
						(
							new Runnable()
							{
								@Override
								public void run()
								{// 遅延で画像をリサイズ
									final ImageView iv = new ImageView(SmallGalleryApplication.this.getApplicationContext());
									SmallGalleryApplication.this.onTransport.add(iv);
									
									iv.setImageBitmap(resizeBitmap);
									iv.setPadding(paddingPx, paddingPx, 0, 0);
									iv.setClickable(true);
									iv.setOnClickListener
									(
										new View.OnClickListener()
										{
											@Override
											public void onClick(View v)
											{
												final Intent intent = new Intent(getIntent());
												intent.setClass(getApplicationContext(), SmallImageViewApplication.class);
												intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(imageFile));
												
												try
												{
													SmallApplicationManager.startApplication(getApplicationContext(), intent);
												}
												catch (SmallAppNotFoundException e)
												{
													Toast.makeText(getApplicationContext(), e.getMessage(),
															Toast.LENGTH_SHORT).show();
												}
											}
										}
									);
									iv.setOnLongClickListener
									(
										new View.OnLongClickListener()
										{
											@Override
											public boolean onLongClick(View v)
											{
												imageFile.delete();
												gridLayoutGallery.removeView(iv);
												return true;
											}
										}
									);
									
									final GridLayout.LayoutParams gridLayoutChildParams = new GridLayout.LayoutParams();
									
									final int column = colIdxTmp;
									final int colSpan = 1;
									gridLayoutChildParams.columnSpec = GridLayout.spec(column, colSpan, GridLayout.FILL);
									
									final int row = rowIdxTmp;
									final int rowSpan = 1;
									gridLayoutChildParams.rowSpec = GridLayout.spec(row, rowSpan, GridLayout.FILL);
									gridLayoutChildParams.width = px;
									gridLayoutChildParams.height = px;
									
									gridLayoutGallery.addView(iv, gridLayoutChildParams);
								}
							}
						);
						
						fileIndex++;
					}
				}
			}
		};
		new Thread(updateGalleryRunnable).start();
	}
	
	private List<BitmapFileInfo> getStorageImageList(final Uri uri)
	{
		final String[] columns = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
		final Cursor cursor = getContentResolver().query(uri, columns, null, null, null);
		final int fieldIdIndex = cursor.getColumnIndex(columns[0]);
		final int fieldDataIndex = cursor.getColumnIndex(columns[1]);
		cursor.moveToFirst();
		
		final List<BitmapFileInfo> files = new ArrayList<BitmapFileInfo>();
		
		if(cursor.getCount() == 0)
			return files;
		
		do
		{
			final long id = cursor.getLong(fieldIdIndex);
			final String data = cursor.getString(fieldDataIndex);
			final File file = new File(data);
			if(file.isFile() && file.exists())
				files.add(new BitmapFileInfo(file, id));
		}
		while(cursor.moveToNext());
		
		return files;
	}
	
	private static List<BitmapFileInfo> getDummyBitmapFileInfoList(final File[] files)
	{
		final List<BitmapFileInfo> tempFiles = new ArrayList<BitmapFileInfo>();
		
		if(files != null)
			for(final File file : files)
				tempFiles.add(new BitmapFileInfo(file, -1));
		
		return tempFiles;
	}
	
	private static class BitmapFileInfo
	{
		private final File file;
		private final long id;
		
		private BitmapFileInfo(final File file, final long id)
		{
			super();
			this.file = file;
			this.id = id;
		}
	}
	
	private final static Comparator<BitmapFileInfo> BITMAP_FILE_INFO_COMPARATOR = 
			new Comparator<BitmapFileInfo>()
	{
		@Override
		public int compare(final BitmapFileInfo o1, final BitmapFileInfo o2)
		{
			final long l1 = o1.file.lastModified();
			final long l2 = o2.file.lastModified();
			
			if(l1 == l2)
				return 0;
			else if(l1 > l2)
				return -1;
			else
				return 1;
		}
	};
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	private void setupOptionMenu()
	{
		final View header = 
				LayoutInflater.from(this).inflate(R.layout.smallapp_gallery_header, null);// リソースからViewを取り出す
		
		getWindow().setHeaderView(header);
	}
	
	private void setupActionBar()
	{
		final View iv_close = findViewById(R.id.btnGyClose);
		iv_close.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					SmallGalleryApplication.this.finish();
				}
			}
		);
		
//		findViewById(R.id.btnGyAllSave).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(final View v)
//			{
//				Toast.makeText(getApplicationContext(), "processing...",
//						Toast.LENGTH_SHORT).show();
//				
//				final File appFile = getFilesDir();
//				final File[] imageFiles = appFile.listFiles(SmallGalleryApplication.this.gOalleryFileFilter);
//				
//				final File saveDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SmallAppViewer");
//				if(!saveDir.exists())
//					saveDir.mkdirs();
//				
//				for(final File imageFile : imageFiles)
//				{
//					final File saveFile = new File(saveDir, imageFile.getName());
//					FileUtils.copyFile(imageFile, saveFile, SmallGalleryApplication.this, false);
//				}
//				
//				Toast.makeText(getApplicationContext(), "complete!",
//						Toast.LENGTH_SHORT).show();
//			}
//		});
		
		
		findViewById(R.id.btnGyMinimized).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.MINIMIZED);
			}
		});
		
		findViewById(R.id.btnGyNormal).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.NORMAL);
			}
		});
		
		findViewById(R.id.btnGyFfitted).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.FITTED);
			}
		});
	}
}