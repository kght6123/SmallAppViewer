package jp.kght6123.smallappviewer.smallapp;

import jp.kght6123.smallappviewer.R;


/**
 * ２ちゃんまとめアプリケーション
 * 
 * @author Hirotaka
 *
 */
public class SmallMatomeReaderApplication extends SmallRssReaderApplication
{
	protected int getMainLayoutId() {
		return R.layout.smallapp_rss_listview_main;
	}
	
	protected int getMinimizedLayoutId()
	{
		return R.layout.smallapp_rss_listview_minimized;
	}
	
	protected int getHeaderLayoutId()
	{
		return R.layout.smallapp_rss_listview_header;
	}
	
	protected int getSpinnerItemLayoutId()
	{
		return R.layout.smallapp_spinner_item;
	}
	
	protected int getSpinnerDropDownItemLayoutId()
	{
		return R.layout.smallapp_spinner_drop_down_item;
	}
	
	protected int getRssListViewId()
	{
		return R.id.listForRssList;
	}
	
	protected int getDateSpinnerViewId()
	{
		return R.id.findMatomeDateSpinner;
	}
	
	protected int getSortSpinnerViewId()
	{
		return R.id.findMatomeSortSpinner;
	}
	
	protected int getTitleStringId()
	{
		return R.string.app_2chmatome_name;
	}
	
	protected int getSelectedViewUrlKeyStringId()
	{
		return R.string._2chmatome_select_viewurl_key;
	}
	
	protected int getViewUrlValueArrayId()
	{
		return R.array._2chmatome_viewurl_value_list;
	}
	
	protected int getDateTimeLabelArrayId()
	{
		return R.array._2chmatome_datetime_label_list;
	}
	
	protected int getDateTimeValueArrayId()
	{
		return R.array._2chmatome_datetime_value_list;
	}
	
	protected int getSortLabelArrayId()
	{
		return R.array._2chmatome_sort_label_list;
	}
	
	protected int getSortValueArrayId()
	{
		return R.array._2chmatome_sort_value_list;
	}
	
	protected String getFindDateIndexPrefKey()
	{
		return "findMatomeDateIndex";
	}
	
	protected String getFindSortIndexPrefKey()
	{
		return "findMatomeSortIndex";
	}
	
}