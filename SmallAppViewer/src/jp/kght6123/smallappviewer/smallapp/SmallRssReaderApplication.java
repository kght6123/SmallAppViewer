package jp.kght6123.smallappviewer.smallapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import jp.kght6123.smallappcommon.listener.OnWindowFocusChangeTransportListener;
import jp.kght6123.smallappcommon.listener.OnWindowStateChangeTransportListener;
import jp.kght6123.smallappcommon.utils.PrefUtils;
import jp.kght6123.smallappcommon.utils.SmallApplicationUtils;
import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.adapter.RssItemArrayAdapter;
import jp.kght6123.smallappviewer.structure.RssItem;
import jp.kght6123.smallappviewer.task.RssParserAsyncTask;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallApplication;

/**
 * SmallRssReaderApplicationの本体
 * @author Hirotakas
 *
 */
public abstract class SmallRssReaderApplication extends SmallApplication
{
	private static final String TAG = SmallRssReaderApplication.class.getSimpleName();
	
	private View mMinimizedView;
	private ListView listForRssList;
	
	private RssItemArrayAdapter adapter;
	private List<RssItem> rssItemList = new ArrayList<RssItem>();
	
	private Spinner findMatomeDateSpinner;
	private Spinner findMatomeSortSpinner;
	
	protected abstract int getMainLayoutId();
	protected abstract int getMinimizedLayoutId();
	protected abstract int getHeaderLayoutId();
	protected abstract int getSpinnerItemLayoutId();
	protected abstract int getSpinnerDropDownItemLayoutId();
	protected abstract int getRssListViewId();
	protected abstract int getDateSpinnerViewId();
	protected abstract int getSortSpinnerViewId();
	protected abstract int getTitleStringId();
	protected abstract int getSelectedViewUrlKeyStringId();
	protected abstract int getViewUrlValueArrayId();
	protected abstract int getDateTimeLabelArrayId();
	protected abstract int getDateTimeValueArrayId();
	protected abstract int getSortLabelArrayId();
	protected abstract int getSortValueArrayId();
	protected abstract String getFindDateIndexPrefKey();
	protected abstract String getFindSortIndexPrefKey();
	
	@SuppressLint("InflateParams")
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		Log.i(TAG, "onCreate");
		
		/* Set the layout of the application */
		final View window = 
				SmallApplicationUtils.setContentViewAndGetSmallApplicationWindowView(this, getMainLayoutId());
		
		/*
		 * Set the layout displayed when the application is minimized.
		 */
		this.mMinimizedView = LayoutInflater.from(this).inflate(getMinimizedLayoutId(), null);
		setMinimizedView(this.mMinimizedView);
		
		this.adapter = new RssItemArrayAdapter(this, this.rssItemList);
		this.listForRssList = (ListView)findViewById(getRssListViewId());
		
		/* Set the title of the application to be displayed in the titlebar */
		setTitle(getTitleStringId());
		
		final SmallAppWindow.Attributes attr = getWindow().getAttributes();
		
		/* Set the requested width of the application */
		attr.width = getResources().getDimensionPixelSize(R.dimen.width);
		/* Set the requested height of the application */
		attr.height = getResources().getDimensionPixelSize(R.dimen.height);
		
		/* Use this flag to make the application window resizable */
		attr.flags |= SmallAppWindow.Attributes.FLAG_RESIZABLE;
		/* Use this flag to remove the titlebar from the window */
//		attr.flags |= SmallAppWindow.Attributes.FLAG_NO_TITLEBAR;
		/* Use this flag to enable hardware accelerated rendering */
		attr.flags |= SmallAppWindow.Attributes.FLAG_HARDWARE_ACCELERATED;
		
		/* Set the window attributes to apply the changes above */
		getWindow().setAttributes(attr);
		
		/**
		 * 全体のテーマ、DarkかLightが使える。
		 */
		getWindow().setWindowTheme(com.sony.smallapp.R.style.Theme.Light);
		final OnWindowFocusChangeTransportListener onTransport = 
				new OnWindowFocusChangeTransportListener(this.listForRssList, window, this);
		getWindow().setOnWindowFocusChangeListener(onTransport);
		getWindow().setOnWindowStateChangeListener(new OnWindowStateChangeTransportListener(onTransport, window, this));
		
		// メニューを設定
		setupOptionMenu();
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		Log.i(TAG, "onStart");
		
