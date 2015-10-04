package jp.kght6123.smallappviewer.smallapp;

import jp.kght6123.smallappcommon.listener.OnWindowFocusChangeTransportListener;
import jp.kght6123.smallappcommon.listener.OnWindowStateChangeTransportListener;
import jp.kght6123.smallappcommon.utils.PrefUtils;
import jp.kght6123.smallappcommon.utils.SmallApplicationUtils;
import jp.kght6123.smallappcommon.utils.WebViewUtils;
import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.dialog.SmallMultiBrowserPositionDialogFragment;
import jp.kght6123.smallappviewer.dialog.SmallMultiBrowserSplitDialogFragment;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallAppWindow.WindowState;

/**
 * SmallMultiBrowserApplicationの本体
 * 
 * @author Hirotaka
 *
 */
public class SmallMultiBrowserApplication extends WebViewUtils.SmallWebViewApplication
{
	//private final String TAG = SmallMultiBrowserApplication.class.getSimpleName();
	
	private View mMinimizedView;
	
	private final  View.OnClickListener onClickBackListener = new View.OnClickListener()
		{
			@Override
			public synchronized void  onClick(View v)
			{
				if(selectWebView.canGoBack())
				{
					selectWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
					selectWebView.goBack();
					selectWebView.getSettings().setCacheMode(WebViewUtils.defaultCacheMode);
					
				}
				else
					SmallMultiBrowserApplication.this.finish();
			}
		};
	
	private final View.OnClickListener onClickForwardListener = new View.OnClickListener()
		{
			@Override
			public synchronized void onClick(View v)
			{
				if(selectWebView.canGoForward())
				{
					selectWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
					selectWebView.goForward();
					selectWebView.getSettings().setCacheMode(WebViewUtils.defaultCacheMode);
				}
				else
					selectWebView.loadUrl("https://www.google.com/", WebViewUtils.additionalHttpHeaders );
			}
		};
	
		private final  View.OnClickListener onClickChromeListener = new View.OnClickListener()
		{
			@Override
			public synchronized void onClick(View v)
			{
				final PackageManager pm = getPackageManager();
				final Intent intent = pm.getLaunchIntentForPackage("com.android.chrome");
				intent.setData(Uri.parse(selectWebView.getUrl()));
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
				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, selectWebView.getUrl());
				intent.putExtra(Intent.EXTRA_SUBJECT, selectWebView.getTitle());
				
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
	
	private WebView selectWebView = null;
	
	private WebView webView0 = null;
	private WebView webView1 = null;
	private WebView webView2 = null;
	private WebView webView3 = null;
	
	private View resizeWidthBar1 = null;
	private View resizeWidthBar2 = null;
	private View resizeHeightBar = null;
	
	private View layoutWebviewBottom = null;
	
	private View.OnTouchListener selectWebViewListener = new View.OnTouchListener()
	{
		@Override
		public boolean onTouch(final View v, final MotionEvent event)
		{
			SmallMultiBrowserApplication.this.selectWebView = (WebView)v;
			setupActionBar((WebView)v);
			return false;
		}
	};
	
	
	private String focusUrl = null;
	
	@SuppressLint("InflateParams")
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		/* Set the layout of the application */
		final View window = 
				SmallApplicationUtils.setContentViewAndGetSmallApplicationWindowView(this, R.layout.smallapp_multibrowser_main);
		
		this.resizeWidthBar1 = findViewById(R.id.resizeWidthBar1);
		this.resizeWidthBar2 = findViewById(R.id.resizeWidthBar2);
		this.resizeHeightBar = findViewById(R.id.resizeHeightBar);
		
		this.layoutWebviewBottom = findViewById(R.id.layoutWebviewBottom);
		
		this.webView0 = (WebView) findViewById(R.id.webview0);
		this.webView1 = (WebView) findViewById(R.id.webview1);
		this.webView2 = (WebView) findViewById(R.id.webview2);
		this.webView3 = (WebView) findViewById(R.id.webview3);
		
		this.webView0.setOnTouchListener(this.selectWebViewListener);
		this.webView1.setOnTouchListener(this.selectWebViewListener);
		this.webView2.setOnTouchListener(this.selectWebViewListener);
		this.webView3.setOnTouchListener(this.selectWebViewListener);
		
		this.selectWebView = this.webView0;
		
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
		
		final ProgressBar progressBarForWeb = 
				(ProgressBar)findViewById(R.id.progressBarForWeb);
		
		WebViewUtils.initialize(this.webView0, progressBarForWeb, this);
		WebViewUtils.initialize(this.webView1, progressBarForWeb, this);
		WebViewUtils.initialize(this.webView2, progressBarForWeb, this);
		WebViewUtils.initialize(this.webView3, progressBarForWeb, this);
		
		setupActionBar(this.selectWebView);
		
		final OnWindowFocusChangeTransportListener onTransLisn = new OnWindowFocusChangeTransportListener(window, this);
		onTransLisn.add(this.webView0);
		onTransLisn.add(this.webView1);
		onTransLisn.add(this.webView2);
		onTransLisn.add(this.webView3);
		getWindow().setOnWindowFocusChangeListener(onTransLisn);
		getWindow().setOnWindowStateChangeListener(new OnWindowStateChangeTransportListener(onTransLisn, window, this));
		
		/**
		 * 全体のテーマ、DarkかLightが使える。
		 */
		getWindow().setWindowTheme(com.sony.smallapp.R.style.Theme.Dark);
		
	}
	
