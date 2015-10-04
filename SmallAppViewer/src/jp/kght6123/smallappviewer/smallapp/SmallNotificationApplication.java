package jp.kght6123.smallappviewer.smallapp;

import java.util.ArrayList;
import java.util.Arrays;

import jp.kght6123.smallappcommon.application.SharedDataApplication;
import jp.kght6123.smallappcommon.listener.OnWindowFocusChangeTransportListener;
import jp.kght6123.smallappcommon.listener.OnWindowStateChangeTransportListener;
import jp.kght6123.smallappcommon.utils.SmallApplicationUtils;
import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.adapter.NotificationListItemAdapter;
import jp.kght6123.smallappviewer.service.SmallAppNotificationListenerService;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallAppWindow.WindowState;
import com.sony.smallapp.SmallApplication;

/**
 * SmallNotificationApplicationの本体、SmallAppNotificationListenerServiceへReciverでリクエストし、Reciverで結果を受け取る
 * @author Hirotaka
 *
 */
public class SmallNotificationApplication extends SmallApplication
{
	private static final String TAG = SmallNotificationApplication.class.getSimpleName();
	
	public static final String EXTRA_WINDOW_STATE = "windowState";
	
	public static final String NOTIFICATION_CATCH_ACTION = "jp.kght6123.smallappviewer.NOTIFICATION_CATCH_ACTION";
	
	public static final String EXTRA_COMMAND = "command";
	public static final String EXTRA_NOTIFICATION_EVENT = "notification_event";
	public static final String EXTRA_PURE_NOTIFICATION_COUNT = "pureNotificationCount";
	public static final String EXTRA_ACTIVE_NOTIFICATIONS = "activeNotifications";
	public static final String EXTRA_NOTIFICATION = "notification";
	
	public static final String COMMAND_NOTIFICATION = "notification";
	public static final String COMMAND_NOTIFICATION_COUNT = "notificationCount";
	public static final String COMMAND_ACTIVE_NOTIFICATIONS = "activeNotifications";
	public static final String COMMAND_ADD_NOTIFICATION = "addNotification";
	public static final String COMMAND_DEL_NOTIFICATION = "delNotification";
	
	private View mMinimizedView;
	private TextView notificationCountView;
	
	//private TextView txtView;
	private SmallAppNotificationReceiver nReceiver;
	
	private OnWindowFocusChangeTransportListener onTransport;
	private NotificationListItemAdapter adapter;
	
	@SuppressLint("InflateParams")
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		Log.d(TAG, "test application.");
		
		/* Set the layout of the application */
		final View window = 
				SmallApplicationUtils.setContentViewAndGetSmallApplicationWindowView(this, R.layout.smallapp_notification_main);
		
		/*
		 * Set the layout displayed when the application is minimized.
		 */
		this.mMinimizedView = LayoutInflater.from(this).inflate(R.layout.smallapp_notification_minimized, null);
		this.notificationCountView = (TextView)this.mMinimizedView.findViewById(R.id.notificationCountView);
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
		getWindow().setWindowTheme(com.sony.smallapp.R.style.Theme.Light);
		
		// メニューを設定
		setupOptionMenu();
		setupActionBar();
		
		// ウィンドウ状態初期設定
		getWindow().setWindowState(WindowState.MINIMIZED);
		
		// フォーカスアウト時の追加の半透明処理を設定
		this.onTransport = new OnWindowFocusChangeTransportListener(window, this);
		this.onTransport.onWindowFocusChanged(false);
		
		getWindow().setOnWindowFocusChangeListener(this.onTransport);
		getWindow().setOnWindowStateChangeListener(new OnWindowStateChangeTransportListener(this.onTransport, window, this));
		
		// 通知情報を取得するためのReceiverを設定
		this.nReceiver = new SmallAppNotificationReceiver();
		final IntentFilter filter = new IntentFilter();
		filter.addAction(NOTIFICATION_CATCH_ACTION);
		registerReceiver(this.nReceiver,filter);
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
		
		Log.d(TAG, "NotificationCountの更新");
		
		// NotificationCountの更新
		final Intent i1 = new Intent(SmallAppNotificationListenerService.NOTIFICATION_FIND_ACTION);
		i1.putExtra(SmallAppNotificationListenerService.EXTRA_COMMAND, SmallAppNotificationListenerService.COMMAND_PURE_NOTIFICATION_COUNT);
		sendBroadcast(i1);
		
		Log.d(TAG, "Notificationリストの更新");
		
