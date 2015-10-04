package jp.kght6123.smallappviewer.adapter;

import java.text.SimpleDateFormat;
import java.util.List;

import jp.kght6123.smallappcommon.utils.PrefUtils;
import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.smallapp.SmallBrowserApplication;
import jp.kght6123.smallappviewer.structure.RssItem;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sony.smallapp.SmallAppNotFoundException;
import com.sony.smallapp.SmallAppWindow.WindowState;
import com.sony.smallapp.SmallApplication;
import com.sony.smallapp.SmallApplicationManager;

public class RssItemArrayAdapter extends ArrayAdapter<RssItem>
{
	private LayoutInflater inflater;
	private final SmallApplication app;
	
	public RssItemArrayAdapter(final SmallApplication app, final List<RssItem> objects)
	{
		super(app.getApplicationContext(), 0, objects);
		this.inflater = (LayoutInflater) app.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.app = app;
	}
	
	//private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final ThreadLocal<SimpleDateFormat> SDF = new ThreadLocal<SimpleDateFormat>() {
			@Override
			protected SimpleDateFormat initialValue() {
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			}
		};
	
	// 1行ごとのビューを生成する
	@SuppressLint("InflateParams")
	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent)
	{
		final View view;
		if (convertView == null)
			view = this.inflater.inflate(R.layout.smallapp_rss_listview_item_row, null);
		else
			view = convertView;
		
		// 現在参照しているリストの位置からItemを取得する
		final RssItem item = this.getItem(position);
		if (item != null)
		{
			final TextView mTitle = (TextView) view.findViewById(R.id.rssItem_title);
			mTitle.setText(item.getTitle());
			
			final TextView mDescr = (TextView) view.findViewById(R.id.rssItem_descr);
			mDescr.setText(item.getDescription());
			
			final TextView mDate = (TextView) view.findViewById(R.id.rssItem_date);
			if(item.getDate() != null)
				mDate.setText(SDF.get().format(item.getDate()));
			
			final TextView mCrea = (TextView) view.findViewById(R.id.rssItem_creator);
			mCrea.setText(item.getName());
			
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					final boolean javaScriptDisabe = PrefUtils.getDefaultBoolean(R.string._2chmatome_javascript_key, false, RssItemArrayAdapter.this.getContext());
					final String browserMode = PrefUtils.getDefaultString(R.string._2chmatome_default_browser_key, "Default", RssItemArrayAdapter.this.getContext());
					
					if(browserMode.equals("SmallAppBrowser"))
					{
						final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink().toString()));
						intent.setClass(RssItemArrayAdapter.this.getContext(), SmallBrowserApplication.class);
						intent.putExtra(SmallBrowserApplication.EXTRA_JAVASCRIPT_DISABLED, javaScriptDisabe);
						try
						{
							SmallApplicationManager.startApplication(RssItemArrayAdapter.this.getContext(), intent);
						}
						catch (SmallAppNotFoundException e)
						{
							Toast.makeText(RssItemArrayAdapter.this.getContext(), e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}
					}
//					else if(browserMode.equals("SmallAppMultiBrowser"))
//					{
//						final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink().toString()), RssItemArrayAdapter.this.getContext(), HttpMultiActionDelegatorActivity.class);
//						intent.addCategory(Intent.CATEGORY_DEFAULT);
//						intent.addCategory(Intent.CATEGORY_BROWSABLE);
//						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						
//						RssItemArrayAdapter.this.getContext().startActivity(intent);
//						
//						app.getWindow().setWindowState(WindowState.MINIMIZED);
//					}
					else
					{
						final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink().toString()));
						intent.addCategory(Intent.CATEGORY_DEFAULT);
						intent.addCategory(Intent.CATEGORY_BROWSABLE);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						
						RssItemArrayAdapter.this.getContext().startActivity(intent);
						
						app.getWindow().setWindowState(WindowState.MINIMIZED);
					}
				}
			});
		}
		return view;
	}
}