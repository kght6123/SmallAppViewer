package jp.kght6123.smallappcommon.listener;

import jp.kght6123.smallappcommon.R;
import jp.kght6123.smallappcommon.utils.PrefUtils;
import android.content.Context;
import android.view.View;

import com.sony.smallapp.SmallAppWindow.OnWindowStateChangeListener;
import com.sony.smallapp.SmallAppWindow.WindowState;

/**
 * SmallApplicationの最小化時に透明にする共通リスナークラス
 * 
 * @author Hirotaka
 *
 */
public class OnWindowStateChangeTransportListener implements OnWindowStateChangeListener
{
	private final OnWindowFocusChangeTransportListener focusChangeTransListener;
	private final View window;
	private boolean enable = true;
	
	private float miniWindowAlpha;
	
	public OnWindowStateChangeTransportListener(final OnWindowFocusChangeTransportListener focusChangeTransListener, final View window, final Context context)
	{
		super();
		this.miniWindowAlpha = PrefUtils.getDefaultPercent(R.string.special_mini_semialpha_value_view_key, 0.50f, context);
		
		this.focusChangeTransListener = focusChangeTransListener;
		this.window = window;
	}
	
	public void setEnable(boolean enable)
	{
		this.enable = enable;
	}
	
	@Override
	public void onWindowStateChanged(final WindowState state)
	{
		if(enable == false)
			return;
		
		switch(state)
		{
		case NORMAL:
		case FITTED:
			focusChangeTransListener.setEnable(true);
			focusChangeTransListener.onWindowFocusChanged(true);
			break;
		case MINIMIZED:
			focusChangeTransListener.setEnable(false);
			//window.setBackgroundColor(Color.argb(255, 255, 255, 255));
			window.setAlpha(this.miniWindowAlpha);
			break;
		}
	}
}
