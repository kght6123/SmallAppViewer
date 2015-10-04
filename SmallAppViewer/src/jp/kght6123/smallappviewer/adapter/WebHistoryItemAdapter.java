package jp.kght6123.smallappviewer.adapter;

import java.util.ArrayList;
import java.util.Map;

import jp.kght6123.smallappcommon.application.SharedDataApplication;
import jp.kght6123.smallappcommon.application.SharedDataApplication.WebHistoryItem;
import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.smallapp.SmallBrowserApplication;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Web履歴アイテムデータをListViewで表示するためのAdapter
 * @author Hirotaka
 *
 */
public class WebHistoryItemAdapter extends ArrayAdapter<WebHistoryItem>
{
	private final SmallBrowserApplication application;
	private final Map<String, String> additionalHttpHeaders;
	private final SharedDataApplication sharedData;
	private final boolean history;
	
	public WebHistoryItemAdapter(final SmallBrowserApplication application, final int textViewResourceId, final ArrayList<WebHistoryItem> objects, final Map<String, String> additionalHttpHeaders, final boolean history)
	{
		super(application, textViewResourceId, objects);
		this.application = application;
		this.additionalHttpHeaders = additionalHttpHeaders;
		this.history = history;
		
		this.sharedData = ((SharedDataApplication)application.getApplicationContext());
	}

	@SuppressLint({ "ViewHolder", "InflateParams" })
	@Override
	public View getView(final int position, View convertView, final ViewGroup parent)
	{
		// 特定の行(position)のデータを得る
		final WebHistoryItem item = (WebHistoryItem)getItem(position);
		
		final View listItem = 
				LayoutInflater.from(this.application).inflate(R.layout.smallapp_browser_listitem, null);
		{
//			final ImageView iv = (ImageView)listItem.findViewById(R.id.webPageFavicon);
//			iv.setImageBitmap(item.getFavicon());
			
			final TextView title = (TextView)listItem.findViewById(R.id.webPageTitle);
			title.setText(item.getTitle());
			
			final TextView url = (TextView)listItem.findViewById(R.id.webPageUrl);
			url.setText(item.getUrl());
			
			listItem.setClickable(true);
			listItem.setOnClickListener
			(
				new View.OnClickListener()
				{
					@Override
					public synchronized void onClick(View v)
					{
						final WebView webView = (WebView)WebHistoryItemAdapter.this.application.findViewById(R.id.webview);
						webView.loadUrl(item.getUrl(), additionalHttpHeaders);
					}
				}
			);
			
			listItem.setLongClickable(true);
			if(this.history)
				listItem.setOnLongClickListener
				(
					new View.OnLongClickListener()
					{
						@Override
						public synchronized boolean onLongClick(View v)
						{
							sharedData.addWebPinItemList(item);
							Toast.makeText(application, R.string.pin_completed, Toast.LENGTH_SHORT).show();
							return true;
						}
					}
				);
			else
				listItem.setOnLongClickListener
				(
					new View.OnLongClickListener()
					{
						@Override
						public synchronized boolean onLongClick(View v)
						{
							sharedData.removeWebPinItemList(item);
							remove(item);
							
							Toast.makeText(application, R.string.pin_deleted, Toast.LENGTH_SHORT).show();
							return true;
						}
					}
				);
			
		}
		return listItem;
	}
}
