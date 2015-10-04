package jp.kght6123.smallappcommon.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

public class PrefUtils
{
	public static void setVisibility(final int resIdKey, final View view, final boolean defaultValue, final Context context, final SharedPreferences pref)
	{
		if(!pref.getBoolean(context.getString(resIdKey), defaultValue))
			view.setVisibility(View.GONE);
	}

	public static void setInt(final String key, final int value, final Class<?> classObj, final Context context)
	{
		final SharedPreferences sPref = context.getSharedPreferences(classObj.getName(), Context.MODE_PRIVATE);
		
		final SharedPreferences.Editor editor = sPref.edit();
		editor.putInt(key, value);
		editor.apply();
	}
	
	public static int getInt(final String key, final int value, final Class<?> classObj, final Context context)
	{
		final SharedPreferences sPref = 
				context.getSharedPreferences(classObj.getName(), Context.MODE_PRIVATE);
		
		return sPref.getInt(key, value);
	}
	
	public static void setLong(final String key, final long value, final Class<?> classObj, final Context context)
	{
		final SharedPreferences sPref = context.getSharedPreferences(classObj.getName(), Context.MODE_PRIVATE);
		
		final SharedPreferences.Editor editor = sPref.edit();
		editor.putLong(key, value);
		editor.apply();
	}
	
	public static long getLong(final String key, final long value, final Class<?> classObj, final Context context)
	{
		final SharedPreferences sPref = 
				context.getSharedPreferences(classObj.getName(), Context.MODE_PRIVATE);
		
		return sPref.getLong(key, value);
	}
	
	public static float getDefaultPercent(final int resIdKey, final float value, final Context context)
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		final String floatValue = pref.getString(context.getString(resIdKey), Float.toString(value*100));
		
		return (float)((float)Float.parseFloat(floatValue) / 100f);
	}
	
	public static boolean getDefaultBoolean(final int resIdKey, final boolean value, final Context context)
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getBoolean(context.getString(resIdKey), value);
	}
	
	public static String getDefaultString(final int resIdKey, final String value, final Context context)
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(context.getString(resIdKey), value);
	}
	
	public static Set<String> getDefaultStringArray(final int resIdKey, final int resIdDefValues, final Context context)
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		final HashSet<String> hashSet = new HashSet<String>(Arrays.asList(context.getResources().getStringArray(resIdDefValues)));
		
		return pref.getStringSet(context.getString(resIdKey), hashSet);
	}
	
}
