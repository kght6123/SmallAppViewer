package jp.kght6123.smallappwebcameraviewer.asynctask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

public class AWCCPrototypeAsyncTask  /*extends AsyncTask<String, Integer, Bitmap>*/ {
	
	private static final String TAG = AWCCPrototypeAsyncTask.class.getSimpleName();
	
//	private static final String USBCAMERA_USE_SET_URL = "http://kght6123.bglb.jp:15790/index.cgi/usbcamera_use";
	private static final String USBCAMERA_USE_SET_URL = "http://kght6123.bglb.jp:15790/index.cgi/usbcamera_use_set";
	private static final String USBCAM_JPG_URL = "http://kght6123.bglb.jp:15790/usbcam.jpg";
	private static final String ID = "kght6123";
	private static final String PASSWORD = "kght1203";
	
	private static final Pattern SESSION_ID_PATTERN = Pattern.compile("<input type='hidden' id='SESSION_ID' name='SESSION_ID' value='(.{32})' />");
	
	// Log.d(TAG, "match sessionId="+AWCCPrototypeAsyncTask.this.sessionId[0]);
	
	private static final Pattern IMAGE_SIZE_LIST_START_PATTERN = 
			Pattern.compile(".*<select name='IMAGE_SIZE' id='IMAGE_SIZE' .*>.*");
	private static final Pattern IMAGE_SIZE_LIST_PATTERN = 
			Pattern.compile(".*<option value='(.+) (.+)'.*>(.+)x(.+)</option>.*");
	
	private static final Pattern PREVIEW_INTERVAL_LIST_START_PATTERN = 
			Pattern.compile(".*<select name='PREVIEW_INTERVAL' id='PREVIEW_INTERVAL' .*>.*");
	private static final Pattern PREVIEW_INTERVAL_LIST_PATTERN = 
			Pattern.compile(".*<option value='(.+)' .*>(.+) msec</option>.*");
	
	private String[] sessionId = new String[1];
	
	protected final ProgressBar progressBar;
	private CallbackHttpContentImplForConnect connectImpl;
	private CallbackHttpContentImplForPost postImpl;
	private CallbackHttpContentImplForGetImage imageImpl;
	public int count = 0;
	
	public AWCCPrototypeAsyncTask(final ProgressBar progressBar) {
		this.progressBar = progressBar;
		this.connectImpl = new CallbackHttpContentImplForConnect();
		this.postImpl = new CallbackHttpContentImplForPost();
		this.imageImpl = new CallbackHttpContentImplForGetImage();
	}
	
	//@Override
	public void onPreExecute(final Handler mHandler) {
		Log.d(TAG, "onPreExecute start.");
		
		mHandler.post( new Runnable() {
			@Override
			public void run() {
				AWCCPrototypeAsyncTask.this.progressBar.setVisibility(View.VISIBLE);
				AWCCPrototypeAsyncTask.this.progressBar.setIndeterminate(false);
//				this.progressBar.setIndeterminate(true);
			}
		});
	}
	
	//@Override
	public void onCancelled() {
		Log.d(TAG, "onCancelled start.");
		
		synchronized (this.sessionId) {
			try {
				// キャプチャ停止
				this.postImpl.setPostData("IMAGE_SIZE=640+480&PREVIEW_INTERVAL=250&POST_TYPE=stop&CAMERA_TYPE=mjpg&PREVIEW_ST=start");
				this.postImpl.connect();
				
				this.sessionId[0] = null;
				
			} catch(IOException ie) {
				ie.printStackTrace();
			}
		}
	}
	
	//@Override
	protected void onCancelled(Bitmap result) {}
	
	//@Override
	public void onPostExecute(final Handler mHandler, final Bitmap result) {
		Log.d(TAG, "onPostExecute start.");
		mHandler.post( new Runnable() {
			@Override
			public void run() {
				AWCCPrototypeAsyncTask.this.progressBar.setVisibility(View.GONE);
			}
		});
	}
	
	//@Override
	protected void onProgressUpdate(final Handler mHandler, final Integer... values) {
		Log.d(TAG, "onProgressUpdate start.");
		
		mHandler.post( new Runnable() {
			@Override
			public void run() {
				if(AWCCPrototypeAsyncTask.this.progressBar.getMax() != values[1]/*this.progressBar.isIndeterminate()*/){
//					AWCCPrototypeAsyncTask.this.progressBar.setIndeterminate(false);
					AWCCPrototypeAsyncTask.this.progressBar.setProgress(0);
					AWCCPrototypeAsyncTask.this.progressBar.setMax(values[1]);
				}
				AWCCPrototypeAsyncTask.this.progressBar.incrementProgressBy(values[0]);
				//AWCCPrototypeAsyncTask.this.progressBar.incrementSecondaryProgressBy(0);
			}
		});
	}
	
