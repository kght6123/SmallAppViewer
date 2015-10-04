package jp.kght6123.smallappwebcameraviewer.activity;

import jp.kght6123.smallappcommon.activity.BaseDelegatorActivity;
import jp.kght6123.smallappwebcameraviewer.smallapp.SmallWebCameraViewApplication;

import com.sony.smallapp.SmallApplication;

/**
 * Intent���󂯎����SmallWebCameraViewApplication�ɓn�����߂�Activity
 * 
 * @author Hirotaka
 *
 */
public class WebCameraViewActionDelegatorActivity extends BaseDelegatorActivity
{
	@Override
	public Class<? extends SmallApplication> getDelegateSmallApplicationClass()
	{
		return SmallWebCameraViewApplication.class;
	}
}
