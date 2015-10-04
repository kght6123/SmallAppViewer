package jp.kght6123.smallappviewer.dialog;

import com.sony.smallapp.SmallAppNotFoundException;
import com.sony.smallapp.SmallApplicationManager;

import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.smallapp.SmallMultiBrowserApplication;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SmallMultiBrowserSplitDialogFragment extends DialogFragment
{
	public static final String SPLIT_MODE = "SPLIT_MODE";
	public boolean finish = true;
	
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState)
	{
		final DialogInterface.OnClickListener listener = 
				new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					if(which == 0)
					{// •ªŠ„‚µ‚È‚¢‚Æ‚«
						final Intent intent = new Intent(getActivity().getIntent());
						intent.setClass(getActivity(), SmallMultiBrowserApplication.class);
						
						try
						{
							SmallApplicationManager.startApplication(getActivity(), intent);
						}
						catch (SmallAppNotFoundException e)
						{
							Toast.makeText(getActivity(), e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}
					}
					else
					{// •ªŠ„‚·‚é‚Æ‚«
						setSplitMode(which, getActivity());
						
						final SmallMultiBrowserPositionDialogFragment fragment = new SmallMultiBrowserPositionDialogFragment();
						fragment.show(getFragmentManager(), "position_select_dialog");
						
						SmallMultiBrowserSplitDialogFragment.this.finish = false;
					}
				}
			};
		
		final AlertDialog.Builder myDialogBuilder = new AlertDialog.Builder(getActivity());
		myDialogBuilder
			.setTitle(R.string.app_multibrowser_select_split_mode)
			.setItems(R.array.multibrowser_split_list, listener)
			.setCancelable(true);
		
		final AlertDialog dialog = myDialogBuilder.create();
		dialog.setCanceledOnTouchOutside(true);
		
		return dialog;
	}
	
	public static int getSplitMode(Context context)
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		final String spitModeSetting = pref.getString(context.getString(R.string.multibrowser_4split_mode_key), "");
		
		final SharedPreferences sPref = context.getSharedPreferences(SmallMultiBrowserSplitDialogFragment.class.getName(), Context.MODE_PRIVATE);
		final int splitMode = sPref.getInt(SPLIT_MODE, -1);
		
		final TypedArray ta = 
				context.getResources().obtainTypedArray(R.array.multibrowser_split_setting_value_list);
		if(spitModeSetting.equals("") || ta.getString(4).equals(spitModeSetting))
			return splitMode;
		else if(ta.getString(0).equals(spitModeSetting))
			return 0;
		else if(ta.getString(1).equals(spitModeSetting))
			return 1;
		else if(ta.getString(2).equals(spitModeSetting))
			return 2;
		else if(ta.getString(3).equals(spitModeSetting))
			return 3;
		else
			return 0;
	}
	
	public static void setSplitMode(int splitMode, Context context)
	{
		final SharedPreferences sPref = context.getSharedPreferences(SmallMultiBrowserSplitDialogFragment.class.getName(), Context.MODE_PRIVATE);
		
		final SharedPreferences.Editor editor = sPref.edit();
		editor.putInt(SPLIT_MODE, splitMode);
		editor.apply();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		
		if(this.finish)
			getActivity().finish();
	}

}
