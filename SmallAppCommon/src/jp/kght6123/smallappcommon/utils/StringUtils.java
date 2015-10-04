package jp.kght6123.smallappcommon.utils;

public class StringUtils
{
	public static boolean equals(final String s1, final String s2)
	{
		if(s1 == null && s2 == null)
			return true;
		else if(s1 == null || s2 == null)
			return false;
		else
			return s1.equals(s2);
	}

	public static boolean isNotEmpty(String s)
	{
		return (s != null && !s.equals(""));
	}
}
