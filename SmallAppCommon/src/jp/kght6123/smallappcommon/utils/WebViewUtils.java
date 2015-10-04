package jp.kght6123.smallappcommon.utils;

import java.util.Map;
import java.util.TreeMap;

import jp.kght6123.smallappcommon.application.SharedDataApplication;
import jp.kght6123.smallappcommon.application.SharedDataApplication.WebHistoryItem;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.sony.smallapp.SmallAppNotFoundException;
import com.sony.smallapp.SmallAppWindow.WindowState;
import com.sony.smallapp.SmallApplication;
import com.sony.smallapp.SmallApplicationManager;

public class WebViewUtils
{
	private static String TAG = WebViewUtils.class.getSimpleName();
	
	public static Map<String, String> additionalHttpHeaders  = new TreeMap<String, String>();
	static {
		additionalHttpHeaders.put("Accept-Encoding", "gzip");
	}
	public static int defaultCacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK;
	
	public static void initialize(final WebView webView, final ProgressBar progressBarForWeb, final SmallWebViewApplication application)
	{
		webView.setWebViewClient
			(
				new WebViewClient()
				{
					private WebHistoryItem histryItem;
					
					@Override
					public void onPageStarted (WebView view, String url, Bitmap favicon)
					{
						progressBarForWeb.setVisibility(View.VISIBLE);
						
						this.histryItem = new WebHistoryItem(url, view.getTitle(), favicon);
						
						final SharedDataApplication sharedData = ((SharedDataApplication)application.getApplicationContext());
						sharedData.addWebHistoryItemList(this.histryItem);
					}
					
					@Override
					public boolean shouldOverrideUrlLoading
					(
						final WebView view,
						final String url
					)
					{
						view.loadUrl(url, additionalHttpHeaders);
						return true;
					}
					
					@Override
					public void onPageFinished(WebView view, String url)
					{
						super.onPageFinished(webView, url);
						
						progressBarForWeb.setVisibility(View.GONE);
					}
					
					@Override
					public void onReceivedError(WebView view, int errorCode,
							String description, String failingUrl)
					{
						progressBarForWeb.setVisibility(View.GONE);
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
						application.setFaviconToMinimizedView(null);
						return result;
					}
					
					@Override
					public void onCloseWindow(WebView view)
					{
						super.onCloseWindow(view);
						application.setFaviconToMinimizedView(view.getFavicon());
					}
					
					@Override
					public void onReceivedIcon(WebView view, Bitmap icon)
					{
						super.onReceivedIcon(view, icon);
						application.setFaviconToMinimizedView(icon);
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
				application.setFocusUrl(msg.getData().getString("url"));
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
								&& application.getFocusUrl() != null && !application.getFocusUrl().equals(""))
							url = application.getFocusUrl();
						else
							url = hr.getExtra();
						
						try
						{
							final Class<?> cls = Class.forName("jp.kght6123.smallappimageviewer.smallapp.SmallImageViewApplication");
							
							final Intent intent = new Intent(application.getIntent());
							intent.setClass(application, cls);
							intent.putExtra("android.intent.extra.STREAM", url);
							
							SmallApplicationManager.startApplication(application, intent);
						}
						catch (ClassNotFoundException e1)
						{
							return false;
						}
						catch (SmallAppNotFoundException e)
						{
							return false;
						}
						return true;
					}
					else if(hr.getType() == WebView.HitTestResult .SRC_ANCHOR_TYPE)
					{
						final Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setData(Uri.parse(hr.getExtra()));
						application.startActivity(intent);
						
						application.getWindow().setWindowState(WindowState.MINIMIZED);
						
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
		
//		ws.setJavaScriptEnabled(true);
		ws.setJavaScriptCanOpenWindowsAutomatically(true);
		
		ws.setLoadWithOverviewMode(true);
		
		ws.setLoadsImagesAutomatically(true);
		ws.setBlockNetworkImage(false);
		ws.setBlockNetworkLoads(false);
		
		ws.setMediaPlaybackRequiresUserGesture(true);
		ws.setUseWideViewPort(true);
		
		// キャッシュを設定
		ws.setAppCacheEnabled(true);
		ws.setAppCachePath(application.getExternalCacheDir().getPath());
		
		ws.setDatabaseEnabled(true);
		ws.setDomStorageEnabled(true);
		ws.setLoadWithOverviewMode(true);
		ws.setUseWideViewPort(true);
	}
	
	public static void load(final WebView webView, final String url, final SmallWebViewApplication application)
	{
		// キャッシュモードをネットワークの接続状況によって切り替え
		final WebSettings ws = webView.getSettings();
		final NetworkInfo info = ((ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if(info != null && info.isConnected())
			defaultCacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK;
		else
			defaultCacheMode = WebSettings.LOAD_CACHE_ONLY;
		ws.setCacheMode(defaultCacheMode);
		
		if(url != null && !url.equals(""))
			webView.loadUrl(url, additionalHttpHeaders );
		else
		{
			webView.loadUrl("https://www.google.com/", additionalHttpHeaders );
		}
	}
	
	public static abstract class SmallWebViewApplication extends SmallApplication
	{
		public abstract void setFaviconToMinimizedView(final Bitmap icon);
		public abstract void setFocusUrl(final String focusUrl);
		public abstract String getFocusUrl();
	}
}
