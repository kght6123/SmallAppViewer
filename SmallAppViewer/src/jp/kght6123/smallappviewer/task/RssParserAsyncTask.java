package jp.kght6123.smallappviewer.task;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jp.kght6123.smallappcommon.utils.StringUtils;
import jp.kght6123.smallappviewer.adapter.RssItemArrayAdapter;
import jp.kght6123.smallappviewer.structure.RssItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

public class RssParserAsyncTask extends AsyncTask<String, Integer, List<RssItem>>
{
	private final String TAG = RssParserAsyncTask.class.getSimpleName();
	
	private XmlPullParser parser = Xml.newPullParser();
	private final Controller controller;
	
	public RssParserAsyncTask(final Controller controller)
	{
		this.controller = controller;
	}
	
	/**
	 * タスクを実行した直後にコールされる
	 */
	@Override
	protected void onPreExecute()
	{
		
	}
	
	/**
	 * バックグラウンドにおける処理を担う。タスク実行時に渡された値を引数とする
	 */
	@Override
	protected List<RssItem> doInBackground(final String... params)
	{
		long tId = Thread.currentThread().getId();
		Log.d(TAG, "[doInBackground]start " + tId);
		
		final List<RssItem> rssItemList = 
				parseXml(params[0], this.controller.fromDate);	// ここで返した値は、onPostExecuteメソッドの引数として渡される
		
		Log.d(TAG, "[doInBackground]end   " + tId);
		return rssItemList;
	}
	
	/**
	 *  メインスレッド上で実行される
	 * @param rssItemList
	 */
	@Override
	protected void onPostExecute(final List<RssItem> rssItemList)
	{
		publishProgress();
		this.controller.addAll(rssItemList);
	}
	