	private class OnDragResizeWidthBar implements View.OnTouchListener
	{
		private final View targetView;
		
		private float downX;
		private final String prefKey;
		
		private OnDragResizeWidthBar(final View targetView, final String prefKey)
		{
			this.targetView = targetView;
			this.prefKey = prefKey;
			
			final int prefWidth = PrefUtils.getInt(this.prefKey, -1, SmallMultiBrowserApplication.class, SmallMultiBrowserApplication.this);
			if(prefWidth != -1)
			{
				final LayoutParams params = this.targetView.getLayoutParams();
				params.width = prefWidth;
				this.targetView.setLayoutParams(params);
			}
		}
		
		@Override
		public synchronized boolean onTouch(final View v, final MotionEvent event)
		{
			if( event.getAction() == MotionEvent.ACTION_DOWN )
			{
				this.downX = event.getRawX();
				return true;
			}
			else if( event.getAction() == MotionEvent.ACTION_MOVE)
			{
				final LayoutParams params = this.targetView.getLayoutParams();
				
				final float x = this.downX - event.getRawX();
				params.width = (int)(params.width + x);
				this.downX = event.getRawX();
				
				PrefUtils.setInt(this.prefKey, params.width, SmallMultiBrowserApplication.class, SmallMultiBrowserApplication.this);
				
				this.targetView.setLayoutParams(params);
				return true;
			}
			return false;
		}
	}
	
	private class OnDragResizeHeightBar implements View.OnTouchListener
	{
		private final View targetView;
		
		private float downY;
		private final String prefKey;
		
		private OnDragResizeHeightBar(final View targetView, final String prefKey)
		{
			this.targetView = targetView;
			this.prefKey = prefKey;
			
			final int prefHeight = PrefUtils.getInt(this.prefKey, -1, SmallMultiBrowserApplication.class, SmallMultiBrowserApplication.this);
			if(prefHeight != -1)
			{
				final LayoutParams params = this.targetView.getLayoutParams();
				params.height = prefHeight;
				this.targetView.setLayoutParams(params);
			}
		}
		
		@Override
		public synchronized boolean onTouch(final View v, final MotionEvent event)
		{
			if( event.getAction() == MotionEvent.ACTION_DOWN )
			{
				this.downY = event.getRawY();
				return true;
			}
			else if( event.getAction() == MotionEvent.ACTION_MOVE)
			{
				final LayoutParams params = this.targetView.getLayoutParams();
				
				final float y = this.downY - event.getRawY();
				params.height = (int)(params.height + y);
				this.downY = event.getRawY();
				
				PrefUtils.setInt(this.prefKey, params.height, SmallMultiBrowserApplication.class, SmallMultiBrowserApplication.this);
				
				this.targetView.setLayoutParams(params);
				return true;
			}
			return false;
		}
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		if (SdkInfo.VERSION.API_LEVEL >= 2) 
		{
			final int splitMode = SmallMultiBrowserSplitDialogFragment.getSplitMode(this);
			SmallMultiBrowserSplitDialogFragment.setSplitMode(-1, this);	// リセット
			
			if(splitMode == 0 || splitMode == -1)// 分割しない
			{
				WebViewUtils.load(this.webView0, getIntent().getDataString(), this);
				
				this.layoutWebviewBottom.setVisibility(View.GONE);
				this.webView2.setVisibility(View.GONE);
				
				this.resizeWidthBar1.setVisibility(View.GONE);
				this.resizeWidthBar2.setVisibility(View.GONE);
				this.resizeHeightBar.setVisibility(View.GONE);
			}
			else if(splitMode == 1)// 上下分割
			{
				WebViewUtils.load(this.webView0, SmallMultiBrowserPositionDialogFragment.getSplitPositionUrl(this, 0), this);
				WebViewUtils.load(this.webView1, SmallMultiBrowserPositionDialogFragment.getSplitPositionUrl(this, 1), this);
				
				this.layoutWebviewBottom.setVisibility(View.VISIBLE);
				this.webView2.setVisibility(View.GONE);
				this.webView3.setVisibility(View.GONE);
				
				this.resizeWidthBar1.setVisibility(View.GONE);
				this.resizeWidthBar2.setVisibility(View.GONE);
				this.resizeHeightBar.setVisibility(View.VISIBLE);
				this.resizeHeightBar.setOnTouchListener(new OnDragResizeHeightBar(this.layoutWebviewBottom, "heightTopBottom"));
			}
			else if(splitMode == 2)// 左右分割
			{
				WebViewUtils.load(this.webView0, SmallMultiBrowserPositionDialogFragment.getSplitPositionUrl(this, 1), this);
				WebViewUtils.load(this.webView2, SmallMultiBrowserPositionDialogFragment.getSplitPositionUrl(this, 0), this);
				
				this.layoutWebviewBottom.setVisibility(View.GONE);
				
				this.resizeWidthBar1.setVisibility(View.VISIBLE);
				this.resizeWidthBar2.setVisibility(View.GONE);
				this.resizeHeightBar.setVisibility(View.GONE);
				this.resizeWidthBar1.setOnTouchListener(new OnDragResizeWidthBar(this.webView2, "widthLeftRight"));
			}
			else if(splitMode == 3)// ４分割
			{
				WebViewUtils.load(this.webView0, SmallMultiBrowserPositionDialogFragment.getSplitPositionUrl(this, 0), this);
				WebViewUtils.load(this.webView1, SmallMultiBrowserPositionDialogFragment.getSplitPositionUrl(this, 1), this);
				WebViewUtils.load(this.webView2, SmallMultiBrowserPositionDialogFragment.getSplitPositionUrl(this, 2), this);
				WebViewUtils.load(this.webView3, SmallMultiBrowserPositionDialogFragment.getSplitPositionUrl(this, 3), this);
				
				this.layoutWebviewBottom.setVisibility(View.VISIBLE);
				this.webView2.setVisibility(View.VISIBLE);
				this.webView3.setVisibility(View.VISIBLE);
				
				this.resizeWidthBar1.setVisibility(View.VISIBLE);
				this.resizeWidthBar2.setVisibility(View.VISIBLE);
				this.resizeHeightBar.setVisibility(View.VISIBLE);
				
				this.resizeHeightBar.setOnTouchListener(new OnDragResizeHeightBar(this.layoutWebviewBottom, "height4Split"));
				this.resizeWidthBar1.setOnTouchListener(new OnDragResizeWidthBar(this.webView2, "width4Split1"));
				this.resizeWidthBar2.setOnTouchListener(new OnDragResizeWidthBar(this.webView3, "width4Split2"));
			}
		}
		else
		{
			Toast.makeText(this, R.string.api_not_supported,
					Toast.LENGTH_SHORT).show();
		}
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

