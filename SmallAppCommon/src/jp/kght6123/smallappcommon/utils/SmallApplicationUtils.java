package jp.kght6123.smallappcommon.utils;

import jp.kght6123.smallappcommon.R;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;

import com.sony.smallapp.SmallApplication;

public class SmallApplicationUtils
{
	private static final String TAG = SmallApplicationUtils.class.getSimpleName();
	
	public static View setContentViewAndGetSmallApplicationWindowView(final SmallApplication application, final int contentViewId)
	{
		final View mainView = LayoutInflater.from(application).inflate(contentViewId, null);
		application.setContentView(mainView);
		
		final boolean semialphaview = 
				PrefUtils.getDefaultBoolean(R.string.special_semialpha_view_key, true, application);
		
		View returnView = null;
		if(semialphaview)
		{
			ViewParent viewParent = mainView.getParent();
			while (viewParent instanceof View)
			{
				final View view = (View)viewParent;
				final String name = view.getClass().getSimpleName();
				Log.d(TAG, "simpleName = " + name);
				Log.d(TAG, "\talpha = " + view.getAlpha());
				if(view.getBackground() != null)
				{
					Log.d(TAG, "\tbackground.alpha = " + view.getBackground().getAlpha());
					
					if(name.equals("RelativeLayout"))
					{
						Log.d(TAG, "\t return View");
						returnView = view;
						//view.setBackgroundColor(0x00ffffff);
						//view.setAlpha(0.3f);
					}
				}
				else
				{
					Log.d(TAG, "\tBackground = null");
					
				}
				viewParent = view.getParent();
			}
		}
		return returnView;
	}
}
