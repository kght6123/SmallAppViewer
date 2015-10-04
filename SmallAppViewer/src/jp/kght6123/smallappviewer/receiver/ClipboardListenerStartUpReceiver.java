package jp.kght6123.smallappviewer.receiver;

import jp.kght6123.smallappviewer.service.ClipboardListenerService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 【未完成】クリップボードの内容を収集するサービスを起動する
 * @author Hirotaka
 *
 */
public class ClipboardListenerStartUpReceiver extends BroadcastReceiver
{
	private static final String TAG = ClipboardListenerStartUpReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		Log.d(TAG, "ClipboardListenerStartUpReceiver.onReceive");
		Log.d(TAG, "ClipboardListenerStartUpReceiver.onReceive:intent.action="+intent.getAction());
		Log.d(TAG, "ClipboardListenerStartUpReceiver.onReceive:intent.data="+intent.getDataString());
		
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
				|| 
				(intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)
				&& intent.getDataString().equals("package:jp.kght6123.smallappviewer"))
				||
				(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)
						&& intent.getDataString().equals("package:jp.kght6123.smallappviewer"))
				||
				(intent.getAction().equals(Intent.ACTION_PACKAGE_FIRST_LAUNCH)
						&& intent.getDataString().equals("package:jp.kght6123.smallappviewer"))
				||
				(intent.getAction().equals(Intent.ACTION_PACKAGE_RESTARTED)
						&& intent.getDataString().equals("package:jp.kght6123.smallappviewer"))
				||
				(intent.getAction().equals(Intent.ACTION_PACKAGE_CHANGED)
						&& intent.getDataString().equals("package:jp.kght6123.smallappviewer"))
			)
		{
			Log.d(TAG, "ClipboardListenerStartUpReceiver.onReceive start service.");
			context.startService(new Intent(context, ClipboardListenerService.class));
		}
	}

}
