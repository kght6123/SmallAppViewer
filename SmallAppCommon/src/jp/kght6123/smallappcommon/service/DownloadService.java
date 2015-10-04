package jp.kght6123.smallappcommon.service;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * 指定されたURLのファイルをダウンロードするサービス
 * 
 * @author Hirotaka
 *
 */
public class DownloadService extends IntentService
{
	private static final String TAG = DownloadService.class.getSimpleName();
	
	public DownloadService()
	{
		super(DownloadService.class.getSimpleName());
	}
	
	@Override
	protected void onHandleIntent(final Intent intent)
	{
		try
		{
			final Bundle bundle = intent.getExtras();
			if(bundle == null)
			{
				Log.d(TAG, "bundle == null");
				return;
			}
			final String urlString = bundle.getString("url");
			
			// HTTP Connection
			final URL url = new URL(urlString);
			final String fileName = getFilenameFromURL(url);
			Log.d(TAG, fileName);
			final URLConnection conn = url.openConnection();
			
			final HttpURLConnection httpConn = (HttpURLConnection)conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();
			final int response = httpConn.getResponseCode();
			
			// Check Response
			if(response != HttpURLConnection.HTTP_OK)
				throw new HttpException();
			
			final int contentLength = httpConn.getContentLength();
			
			final InputStream in = httpConn.getInputStream();
			
			final File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SmallAppViewer");
			downloadDir.mkdirs();
			
			final FileOutputStream outStream = 
					new FileOutputStream(new File(downloadDir, fileName), false)/*openFileOutput(fileName, MODE_PRIVATE)*/;
			
			final DataInputStream dataInStream = new DataInputStream(in);
			final DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(outStream));
			
			// Read Data
			final byte[] b= new byte[4096];
			int readByte = 0, totalByte = 0;
			
			while(-1 != (readByte = dataInStream.read(b)))
			{
				dataOutStream.write(b, 0, readByte);
				totalByte += readByte;
				sendProgressBroadcast(contentLength,totalByte,	fileName);
			}
			
			dataInStream.close();
			dataOutStream.close();
			
			if(contentLength < 0)
				sendProgressBroadcast(totalByte,totalByte,fileName);
		}
		catch (IOException e)
		{
			Log.d(TAG, "IOException", e);
			Toast.makeText(this, e.getClass().getSimpleName()+", "+e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		catch (HttpException e)
		{
			Log.d(TAG, "HttpException", e);
			Toast.makeText(this, e.getClass().getSimpleName()+", "+e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	protected void sendProgressBroadcast
	(
		final int contentLength, 
		final int totalByte, 
		final String filename
	)
	{
		final Intent broadcastIntent = new Intent();
		final int completePercent = contentLength < 0 ? -1 : ((totalByte*1000)/(contentLength*10));
		Log.d(TAG, "completePercent = " + completePercent);
		Log.d(TAG, "totalByte = " + totalByte);
		Log.d(TAG, "fileName = " + filename);
		
		broadcastIntent.putExtra("completePercent", completePercent);
		broadcastIntent.putExtra("totalByte", totalByte);
		broadcastIntent.putExtra("filename", filename);
		broadcastIntent.setAction("DOWNLOAD_PROGRESS_ACTION");
		
		getBaseContext().sendBroadcast(broadcastIntent);
	}
	
	protected String getFilenameFromURL(URL url)
	{
		final String[] p = url.getFile().split("/");
		final String s = p[p.length-1];
		
		if(s.indexOf("?") > -1)
			return s.substring(0, s.indexOf("?"));
		else
			return s;
	}
}
