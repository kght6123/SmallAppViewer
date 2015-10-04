package jp.kght6123.smallappcommon.activity;

import jp.kght6123.smallappcommon.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppNotFoundException;
import com.sony.smallapp.SmallApplication;
import com.sony.smallapp.SmallApplicationManager;

/**
 * Intent‚ðŽó‚¯Žæ‚Á‚ÄSmallApplication‚É“n‚·‚½‚ß‚ÌActivity
 * @author Hirotaka
 *
 */
public abstract class BaseDelegatorActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		if (SdkInfo.VERSION.API_LEVEL >= 2)
		{
			//Intent intent = new Intent(getIntent().getAction(), getIntent().getData(), this, getDelegateSmallApplicationClass());
			Intent intent = new Intent(getIntent());
			intent.setClass( this, getDelegateSmallApplicationClass());
			
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
		finish();
	}
	
	public abstract Class<? extends SmallApplication> getDelegateSmallApplicationClass();
	
}
