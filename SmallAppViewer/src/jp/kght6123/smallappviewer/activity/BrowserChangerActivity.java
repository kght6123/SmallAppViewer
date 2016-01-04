package jp.kght6123.smallappviewer.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * HTTP送信のIntentを受け取ってブラウザを選択させるActivity
 * 
 * @author Hirotaka
 *
 */
public class BrowserChangerActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		
		{
			// ブラウザアプリ一覧を取得する
			final PackageManager pm = this.getPackageManager();
			
			final Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.addCategory(Intent.CATEGORY_BROWSABLE);
			intent.setData(this.getIntent().getData());
			
			//intent.setAction(this.getIntent().getAction());
			//if(this.getIntent().getCategories() != null)
			//	for(final String category : this.getIntent().getCategories())
			//		intent.addCategory(category);
			
			// カテゴリとアクションに一致するアクティビティの情報を取得する
			final List<ResolveInfo> items = pm.queryIntentActivities(intent, 0);
			final ResolveInfoListAdapter adapter = new ResolveInfoListAdapter(this, items);
			
			// リストダイアログを表示する
			// 選択→そのブラウザで表示、長押し選択→ブラウザのアプリ設定
			final AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog);
			builder.setCustomTitle(null)/*setTitle(getResources().getText(R.string.app_browser_changer_dialog_title))*/
					.setAdapter(adapter, new OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int which) {
							
							final ResolveInfo info = items.get(which);
							final Intent intent = new Intent();
							intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
							intent.setAction(Intent.ACTION_VIEW);
							intent.addCategory(Intent.CATEGORY_DEFAULT);
							intent.addCategory(Intent.CATEGORY_BROWSABLE);
							intent.setData(getIntent().getData());
							startActivity(intent);
							finish();
						}
					}).setOnDismissListener(new OnDismissListener(){
						@Override
						public void onDismiss(final DialogInterface in) {
							finish();
						}
					}).setOnCancelListener(new OnCancelListener(){
						@Override
						public void onCancel(DialogInterface arg0) {
							finish();
						}
					});
			
			//builder.show();
			
			final AlertDialog dialog = builder.create();
			final WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
			wmlp.gravity=Gravity.BOTTOM;
			//wmlp.y=50;//中心から下方向に50pxずらす
			dialog.getWindow().setAttributes(wmlp);
			dialog.show();
		}
	}
	
	/**
	 * アイコン付き用リストのアダプター
	 */
	public class ResolveInfoListAdapter extends ArrayAdapter<ResolveInfo> {
		private final Context context;
		private final PackageManager packageManager;
		
		public ResolveInfoListAdapter(final Context context, final List<ResolveInfo> itmes) {
			super(context, android.R.layout.select_dialog_item, android.R.id.text1, itmes);
			this.context = context;
			this.packageManager = context.getPackageManager();
		}
		
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			
			final ResolveInfo item = this.getItem(position);
			final String title = item.loadLabel(this.packageManager).toString();
			
			final Drawable icon = item.loadIcon(this.packageManager);
			icon.setBounds(0, 0, (int)(icon.getIntrinsicWidth() / 1.25f), (int)(icon.getIntrinsicHeight() / 1.25f));
			
			final View v = super.getView(position, convertView, parent);
			final TextView tv = (TextView)v.findViewById(android.R.id.text1);
			
			//Put the image on the TextView
			tv.setText(title);
			tv.setCompoundDrawables/*WithIntrinsicBounds*/(icon, null, null, null);
			//tv.setTextColor(this.context.getResources().getColorStateList(android.R.color.secondary_text_light));
			tv.setTextColor(0xFF666666);
			tv.setTextSize(6 * this.context.getResources().getDisplayMetrics().density + 0.5f);
			
			//Add margin between image and text (support various screen densities)
			final int dp5 = (int) (9 * this.context.getResources().getDisplayMetrics().density + 0.5f);
			tv.setCompoundDrawablePadding(dp5);
			
//			tv.setOnLongClickListener(new OnLongClickListener(){
//				@Override
//				public boolean onLongClick(final View view) {
//					final Intent intent=new Intent();
//					intent.setData(Uri.fromParts("package",item.activityInfo.packageName,null));
//					intent.setComponent(ComponentName.unflattenFromString("com.android.settings/.applications.InstalledAppDetails"));
//					startActivity(intent);
//					finish();
//					return true;
//				}
//			});
			return v;
		}
	}
}
