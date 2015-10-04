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
		final float density = context.getResources().getDisplayMetrics().density;// density (�䗦)���擾����
		return (int) ( ((float)dp) * density + 0.5f );
	}
	
	/**
	 * �ǂݍ��މ摜�T�C�Y�𐧌�����
	 * 
	 * @param file
	 * @return
	 */
	public static Bitmap getResizeBitmap(final File file, final int size/* �k�����ēǂݍ��ފ*/)
	{
		if(file == null)
			return null;
		
		BitmapFactory.Options imageOptions;
		
		// ��������ɉ摜��ǂݍ��܂��A�摜�T�C�Y���݂̂��擾����
		imageOptions = new BitmapFactory.Options();				   
		imageOptions.inJustDecodeBounds = true;// �摜�T�C�Y�̂�
		BitmapFactory.decodeFile(file.getPath(), imageOptions);
		
		// �����ǂݍ��މ摜���傫��������k�����ēǂݍ���
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
	 * �����`��Bitmap�摜�ɉ��H����
	 * 
	 * @param px
	 * @param bitmap
	 * @return
	 */
	public static Bitmap getSquareBitmap(final int px, final Bitmap bitmap)
	{
		// BITMAP�����T�C�Y&�N���b�v����1:1�̃T���l�C���쐬
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		Log.d(TAG, "width="+width);
		Log.d(TAG, "height="+height);
		
		if(width > height)
		{// �������L��
			final Matrix matrix = new Matrix();
			final float scale = (float)px / (float)width;
			Log.d(TAG, "scale="+String.valueOf(scale));
			matrix.postScale(scale, scale);
			final int x = (int)(width-height)/2;
			Log.d(TAG, "x="+x);
			return Bitmap.createBitmap(bitmap, x, 0, width-x*2, height, matrix, true);
		}
		else if(width < height)
		{// �c�����L��
			final Matrix matrix = new Matrix();
			final float scale = (float)px / (float)height;
			Log.d(TAG, "scale="+String.valueOf(scale));
			matrix.postScale(scale, scale);
			final int y = (int)(height-width)/2;
			Log.d(TAG, "y="+y);
			return Bitmap.createBitmap(bitmap, 0, y, width, height-y*2, matrix, true);
		}
		else
		{// �P�F�P�̂Ƃ�
			final Matrix matrix = new Matrix();
			final float scale = (float)px / (float)height;
			Log.d(TAG, "scale="+String.valueOf(scale));
			matrix.postScale(scale, scale);
			return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		}
	}
}
