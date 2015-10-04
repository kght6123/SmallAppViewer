package jp.kght6123.smallappviewer.adapter;

import java.util.Comparator;
import java.util.List;

import jp.kght6123.smallappcommon.utils.StringUtils;
import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.service.SmallAppNotificationListenerService;
import jp.kght6123.smallappviewer.smallapp.SmallNotificationApplication;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sony.smallapp.SmallAppWindow.WindowState;

/**
 * 通知データをListViewで表示するためのAdapter
 * @author Hirotaka
 *
 */
public class NotificationListItemAdapter extends ArrayAdapter<StatusBarNotification>
{
	private static final String TAG = NotificationListItemAdapter.class.getSimpleName();
	private LayoutInflater layoutInflater_;
	private final SmallNotificationApplication application;
	private final Comparator<StatusBarNotification> comparator;
	
	public NotificationListItemAdapter(final SmallNotificationApplication application, int textViewResourceId, List<StatusBarNotification> objects)
	{
		super(application, textViewResourceId, objects);
		this.application = application;
		this.layoutInflater_ = (LayoutInflater) application.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		this.comparator = new Comparator<StatusBarNotification>()
		{
			@Override
			public int compare(final StatusBarNotification lhs,
					final StatusBarNotification rhs) {
//				return (int)(rhs.getPostTime() - lhs.getPostTime());
				return (int)(rhs.getNotification().priority - lhs.getNotification().priority);
			}
		};
		this.sort(comparator);
	}
	
