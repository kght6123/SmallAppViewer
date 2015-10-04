package jp.kght6123.smallappviewer.activity;

import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.dialog.SmallMultiBrowserPositionDialogFragment;
import jp.kght6123.smallappviewer.dialog.SmallMultiBrowserSplitDialogFragment;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.sony.smallapp.SdkInfo;

/**
 * Intent‚ðŽó‚¯Žæ‚Á‚ÄSmallMultiBrowserApplication‚É“n‚·‚½‚ß‚ÌActivity
 * @author Hirotaka
 *
 */
public class HttpMultiActionDelegatorActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		if (SdkInfo.VERSION.API_LEVEL >= 2)
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
			Toast.makeText(this, R.string.api_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}
}
