package jp.kght6123.smallappviewer.adapter;

import java.util.List;
import jp.kght6123.smallappcommon.application.SharedDataApplication.ClipDataItem;
import jp.kght6123.smallappviewer.R;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.sony.smallapp.SmallAppWindow.WindowState;
import com.sony.smallapp.SmallApplication;

/**
 * 【未完成】クリップボードデータをListVewで表示するためのAdapter
 * @author Hirotaka
 *
 */
public class ClipDataListItemAdapter extends ArrayAdapter<ClipDataItem>
{
	private static final String TAG = ClipDataListItemAdapter.class.getSimpleName();
	private LayoutInflater layoutInflater_;
	private final SmallApplication application;
//	private final Comparator<ClipDataItem> comparator;
	
	public ClipDataListItemAdapter(final SmallApplication application, int textViewResourceId, List<ClipDataItem> objects)
	{
		super(application, textViewResourceId, objects);
		this.application = application;
		this.layoutInflater_ = (LayoutInflater) application.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
//		this.comparator = new Comparator<ClipDataItem>()
//		{
//			@Override
//			public int compare(final ClipDataItem lcd,
//					final ClipDataItem rcd) {
//				return 0;
//			}
//		};
//		this.sort(comparator);
	}
	
	@SuppressLint({ "ViewHolder", "InflateParams" })
	@Override
	public View getView(final int position, View convertView, final ViewGroup parent)
	{
		// 特定の行(position)のデータを得る
		final ClipDataItem item = (ClipDataItem)getItem(position);
		
		// 常に新しく作る
		convertView = this.layoutInflater_.inflate(R.layout.smallapp_clipboard_listitem, null);
		
		Log.d(TAG, position+".ClipDataItem.ClipData="+item.getClipData().toString());
		Log.d(TAG, position+".ClipDataItem.Item="+item.getItem().toString());
		
		// LinearLayoutを取得
		final LinearLayout clipLinearLayout = (LinearLayout)convertView.findViewById(R.id.clipLinearLayout);
		final Button btnCopyClip = (Button)convertView.findViewById(R.id.btnCopyClip);
		final Button btnDelClipData = (Button)convertView.findViewById(R.id.btnDelClip);
		final Button btnDetailClip = (Button)convertView.findViewById(R.id.btnDetailClip);
		final LinearLayout clipVeil = (LinearLayout)convertView.findViewById(R.id.clipVeil);
		final TextView textClipTextView = (TextView)convertView.findViewById(R.id.textClipTextView);
		
		textClipTextView.setText(item.getIndex() + ". " + item.getItem().getText());
		
		btnCopyClip.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Log.d(TAG, "ClipDataのコピー");
					final ClipboardManager cm = (ClipboardManager) application.getSystemService(Context.CLIPBOARD_SERVICE);
					cm.setPrimaryClip(item.getClipData());
				}
			}
		);
		
		btnDelClipData.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Log.d(TAG, "ClipDataの削除");
					remove(item);
				}
			}
		);
		
		btnDetailClip.setOnClickListener
		(
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Log.d(TAG, "ClipDataの詳細");
					try
					{
						parent.getContext().startActivity(item.getItem().getIntent());
						ClipDataListItemAdapter.this.application.getWindow().setWindowState(WindowState.MINIMIZED);	// 最小化
					}
					catch (Exception e)
					{
						Toast.makeText(parent.getContext(), e.getClass().getSimpleName() + ":" +e.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		);
		
		clipLinearLayout.setOnClickListener
		(
			new View.OnClickListener()
			{
				private boolean controlView = false;
				
				@Override
				public synchronized void onClick(View v)
				{
					if(this.controlView)
					{
						btnCopyClip.setVisibility(View.GONE);
						btnDelClipData.setVisibility(View.GONE);
						btnDetailClip.setVisibility(View.GONE);
						clipVeil.setBackgroundColor(0x00000000);
						this.controlView = false;
					}
					else
					{
						btnCopyClip.setVisibility(View.VISIBLE);
						btnDelClipData.setVisibility(View.VISIBLE);
						
						if(item.getItem().getIntent() != null)
							btnDetailClip.setVisibility(View.VISIBLE);
						
						clipVeil.setBackgroundColor(0x44000000);
						this.controlView = true;
					}
				}
			}
		);
		
		return convertView;
	}
	
	public synchronized ClipData remove(final int index)
	{
		return this.remove(index);
	}
}
