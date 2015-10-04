package jp.kght6123.smallappcommon.custom;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

public class AvoidDefectSwitchPreference extends SwitchPreference {

	public AvoidDefectSwitchPreference(Context context) {
		super(context);
	}

	public AvoidDefectSwitchPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public AvoidDefectSwitchPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onBindView(View view) {
		// Clean listener before invoke SwitchPreference.onBindView
		ViewGroup viewGroup= (ViewGroup)view;
		clearListenerInViewGroup(viewGroup);
		super.onBindView(view);
	}

	/**
	 * Clear listener in Switch for specify ViewGroup.
	 *
	 * @param viewGroup The ViewGroup that will need to clear the listener.
	 */
	private void clearListenerInViewGroup(ViewGroup viewGroup) {
		if (null == viewGroup) {
			return;
		}

		int count = viewGroup.getChildCount();
		for(int n = 0; n < count; ++n) {
			View childView = viewGroup.getChildAt(n);
			if(childView instanceof Switch) {
				final Switch switchView = (Switch) childView;
				switchView.setOnCheckedChangeListener(null);
				return;
			} else if (childView instanceof ViewGroup){
				ViewGroup childGroup = (ViewGroup)childView;
				clearListenerInViewGroup(childGroup);
			}
		}
	}

	
}
