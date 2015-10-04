package jp.kght6123.smallappviewer.service;

import jp.kght6123.smallappcommon.application.SharedDataApplication;
import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.smallapp.SmallClipboardApplication;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppNotFoundException;
import com.sony.smallapp.SmallApplicationManager;

/**
 * 【未完成】クリップボードの内容を収集するサービス
 * 
 * @author Hirotaka
 *
 */
public class ClipboardListenerService extends Service
{
	private static final String TAG = ClipboardListenerService.class.getSimpleName();
	
	private SavedPrimaryClipChanged savedPrimaryClipChanged = null;
	
	@Override
	public IBinder onBind(Intent intent)
	{
		Log.d(TAG, "ClipboardListenerService.onBind");
		return null;
	}

	@Override
	public void onCreate()
	{
		Log.d(TAG, "ClipboardListenerService.onCreate");
		
		final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		
		if(this.savedPrimaryClipChanged == null)
			this.savedPrimaryClipChanged = new SavedPrimaryClipChanged();
		
		//PrimaryClipChangedListenerの追加
		//cm.removePrimaryClipChangedListener(this.savedPrimaryClipChanged);
		cm.addPrimaryClipChangedListener(this.savedPrimaryClipChanged);
		
		super.onCreate();
	}
	
	private class SavedPrimaryClipChanged implements OnPrimaryClipChangedListener
	{
		@Override
		public synchronized  void onPrimaryClipChanged()
		{
			Log.d(TAG, "ClipboardListenerService.savedPrimaryClipChanged.onPrimaryClipChanged");
			
			final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			
			((SharedDataApplication) ClipboardListenerService.this.getApplication())
				.putClipData(cm.getPrimaryClip());
			
			startSmallNotificationApplicationView();
		}
	}
	
	@Override
	public void onDestroy()
	{
		Log.d(TAG, "ClipboardListenerService.onDestroy");
		
		final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		cm.removePrimaryClipChangedListener(this.savedPrimaryClipChanged);
		
		super.onDestroy();
	}
	
	private void startSmallNotificationApplicationView(/*final WindowState state*/)
	{
		if (SdkInfo.VERSION.API_LEVEL >= 2)
		{
			final Intent intent = new Intent(this, SmallClipboardApplication.class);
			//intent.putExtra(SmallClipboardApplication.EXTRA_WINDOW_STATE, state.name());
			
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
