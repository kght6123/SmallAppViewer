package jp.kght6123.smallappviewer.dialog;

import jp.kght6123.smallappviewer.R;
import jp.kght6123.smallappviewer.smallapp.SmallMultiBrowserApplication;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.sony.smallapp.SmallAppNotFoundException;
import com.sony.smallapp.SmallApplicationManager;

public class SmallMultiBrowserPositionDialogFragment extends DialogFragment
{
	private static String TAG = SmallMultiBrowserPositionDialogFragment.class.getSimpleName();
	
	public static final String SPLIT_POSITON = "SPLIT_POSITON";
	public static final String SPLIT_POSITON_URL_ = "SPLIT_POSITON_URL_";
	
	private int prevWhich = 0;
	
	private class ConfirmOnClickListener implements DialogInterface.OnClickListener
	{
		private final boolean doubleclick;
		
		private ConfirmOnClickListener(boolean doubleclick)
		{
			this.doubleclick = doubleclick;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			if(this.doubleclick && SmallMultiBrowserPositionDialogFragment.this.prevWhich != which)
			{
				SmallMultiBrowserPositionDialogFragment.this.prevWhich = which;
				return;
			}
			
			if(which == -1)
				setSplitPositionUrl(getActivity(), getActivity().getIntent().getDataString(), SmallMultiBrowserPositionDialogFragment.this.prevWhich);
			else
				setSplitPositionUrl(getActivity(), getActivity().getIntent().getDataString(), which);
			
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
			dialog.cancel();
		}
	};
	
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState)
	{
		int splitMode = SmallMultiBrowserSplitDialogFragment.getSplitMode(getActivity());
		
		final AlertDialog.Builder myDialogBuilder = new AlertDialog.Builder(getActivity());
		myDialogBuilder
			.setTitle(R.string.app_multibrowser_select_position)
			.setCancelable(true);
		
		if(splitMode == 1)
		{// è„â∫ï™äÑ
			myDialogBuilder.setSingleChoiceItems(R.array.multibrowser_position_top_bottom_list, 0, new ConfirmOnClickListener(true));
		}
		else if(splitMode == 2)
		{// ç∂âEï™äÑ
			myDialogBuilder.setSingleChoiceItems(R.array.multibrowser_position_right_left_list, 0, new ConfirmOnClickListener(true));
		}
		else if(splitMode == 3)
		{// ÇSï™äÑ
			myDialogBuilder.setSingleChoiceItems(R.array.multibrowser_position_4split_list, 0, new ConfirmOnClickListener(true));
		}
		
		myDialogBuilder.setPositiveButton(R.string.app_multibrowser_select_open, new ConfirmOnClickListener(false));
		myDialogBuilder.setNeutralButton(R.string.app_multibrowser_select_save, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				setSplitPositionUrl(getActivity(), getActivity().getIntent().getDataString(), SmallMultiBrowserPositionDialogFragment.this.prevWhich);
			}
		});
		
		final AlertDialog dialog = myDialogBuilder.create();
		dialog.setCanceledOnTouchOutside(true);
		
		return dialog;
	}
	
	public static String getSplitPositionUrl(Context context, int position)
	{
		final SharedPreferences sPref = context.getSharedPreferences(SmallMultiBrowserPositionDialogFragment.class.getName(), Context.MODE_PRIVATE);
		return sPref.getString(SPLIT_POSITON_URL_+position, "");
	}
	
	public static void setSplitPositionUrl(Context context, String url, int position)
	{
		Log.d(TAG, "setSplitPositionUrl:url="+url+",positon="+position);
		
		final SharedPreferences sPref = context.getSharedPreferences(SmallMultiBrowserPositionDialogFragment.class.getName(), Context.MODE_PRIVATE);
		
		final SharedPreferences.Editor editor = sPref.edit();
		editor.putString(SPLIT_POSITON_URL_+position, url);
		editor.apply();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		getActivity().finish();
	}

}
