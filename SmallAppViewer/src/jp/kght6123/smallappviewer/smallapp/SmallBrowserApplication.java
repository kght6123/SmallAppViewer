package jp.kght6123.smallappviewer.smallapp;

import java.util.Map;
import java.util.TreeMap;

import jp.kght6123.smallappcommon.application.SharedDataApplication;
import jp.kght6123.smallappcommon.application.SharedDataApplication.WebHistoryItem;
import jp.kght6123.smallappcommon.listener.OnWindowFocusChangeTransportListener;
import jp.kght6123.smallappcommon.listener.OnWindowStateChangeTransportListener;
import jp.kght6123.smallappcommon.utils.PrefUtils;
import jp.kght6123.smallappcommon.utils.SmallApplicationUtils;
import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.adapter.WebHistoryItemAdapter;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppNotFoundException;
import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallAppWindow.WindowState;
import com.sony.smallapp.SmallApplication;
import com.sony.smallapp.SmallApplicationManager;

/**
 * SmallApplicationBrowserの本体
 * 
 * @author Hirotaka
 *
 */
public class SmallBrowserApplication extends SmallApplication
{
	private final String TAG = SmallBrowserApplication.class.getSimpleName();
	private View mMinimizedView;
	
	private static Map<String, String> additionalHttpHeaders  = new TreeMap<String, String>();
	static {
		additionalHttpHeaders.put("Accept-Encoding", "gzip");
	}
	
	public static final String EXTRA_JAVASCRIPT_DISABLED = "kght6123.intent.EXTRA_JAVASCRIPT_DISABLED";
	
	private int defaultCacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK;
	
	private final  View.OnClickListener onClickBackListener = new View.OnClickListener()
		{
			@Override
			public synchronized void  onClick(View v)
			{
				final WebView webView = (WebView) findViewById(R.id.webview);
				
				if(webView.canGoBack())
				{
					webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
					webView.goBack();
					webView.getSettings().setCacheMode(defaultCacheMode);
					
				}
				else
					SmallBrowserApplication.this.finish();
			}
		};
	
	private final View.OnClickListener onClickForwardListener = new View.OnClickListener()
		{
			@Override
			public synchronized void onClick(View v)
			{
				final WebView webView = (WebView) findViewById(R.id.webview);
				
				if(webView.canGoForward())
				{
					webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
					webView.goForward();
					webView.getSettings().setCacheMode(defaultCacheMode);
				}
				else
					webView.loadUrl("https://www.google.com/", additionalHttpHeaders );
			}
		};
	
