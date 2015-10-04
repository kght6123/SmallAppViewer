package jp.kght6123.smallappviewer.activity;

import jp.kght6123.smallappcommon.utils.PrefUtils;
import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.dialog.SmallMultiBrowserPositionDialogFragment;
import jp.kght6123.smallappviewer.dialog.SmallMultiBrowserSplitDialogFragment;
import jp.kght6123.smallappviewer.smallapp.SmallBrowserApplication;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppNotFoundException;
import com.sony.smallapp.SmallApplicationManager;

/**
 * HTTP‘—M‚ÌIntent‚ðŽó‚¯Žæ‚Á‚ÄSmallBrowserApplication‚É“n‚·‚½‚ß‚ÌActivity
 * 
 * @author Hirotaka
 *
 */
public class HttpActionDelegatorActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		if (SdkInfo.VERSION.API_LEVEL >= 2)
		{
			final boolean multibrowser = 
					PrefUtils.getDefaultBoolean(R.string.multibrowser_enable_key, false, this);
			
			if(multibrowser)
			{
				if(SmallMultiBrowserSplitDialogFragment.getSplitMode(this) == -1)
				{
					final SmallMultiBrowserSplitDialogFragment fragment = new SmallMultiBrowserSplitDialogFragment();
					fragment.show(getFragmentManager(), "split_select_dialog");
				}
				else
				{
					final SmallMultiBrowserPositionDialogFragment fragment = new SmallMultiBrowserPositionDialogFragment();
					fragment.show(getFragmentManager(), "position_select_dialog");
				}
			}
			else
			{
				Intent intent = new Intent(getIntent());
				intent.setClass(this, SmallBrowserApplication.class);
				
				try
				{
					SmallApplicationManager.startApplication(this, intent);
				}
				catch (SmallAppNotFoundException e)
				{
					Toast.makeText(this, e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
				finish();
			}
		}
		else
		{
			Toast.makeText(this, R.string.api_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
		}
		
	}
}