		if (SdkInfo.VERSION.API_LEVEL < 2) 
		{
			Toast.makeText(this, R.string.api_not_supported,
					Toast.LENGTH_SHORT).show();
			return;
		}
	}
	
	private enum CalenderValueKey
	{
		min,
		hour,
	}
	
	private void refresh(final CalenderValueKey calKey, final int calValue, final RssParserAsyncTask.SortValueKey sortKey)
	{
		Log.d(TAG, "Refresh");
		
		// 有効な記事一覧を取得
		final Set<String> urls = 
				PrefUtils.getDefaultStringArray(getSelectedViewUrlKeyStringId(), getViewUrlValueArrayId(), this.getApplicationContext());
		
		// 一日前のDateを取得する
		final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+09:00"), Locale.JAPAN);
		switch(calKey)
		{
		case hour :
			cal.set(Calendar.HOUR, cal.get(Calendar.HOUR)-calValue);
			break;
		case min :
			cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE)-calValue);
			break;
		}
		
		// 非同期での記事の取得を実行
		final RssParserAsyncTask.Controller controller = 
				new RssParserAsyncTask.Controller
				(
					this.adapter,
					(ProgressBar)findViewById(R.id.progressBarForRssList),
					cal.getTime(),
					this.listForRssList, urls.size(),
					sortKey
				);
		
		// 現在、表示されている記事を全消去
		this.adapter.clear();
		
		for(final String url : urls)
		{// 記事の取得処理を並列実行
			final RssParserAsyncTask task = 
					new RssParserAsyncTask(controller);
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
		}
		
		// 記事が取得できなかったときのメッセージを非表示化
		final TextView emptyForRssList = (TextView)findViewById(R.id.emptyForRssList);
		emptyForRssList.setVisibility(View.GONE);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	@SuppressLint("InflateParams")
	private void setupOptionMenu()
	{
		final View header = 
				LayoutInflater.from(this).inflate(getHeaderLayoutId(), null);// リソースからViewを取り出す
		
		this.findMatomeDateSpinner = (Spinner) header.findViewById(getDateSpinnerViewId());
		this.findMatomeSortSpinner = (Spinner) header.findViewById(getSortSpinnerViewId());
		{
			final ArrayAdapter<CharSequence> adapter = 
					ArrayAdapter.createFromResource(this, getDateTimeLabelArrayId(), getSpinnerItemLayoutId());
			adapter.setDropDownViewResource(getSpinnerDropDownItemLayoutId());
			this.findMatomeDateSpinner.setAdapter(adapter);
			this.findMatomeDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
				
				@Override
				public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id)
				{
					PrefUtils.setInt(getFindDateIndexPrefKey(), position, SmallRssReaderApplication.this.getClass(), SmallRssReaderApplication.this.getApplicationContext());
					
					final String[] values = 
							getResources().getStringArray(getDateTimeValueArrayId());
					final String[] keyValues = values[position].split(",");
					
					final String[] sortValues = 
							getResources().getStringArray(getSortValueArrayId());
					final RssParserAsyncTask.SortValueKey key = 
							RssParserAsyncTask.SortValueKey.valueOf(sortValues[findMatomeSortSpinner.getSelectedItemPosition()]);
					
					refresh(CalenderValueKey.valueOf(keyValues[0]), Integer.parseInt(keyValues[1]), key);
				}
				@Override
				public void onNothingSelected(final AdapterView<?> parent){}
				
			});
		}
		{
			final ArrayAdapter<CharSequence> adapter = 
					ArrayAdapter.createFromResource(this, getSortLabelArrayId(), getSpinnerItemLayoutId());
			adapter.setDropDownViewResource(getSpinnerDropDownItemLayoutId());
			this.findMatomeSortSpinner.setAdapter(adapter);
			this.findMatomeSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
				
				@Override
				public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id)
				{
					PrefUtils.setInt(getFindSortIndexPrefKey(), position, SmallRssReaderApplication.this.getClass(), SmallRssReaderApplication.this.getApplicationContext());
					
					final String[] sortValues = 
							getResources().getStringArray(getSortValueArrayId());
					final RssParserAsyncTask.SortValueKey key = RssParserAsyncTask.SortValueKey.valueOf(sortValues[position]);
					
					SmallRssReaderApplication.this.adapter.sort(key.comparator);
				}
				@Override
				public void onNothingSelected(final AdapterView<?> parent){}
				
			});
		}
		this.findMatomeDateSpinner.setSelection(PrefUtils.getInt(getFindDateIndexPrefKey(), 0, this.getClass(), this.getApplicationContext()));
		this.findMatomeSortSpinner.setSelection(PrefUtils.getInt(getFindSortIndexPrefKey(), 0, this.getClass(), this.getApplicationContext()));
		
		getWindow().setHeaderView(header);
	}
}