		private final  View.OnClickListener onClickChromeListener = new View.OnClickListener()
		{
			@Override
			public synchronized void onClick(View v)
			{
				final WebView webView = (WebView) findViewById(R.id.webview);
				
				final PackageManager pm = getPackageManager();
				final Intent intent = pm.getLaunchIntentForPackage("com.android.chrome");
				intent.setData(Uri.parse(webView.getUrl()));
				intent.setAction(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				startActivity(intent);
				
				getWindow().setWindowState(WindowState.MINIMIZED);
			}
		};
		
		private final  View.OnClickListener onClickSharedListener = new View.OnClickListener()
		{
			@Override
			public synchronized void onClick(View v)
			{
				final WebView webView = (WebView) findViewById(R.id.webview);
				
				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
				intent.putExtra(Intent.EXTRA_SUBJECT, webView.getTitle());
//				intent.putExtra(Browser.EXTRA_SHARE_FAVICON, favicon);
//				intent.putExtra(Browser.EXTRA_SHARE_SCREENSHOT, screenshot);
				
				try
				{
					final Intent send = Intent.createChooser(intent, getText(R.string.send));
					send.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(send);
				}
				catch(final ActivityNotFoundException ex)
				{
					// if no app handles it, do nothing
				}
				
				getWindow().setWindowState(WindowState.MINIMIZED);
			}
		};
		
		private WebHistoryItemAdapter histAdapter = null;
		private WebHistoryItemAdapter pinAdapter = null;
		
		private final View.OnClickListener onClickHistListener = new View.OnClickListener()
		{
			@Override
			public synchronized void onClick(View v)
			{
				final ListView listView = (ListView)findViewById(R.id.browserHistoryListView);
				if(listView.getVisibility() != View.GONE)
				{
					listView.setVisibility(View.GONE);
					listView.setAdapter(null);
					histAdapter = null;
				}
				else
				{
					final SharedDataApplication sharedData = ((SharedDataApplication)SmallBrowserApplication.this.getApplicationContext());
					histAdapter = new WebHistoryItemAdapter(SmallBrowserApplication.this, 0, sharedData.getWebHistoryItemList(), additionalHttpHeaders, true);
					
					listView.setVisibility(View.VISIBLE);
					listView.setAdapter(histAdapter);
				}
			}
		};
		
		private final View.OnClickListener onClickPinListener = new View.OnClickListener()
		{
			@Override
			public synchronized void onClick(View v)
			{
				final ListView listView = (ListView)findViewById(R.id.browserPinListView);
				if(listView.getVisibility() != View.GONE)
				{
					listView.setVisibility(View.GONE);
					listView.setAdapter(null);
					pinAdapter = null;
				}
				else
				{
					final SharedDataApplication sharedData = ((SharedDataApplication)SmallBrowserApplication.this.getApplicationContext());
					pinAdapter = new WebHistoryItemAdapter(SmallBrowserApplication.this, 0, sharedData.getWebPinItemList(), additionalHttpHeaders, false);
					
					listView.setVisibility(View.VISIBLE);
					listView.setAdapter(pinAdapter);
				}
			}
		};
	
	private enum MoveControlArea
	{
		Left,
		Right,
		Bottom,
		Bottom2
	}
	
	private OnWindowFocusChangeTransportListener onFocusChange = null;
	private String focusUrl = null;
	
//	@SuppressWarnings("deprecation")
	@SuppressLint("InflateParams")
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		/* Set the layout of the application */
		final View window = 
				SmallApplicationUtils.setContentViewAndGetSmallApplicationWindowView(this, R.layout.smallapp_browser_main);
		
		/*
		 * Set the layout displayed when the application is minimized.
		 */
		mMinimizedView = LayoutInflater.from(this).inflate(R.layout.smallapp_browser_minimized, null);
		setMinimizedView(mMinimizedView);
		setFaviconToMinimizedView(null);
		
		/* Set the title of the application to be displayed in the titlebar */
		setTitle(R.string.app_browser_name);
		
		SmallAppWindow.Attributes attr = getWindow().getAttributes();
		
		/* Set the requested width of the application */
		attr.width = getResources().getDimensionPixelSize(R.dimen.width);
		/* Set the requested height of the application */
		attr.height = getResources().getDimensionPixelSize(R.dimen.height);
		
		/* Use this flag to make the application window resizable */
		attr.flags |= SmallAppWindow.Attributes.FLAG_RESIZABLE;
		/* Use this flag to remove the titlebar from the window */
		attr.flags |= SmallAppWindow.Attributes.FLAG_NO_TITLEBAR;
		/* Use this flag to enable hardware accelerated rendering */
		attr.flags |= SmallAppWindow.Attributes.FLAG_HARDWARE_ACCELERATED;
		
		/* Set the window attributes to apply the changes above */
		getWindow().setAttributes(attr);
		
		/* Set Background Drawable */
		//getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.translucent_background));
		
		final ProgressBar progressBarForWeb = 
				(ProgressBar)findViewById(R.id.progressBarForWeb);
		
		final WebView webView = (WebView) findViewById(R.id.webview);
		webView.setWebViewClient
			(
				new WebViewClient()
				{
					private WebHistoryItem histryItem;
					
					@Override
					public void onPageStarted (WebView view, String url, Bitmap favicon)
					{
						progressBarForWeb.setVisibility(View.VISIBLE);
						
						//doImageViewWindow(view, url);
						
						this.histryItem = new WebHistoryItem(url, view.getTitle(), favicon);
						
						final SharedDataApplication sharedData = ((SharedDataApplication)SmallBrowserApplication.this.getApplicationContext());
						sharedData.addWebHistoryItemList(this.histryItem);
						
						// WebViewをロード前に半透明にするとおかしくなるので、ここで半透明機能を有効にする。
						if(SmallBrowserApplication.this.onFocusChange != null)
							SmallBrowserApplication.this.onFocusChange.setEnable(true);
					}
					
//					@Override
//					public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
//						switch (event.getKeyCode()) {
//							case KeyEvent.KEYCODE_VOLUME_UP:
//								if (event.getAction() == KeyEvent.ACTION_DOWN) {
//									//webview.pageUp(false);
//									view.flingScroll(0, 50);
//									return;
//								}
//							case KeyEvent.KEYCODE_VOLUME_DOWN:
//								if (event.getAction() == KeyEvent.ACTION_DOWN) {
//									//webview.pageDown(false);
//									view.flingScroll(0, 50);
//									return;
//								}
//						}
//						super.onUnhandledKeyEvent(view, event);
//					}
//					
//					@Override
//					public boolean shouldOverrideKeyEvent(WebView view,
//							KeyEvent event) {
//						
//						switch (event.getKeyCode()) {
//							case KeyEvent.KEYCODE_VOLUME_UP:
//							if (event.getAction() == KeyEvent.ACTION_DOWN) {
//								//webview.pageUp(false);
//								view.flingScroll(0, 50);
//								return true;
//							}
//						case KeyEvent.KEYCODE_VOLUME_DOWN:
//							if (event.getAction() == KeyEvent.ACTION_DOWN) {
//								//webview.pageDown(false);
//								view.flingScroll(0, 50);
//								return true;
//							}
//						}
//						return super.shouldOverrideKeyEvent(view, event);
//					}
					@Override
					public boolean shouldOverrideUrlLoading
					(
						final WebView view,
						final String url
					)
					{
						//return super.shouldOverrideUrlLoading(view, url);
						//return false;
						
						view.loadUrl(url, additionalHttpHeaders);
						return true;
					}
					
					@Override
					public void onPageFinished(WebView view, String url)
					{
						super.onPageFinished(webView, url);
						
						progressBarForWeb.setVisibility(View.GONE);
						setupBackForward(view);
						
						if(this.histryItem != null)
						{
//							this.histryItem.setFavicon(view.getFavicon());
							this.histryItem.setTitle(view.getTitle());
//							this.histryItem.setUrl(view.getUrl());
							if(histAdapter != null)
								histAdapter.insert(this.histryItem, 0);
						}
					}

					@Override
					public void onReceivedError(WebView view, int errorCode,
							String description, String failingUrl)
					{
						progressBarForWeb.setVisibility(View.GONE);
						setupBackForward(view);
						
						if(this.histryItem != null)
						{
//							this.histryItem.setFavicon(view.getFavicon());
							this.histryItem.setTitle(view.getTitle());
//							this.histryItem.setUrl(view.getUrl());
						}
					}
				}
			);
		webView.setWebChromeClient
			(
				new WebChromeClient() 
				{
					@Override
					public boolean onCreateWindow(WebView view,
							boolean isDialog, boolean isUserGesture,
							Message resultMsg) 
					{
						final boolean result = super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
						setFaviconToMinimizedView(null);
						return result;
					}
					
					@Override
					public void onCloseWindow(WebView view)
					{
						super.onCloseWindow(view);
						setFaviconToMinimizedView(view.getFavicon());
					}
					
					@Override
					public void onReceivedIcon(WebView view, Bitmap icon)
					{
						super.onReceivedIcon(view, icon);
						setFaviconToMinimizedView(icon);
					}
					
					@Override
					public void onProgressChanged(WebView view, int newProgress)
					{
						progressBarForWeb.setProgress(newProgress);
					}
					
					
				}
			);
		webView.setVerticalScrollbarOverlay(true);
		
		final Handler urlHandler = new Handler()
		{
			@Override
			public void handleMessage(final Message msg) {
				focusUrl = msg.getData().getString("url");
			}
		};
		webView.requestFocusNodeHref(urlHandler.obtainMessage());
		
		webView.setOnLongClickListener
		(
			new View.OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View view)
				{
					Log.d(TAG, "onLongClick");
					
					final WebView webView = (WebView) view;
					WebView.HitTestResult hr = webView.getHitTestResult();
					Log.d(TAG, "HitTestResult.getType:" + hr.getType());
					Log.d(TAG, "HitTestResult.getExtra:" + hr.getExtra());
					
					if(hr.getExtra() == null)
						return false;
					else if(hr.getType() == WebView.HitTestResult .IMAGE_TYPE
							|| hr.getType() == WebView.HitTestResult .SRC_IMAGE_ANCHOR_TYPE)
					{
						final String url;
						if(hr.getType() == WebView.HitTestResult .SRC_IMAGE_ANCHOR_TYPE
								&& focusUrl != null && !focusUrl.equals(""))
							url = focusUrl;
						else
							url = hr.getExtra();
						
						try
						{
							final Intent intent = new Intent(getIntent());
							intent.setClassName("jp.kght6123.smallappimageviewer",
									"jp.kght6123.smallappimageviewer.smallapp.SmallImageViewApplication");
							intent.putExtra("android.intent.extra.STREAM", url);
							
							SmallApplicationManager.startApplication(getApplicationContext(), intent);
						}
						catch (SmallAppNotFoundException e)
						{
//							Log.e(TAG, "SmallAppNotFoundException", e);
//							Toast.makeText(getApplicationContext(), e.getMessage(),
//									Toast.LENGTH_SHORT).show();
							return false;
						}
						return true;
					}
					else if(hr.getType() == WebView.HitTestResult .SRC_ANCHOR_TYPE)
					{
						final Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setData(Uri.parse(hr.getExtra()));
						//intent.addCategory(Intent.CATEGORY_DEFAULT);
						//intent.addCategory(Intent.CATEGORY_BROWSABLE);
						startActivity(intent);
						
						getWindow().setWindowState(WindowState.MINIMIZED);
						
						return true;
					}
					else
						return false;
				}
			}
		);
		webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		
		// マルチタッチでのピンチズームを有効化
		final WebSettings ws = webView.getSettings();
		ws.setBuiltInZoomControls(true);
		ws.setSupportZoom(true);
		ws.setDisplayZoomControls(false);// ズームボタンを出さない
		
		ws.setJavaScriptCanOpenWindowsAutomatically(true);
		
		ws.setLoadWithOverviewMode(true);
		
		ws.setLoadsImagesAutomatically(true);
		ws.setBlockNetworkImage(false);
		ws.setBlockNetworkLoads(false);
		
		ws.setMediaPlaybackRequiresUserGesture(true);
		ws.setUseWideViewPort(true);
		
		//ws.setPluginState(PluginState.ON);
		//ws.setPluginsEnabled(true);
		//ws.setSupportMultipleWindows(support);
		
		// キャッシュを設定
		ws.setAppCacheEnabled(true);
		ws.setAppCachePath(getExternalCacheDir().getPath());
		
		ws.setDatabaseEnabled(true);
		ws.setDomStorageEnabled(true);
		ws.setLoadWithOverviewMode(true);
		ws.setUseWideViewPort(true);
		
		setupOptionMenu(webView);
		setupActionBar(webView, R.id.moveControlAreaBottom, R.id.webviewControlAreaBottom2, R.id.smallappControlAreaBottom2, MoveControlArea.Bottom2);
		setupActionBar(webView, R.id.moveControlAreaBottom, R.id.webviewControlAreaBottom, R.id.smallappControlAreaBottom, MoveControlArea.Bottom);
		setupActionBar(webView, R.id.moveControlAreaRight, R.id.webviewControlAreaRight, R.id.smallappControlAreaRight, MoveControlArea.Right);
		setupActionBar(webView, R.id.moveControlAreaLeft, R.id.webviewControlAreaLeft, R.id.smallappControlAreaLeft, MoveControlArea.Left);
		
		this.onFocusChange = new OnWindowFocusChangeTransportListener(webView, window, this);
		this.onFocusChange.setEnable(false);
		getWindow().setOnWindowFocusChangeListener(this.onFocusChange);
		getWindow().setOnWindowStateChangeListener(new OnWindowStateChangeTransportListener(this.onFocusChange, window, this));
		
		/**
		 * 全体のテーマ、DarkかLightが使える。
		 */
		getWindow().setWindowTheme(com.sony.smallapp.R.style.Theme.Dark);
		
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		if (SdkInfo.VERSION.API_LEVEL >= 2) 
		{
			final WebView webView = (WebView) findViewById(R.id.webview);
			
			// キャッシュモードをネットワークの接続状況によって切り替え
			final WebSettings ws = webView.getSettings();
			final NetworkInfo info = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			if(info != null && info.isConnected())
				defaultCacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK;
			else
				defaultCacheMode = WebSettings.LOAD_CACHE_ONLY;
			ws.setCacheMode(defaultCacheMode);
			
			final boolean javaScriptDisabled = getIntent().getExtras() != null ? getIntent().getExtras().getBoolean(EXTRA_JAVASCRIPT_DISABLED, false) : false;
			if(javaScriptDisabled)
				ws.setJavaScriptEnabled(false);
			else
				ws.setJavaScriptEnabled(true);
			
			final String url = getIntent().getDataString();
			
			if(url != null && !url.equals(""))
				webView.loadUrl(url, additionalHttpHeaders );
			else
			{
				//ws.setUserAgentString("Mozilla/5.0 (Windows NT 5.1; rv:20.0) Gecko/20100101 Firefox/20.0");
				//webView.loadUrl("http://www.dmm.com/netgame/social/-/gadgets/=/app_id=854854/", additionalHttpHeaders );
				webView.loadUrl("https://www.google.com/", additionalHttpHeaders );
			}
		}
		else
		{
			Toast.makeText(this, R.string.api_not_supported,
					Toast.LENGTH_SHORT).show();
		}
		
