package edu.mit.mobile.android.demomode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.example.android.home.ApplicationInfo;

public class DemoMode extends FragmentActivity implements LoaderCallbacks<Cursor>,
		OnItemClickListener, OnItemLongClickListener {
	private static final String TAG = DemoMode.class.getSimpleName();

	private GridView mGridView;
	private GridView mAllApps;

	private LauncherItemAdapter mAdapter;

	private static ArrayList<ApplicationInfo> mApplications;

	private boolean mLocked;
	private String mPassword;
	private SharedPreferences mPrefs;

	private SlidingDrawer mDrawer;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mGridView = (GridView) findViewById(R.id.grid);

		getSupportLoaderManager().initLoader(0, null, this);
		mAdapter = new LauncherItemAdapter(this, R.layout.launcher_item, null);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);

		mAllApps = (GridView) findViewById(R.id.all_apps);

		loadApplications(true);

		mDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer1);

		mAllApps.setAdapter(new ApplicationsAdapter(this, mApplications));

		mAllApps.setOnItemClickListener(this);
		mAllApps.setOnItemLongClickListener(this);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		mPassword = mPrefs.getString(Preferences.KEY_PASSWORD, null);
		mLocked = mPrefs.getBoolean(Preferences.KEY_LOCKED, false);

		updateLocked();
	}

	/**
	 * Loads the list of installed applications in mApplications.
	 */
	private void loadApplications(boolean isLaunching) {
		if (isLaunching && mApplications != null) {
			return;
		}

		final PackageManager manager = getPackageManager();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
		Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

		if (apps != null) {
			final int count = apps.size();

			if (mApplications == null) {
				mApplications = new ArrayList<ApplicationInfo>(count);
			}
			mApplications.clear();

			for (int i = 0; i < count; i++) {
				final ApplicationInfo application = new ApplicationInfo();
				final ResolveInfo info = apps.get(i);

				application.title = info.loadLabel(manager);
				application.setActivity(new ComponentName(
						info.activityInfo.applicationInfo.packageName, info.activityInfo.name),
						Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				application.icon = info.activityInfo.loadIcon(manager);

				mApplications.add(application);
			}
		}
	}

	private static class LauncherItemAdapter extends CursorAdapter {
		private final LayoutInflater mLayoutInflater;
		private final int mLayout;
		private final PackageManager mPackageManager;
		private final int mAppIconWidth;
		private final int mAppIconHeight;

		public LauncherItemAdapter(Context context, int layout, Cursor c) {
			super(context, c, 0);
			mLayout = layout;
			mLayoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			mPackageManager = context.getPackageManager();

			final Resources resources = context.getResources();
			mAppIconWidth = (int) resources.getDimension(android.R.dimen.app_icon_size);
			mAppIconHeight = (int) resources.getDimension(android.R.dimen.app_icon_size);
		}

		private Drawable scaleDrawableToAppIconSize(Drawable icon) {
			int width = mAppIconWidth;
			int height = mAppIconHeight;

			final int iconWidth = icon.getIntrinsicWidth();
			final int iconHeight = icon.getIntrinsicHeight();

			if (width > 0 && height > 0 && (width < iconWidth || height < iconHeight)) {
				final float ratio = (float) iconWidth / iconHeight;

				if (iconWidth > iconHeight) {
					height = (int) (width / ratio);
				} else if (iconHeight > iconWidth) {
					width = (int) (height * ratio);
				}
			}
			icon.setBounds(0, 0, width, height);
			return icon;
		}

		@Override
		public void bindView(View v, Context context, Cursor c) {
			final String pkg = c.getString(c.getColumnIndex(LauncherItem.PACKAGE_NAME));
			final String cls = c.getString(c.getColumnIndex(LauncherItem.ACTIVITY_NAME));
			final ComponentName activity = new ComponentName(pkg, cls);
			try {
				final ActivityInfo i = mPackageManager.getActivityInfo(activity, 0);
				final android.content.pm.ApplicationInfo appInfo = mPackageManager
						.getApplicationInfo(pkg, 0);
				final TextView label = (TextView) v.findViewById(R.id.label);
				final Drawable icon = i.loadIcon(mPackageManager);

				scaleDrawableToAppIconSize(icon);

				label.setCompoundDrawables(null, icon, null, null);

				label.setText(mPackageManager.getText(cls,
						i.labelRes == 0 ? i.applicationInfo.labelRes : i.labelRes, appInfo));

			} catch (final NameNotFoundException e) {
				e.printStackTrace();
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View v = mLayoutInflater.inflate(mLayout, parent, false);
			bindView(v, context, cursor);
			return v;
		}

	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(this, LauncherItem.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		mAdapter.swapCursor(c);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		menu.findItem(R.id.lock).setVisible(!mLocked);
		menu.findItem(R.id.unlock).setVisible(mLocked);
		menu.findItem(R.id.settings).setVisible(!mLocked);
		menu.findItem(R.id.preferences).setVisible(!mLocked);

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		if (!mLocked) {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings:
				startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
				return true;

			case R.id.lock:
				setLocked(true);
				return true;

			case R.id.unlock:
				showDialog(DIALOG_PASSWORD, null);
				return true;

			case R.id.preferences:
				startActivity(new Intent(this, Preferences.class));
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}



	private void setLocked(boolean locked) {
		mPrefs.edit().putBoolean(Preferences.KEY_LOCKED, locked).commit();

		mLocked = locked;

		updateLocked();
	}

	private void updateLocked() {
		findViewById(R.id.slidingDrawer1).setVisibility(mLocked ? View.GONE : View.VISIBLE);
		if (mLocked) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	/**
	 * GridView adapter to show the list of all installed applications.
	 */
	private class ApplicationsAdapter extends ArrayAdapter<ApplicationInfo> {
		private final Rect mOldBounds = new Rect();
		private final int mAppIconHeight;
		private final int mAppIconWidth;

		public ApplicationsAdapter(Context context, ArrayList<ApplicationInfo> apps) {
			super(context, 0, apps);
			final Resources resources = getContext().getResources();
			mAppIconWidth = (int) resources.getDimension(android.R.dimen.app_icon_size);
			mAppIconHeight = (int) resources.getDimension(android.R.dimen.app_icon_size);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ApplicationInfo info = mApplications.get(position);

			if (convertView == null) {
				final LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.launcher_item, parent, false);
			}

			Drawable icon = info.icon;

			if (!info.filtered) {
				int width = mAppIconWidth;
				int height = mAppIconHeight;

				final int iconWidth = icon.getIntrinsicWidth();
				final int iconHeight = icon.getIntrinsicHeight();

				if (icon instanceof PaintDrawable) {
					final PaintDrawable painter = (PaintDrawable) icon;
					painter.setIntrinsicWidth(width);
					painter.setIntrinsicHeight(height);
				}

				if (width > 0 && height > 0 && (width < iconWidth || height < iconHeight)) {
					final float ratio = (float) iconWidth / iconHeight;

					if (iconWidth > iconHeight) {
						height = (int) (width / ratio);
					} else if (iconHeight > iconWidth) {
						width = (int) (height * ratio);
					}

					final Bitmap.Config c = icon.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
							: Bitmap.Config.RGB_565;
					final Bitmap thumb = Bitmap.createBitmap(width, height, c);
					final Canvas canvas = new Canvas(thumb);
					canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));
					// Copy the old bounds to restore them later
					// If we were to do oldBounds = icon.getBounds(),
					// the call to setBounds() that follows would
					// change the same instance and we would lose the
					// old bounds
					mOldBounds.set(icon.getBounds());
					icon.setBounds(0, 0, width, height);
					icon.draw(canvas);
					icon.setBounds(mOldBounds);
					icon = info.icon = new BitmapDrawable(thumb);
					info.filtered = true;
				}
			}

			final TextView textView = (TextView) convertView.findViewById(R.id.label);
			textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
			textView.setText(info.title);

			return convertView;
		}
	}

	private static final int DIALOG_PASSWORD = 100;

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
			case DIALOG_PASSWORD: {
				final EditText v = (EditText) getLayoutInflater().inflate(R.layout.password, null);
				return new AlertDialog.Builder(this)
						.setView(v)
						.setCancelable(true)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										if (mPassword.equals(v.getText().toString())) {
											v.getEditableText().clear();
											setLocked(false);
										}
									}
								}).create();
			}
			default:
				return super.onCreateDialog(id, args);

		}

	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		switch (adapter.getId()) {
			case R.id.grid: {
				final Intent launch = new Intent();
				final Cursor c = mAdapter.getCursor();
				launch.setClassName(c.getString(c.getColumnIndex(LauncherItem.PACKAGE_NAME)),
						c.getString(c.getColumnIndex(LauncherItem.ACTIVITY_NAME)));
				launch.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(launch);
			}
				break;

			case R.id.all_apps: {
				final ApplicationInfo appInfo = (ApplicationInfo) mAllApps.getAdapter().getItem(
						position);
				startActivity(appInfo.intent);
			}
				break;
		}

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View v, int position, long id) {
		switch (adapter.getId()) {
			case R.id.grid: {
				if (!mLocked) {
					getContentResolver().delete(
							ContentUris.withAppendedId(LauncherItem.CONTENT_URI, id), null, null);
					return true;
				}
			}
				break;

			case R.id.all_apps: {
				final ApplicationInfo appInfo = (ApplicationInfo) mAllApps
						.getItemAtPosition(position);
				final ContentValues cv = new ContentValues();
				final ComponentName c = appInfo.intent.getComponent();

				cv.put(LauncherItem.ACTIVITY_NAME, c.getClassName());
				cv.put(LauncherItem.PACKAGE_NAME, c.getPackageName());
				getContentResolver().insert(LauncherItem.CONTENT_URI, cv);
				mDrawer.close();
				return true;

			}
		}
		return false;
	}

}
