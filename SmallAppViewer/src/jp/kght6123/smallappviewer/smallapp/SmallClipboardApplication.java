package jp.kght6123.smallappviewer.smallapp;

import java.util.List;

import jp.kght6123.smallappcommon.application.SharedDataApplication;
import jp.kght6123.smallappcommon.application.SharedDataApplication.ClipDataItem;
import jp.kght6123.smallappcommon.listener.OnWindowFocusChangeTransportListener;
import jp.kght6123.smallappcommon.listener.OnWindowStateChangeTransportListener;
import jp.kght6123.smallappcommon.utils.SmallApplicationUtils;
import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.adapter.ClipDataListItemAdapter;
import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallAppWindow.WindowState;
import com.sony.smallapp.SmallApplication;

/**
 * 【未完成】SmallClipboardApplicationの本体
 * @author Hirotaka
 *
 */
public class SmallClipboardApplication extends SmallApplication
{
	private static final String TAG = SmallClipboardApplication.class.getSimpleName();
	
	private View mMinimizedView;
	
	private OnWindowFocusChangeTransportListener onTransport;
	private ClipDataListItemAdapter adapter;
	
	@SuppressLint("InflateParams")
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		Log.d(TAG, "clip application.");
		
		/* Set the layout of the application */
		final View window = 
				SmallApplicationUtils.setContentViewAndGetSmallApplicationWindowView(this, R.layout.smallapp_clipboard_main);
		
		/*
		 * Set the layout displayed when the application is minimized.
		 */
		this.mMinimizedView = LayoutInflater.from(this).inflate(R.layout.smallapp_clipboard_minimized, null);
		setMinimizedView(this.mMinimizedView);
		
		/* Set the title of the application to be displayed in the titlebar */
		setTitle("");
		
		SmallAppWindow.Attributes attr = getWindow().getAttributes();
		
		/* Set the requested width of the application */
		attr.width = getResources().getDimensionPixelSize(R.dimen.width);
		/* Set the requested height of the application */
		attr.height = getResources().getDimensionPixelSize(R.dimen.height);
		
		/* Use this flag to make the application window resizable */
		attr.flags |= SmallAppWindow.Attributes.FLAG_RESIZABLE;
		/* Use this flag to remove the titlebar from the window */
		//attr.flags |= SmallAppWindow.Attributes.FLAG_NO_TITLEBAR;
		/* Use this flag to enable hardware accelerated rendering */
		attr.flags |= SmallAppWindow.Attributes.FLAG_HARDWARE_ACCELERATED;
		
		/* Set the window attributes to apply the changes above */
		getWindow().setAttributes(attr);
		
		/**
		 * 全体のテーマ、DarkかLightが使える。
		 */
		getWindow().setWindowTheme(com.sony.smallapp.R.style.Theme.Dark);
		
		// メニューを設定
		setupOptionMenu();
		setupActionBar();
		
		// ウィンドウ状態初期設定
		getWindow().setWindowState(WindowState.MINIMIZED);
		
		// フォーカスアウト時の追加の半透明処理を設定
		this.onTransport = new OnWindowFocusChangeTransportListener(window, this);
		getWindow().setOnWindowFocusChangeListener(this.onTransport);
		getWindow().setOnWindowStateChangeListener(new OnWindowStateChangeTransportListener(this.onTransport, window, this));
		
		createClipboardList();
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		Log.d(TAG, "onStart");
		
		if (SdkInfo.VERSION.API_LEVEL < 2) 
		{
			Toast.makeText(this, R.string.api_not_supported,
					Toast.LENGTH_SHORT).show();
			return;
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@SuppressLint("InflateParams")
	private void setupOptionMenu()
	{
		final View header = 
				LayoutInflater.from(this).inflate(R.layout.smallapp_clipboard_header, null);// リソースからViewを取り出す
		
		header.findViewById(R.id.btnClipMinimized).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.MINIMIZED);
			}
		});
		
		header.findViewById(R.id.btnClipNormal).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.NORMAL);
			}
		});
		
		header.findViewById(R.id.btnClipFfitted).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.FITTED);
			}
		});
		
		header.findViewById(R.id.btnClipAllClear).setOnClickListener(new View.OnClickListener() {
			@Override
			public synchronized void onClick(final View v)
			{
				Log.d(TAG, "ClipboardCountのすべて削除");
				
				((SharedDataApplication)SmallClipboardApplication.this.getApplicationContext()).getClipDataItemList().clear();
				createClipboardList();
			}
		});
		getWindow().setHeaderView(header);
	}
	
	private void setupActionBar()
	{
		final View iv_close = findViewById(R.id.cb_btnClose);
		iv_close.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					SmallClipboardApplication.this.finish();
				}
			}
		);
	}
	
	private void createClipboardList()
	{
		final List<ClipDataItem> clipDataItemList = 
				((SharedDataApplication) this.getApplicationContext()).getClipDataItemList();
		
		this.adapter = 
				new ClipDataListItemAdapter(this, 0, clipDataItemList);
		final ListView listView = (ListView)findViewById(R.id.clipListView);
		listView.setAdapter(this.adapter);
	}
}