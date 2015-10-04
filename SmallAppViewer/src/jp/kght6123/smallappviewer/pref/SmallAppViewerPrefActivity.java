package jp.kght6123.smallappviewer.pref;

import android.app.Activity;
import android.os.Bundle;

public class SmallAppViewerPrefActivity extends Activity
{
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.smallapp_pref);
		
		getFragmentManager()
			.beginTransaction()
				.replace(android.R.id.content, new SmallAppViewerPreferenceFragment()).commit(); 
	}

}
