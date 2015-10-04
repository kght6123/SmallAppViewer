package jp.kght6123.smallappimageviewer.activity;

import jp.kght6123.smallappcommon.activity.BaseDelegatorActivity;
import jp.kght6123.smallappimageviewer.smallapp.SmallImageViewApplication;

import com.sony.smallapp.SmallApplication;

/**
 * ‰æ‘œ‹¤—L‚ÌIntent‚ðŽó‚¯Žæ‚Á‚ÄSmallImageViewApplication‚É“n‚·‚½‚ß‚ÌActivity
 * 
 * @author Hirotaka
 *
 */
public class ImageViewActionDelegatorActivity extends BaseDelegatorActivity
{
	@Override
	public Class<? extends SmallApplication> getDelegateSmallApplicationClass()
	{
		return SmallImageViewApplication.class;
	}
}