		// Notificationリストの更新
		final Intent i2 = new Intent(SmallAppNotificationListenerService.NOTIFICATION_FIND_ACTION);
		i2.putExtra(SmallAppNotificationListenerService.EXTRA_COMMAND, SmallAppNotificationListenerService.COMMAND_INFO_LIST);
		sendBroadcast(i2);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(nReceiver);
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
				LayoutInflater.from(this).inflate(R.layout.smallapp_notification_header, null);// リソースからViewを取り出す
		
		// 通知を取得するReceiverを設定する
		header.findViewById(R.id.btnNotifySetting).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent intent = 
						new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
		
		header.findViewById(R.id.btnNotifyMinimized).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.MINIMIZED);
			}
		});
		
		header.findViewById(R.id.btnNotifyNormal).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.NORMAL);
			}
		});
		
		header.findViewById(R.id.btnNotifyFfitted).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.FITTED);
			}
		});
		
		header.findViewById(R.id.btnNotifyAllClear).setOnClickListener(new View.OnClickListener() {
			@Override
			public synchronized void onClick(final View v)
			{
				Log.d(TAG, "NotificationCountのすべて削除");
				
				final Intent i1 = new Intent(SmallAppNotificationListenerService.NOTIFICATION_FIND_ACTION);
				i1.putExtra(SmallAppNotificationListenerService.EXTRA_COMMAND, SmallAppNotificationListenerService.COMMAND_CLEAR_ALL);
				sendBroadcast(i1);
			}
		});
		
//		header.findViewById(R.id.btnCreateNotify).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(final View v) {
//				buttonClicked(v);
//			}
//		});
//		header.findViewById(R.id.btnClearNotify).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(final View v) {
//				buttonClicked(v);
//			}
//		});
//		header.findViewById(R.id.btnListNotify).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(final View v) {
//				buttonClicked(v);
//			}
//		});
		
		getWindow().setHeaderView(header);
	}
	
	private void setupActionBar()
	{
		final View iv_close = findViewById(R.id.nt_btnClose);
		iv_close.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					SmallNotificationApplication.this.finish();
				}
			}
		);
	}
	