//		if(this.onFocusChange != null)
//			this.onFocusChange.setEnable(true);
		
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	protected boolean onSmallAppConfigurationChanged(Configuration newConfig)
	{
		return super.onSmallAppConfigurationChanged(newConfig);
	}

	/*
	 * Show the favicon of displayed web page. When the favicon is unavailable,
	 * the name of application is shown.
	 */
	private void setFaviconToMinimizedView(Bitmap icon)
	{
		ImageView faviconView = (ImageView) mMinimizedView.findViewById(R.id.favicon_view);
		TextView fallbackView = (TextView) mMinimizedView.findViewById(R.id.fallback_view);
		if (icon != null)
		{
			faviconView.setVisibility(View.VISIBLE);
			faviconView.setImageBitmap(icon);
			fallbackView.setVisibility(View.GONE);
		}
		else
		{
			faviconView.setVisibility(View.GONE);
			fallbackView.setVisibility(View.VISIBLE);
		}
	}
	
	@SuppressLint("InflateParams")
	private void setupOptionMenu(final WebView webView)
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		final View header = 
				LayoutInflater.from(this).inflate(R.layout.smallapp_browser_header, null);// リソースからViewを取り出す
		
		final View optionMenu = header.findViewById(R.id.option_menu);
		optionMenu.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final PopupMenu popup = new PopupMenu(SmallBrowserApplication.this, optionMenu);
				popup.getMenuInflater().inflate(R.menu.smallapp_browser_menus, popup.getMenu());
				
				final Menu menu = popup.getMenu();
				switch(getWindow().getWindowState())
				{
				case NORMAL:
					menu.findItem(R.id.returned).setVisible(false);
					menu.findItem(R.id.maximization).setVisible(true);
					menu.findItem(R.id.minimization).setVisible(true);
					break;
				case FITTED:
					menu.findItem(R.id.returned).setVisible(true);
					menu.findItem(R.id.maximization).setVisible(false);
					menu.findItem(R.id.minimization).setVisible(true);
					break;
				case MINIMIZED:
					menu.findItem(R.id.returned).setVisible(true);
					menu.findItem(R.id.maximization).setVisible(true);
					menu.findItem(R.id.minimization).setVisible(false);
					break;
				}
				
				if(webView.getSettings().getJavaScriptEnabled())
					menu.findItem(R.id.javascript).setTitle(R.string.javascriptDisable);
				else
					menu.findItem(R.id.javascript).setTitle(R.string.javascriptEnable);
				
				popup.setOnMenuItemClickListener
				(
					new PopupMenu.OnMenuItemClickListener()
					{
						@Override
						public boolean onMenuItemClick(final MenuItem item)
						{
							if(R.id.minimization == item.getItemId())
							{
								getWindow().setWindowState(WindowState.MINIMIZED);
							}
							else if(R.id.maximization == item.getItemId())
							{
								getWindow().setWindowState(WindowState.FITTED);
							}
							else if(R.id.returned == item.getItemId())
							{
								getWindow().setWindowState(WindowState.NORMAL);
							}
							else if(R.id.javascript == item.getItemId())
							{
								if(webView.getSettings().getJavaScriptEnabled())
								{
									menu.findItem(R.id.javascript).setTitle(R.string.javascriptEnable);
									webView.getSettings().setJavaScriptEnabled(false);
									webView.reload();
								}
								else
								{
									menu.findItem(R.id.javascript).setTitle(R.string.javascriptDisable);
									webView.getSettings().setJavaScriptEnabled(true);
									webView.reload();
								}
								
							}
							return true;
						}
					}
				);
				popup.show();
			}
		});
		
		final View back = header.findViewById(R.id.back);
		back.setOnClickListener(this.onClickBackListener);
		PrefUtils.setVisibility(R.string.back_view_title_key, back, false, this, pref);
		
		final View forward = header.findViewById(R.id.forward);
		forward.setOnClickListener(this.onClickForwardListener);
		PrefUtils.setVisibility(R.string.forward_view_title_key, forward, false, this, pref);
		
		final View chrome = header.findViewById(R.id.chrome);
		chrome.setOnClickListener(this.onClickChromeListener);
		PrefUtils.setVisibility(R.string.chrome_view_title_key, chrome, false, this, pref);
		
		/**
		 * タイトルビューの設定
		 * 
		 * 縦は48dpぐらい。AndoridのActionBarと同じサイズ。
		 * ActionButtonは48dp×48dpにすること。（UIのガイドラインレベル）
		 * Action button icons on header area should be 48x48 dp. 
		 * 
		 */
		/* Deploy the option menu in the header area of the titlebar */
		getWindow().setHeaderView(header);
	}
	
	private void setupBackForward(WebView webView)
	{
//		final View header = 
//				LayoutInflater.from(getApplicationContext()).inflate(R.layout.smallapp_browser_header, null);// リソースからViewを取り出す
//		
//		final ImageView back = (ImageView)header.findViewById(R.id.back);
//		if(webView.canGoBack())
//			back.setImageResource(R.drawable.ic_back);
//		else
//			back.setImageResource(R.drawable.ic_back_disabled);
//		
//		final ImageView forward = (ImageView)header.findViewById(R.id.forward);
//		if(webView.canGoForward())
//			forward.setImageResource(R.drawable.ic_forward);
//		else
//			forward.setImageResource(R.drawable.ic_forward_disabled);
	}
	
	private void setupActionBar(final WebView webView, final int moveControlAreaId, final int webviewControlAreaId, final int smallappControlAreaId, final MoveControlArea targetMoveControlArea)
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		final View moveControlArea = findViewById(moveControlAreaId);
		final View webviewControlArea = findViewById(webviewControlAreaId);
		final View smallappControlArea = findViewById(smallappControlAreaId);
		
		final Button back = (Button) webviewControlArea.findViewById(R.id.btnBack);
		back.setOnClickListener(this.onClickBackListener);
		PrefUtils.setVisibility(R.string.back_view_bar_key, back, true, this, pref);
		
		final Button forward = (Button) webviewControlArea.findViewById(R.id.btnForward);
		forward.setOnClickListener(this.onClickForwardListener);
		PrefUtils.setVisibility(R.string.forward_view_bar_key, forward, false, this, pref);
		
		final Button chrome = (Button) smallappControlArea.findViewById(R.id.btnChrome);
		chrome.setOnClickListener(this.onClickChromeListener);
		PrefUtils.setVisibility(R.string.chrome_view_bar_key, chrome, true, this, pref);
		
		final Button pin = (Button) webviewControlArea.findViewById(R.id.btnPin);
		pin.setOnClickListener(this.onClickPinListener);
		PrefUtils.setVisibility(R.string.pin_view_bar_key, pin, false, this, pref);
		
		final Button histry = (Button) webviewControlArea.findViewById(R.id.btnHistry);
		histry.setOnClickListener(this.onClickHistListener);
		PrefUtils.setVisibility(R.string.history_view_bar_key, histry, false, this, pref);
		
		final Button shared = (Button) webviewControlArea.findViewById(R.id.btnShared);
		shared.setOnClickListener(this.onClickSharedListener);
		PrefUtils.setVisibility(R.string.shared_view_bar_key, shared, true, this, pref);
		
		final Button close = (Button) smallappControlArea.findViewById(R.id.btnClose);
		close.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public synchronized void onClick(final View v)
				{
					SmallBrowserApplication.this.finish();
				}
			}
		);
		PrefUtils.setVisibility(R.string.close_view_bar_key, close, true, this, pref);
		
		final Button refresh = (Button) webviewControlArea.findViewById(R.id.btnRefresh);
		refresh.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public synchronized void onClick(final View v)
				{
					webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
					webView.reload();
					webView.getSettings().setCacheMode(defaultCacheMode);
				}
			}
		);
		PrefUtils.setVisibility(R.string.refresh_view_bar_key, refresh, false, this, pref);
		
		webviewControlArea.findViewById(R.id.btnMinimized).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.MINIMIZED);
			}
		});
		PrefUtils.setVisibility(R.string.minimization_view_bar_key, webviewControlArea.findViewById(R.id.btnMinimized), true, this, pref);
		
		webviewControlArea.findViewById(R.id.btnNormal).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.NORMAL);
			}
		});
		PrefUtils.setVisibility(R.string.returned_view_bar_key, webviewControlArea.findViewById(R.id.btnNormal), true, this, pref);
		
		webviewControlArea.findViewById(R.id.btnFfitted).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.FITTED);
			}
		});
		PrefUtils.setVisibility(R.string.maximization_view_bar_key, webviewControlArea.findViewById(R.id.btnFfitted), true, this, pref);
		
		if(targetMoveControlArea != MoveControlArea.Bottom2)
		{
			final Button btnMoveControl = (Button) moveControlArea.findViewById(R.id.btnMoveControl);
			final OnClickListenerForMoveControl onClickMoveCtrl = new OnClickListenerForMoveControl(targetMoveControlArea);
			btnMoveControl.setOnClickListener(onClickMoveCtrl);
			
			if(targetMoveControlArea == getMoveControlArea())
				onClickMoveCtrl.onClick(btnMoveControl);
		}
	}
	
	public MoveControlArea getMoveControlArea()
	{
		final SharedPreferences sPref = getSharedPreferences(SmallBrowserApplication.class.getName(), Context.MODE_PRIVATE);
		final String moveControlAreaStr = sPref.getString(MoveControlArea.class.getSimpleName(), MoveControlArea.Left.name());
		final MoveControlArea moveControlArea = MoveControlArea.valueOf(moveControlAreaStr);
		return moveControlArea;
	}
	
	private void setMoveControlArea(MoveControlArea moveControlArea)
	{
		final SharedPreferences sPref = getSharedPreferences(SmallBrowserApplication.class.getName(), Context.MODE_PRIVATE);
		
		final SharedPreferences.Editor editor = sPref.edit();
		editor.putString(MoveControlArea.class.getSimpleName(), moveControlArea.name());
		editor.apply();
	}
	
	public class OnClickListenerForMoveControl implements View.OnClickListener
	{
		private final MoveControlArea moveControlArea;
		
		private final LinearLayout moveControlAreaLeft = (LinearLayout) findViewById(R.id.moveControlAreaLeft);
		private final LinearLayout moveControlAreaRight = (LinearLayout) findViewById(R.id.moveControlAreaRight);
		private final LinearLayout moveControlAreaBottom = (LinearLayout) findViewById(R.id.moveControlAreaBottom);
		
		private final LinearLayout webViewControlAreaLeft = (LinearLayout) findViewById(R.id.webviewControlAreaLeft);
		private final LinearLayout webViewControlAreaRight = (LinearLayout) findViewById(R.id.webviewControlAreaRight);
		private final LinearLayout webViewControlAreaBottom = (LinearLayout) findViewById(R.id.webviewControlAreaBottom);
		private final LinearLayout webViewControlAreaBottom2 = (LinearLayout) findViewById(R.id.webviewControlAreaBottom2);
		
		private final LinearLayout smallappControlAreaLeft = (LinearLayout) findViewById(R.id.smallappControlAreaLeft);
		private final LinearLayout smallappControlAreaRight = (LinearLayout) findViewById(R.id.smallappControlAreaRight);
		private final LinearLayout smallappControlAreaBottom = (LinearLayout) findViewById(R.id.smallappControlAreaBottom);
		private final LinearLayout smallappControlAreaBottom2 = (LinearLayout) findViewById(R.id.smallappControlAreaBottom2);
		
		public OnClickListenerForMoveControl(final MoveControlArea moveControlArea)
		{
			super();
			this.moveControlArea = moveControlArea;
		}

		@Override
		public synchronized void onClick(final View v)
		{
			switch(moveControlArea)
			{
			case Left:
				moveControlAreaRight.setVisibility(View.VISIBLE);
				moveControlAreaBottom.setVisibility(View.VISIBLE);
				
				webViewControlAreaRight.setVisibility(View.INVISIBLE);
				webViewControlAreaBottom.setVisibility(View.INVISIBLE);
				webViewControlAreaBottom2.setVisibility(View.INVISIBLE);
				
				smallappControlAreaRight.setVisibility(View.INVISIBLE);
				smallappControlAreaBottom.setVisibility(View.INVISIBLE);
				smallappControlAreaBottom2.setVisibility(View.INVISIBLE);
				
				moveControlAreaLeft.setVisibility(View.INVISIBLE);
				webViewControlAreaLeft.setVisibility(View.VISIBLE);
				smallappControlAreaLeft.setVisibility(View.VISIBLE);
				break;
			case Right:
				moveControlAreaLeft.setVisibility(View.VISIBLE);
				moveControlAreaBottom.setVisibility(View.VISIBLE);
				
				webViewControlAreaLeft.setVisibility(View.INVISIBLE);
				webViewControlAreaBottom.setVisibility(View.INVISIBLE);
				webViewControlAreaBottom2.setVisibility(View.INVISIBLE);
				
				smallappControlAreaLeft.setVisibility(View.INVISIBLE);
				smallappControlAreaBottom.setVisibility(View.INVISIBLE);
				smallappControlAreaBottom2.setVisibility(View.INVISIBLE);
				
				moveControlAreaRight.setVisibility(View.INVISIBLE);
				webViewControlAreaRight.setVisibility(View.VISIBLE);
				smallappControlAreaRight.setVisibility(View.VISIBLE);
				break;
			case Bottom:
			case Bottom2:
				moveControlAreaLeft.setVisibility(View.VISIBLE);
				moveControlAreaRight.setVisibility(View.VISIBLE);
				
				webViewControlAreaLeft.setVisibility(View.INVISIBLE);
				webViewControlAreaRight.setVisibility(View.INVISIBLE);
				
				smallappControlAreaLeft.setVisibility(View.INVISIBLE);
				smallappControlAreaRight.setVisibility(View.INVISIBLE);
				
				
				final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(SmallBrowserApplication.this);
				if(pref.getBoolean(SmallBrowserApplication.this.getString(R.string.bottom_actionbar_view_close_let_bar_key), false))
				{
					moveControlAreaBottom.setVisibility(View.INVISIBLE);
					webViewControlAreaBottom.setVisibility(View.INVISIBLE);
					smallappControlAreaBottom.setVisibility(View.INVISIBLE);
					
					webViewControlAreaBottom2.setVisibility(View.VISIBLE);
					smallappControlAreaBottom2.setVisibility(View.VISIBLE);
				}
				else
				{
					moveControlAreaBottom.setVisibility(View.INVISIBLE);
					webViewControlAreaBottom.setVisibility(View.VISIBLE);
					smallappControlAreaBottom.setVisibility(View.VISIBLE);
					
					webViewControlAreaBottom2.setVisibility(View.INVISIBLE);
					smallappControlAreaBottom2.setVisibility(View.INVISIBLE);
				}
				break;
			}
			setMoveControlArea(moveControlArea);
		}
		
//		private void changeGravityForFrameLayoutParams(final ViewGroup vGrp, final int gravity)
//		{
//			FrameLayout.LayoutParams frameLayoutParams = 
//					(FrameLayout.LayoutParams) vGrp.getLayoutParams();
//			frameLayoutParams.gravity = gravity;
//			vGrp.setLayoutParams(frameLayoutParams);
//		}
//		
//		private void changeBottomMarginAndGravityForFrameLayoutParams(final ViewGroup vGrp, final int marginTopDp, final int gravity)
//		{
//			final int px = ImageUtils.dp2Px(marginTopDp, SmallBrowserApplication.this);// dpをpxへ変換する
//			
//			FrameLayout.LayoutParams frameLayoutParams = 
//					(FrameLayout.LayoutParams) vGrp.getLayoutParams();
//			frameLayoutParams.gravity = gravity;
//			frameLayoutParams.bottomMargin = px;
//			vGrp.setLayoutParams(frameLayoutParams);
//		}
	};
	
