package jp.kght6123.smallappcommon.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * 横にもスクロールするグリッドビュー
 * @author Hirotaka
 *
 */
public class ExpandableHeightGridView extends GridView
{
	private boolean expanded = false;
	
	public ExpandableHeightGridView(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
	}
	public ExpandableHeightGridView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
	}
	public ExpandableHeightGridView(final Context context)
	{
		super(context);
	}
	
	public boolean isExpanded()
	{
		return expanded;
	}
	
	@Override
	public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		// HACK! TAKE THAT ANDROID!
		if (isExpanded())
		{
			// Calculate entire height by providing a very large height hint.
			// View.MEASURED_SIZE_MASK represents the largest height possible.
			final int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
					MeasureSpec.AT_MOST);
			super.onMeasure(widthMeasureSpec, expandSpec);
			
			ViewGroup.LayoutParams params = getLayoutParams();
			params.height = getMeasuredHeight();
		}
		else
		{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	public void setExpanded(final boolean expanded)
	{
		this.expanded = expanded;
	}
}