	//@Override
	public Bitmap doInBackground(Handler mHandler, String... arsgs) {
		Log.d(TAG, "doInBackground start.");
		
		synchronized (this.sessionId) {
			try {
				if(this.sessionId[0] == null || this.sessionId[0].equals("")) {
					Log.d(TAG, "sessionId、imageSizeList、PREVIEW_INTERVAL_LIST取得");
					this.connectImpl.connect();
					
					// 取得結果出力
					Log.d(TAG, "sessionId="+this.sessionId[0]);
					
					Log.d(TAG, "imageSizeList=");
					for(final String imageSize : this.connectImpl.imageSizeList){
						Log.d(TAG, imageSize);
					}
					Log.d(TAG, "previewIntervalList=");
					for(final String previewInterval : this.connectImpl.previewIntervalList){
						Log.d(TAG, previewInterval);
					}
					Log.d(TAG, "初期画像取得");
					this.imageImpl.connect();
					
					Log.d(TAG, "キャプチャ開始");
					this.postImpl.setPostData("IMAGE_SIZE=640+480&PREVIEW_INTERVAL=250&POST_TYPE=start&CAMERA_TYPE=mjpg&PREVIEW_ST=");
					this.postImpl.connect();
				}
				Log.d(TAG, "定期画像取得");
				this.imageImpl.connect();
				return getBitmap(mHandler);
				
			} catch(IOException ie) {
				ie.printStackTrace();
				return null;
			}
		}
	}
	
	private Bitmap getBitmap(Handler mHandler) throws IOException {
		try (final BufferedInputStream bi = new BufferedInputStream(this.imageImpl.in)) {
			
			final byte[] buffer = new byte[1 * 1024 * 10/*10 KByte*/];
			
			final List<Byte> byteList = new ArrayList<Byte>(); 
			int readByteCount;
			while((readByteCount  = bi.read(buffer)) != -1) {
				final byte[] readBytes = Arrays.copyOfRange(buffer, 0, readByteCount);
				for(final byte b : readBytes)
					byteList.add(b);
				
				onProgressUpdate(mHandler, readByteCount, this.imageImpl.contentLength);
			}
			
			final byte[] byteArray = new byte[byteList.size()];
			for (int index = 0; index < byteList.size(); index++) {
				byteArray[index] = byteList.get(index);
			}
			
			return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, null);
		}
	}
	