//	private void doImageViewWindow(final WebView view, final String url)
//	{
//		if(url.endsWith(".jpg")
//				|| url.endsWith(".jpeg")
//				|| url.endsWith(".png")
//				|| url.endsWith(".gif")
//				)
//		{
//			//if(view.getOriginalUrl().endsWith(".jpg")
//			//		|| view.getOriginalUrl().endsWith(".jpeg")
//			//		|| view.getOriginalUrl().endsWith(".png")
//			//		|| view.getOriginalUrl().endsWith(".gif"))
//			{
//				//Toast.makeText(getApplicationContext(), view.getOriginalUrl(),
//				//		Toast.LENGTH_SHORT).show();
//				
//				final Intent intent = new Intent(getIntent());
//				intent.setClass(getApplicationContext(), SmallImageViewApplication.class);
//				intent.putExtra("android.intent.extra.STREAM", url);
//				
//				try
//				{
//					SmallApplicationManager.startApplication(getApplicationContext(), intent);
//				}
//				catch (SmallAppNotFoundException e)
//				{
//					Toast.makeText(getApplicationContext(), e.getMessage(),
//							Toast.LENGTH_SHORT).show();
//				}
//				view.stopLoading();
//				//view.goBack();
//			}
//			//else
//			//return;
//		}
//	}
}