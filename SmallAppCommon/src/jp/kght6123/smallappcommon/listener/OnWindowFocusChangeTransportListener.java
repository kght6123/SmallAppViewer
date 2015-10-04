package jp.kght6123.smallappcommon.listener;

import java.util.ArrayList;
import java.util.List;

import jp.kght6123.smallappcommon.R;
import jp.kght6123.smallappcommon.utils.PrefUtils;
import android.content.Context;
import android.view.View;

import com.sony.smallapp.SmallAppWindow.OnWindowFocusChangeListener;

/**
 * SmallApplicationからフォーカスが離れたら、対象のViewを半透明にする共通リスナークラス
 * 
 * @author Hirotaka
 *
 */
public class OnWindowFocusChangeTransportListener implements OnWindowFocusChangeListener
{
	private final float focusAlpha;
	private final float notFocusAlpha;
	
	private final float focusWindowAlpha;
	private final float notFocusWindowAlpha;
	
	private final List<View> viewList =new ArrayList<View>();
	private final View window;
	
	private boolean enable = true;
	
	public OnWindowFocusChangeTransportListener(final View window, final Context context)
	{
		super();
		this.focusAlpha = PrefUtils.getDefaultPercent(R.string.special_focus_alpha_value_view_key, 0.99f, context);
		this.notFocusAlpha = PrefUtils.getDefaultPercent(R.string.special_unfocus_alpha_value_view_key, 0.30f, context);
		
		this.focusWindowAlpha = PrefUtils.getDefaultPercent(R.string.special_focus_semialpha_value_view_key, 0.90f, context);
		this.notFocusWindowAlpha = PrefUtils.getDefaultPercent(R.string.special_unfocus_semialpha_value_view_key, 0.40f, context);
		
		this.window = window;
//		re();
	}
	public OnWindowFocusChangeTransportListener(final View view, final View window, final Context context)
	{
		super();
		this.focusAlpha = PrefUtils.getDefaultPercent(R.string.special_focus_alpha_value_view_key, 0.99f, context);
		this.notFocusAlpha = PrefUtils.getDefaultPercent(R.string.special_unfocus_alpha_value_view_key, 0.30f, context);
		
		this.focusWindowAlpha = PrefUtils.getDefaultPercent(R.string.special_focus_semialpha_value_view_key, 0.90f, context);
		this.notFocusWindowAlpha = PrefUtils.getDefaultPercent(R.string.special_unfocus_semialpha_value_view_key, 0.40f, context);
		
		this.viewList.add(view);
		this.window = window;
//		re();
	}
	
//	private void re()
//	{
//		// 一度再設定しないと、WebViewの時に裏に変なViewが出現するため
//		onWindowFocusChanged(false);
//		onWindowFocusChanged(true);
//	}
	
	public void add(final View view)
	{
		this.viewList.add(view);
	}
	
	public void setEnable(boolean enable)
	{
		this.enable = enable;
	}
	
	@Override
	public synchronized void onWindowFocusChanged(final boolean hasFocus)
	{
		if(enable == false)
			return;
		
		if(hasFocus)
		{
			for(final View view : viewList)
				view.setAlpha(this.focusAlpha);
			if(this.window != null)
				window.setAlpha(this.focusWindowAlpha);
		}
		else
		{
			for(final View view : viewList)
				view.setAlpha(this.notFocusAlpha);
			if(this.window != null)
				window.setAlpha(this.notFocusWindowAlpha);
		}
	}
}