	private void setupActionBar(final WebView webView)
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		final Button back = (Button) findViewById(R.id.btnBack);
		back.setOnClickListener(this.onClickBackListener);
		PrefUtils.setVisibility(R.string.back_view_bar_key, back, true, this, pref);
		
		final Button forward = (Button) findViewById(R.id.btnForward);
		forward.setOnClickListener(this.onClickForwardListener);
		PrefUtils.setVisibility(R.string.forward_view_bar_key, forward, false, this, pref);
		
		final Button chrome = (Button) findViewById(R.id.btnChrome);
		chrome.setOnClickListener(this.onClickChromeListener);
		PrefUtils.setVisibility(R.string.chrome_view_bar_key, chrome, true, this, pref);
		
		final Button shared = (Button) findViewById(R.id.btnShared);
		shared.setOnClickListener(this.onClickSharedListener);
		PrefUtils.setVisibility(R.string.shared_view_bar_key, shared, true, this, pref);
		
		final Button close = (Button) findViewById(R.id.btnClose);
		close.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public synchronized void onClick(final View v)
				{
					SmallMultiBrowserApplication.this.finish();
				}
			}
		);
		PrefUtils.setVisibility(R.string.close_view_bar_key, close, true, this, pref);
		
		final Button refresh = (Button) findViewById(R.id.btnRefresh);
		refresh.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public synchronized void onClick(final View v)
				{
					webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
					webView.reload();
					webView.getSettings().setCacheMode(WebViewUtils.defaultCacheMode);
				}
			}
		);
		PrefUtils.setVisibility(R.string.refresh_view_bar_key, refresh, false, this, pref);
		
		findViewById(R.id.btnMinimized).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.MINIMIZED);
			}
		});
		PrefUtils.setVisibility(R.string.minimization_view_bar_key, findViewById(R.id.btnMinimized), true, this, pref);
		
		findViewById(R.id.btnNormal).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.NORMAL);
			}
		});
		PrefUtils.setVisibility(R.string.returned_view_bar_key, findViewById(R.id.btnNormal), true, this, pref);
		
		findViewById(R.id.btnFfitted).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				getWindow().setWindowState(WindowState.FITTED);
			}
		});
		PrefUtils.setVisibility(R.string.maximization_view_bar_key, findViewById(R.id.btnFfitted), true, this, pref);
	}
	
	@Override
	public void setFaviconToMinimizedView(Bitmap icon)
	{
		final ImageView faviconView = (ImageView) mMinimizedView.findViewById(R.id.favicon_view);
		final TextView fallbackView = (TextView) mMinimizedView.findViewById(R.id.fallback_view);
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

	@Override
	public void setFocusUrl(String focusUrl)
	{
		this.focusUrl = focusUrl;
	}

	@Override
	public String getFocusUrl()
	{
		return focusUrl;
	};
}