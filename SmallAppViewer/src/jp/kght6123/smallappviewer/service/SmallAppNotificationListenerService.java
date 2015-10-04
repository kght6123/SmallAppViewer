package jp.kght6123.smallappviewer.service;

import jp.kght6123.smallappcommon.application.SharedDataApplication;
import jp.kght6123.smallappcommon.utils.NotiCrunch;
import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.smallapp.SmallNotificationApplication;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppNotFoundException;
import com.sony.smallapp.SmallAppWindow.WindowState;
import com.sony.smallapp.SmallApplicationManager;

/**
 * 通知情報を収集するサービス
 *  → SmallApplicationからReciver経由での呼び出しで収集したり、SmallApplicationのReciverに結果を返したりする。
 *  
 * @author Hirotaka
 *
 */
public class SmallAppNotificationListenerService
	extends NotificationListenerService
{
	private String TAG = this.getClass().getSimpleName();
	private SmallAppNotificationListenerServiceReceiver nlservicereciver;
	
	public static final String NOTIFICATION_FIND_ACTION = "jp.kght6123.smallappviewer.NOTIFICATION_FIND_ACTION";
	public static final String EXTRA_COMMAND = "command";
	public static final String EXTRA_PKG = "package";
	public static final String EXTRA_TAG = "tag";
	public static final String EXTRA_ID = "id";
	
	public static final String COMMAND_LIST = "list";
	public static final String COMMAND_INFO_LIST = "infoList";
	public static final String COMMAND_CLEAR_ALL = "clearall";
	public static final String COMMAND_CLEAR = "clear";
	public static final String COMMAND_PURE_NOTIFICATION_COUNT = "pureNotificationCount";
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		this.nlservicereciver = new SmallAppNotificationListenerServiceReceiver();
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction(NOTIFICATION_FIND_ACTION);
		registerReceiver(this.nlservicereciver, filter);
		
		startSmallNotificationApplicationView(WindowState.MINIMIZED);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(this.nlservicereciver);
	}
	
	@Override
	public void onNotificationPosted(final StatusBarNotification sbn)
	{
		Log.i(TAG,"**********  onNotificationPosted");
		Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
		
		final int pureNotificationCount = getPureNotificationCount();
		Log.d(TAG, "pureNotificationCount="+pureNotificationCount);
		
		sendNotificationCatchAction
		(
			SmallNotificationApplication.COMMAND_ADD_NOTIFICATION,
			"",
			pureNotificationCount,
			sbn,
			null
		);
	}
	@Override
	public void onNotificationRemoved(final StatusBarNotification sbn)
	{
		Log.i(TAG,"********** onNOtificationRemoved");
		Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
		
		final int pureNotificationCount = getPureNotificationCount();
		Log.d(TAG, "pureNotificationCount="+pureNotificationCount);
		
		sendNotificationCatchAction
		(
			SmallNotificationApplication.COMMAND_DEL_NOTIFICATION,
			"",
			pureNotificationCount,
			sbn,
			null
		);
	}
	
	/**
	 * 現状の通知をActivity等から取得するBroadcastReceiver
	 * 
	 * @author Hirotaka
	 *
	 */
	private class SmallAppNotificationListenerServiceReceiver
		extends BroadcastReceiver
	{
		@Override
		public void onReceive(final Context context, final Intent intent)
		{
			Log.d(TAG, "SmallAppNotificationListenerServiceReceiver.onReceive");
			
			if(intent.getStringExtra(EXTRA_COMMAND).equals(COMMAND_CLEAR_ALL))
			{
				Log.d(TAG, "COMMAND_CLEAR_ALL");
				
				SmallAppNotificationListenerService.this.cancelAllNotifications();
			}
			else if(intent.getStringExtra(EXTRA_COMMAND).equals(COMMAND_CLEAR))
			{
				Log.d(TAG, "COMMAND_CLEAR");
				
				final String pkg = intent.getStringExtra(EXTRA_PKG);
				final String tag = intent.getStringExtra(EXTRA_TAG);
				final int id = intent.getIntExtra(EXTRA_ID, -1);
				
				Log.d(TAG, String.format("COMMAND_CLEAR, pkg=%s tag=%s id=%d", pkg, tag, id));
				
				if(pkg != null && !pkg.equals("") && id != -1)
					SmallAppNotificationListenerService.this.cancelNotification(pkg, tag, id);
			}
			else if(intent.getStringExtra(EXTRA_COMMAND).equals(COMMAND_LIST))
			{
				Log.d(TAG, "COMMAND_LIST");
				
				sendNotificationCatchAction(SmallNotificationApplication.COMMAND_NOTIFICATION, "=====================\n", 	getPureNotificationCount(), null, null);
				
				int i=1;
				final StatusBarNotification[] activeNotifications = SmallAppNotificationListenerService.this.getActiveNotifications();
				if(activeNotifications != null)
				{
					for (StatusBarNotification sbn : activeNotifications)
					{
						sendNotificationCatchAction
						(
							SmallNotificationApplication.COMMAND_NOTIFICATION,
							i +" " + NotiCrunch.extractTextFromNotification(getApplicationContext(), sbn.getNotification())/*sbn.getPackageName()*/ + "\n",
							getPureNotificationCount(),
							null,
							null
						);
						i++;
					}
				}
				sendNotificationCatchAction(SmallNotificationApplication.COMMAND_NOTIFICATION, "===== Notification List ====\n", getPureNotificationCount(), null, null);
			}
			else if(intent.getStringExtra(EXTRA_COMMAND).equals(COMMAND_INFO_LIST))
			{
				Log.d(TAG, "COMMAND_INFO_LIST");
				
				sendNotificationCatchAction
				(
					SmallNotificationApplication.COMMAND_ACTIVE_NOTIFICATIONS,
					"",
					getPureNotificationCount(),
					null,
					SmallAppNotificationListenerService.this.getActiveNotifications()
				);
			}
			else if(intent.getStringExtra(EXTRA_COMMAND).equals(COMMAND_PURE_NOTIFICATION_COUNT))
			{
				Log.d(TAG, "COMMAND_PURE_NOTIFICATION_COUNT");
				
				final int pureNotificationCount = getPureNotificationCount();
				
				sendNotificationCatchAction(SmallNotificationApplication.COMMAND_NOTIFICATION_COUNT, "", pureNotificationCount, null, null);
				
				Log.d(TAG, "pureNotificationCount="+pureNotificationCount);
			}
		}
	}
	
	private int getPureNotificationCount()
	{
		int pureNotificationCount = 0;
		final StatusBarNotification[] activeNotifications = this.getActiveNotifications();
		if(activeNotifications == null)
			return pureNotificationCount;
		
		for (StatusBarNotification sbn : activeNotifications)
		{
			if(isPureNotification(sbn))
				pureNotificationCount++;
		}
		return pureNotificationCount;
	}
	
	private static boolean isPureNotification(final StatusBarNotification sbn)
	{
		return true;
				/*
				(
					(sbn.getNotification().flags & Notification.FLAG_FOREGROUND_SERVICE) != 0
					|| (sbn.getNotification().flags & Notification.FLAG_NO_CLEAR) != 0)
				&&
				(
					(sbn.getNotification().flags & Notification.FLAG_AUTO_CANCEL) == 0
					|| (sbn.getNotification().flags & Notification.FLAG_INSISTENT) == 0
					|| (sbn.getNotification().flags & Notification.FLAG_ONGOING_EVENT) == 0
					|| (sbn.getNotification().flags & Notification.FLAG_ONLY_ALERT_ONCE) == 0
					|| (sbn.getNotification().flags & Notification.FLAG_SHOW_LIGHTS) == 0
				);*/
	}
	/**
	 * Notification.flags
	 * int	FLAG_AUTO_CANCEL	Bit to be bitwise-ored into the flags field that should be set if the notification should be canceled when it is clicked by the user.
	 * それがユーザーによってクリックされたときに通知をキャンセルする場合には、設定する必要があります。
	 * 
	 * int	FLAG_FOREGROUND_SERVICE	Bit to be bitwise-ored into the flags field that should be set if this notification represents a currently running service.
	 * この通知は、現在実行中のサービスを表す場合には、設定する必要があります。
	 * 
	 * int	FLAG_HIGH_PRIORITY	 This constant was deprecated in API level 16. Use priority with a positive value.
	 * この定数は、APIレベル16で非推奨になりました。正の値で優先順位を使用してください。
	 * 
	 * int	FLAG_INSISTENT	Bit to be bitwise-ored into the flags field that if set, the audio will be repeated until the notification is cancelled or the notification window is opened.
	 * 設定した場合、通知が取り消されるか、通知ウィンドウが開かれるまで、オーディオが繰り返されること。
	 * 
	 * int	FLAG_NO_CLEAR	Bit to be bitwise-ored into the flags field that should be set if the notification should not be canceled when the user clicks the Clear all button.
	 * ユーザーは、[すべてをクリア]ボタンをクリックしたときに通知が取り消されるべきでない場合には、設定する必要があります。
	 * 
	 * int	FLAG_ONGOING_EVENT	Bit to be bitwise-ored into the flags field that should be set if this notification is in reference to something that is ongoing, like a phone call.
	 * この通知は、電話のように、継続しているものを参考にされた場合は、設定する必要があります。
	 * 
	 * int	FLAG_ONLY_ALERT_ONCE	Bit to be bitwise-ored into the flags field that should be set if you would only like the sound, vibrate and ticker to be played if the notification was not already showing.
	 * 通知がすでに表示されなかった場合は、音、振動してティッカーを再生できるようにする唯一のような場合には、設定する必要があります。
	 * 
	 * int	FLAG_SHOW_LIGHTS	Bit to be bitwise-ored into the flags field that should be set if you want the LED on for this notification.
	 * この通知でLED ONしたい場合に設定する必要があります。
	 */
	
	private void sendNotificationCatchAction(String command, String notificationEvent, int pureNotificationCount, StatusBarNotification statusBarNotification1, StatusBarNotification[] statusBarNotifications)
	{
		final Intent i = new  Intent(SmallNotificationApplication.NOTIFICATION_CATCH_ACTION);
		i.putExtra(SmallNotificationApplication.EXTRA_COMMAND, command);
		i.putExtra(SmallNotificationApplication.EXTRA_NOTIFICATION_EVENT, notificationEvent);
		i.putExtra(SmallNotificationApplication.EXTRA_PURE_NOTIFICATION_COUNT, pureNotificationCount);
		
		if(statusBarNotification1 != null)
		{
			Log.d(TAG, "sendNotificationCatchAction statusBarNotification1 not null");
			
			((SharedDataApplication) SmallAppNotificationListenerService.this.getApplication())
			.pushStatusBarNotification(statusBarNotification1);
		}
		
		if(statusBarNotifications != null)
		{
			Log.d(TAG, "sendNotificationCatchAction statusBarNotifications not null");
			
			((SharedDataApplication) SmallAppNotificationListenerService.this.getApplication())
			.setStatusBarNotifications(statusBarNotifications);
		}
		else
			Log.d(TAG, "sendNotificationCatchAction statusBarNotifications is null");
		
		sendBroadcast(i);
	}
	
	private void startSmallNotificationApplicationView(final WindowState state)
	{
		if (SdkInfo.VERSION.API_LEVEL >= 2)
		{
			final Intent intent = new Intent(this, SmallNotificationApplication.class);
			intent.putExtra(SmallNotificationApplication.EXTRA_WINDOW_STATE, state.name());
			
			try
			{
				SmallApplicationManager.startApplication(this, intent);
			}
			catch (SmallAppNotFoundException e)
			{
				Toast.makeText(this, e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			Toast.makeText(this, R.string.api_not_supported,
					Toast.LENGTH_SHORT).show();
		}
	}
}
