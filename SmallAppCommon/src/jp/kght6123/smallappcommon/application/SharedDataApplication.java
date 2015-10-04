package jp.kght6123.smallappcommon.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.graphics.Bitmap;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * 通知データとクリップボードデータを機能間で共有するためのApplication
 * （Bundleにそのままputするとエラーとなるため）
 * @author Hirotaka
 *
 */
public class SharedDataApplication extends Application
{
	private final String TAG = SharedDataApplication.class.getSimpleName();
	
	private StatusBarNotification[] statusBarNotifications;
	//private List<StatusBarNotification> statusBarNotificationTempList = Collections.synchronizedList(new LinkedList<StatusBarNotification>());
	private StatusBarNotification statusBarNotificationTemp = null;
	private final List<ClipData> clipDataList = Collections.synchronizedList(new ArrayList<ClipData>());
	
	private final ArrayList<WebHistoryItem> webHistoryItemList = new ArrayList<WebHistoryItem>();
	private final ArrayList<WebHistoryItem> webPinItemList = new ArrayList<WebHistoryItem>();
	
	@Override
	public void onCreate() {
		/** Called when the Application-class is first created. */
		Log.v(TAG,"--- onCreate() in ---");
	}
	
	@Override
	public void onTerminate() {
		/** This Method Called when this Application finished. */
		Log.v(TAG,"--- onTerminate() in ---");
	}
	
	public StatusBarNotification[] getStatusBarNotifications() {
		return statusBarNotifications;
	}
	public void setStatusBarNotifications(
			StatusBarNotification[] statusBarNotifications) {
		this.statusBarNotifications = statusBarNotifications;
	}
	
	public StatusBarNotification popStatusBarNotification() {
		return this.statusBarNotificationTemp;
		//return this.statusBarNotificationTempList.size() > 0 ? this.statusBarNotificationTempList.remove(0) : null;
	}
	public void pushStatusBarNotification(StatusBarNotification statusBarNotification) {
		this.statusBarNotificationTemp = statusBarNotification;
		//this.statusBarNotificationTempList.add(statusBarNotification);
	}
	
	public void putClipData(final ClipData primaryClip)
	{
		if(primaryClip != null
				&& primaryClip.getItemCount() > 0
				&& 
				(primaryClip.getItemAt(0).getText() != null
				|| primaryClip.getItemAt(0).getHtmlText() != null
				|| primaryClip.getItemAt(0).getIntent() != null
				|| primaryClip.getItemAt(0).getUri() != null
				)
			)
			this.clipDataList.add(primaryClip);
	}
	public List<ClipDataItem> getClipDataItemList()
	{
		final List<ClipDataItem> clipDataItemList = 
				new ArrayList<ClipDataItem>();
		
		int index = 1;
		for(final ClipData clipData : this.clipDataList)
		{
//			final int itemConut = clipData.getItemCount();
//			for(int index = 0; index < itemConut; index++)
//			{
//				final Item item = clipData.getItemAt(index);
//				clipDataItemList.add(new ClipDataItem(clipData, item));
//			}
			clipDataItemList.add(new ClipDataItem(index, clipData, clipData.getItemAt(0)));
			index++;
		}
		return clipDataItemList;
	}
	
	public ArrayList<WebHistoryItem> getWebHistoryItemList() {
		return webHistoryItemList;
	}
	public void addWebHistoryItemList(final WebHistoryItem item) {
		webHistoryItemList.remove(item);
		webHistoryItemList.add(0, item);
	}
	
	public ArrayList<WebHistoryItem> getWebPinItemList() {
		return webPinItemList;
	}
	public void addWebPinItemList(final WebHistoryItem item) {
		webPinItemList.remove(item);
		webPinItemList.add(0, item);
	}
	public void removeWebPinItemList(final WebHistoryItem item) {
		webPinItemList.remove(item);
	}
	
	public class ClipDataItem
	{
		private final int index;
		private final ClipData clipData;
		private final ClipData.Item item;
		
		public ClipDataItem(final int index, final ClipData clipData, final Item item)
		{
			super();
			this.index = index;
			this.clipData = clipData;
			this.item = item;
		}
		public ClipData getClipData()
		{
			return clipData;
		}
		public ClipData.Item getItem()
		{
			return item;
		}
		public int getIndex()
		{
			return index;
		}
	}
	
	public static class WebHistoryItem implements Comparable<WebHistoryItem>
	{
		private String url;
		private String title;
//		private Bitmap favicon;
		
		public WebHistoryItem(final String url, final String title, final Bitmap favicon)
		{
			super();
			this.url = url;
			this.title = title;
//			this.favicon = favicon;
		}
		public String getUrl() {
			return url;
		}
		public String getTitle() {
			return title;
		}
//		public Bitmap getFavicon() {
//			return favicon;
//		}
		public void setUrl(String url) {
			this.url = url;
		}
		public void setTitle(String title) {
			this.title = title;
		}
//		public void setFavicon(Bitmap favicon) {
//			this.favicon = favicon;
//		}
		@Override
		public int compareTo(WebHistoryItem another)
		{
			return this.url.compareTo(another.url);
		}
		@Override
		public boolean equals(Object o)
		{
			return this.compareTo((WebHistoryItem)o) == 0;
		}
	}
}
