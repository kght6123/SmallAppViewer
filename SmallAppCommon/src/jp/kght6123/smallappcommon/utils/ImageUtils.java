package jp.kght6123.smallappcommon.utils;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class ImageUtils
{
	private final static String TAG = ImageUtils.class.getSimpleName();
	
	public static int dp2Px(final int dp, final Context context)
	{
		final float density = context.getResources().getDisplayMetrics().density;// density (比率)を取得する
		return (int) ( ((float)dp) * density + 0.5f );
	}
	
	/**
	 * 読み込む画像サイズを制限する
	 * 
	 * @param file
	 * @return
	 */
	public static Bitmap getResizeBitmap(final File file, final int size/* 縮小して読み込む基準*/)
	{
		if(file == null)
			return null;
		
		BitmapFactory.Options imageOptions;
		
		// メモリ上に画像を読み込まず、画像サイズ情報のみを取得する
		imageOptions = new BitmapFactory.Options();				   
		imageOptions.inJustDecodeBounds = true;// 画像サイズのみ
		BitmapFactory.decodeFile(file.getPath(), imageOptions);
		
		// もし読み込む画像が大きかったら縮小して読み込む
		if (imageOptions.outWidth > size || imageOptions.outHeight > size)
		{
			final int inSampleSize;
			if(imageOptions.outWidth > imageOptions.outHeight)
			{
				final double inSampleSizeTmp = imageOptions.outWidth/size;
				inSampleSize = (int)Math.floor(inSampleSizeTmp/(double)2)*2;
			}
			else
			{
				final double inSampleSizeTmp = imageOptions.outHeight/size;
				inSampleSize = (int)Math.floor(inSampleSizeTmp/(double)2)*2;
			}
			imageOptions = new BitmapFactory.Options();
			imageOptions.inSampleSize = inSampleSize;
			return BitmapFactory.decodeFile(file.getPath(), imageOptions);
		}
		else
		{
			return BitmapFactory.decodeFile(file.getPath(), null);
		}
	}
	
	/**
	 * 正方形のBitmap画像に加工する
	 * 
	 * @param px
	 * @param bitmap
	 * @return
	 */
	public static Bitmap getSquareBitmap(final int px, final Bitmap bitmap)
	{
		// BITMAPをリサイズ&クリップして1:1のサムネイル作成
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		Log.d(TAG, "width="+width);
		Log.d(TAG, "height="+height);
		
		if(width > height)
		{// 横幅が広い
			final Matrix matrix = new Matrix();
			final float scale = (float)px / (float)width;
			Log.d(TAG, "scale="+String.valueOf(scale));
			matrix.postScale(scale, scale);
			final int x = (int)(width-height)/2;
			Log.d(TAG, "x="+x);
			return Bitmap.createBitmap(bitmap, x, 0, width-x*2, height, matrix, true);
		}
		else if(width < height)
		{// 縦幅が広い
			final Matrix matrix = new Matrix();
			final float scale = (float)px / (float)height;
			Log.d(TAG, "scale="+String.valueOf(scale));
			matrix.postScale(scale, scale);
			final int y = (int)(height-width)/2;
			Log.d(TAG, "y="+y);
			return Bitmap.createBitmap(bitmap, 0, y, width, height-y*2, matrix, true);
		}
		else
		{// １：１のとき
			final Matrix matrix = new Matrix();
			final float scale = (float)px / (float)height;
			Log.d(TAG, "scale="+String.valueOf(scale));
			matrix.postScale(scale, scale);
			return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		}
	}
}
