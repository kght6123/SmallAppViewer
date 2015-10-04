package jp.kght6123.smallappviewer.pref;

import jp.kght6123.smallappviewer.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SmallAppViewerPreferenceFragment extends PreferenceFragment
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefes);
	}
}