//	public void buttonClicked(View v)
//	{
//		if(v.getId() == R.id.btnCreateNotify)
//		{
//			final NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//			final NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this);
//			ncomp.setContentTitle("My Notification");
//			ncomp.setContentText("Notification Listener Service Example");
//			ncomp.setTicker("Notification Listener Service Example");
//			ncomp.setSmallIcon(R.drawable.ic_launcher);
//			ncomp.setAutoCancel(true);
//			nManager.notify((int)System.currentTimeMillis(), ncomp.build());
//		}
//		else if(v.getId() == R.id.btnClearNotify)
//		{
//			final Intent i = new Intent(SmallAppNotificationListenerService.NOTIFICATION_FIND_ACTION);
//			i.putExtra(SmallAppNotificationListenerService.EXTRA_COMMAND, SmallAppNotificationListenerService.COMMAND_CLEAR_ALL);
//			sendBroadcast(i);
//		}
//		else if(v.getId() == R.id.btnListNotify)
//		{
//			final Intent i = new Intent(SmallAppNotificationListenerService.NOTIFICATION_FIND_ACTION);
//			i.putExtra(SmallAppNotificationListenerService.EXTRA_COMMAND, SmallAppNotificationListenerService.COMMAND_LIST);
//			sendBroadcast(i);
//		}
//	}
	
	private void updateNotificationCount(Intent intent)
	{
		final int tempCount = intent.getIntExtra(EXTRA_PURE_NOTIFICATION_COUNT, -1);
		if(tempCount != -1)
			SmallNotificationApplication.this.notificationCountView.setText(String.valueOf(tempCount));
	}
	
	private class SmallAppNotificationReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(final Context context, final Intent intent)
		{
			Log.d(TAG, "SmallAppNotificationReceiver.onReceive");
			
			if(intent.getStringExtra(EXTRA_COMMAND).equals(COMMAND_NOTIFICATION))
			{
				Log.d(TAG, "COMMAND_NOTIFICATION");
				
				//final String temp = intent.getStringExtra(EXTRA_NOTIFICATION_EVENT) + "\n" + SmallNotificationApplication.this.txtView.getText();
				//SmallNotificationApplication.this.txtView.setText(temp);
				
				Log.d(TAG, "COMMAND_NOTIFICATION="+intent.getStringExtra(EXTRA_NOTIFICATION_EVENT));
				
				updateNotificationCount(intent);
			}
			else if(intent.getStringExtra(EXTRA_COMMAND).equals(COMMAND_NOTIFICATION_COUNT))
			{
				Log.d(TAG, "COMMAND_NOTIFICATION_COUNT");
				
				updateNotificationCount(intent);
			}
			else if(intent.getStringExtra(EXTRA_COMMAND).equals(COMMAND_ACTIVE_NOTIFICATIONS))
			{
				Log.d(TAG, "COMMAND_ACTIVE_NOTIFICATIONS");
				
//				final Parcelable[] parcelables = 
//						intent.getParcelableArrayExtra(EXTRA_ACTIVE_NOTIFICATIONS);
//				
//				if(parcelables != null)
//				{
//					Log.d(TAG, "COMMAND_ACTIVE_NOTIFICATIONS, "
//							+ "parcelables != null, "
//							+ "class="+parcelables.getClass().getName()+", "
//							+ "length="+parcelables.length+", "
//							+ "class[0]="+parcelables[0].getClass().getName()+", "
//							);
//					
//					final StatusBarNotification[] notifications = new StatusBarNotification[parcelables.length];
//					for(int n = 0; n < parcelables.length; n++)
//					{
//						notifications[n] = (StatusBarNotification)parcelables[n];
//					}
//					
//					SmallNotificationApplication.this.adapter = 
//							new NotificationListItemAdapter(SmallNotificationApplication.this, 0, new ArrayList<StatusBarNotification>(Arrays.asList(notifications)));
//					final ListView listView = (ListView)findViewById(R.id.notificatinListView);
//					listView.setAdapter(adapter);
//				}
				
				final StatusBarNotification[] notifications = 
						((SharedDataApplication) SmallNotificationApplication.this.getApplicationContext()).getStatusBarNotifications();
				
				SmallNotificationApplication.this.adapter = 
						new NotificationListItemAdapter(SmallNotificationApplication.this, 0, new ArrayList<StatusBarNotification>(Arrays.asList(notifications)));
				final ListView listView = (ListView)findViewById(R.id.notificatinListView);
				listView.setAdapter(adapter);
			}
			else if(intent.getStringExtra(EXTRA_COMMAND).equals(COMMAND_ADD_NOTIFICATION))
			{
				Log.d(TAG, "COMMAND_ADD_NOTIFICATION");
				
//				final Parcelable parcelable = 
//						intent.getParcelableExtra(EXTRA_NOTIFICATION);
//				
//				if(parcelable != null)
//				{
//					Log.d(TAG, "COMMAND_ADD_NOTIFICATION, "
//							+ "parcelable != null, "
//							+ "class="+parcelable.getClass().getName()+", "
//							);
//					
//					if(SmallNotificationApplication.this.adapter != null)
//						SmallNotificationApplication.this.adapter.setItemForId((StatusBarNotification)parcelable);
//				}
				
				final StatusBarNotification notification = 
						((SharedDataApplication) SmallNotificationApplication.this.getApplicationContext()).popStatusBarNotification();
				
				if(notification != null && SmallNotificationApplication.this.adapter != null)
					SmallNotificationApplication.this.adapter.setItemForId(notification);
				
				updateNotificationCount(intent);
			}
			else if(intent.getStringExtra(EXTRA_COMMAND).equals(COMMAND_DEL_NOTIFICATION))
			{
				Log.d(TAG, "COMMAND_DEL_NOTIFICATION");
				
//				final Parcelable parcelable = 
//						intent.getParcelableExtra(EXTRA_NOTIFICATION);
//				
//				if(parcelable != null)
//				{
//					Log.d(TAG, "COMMAND_DEL_NOTIFICATION, "
//							+ "parcelable != null, "
//							+ "class="+parcelable.getClass().getName()+", "
//							);
//					
//					if(SmallNotificationApplication.this.adapter != null)
//					{
//						final StatusBarNotification sbn = (StatusBarNotification)parcelable;
//						SmallNotificationApplication.this.adapter.removeForId(sbn);
//					}
//				}
				
				final StatusBarNotification notification = 
						((SharedDataApplication) SmallNotificationApplication.this.getApplicationContext()).popStatusBarNotification();
				
				if(notification != null && SmallNotificationApplication.this.adapter != null)
					SmallNotificationApplication.this.adapter.removeForId(notification);
				
				updateNotificationCount(intent);
			}
		}
	}
}