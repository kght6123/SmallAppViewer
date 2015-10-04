package jp.kght6123.smallappcommon.utils;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.widget.Toast;

public class FileUtils
{
	private static final String TAG = FileUtils.class.getSimpleName();
	
	public static void copyFile(final File fromFile, final File toFile, final Context context, final boolean compToast)
	{
		try
		{
			final FileInputStream fis = new FileInputStream(fromFile);
			final FileOutputStream fos = new FileOutputStream(toFile);
			
			final FileChannel iChannel = fis.getChannel();
			final FileChannel oChannel = fos.getChannel();
			iChannel.transferTo(0, iChannel.size(), oChannel);
			iChannel.close();
			oChannel.close();
			fis.close();
			fos.close();
			
			if(compToast)
				Toast.makeText(context, "ï€ë∂ÇµÇ‹ÇµÇΩÅB"+toFile.getPath(), Toast.LENGTH_SHORT).show();
		}
		catch (FileNotFoundException e)
		{
			Log.w(TAG, "file copy error.", e);
			if(compToast)
				Toast.makeText(context, e.getClass().getSimpleName()+", "+e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		catch (IOException e)
		{
			Log.w(TAG, "file copy error.", e);
			if(compToast)
				Toast.makeText(context, e.getClass().getSimpleName()+", "+e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	public static File toFile(final Uri uri, final Context context)
	{
		final String scheme = uri.getScheme();
		
		String path = null;
		if ("file".equals(scheme))
			path = uri.getPath();
		
		else if("content".equals(scheme))
		{
			final ContentResolver contentResolver = context.getContentResolver();
			final Cursor cursor = contentResolver.query(uri, new String[] { MediaColumns.DATA }, null, null, null);
			if (cursor != null)
			{
				cursor.moveToFirst();
				path = cursor.getString(0);
				cursor.close();
			}
		}
		return (null == path ? null : new File(path));
	}
	
	public static void saveFile(final InputStream is, final File outFile) throws IOException
	{
		DataInputStream dataInStream = null;
		DataOutputStream dataOutStream = null;
		try
		{
			// Input Stream
			dataInStream = new DataInputStream(is);
			
			// Output Stream
			dataOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
			// Read Data
			byte[] b = new byte[4096];
			int readByte = 0;
			  
			while(-1 != (readByte = dataInStream.read(b))){
				dataOutStream.write(b, 0, readByte);
			}
		}
		finally
		{
			if(dataInStream != null)
				dataInStream.close();
			if(dataOutStream != null)
				dataOutStream.close();
		}
	}
	
	public static String getDir(final String path)
	{
		String dir = "/";
		final String[] paths = path.split("/");
		for(int x = 0; x < paths.length-1; x++)
		{
			dir+=(paths[x]+"/");
		}
		return dir;
	}
}