//	public String getSessionId() {
//		synchronized (this.sessionId) {
//			return sessionId[0];
//		}
//	}
//	
//	public void setSessionId(final String sessionId) {
//		synchronized (this.sessionId) {
//			this.sessionId[0] = sessionId;
//		}
//	}
	
	private enum HttpContentType {
		Text,Stream,
	}
	
	private abstract class CallbackHttpContentImpl {
		public abstract HttpContentType getContentType();
		public abstract String getURL();
//		public abstract String getIdPassword();
		
		public abstract boolean isPost();
		public String getPostData(){return null;};
		
		public void setInputStream(final InputStream in, final int contentLength) throws IOException {}
		public boolean addTextLine(final String line){return false;}
		
		public void connect() throws IOException
		{
			Authenticator.setDefault(new Authenticator(){
				@Override protected PasswordAuthentication getPasswordAuthentication(){
						return new PasswordAuthentication(ID, PASSWORD.toCharArray());
					}
				});
			
			final URL url = new URL(this.getURL());
			
			final HttpURLConnection uconn = (HttpURLConnection)url.openConnection();
			uconn.setRequestMethod(this.isPost() ? "POST" : "GET");
			if(this.isPost())
				uconn.setDoOutput(true);
			uconn.setDoInput(true);
//			uconn.setRequestProperty("Authorization", "Basic "
//					+ Base64.encode(this.getIdPassword().getBytes(), Base64.DEFAULT));
			uconn.setInstanceFollowRedirects(true);
			uconn.connect();
			
			if(this.getPostData() != null)
				try(final OutputStream os = uconn.getOutputStream();
						PrintStream ps = new PrintStream(os);){
					ps.print(this.getPostData());
				}
			
			switch(this.getContentType()){
			case Text:
				try(final InputStream in = (uconn.getResponseCode() >= HttpStatus.SC_BAD_REQUEST) ? uconn.getErrorStream() : uconn.getInputStream();
						final BufferedReader br = new BufferedReader(new InputStreamReader(in, "EUC-JP"));)
				{
					String line;
					while ((line = br.readLine()) != null) {
						if(!this.addTextLine(line))
							break;
					}
				}
				break;
			case Stream:
				this.setInputStream(uconn.getInputStream(), uconn.getContentLength());
				break;
			}
		}
	}
	
	private class CallbackHttpContentImplForConnect extends CallbackHttpContentImpl {
		
		private List<String> imageSizeList = new ArrayList<String>();
		private List<String> previewIntervalList = new ArrayList<String>();
		
		private boolean imageSizeListMatch = false;
		private boolean previewIntervalListMatch = false;
		
		@Override
		public HttpContentType getContentType() {
			return HttpContentType.Text;
		}
		@Override
		public String getURL() {
			return USBCAMERA_USE_SET_URL;
		}
//		@Override
//		public String getIdPassword() {
//			return ID_PASSWORD;
//		}
		@Override
		public boolean isPost() {
			return false;
		}
		
		@Override
		public boolean addTextLine(String line) {
			Log.v(TAG, line);
			
			if(this.imageSizeListMatch){
				final Matcher matcher1 = IMAGE_SIZE_LIST_PATTERN.matcher(line);
				if(matcher1.matches())
					this.imageSizeList.add(matcher1.group(1) + "," + matcher1.group(2));
				else
					this.imageSizeListMatch = false;
			}
			
			if(this.previewIntervalListMatch){
				final Matcher matcher1 = PREVIEW_INTERVAL_LIST_PATTERN.matcher(line);
				if(matcher1.matches())
					this.previewIntervalList.add(matcher1.group(1));
				else
					this.previewIntervalListMatch = false;
			}
			
			final Matcher matcher1 = IMAGE_SIZE_LIST_START_PATTERN.matcher(line);
			if(matcher1.matches()){
				this.imageSizeListMatch = true;
			}
			
			final Matcher matcher2 = PREVIEW_INTERVAL_LIST_START_PATTERN.matcher(line);
			if(matcher2.matches()){
				this.previewIntervalListMatch = true;
			}
			
			final Matcher matcher3 = SESSION_ID_PATTERN.matcher(line);
			if(matcher3.matches()){
				AWCCPrototypeAsyncTask.this.sessionId[0] = matcher3.group(1);
				Log.d(TAG, "match conn sessionId="+AWCCPrototypeAsyncTask.this.sessionId[0]);
				return false;
			}
			else
				return true;
		}
	}

	private class CallbackHttpContentImplForPost extends CallbackHttpContentImpl {
		
		private String postData;
		
		public void setPostData(String postData) {
			this.postData = postData;
		}
		
		@Override
		public HttpContentType getContentType() {
			return HttpContentType.Text;
		}
		@Override
		public String getURL() {
			return USBCAMERA_USE_SET_URL;
		}
//		@Override
//		public String getIdPassword() {
//			return ID_PASSWORD;
//		}
		@Override
		public boolean isPost() {
			return true;
		}
		@Override
		public String getPostData() {
			return this.postData+"&SESSION_ID="+AWCCPrototypeAsyncTask.this.sessionId[0];
		}
		@Override
		public boolean addTextLine(String line) {
			final Matcher matcher = SESSION_ID_PATTERN.matcher(line);
			if(matcher.matches()){
				AWCCPrototypeAsyncTask.this.sessionId[0] = matcher.group(1);
				Log.d(TAG, "match post sessionId="+AWCCPrototypeAsyncTask.this.sessionId[0]);
				return false;
			}
			else
				return true;
		}
	}
	
	private class CallbackHttpContentImplForGetImage extends CallbackHttpContentImpl {
		
		private InputStream in;
		private int contentLength;
		
		@Override
		public HttpContentType getContentType() {
			return HttpContentType.Stream;
		}
		@Override
		public String getURL() {
			return USBCAM_JPG_URL+(AWCCPrototypeAsyncTask.this.count == 0 ? "" : "?"+AWCCPrototypeAsyncTask.this.count);
		}
//		@Override
//		public String getIdPassword() {
//			return ID_PASSWORD;
//		}
		@Override
		public boolean isPost() {
			return false;
		}
		@Override
		public void setInputStream(final InputStream in, final int contentLength) throws IOException {
			this.in = in;
			this.contentLength = contentLength;
			
			AWCCPrototypeAsyncTask.this.count++;
			if(AWCCPrototypeAsyncTask.this.count > 100) AWCCPrototypeAsyncTask.this.count = 1;
		}
	}
}
