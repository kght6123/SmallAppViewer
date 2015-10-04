package jp.kght6123.smallappviewer.activity;

import jp.kght6123.smallappcommon.activity.BaseDelegatorActivity;
import jp.kght6123.smallappviewer.smallapp.SmallNewsReaderApplication;

import com.sony.smallapp.SmallApplication;

/**
 * SmallRssReaderApplication‚ðŠJ‚­‚½‚ß‚ÌActivity
 * 
 * @author Hirotaka
 *
 */
public class NewsActionDelegatorActivity extends BaseDelegatorActivity
{
	@Override
	public Class<? extends SmallApplication> getDelegateSmallApplicationClass()
	{
		return SmallNewsReaderApplication.class;
	}
}