	private enum Tags
	{
		item,
		title,
		description,
		date,
		pubDate,
		creator,
		link,
	}
	private final static String CR = "\\r";
	private final static String LF = "\\n";
	private final static String EMP = "";
	
//	private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+09:00'");
	private static final ThreadLocal<SimpleDateFormat> SDF = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+09:00'");
		}
	};
	
	private static final ThreadLocal<SimpleDateFormat> SDF2 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
		}
	};
	
	/**
	 * XMLをパースする
	 * 
	 * @param param
	 * @param date 
	 * @return
	 */
	public ArrayList<RssItem> parseXml(final String param, final Date fromDate)
	{
		final long tId = Thread.currentThread().getId();
		
		final ArrayList<RssItem> rssItemList = new ArrayList<RssItem>();
		
		HttpURLConnection conn = null;
		InputStream is = null;
		try
		{// HTTP経由でアクセスし、InputStreamを取得する
			final URL url = new URL(param);
			Log.d(TAG, "RSS読み込み開始 "+tId+" "+param);
			
			conn = (HttpURLConnection)(url.openConnection());
//			conn.setRequestMethod("GET");
//			//conn.addRequestProperty("Accept-Encoding", "gzip");
//			conn.connect();
//			
//			final Context context = 
//					this.controller.adapter.getContext();
//			
//			final String key = url.getHost()+"/"+url.getPath();
//			final String dir = FileUtils.getDir(key);
//			
//			final long prevDateTime = PrefUtils.getLong(key, 0, this.getClass(), context);
//			final long nowDateTime = conn.getExpiration();
//			
//			final File cacheRssXmlFile = new File(context.getExternalCacheDir(), key);
//			final File cacheRssXmlDir = new File(context.getExternalCacheDir(), dir);
//			
//			Log.d(TAG, "prevDateTime= "+prevDateTime+" ,nowDateTime="+nowDateTime);
//			
//			if(prevDateTime == 0 
//					|| prevDateTime < nowDateTime
//					|| !cacheRssXmlFile.exists() 
//					|| !cacheRssXmlFile.isFile())
//			{// キャッシュなし、または無効
//				Log.d(TAG, "キャッシュ無効／なし "+tId);
//				// ディレクトリ作成
//				cacheRssXmlDir.mkdirs();
//				// キャッシュへ保存
//				FileUtils.saveFile(conn.getInputStream(), cacheRssXmlFile);
//				// Prefへ保存
//				PrefUtils.setLong(key, nowDateTime, this.getClass(), context);
//			}
//			else
//				Log.d(TAG, "キャッシュ有効 "+tId);
//			
//			is = new FileInputStream(cacheRssXmlFile);
			
			is = conn.getInputStream();
			this.parser.setInput(is, "UTF-8");
			
			int eventType = this.parser.getEventType();
			RssItem item = null;
			CharSequence siteName = "";
			
			tagLoop : 
				while (eventType != XmlPullParser.END_DOCUMENT)
			{
				final String tag = this.parser.getName();
				
				switch (eventType)
				{
					case XmlPullParser.START_TAG:
						if (tag.equals(Tags.item.name()))
							item = new RssItem(siteName);
						else if (item != null)
						{
							final String txt = this.parser.nextText();
							if (tag.equals(Tags.title.name()))
								item.setTitle(txt);
							else if (tag.equals(Tags.description.name()))
								item.setDescription(txt.replaceAll(CR, EMP).replaceAll(LF, EMP));
							else if (tag.equals(Tags.date.name())
									&& StringUtils.isNotEmpty(txt))
							{
								final Date itemDate = SDF.get().parse(txt);
								if(fromDate.before(itemDate))
									item.setDate(itemDate);
								else
									break tagLoop;
							}
							else if (tag.equals(Tags.pubDate.name())
									&& StringUtils.isNotEmpty(txt)
									&& txt.length() >= 25)
							{
								final Date itemDate = SDF2.get().parse(txt.substring(0, 25));
								if(fromDate.before(itemDate))
									item.setDate(itemDate);
								else
									break tagLoop;
							}
							else if (tag.equals(Tags.creator.name()))
								item.setCreator(txt);
							else if (tag.equals(Tags.link.name()))
								item.setLink(txt);
						}
						else if (tag.equals(Tags.title.name()))
							siteName = this.parser.nextText();
						
						break;
					case XmlPullParser.END_TAG:
						if (tag.equals(Tags.item.name()))
							rssItemList.add(item);
						
						break;
				}
				eventType = this.parser.next();
			}
			
			return rssItemList;	// ここで返した値は、onPostExecuteメソッドの引数として渡される
		}
		catch (final IOException | XmlPullParserException | ParseException | RuntimeException e)
		{
			Log.e(TAG, e.getClass().getSimpleName()+" "+tId, e);
			return rssItemList;
		}
		finally
		{
			if(is != null)
			{
				try
				{
					is.close();
				}
				catch (final IOException e)
				{
					Log.e(TAG, "IOException", e);
				}
			}
			if(conn != null)
				conn.disconnect();
		}
	}
	
	@Override
	protected void onProgressUpdate(final Integer... values)
	{
		this.controller.addCount();
	}
	
	public static class Controller
	{
		private final RssItemArrayAdapter adapter;
		
		private final ProgressBar progressBar;
		private final Date fromDate;
		private int progress = 0;
		private final int maxProgress;
		private final ListView listView;
		
		private final SortValueKey sortKey;
		
		public Controller
		(
			final RssItemArrayAdapter adapter,
			final ProgressBar progressBar,
			final Date fromDate,
			final ListView listView,
			final int maxProgress,
			final SortValueKey sortKey
		)
		{
			super();
			this.adapter = adapter;
			
			this.progressBar = progressBar;
			this.progressBar.setMax(maxProgress);
			this.progressBar.setVisibility(View.VISIBLE);
			
			this.fromDate = fromDate;
			this.listView = listView;
			this.maxProgress = maxProgress;
			this.sortKey = sortKey;
		}

		public void addAll(final List<RssItem> rssItemList)
		{
			this.adapter.addAll(rssItemList);
			this.adapter.sort(this.sortKey.comparator);
			this.listView.setAdapter(this.adapter);
		}

		public void addCount()
		{
			this.progress++;
			this.progressBar.setProgress(this.progress);
			
			if(this.maxProgress == this.progress)
			{// すべての処理終了時
				this.progressBar.setVisibility(View.GONE);
			}
		}
	}
	public enum SortValueKey
	{
		newer(new Comparator<RssItem>(){
			@Override
			public int compare(final RssItem lhs, final RssItem rhs)
			{
//				if(lhs.getDate() == null
//						&& rhs.getDate() == null)
//					return 0;
//				else if(lhs.getDate() == null)
//					return 1;
//				else if(rhs.getDate() == null)
//					return -1;
				return rhs.getDate().compareTo(lhs.getDate());
			}}),
		older(new Comparator<RssItem>(){
			@Override
			public int compare(final RssItem lhs, final RssItem rhs)
			{
//				if(lhs.getDate() == null
//						&& rhs.getDate() == null)
//					return 0;
//				else if(lhs.getDate() == null)
//					return -1;
//				else if(rhs.getDate() == null)
//					return 1;
				return lhs.getDate().compareTo(rhs.getDate());
			}}),
		;
		public final Comparator<RssItem> comparator;
		private SortValueKey(final Comparator<RssItem> comparator)
		{
			this.comparator = comparator;
		}
	}
}
