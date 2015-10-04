package jp.kght6123.smallappviewer.activity;

import jp.kght6123.smallappcommon.activity.BaseDelegatorActivity;
import jp.kght6123.smallappviewer.smallapp.SmallMatomeReaderApplication;

import com.sony.smallapp.SmallApplication;

/**
 * SmallRssReaderApplication���J�����߂�Activity
 * 
 * @author Hirotaka
 *
 */
public class MatomeActionDelegatorActivity extends BaseDelegatorActivity
{
	@Override
	public Class<? extends SmallApplication> getDelegateSmallApplicationClass()
	{
		return SmallMatomeReaderApplication.class;
	}
}
