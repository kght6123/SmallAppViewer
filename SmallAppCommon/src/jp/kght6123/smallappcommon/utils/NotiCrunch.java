package jp.kght6123.smallappcommon.utils;

import java.util.ArrayList;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * 【ほぼ未使用】通知オブジェクトを文字列に変換する
 * @author Hirotaka
 *
 */
public class NotiCrunch
{
	private static String TAG = "NotiCrunch";
	private static final int TIMESTAMPID = 16908388;
	
	private static <V extends View> void extractViewType(ArrayList<View> outViews, Class<V> viewtype, View source)
	{
		if (ViewGroup.class.isInstance(source)) {
			ViewGroup vg = (ViewGroup) source;
			for (int i = 0; i < vg.getChildCount(); i++) {
				extractViewType(outViews, viewtype, vg.getChildAt(i));

			}
		} else if(viewtype.isInstance(source)) {
			outViews.add(source);
		}
	}
	
	public static String extractTextFromNotification(Context context, Notification notification)
	{
		ArrayList<String> result = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			result = extractTextFromNotification(context, notification.bigContentView);
			Log.d(TAG, "It tried Big View");
			Log.d(TAG, "Ticker:" + notification.tickerText);
		}
		if (result == null)
		{
			result = extractTextFromNotification(context, notification.contentView);
			Log.d(TAG, "It tried little view");
		}
		if (result == null)
		{
			result = extractTextFromNotification(context, notification.tickerView);
			Log.d(TAG, "It tried ticker view");
		}
		if (result == null && notification.tickerText != null)
		{
			Log.d(TAG, "It is returning tickerText");
			return notification.tickerText.toString();
		}
		else if (result == null && notification.tickerText == null)
		{
			Log.d(TAG, "It is returning null");
			return "";
		}
		return TextUtils.join("\n", result);
	}
	
	private static ArrayList<String> extractTextFromNotification(Context context, RemoteViews view)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ArrayList<String> result = new ArrayList<String>();
		
		if (view == null)
		{
			Log.d(TAG, "Initial view is Empty");
			return null;
		}
		
		try
		{
			ViewGroup localView = (ViewGroup) inflater.inflate(view.getLayoutId(), null);
			view.reapply(context, localView);
			
			ArrayList<View> outViews = new ArrayList<View>();
			extractViewType(outViews, TextView.class, localView);	// TextViewのみを取得
			
			for (View  ttv: outViews)
			{
				TextView tv = (TextView) ttv;
				String txt = tv.getText().toString();
				if (!TextUtils.isEmpty(txt) && tv.getId() != TIMESTAMPID)
					result.add(txt);
			}
		}
		catch (Exception e)
		{
			Log.d(TAG, "FAILED to load notification! " + e.toString());
			Log.wtf(TAG, e);
			return null;
		}
		Log.d(TAG, "Return result" + result);
		return result;
	}
}