	@SuppressLint({ "ViewHolder", "InflateParams" })
	@Override
	public View getView(final int position, View convertView, final ViewGroup parent)
	{
		// 特定の行(position)のデータを得る
		final StatusBarNotification item = (StatusBarNotification)getItem(position);
		
//		// convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
//		if (convertView == null) {
//			convertView = this.layoutInflater_.inflate(R.layout.smallapp_notification_listitem, null);
//		}
//		
//		// CustomDataのデータをViewの各Widgetにセットする
//		final ImageView imageView = (ImageView)convertView.findViewById(R.id.notificationImage);
//		
//		if(item.getNotification().largeIcon != null)
//			imageView.setImageBitmap(item.getNotification().largeIcon);
//		else if(item.getNotification().icon != 0)
//			imageView.setImageResource(item.getNotification().icon);
//		
//		final TextView textView = (TextView)convertView.findViewById(R.id.notificationText1);
//		textView.setText(item.getNotification().tickerText);
//		
//		// LinearLayoutを取得
//		final LinearLayout notificationLinearLayout = (LinearLayout)convertView.findViewById(R.id.notificationLinearLayout);
//		
//		if(item.getNotification().bigContentView != null)
//			notificationLinearLayout.addView(item.getNotification().bigContentView.apply(getContext(), notificationLinearLayout));
//		else if(item.getNotification().contentView != null)
//			notificationLinearLayout.addView(item.getNotification().contentView.apply(getContext(), notificationLinearLayout));
		
		// 常に新しく作る
		convertView = this.layoutInflater_.inflate(R.layout.smallapp_notification_listitem, null);
		
		// LinearLayoutを取得
		final LinearLayout notificationLinearLayout = (LinearLayout)convertView.findViewById(R.id.notificationLinearLayout2);
		final Button btnDelNotification = (Button)convertView.findViewById(R.id.btnDelNotification);
		final Button btnDetailNotification = (Button)convertView.findViewById(R.id.btnDetailNotification);
		final LinearLayout notificationVeil = (LinearLayout)convertView.findViewById(R.id.NotificationVeil);
		
		btnDelNotification.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Log.d(TAG, "Notificationの削除");
					
					final Intent i1 = new Intent(SmallAppNotificationListenerService.NOTIFICATION_FIND_ACTION);
					i1.putExtra(SmallAppNotificationListenerService.EXTRA_COMMAND, SmallAppNotificationListenerService.COMMAND_CLEAR);
					i1.putExtra(SmallAppNotificationListenerService.EXTRA_PKG, item.getPackageName());
					i1.putExtra(SmallAppNotificationListenerService.EXTRA_TAG, item.getTag());
					i1.putExtra(SmallAppNotificationListenerService.EXTRA_ID, item.getId());
					getContext().sendBroadcast(i1);
				}
			}
		);
		
		btnDetailNotification.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						parent.getContext().startIntentSender(item.getNotification().contentIntent.getIntentSender(), new Intent(), 0, 0, 0);
						NotificationListItemAdapter.this.application.getWindow().setWindowState(WindowState.MINIMIZED);	// 最小化
					}
					catch (Exception e)
					{
						Toast.makeText(parent.getContext(), e.getClass().getSimpleName() + ":" +e.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		);
		
//		// 常に新しく作る
//		convertView = this.layoutInflater_.inflate(R.layout.smallapp_notification_content_listitem, null);
//		
//		// LinearLayoutを取得
//		final LinearLayout notificationLinearLayout = (LinearLayout)convertView.findViewById(R.id.notificationLinearLayout);
		
		
		if(item.getNotification().bigContentView != null)
			notificationLinearLayout.addView(item.getNotification().bigContentView.apply(getContext(), notificationLinearLayout));
		else if(item.getNotification().contentView != null)
			notificationLinearLayout.addView(item.getNotification().contentView.apply(getContext(), notificationLinearLayout));
		
		notificationLinearLayout.setOnClickListener
		(
			new View.OnClickListener()
			{
				private boolean controlView = false;
				
				@Override
				public synchronized void onClick(View v)
				{
					if(this.controlView)
					{
						btnDelNotification.setVisibility(View.GONE);
						btnDetailNotification.setVisibility(View.GONE);
						notificationVeil.setVisibility(View.GONE);
						this.controlView = false;
					}
					else
					{
						if(item.isClearable())
							btnDelNotification.setVisibility(View.VISIBLE);
						
						if(item.getNotification().contentIntent != null)
							btnDetailNotification.setVisibility(View.VISIBLE);
						
						notificationVeil.setVisibility(View.VISIBLE);
						this.controlView = true;
					}
				}
			}
		);
		
		return convertView;
	}
	
	public synchronized void setItemForId(final StatusBarNotification sbn)
	{
		this.removeForId(sbn);
		this.add(sbn);
		this.sort(this.comparator);
	}
	
	/**
	 * StatusBarNotificationを取得する
	 * 
	 * ・id 通知に対して一意のID、追加時に同じIDは置き換えられる。
	 * ・key 通知オブジェクト毎に一意のKey。（参照できない）
	 * ・id＆Tag tagも指定されている場合は、タグも含めて一意となる
	 */
	public synchronized StatusBarNotification getItemForId(final StatusBarNotification sbn)
	{
		final int id = sbn.getId();
		final String tag = sbn.getTag();
		final String packageName = sbn.getPackageName();
		
		Log.d(TAG, String.format("getItemForId, id=%d, tag=%s, packageName=%s", id, tag, packageName));
		
		final int count = this.getCount();
		for(int x = 0; x < count; x++)
		{
			final StatusBarNotification sbnTemp = 
					this.getItem(x);
			
			final int idTemp = sbnTemp.getId();
			final String tagTemp = sbnTemp.getTag();
			final String packageNameTemp = sbnTemp.getPackageName();
			
			Log.d(TAG, String.format("getItemForId, idTemp=%d, tagTemp=%s, packageNameTemp=%s", idTemp, tagTemp, packageNameTemp));
			
			if(idTemp == id
					&& StringUtils.equals(tag, tagTemp)
					&& StringUtils.equals(packageName, packageNameTemp)
					)
			{
				Log.d(TAG, "getItemForId, 一致");
				return sbnTemp;
			}
		}
		Log.d(TAG, "getItemForId, 不一致");
		return null;
	}
	
//	public synchronized StatusBarNotification getItemForPostTime(final StatusBarNotification sbn)
//	{
//		final long postTime = sbn.getPostTime();
//		final int count = this.getCount();
//		for(int x = 0; x < count; x++)
//		{
//			final StatusBarNotification sbnTemp = 
//					this.getItem(x);
//			if(sbnTemp.getPostTime() == postTime)
//				return sbnTemp;
//		}
//		return null;
//	}
//	
//	public synchronized StatusBarNotification removeForPostTime(final StatusBarNotification sbn)
//	{
//		final StatusBarNotification sbnTemp = getItemForPostTime(sbn);
//		if(sbnTemp != null)
//			this.remove(sbnTemp);
//		return sbnTemp;
//	}
	
	public synchronized StatusBarNotification removeForId(final StatusBarNotification sbn)
	{
		Log.d(TAG, "removeForId, id="+sbn.getId());
		final StatusBarNotification sbnTemp = getItemForId(sbn);
		if(sbnTemp != null)
		{
			Log.d(TAG, "removeForId, 既存に同じ通知ありのため削除");
			Log.d(TAG, "removeForId, id="+sbnTemp.getId());
			
			this.remove(sbnTemp);
			this.sort(this.comparator);
		}
		return sbnTemp;
	}
}
