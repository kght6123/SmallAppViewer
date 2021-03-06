package jp.kght6123.smallappimageviewer.activity;

import jp.kght6123.smallappcommon.activity.BaseDelegatorActivity;
import jp.kght6123.smallappimageviewer.smallapp.SmallImageViewApplication;

import com.sony.smallapp.SmallApplication;

/**
 * 画像共有のIntentを受け取ってSmallImageViewApplicationに渡すためのActivity
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
