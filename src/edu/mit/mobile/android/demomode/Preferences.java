package edu.mit.mobile.android.demomode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Preferences extends PreferenceActivity implements OnPreferenceClickListener {
	private static final String TAG = Preferences.class.getSimpleName();

	private SharedPreferences mPrefs;

	public static final String KEY_PASSWORD = "password";
	public static final String KEY_LOCKED = "locked";
	public static final String KEY_SHARE_CONFIG = "share_config";
	public static final String KEY_SCAN_CONFIG = "scan_config";
	public static final String KEY_ENABLED = "enable";

	private static String CFG_PKG_SEP = ",";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.preferences);
		mPrefs = getPreferenceManager().getSharedPreferences();

		findPreference(KEY_SHARE_CONFIG).setOnPreferenceClickListener(this);
		findPreference(KEY_SCAN_CONFIG).setOnPreferenceClickListener(this);

		mPrefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (KEY_ENABLED.equals(key)) {
					updateDemoModeEnabled();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mPrefs.getBoolean(KEY_LOCKED, false)) {
			finish();
		}
	}

	private static final String CFG_K_VER = "v", CFG_K_SECRETKEY = "k", CFG_K_APPS = "a";
	public static final String CFG_MIME_TYPE = "application/x-demo-mode";

	private String toConfigString() {

		final String password = mPrefs.getString(KEY_PASSWORD, null);

		final ArrayList<BasicNameValuePair> nvp = new ArrayList<BasicNameValuePair>();

		int ver;
		try {
			ver = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

		} catch (final NameNotFoundException e) {
			// this should never happen
			ver = 0;
			e.printStackTrace();
		}
		nvp.add(new BasicNameValuePair(CFG_K_VER, String.valueOf(ver)));
		nvp.add(new BasicNameValuePair(CFG_K_SECRETKEY, password));
		final Cursor c = getContentResolver().query(LauncherItem.CONTENT_URI, null, null, null,
				null);

		try {
			final int pkgCol = c.getColumnIndex(LauncherItem.PACKAGE_NAME);
			final int actCol = c.getColumnIndex(LauncherItem.ACTIVITY_NAME);

			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				nvp.add(new BasicNameValuePair(CFG_K_APPS, c.getString(pkgCol) + CFG_PKG_SEP
						+ c.getString(actCol)));
			}

			return "data:" + CFG_MIME_TYPE + "," + URLEncodedUtils.format(nvp, "utf-8");

		} finally {
			c.close();
		}

	}

	private void shareConfig() {
		startActivity(Intent.createChooser(
				new Intent(Intent.ACTION_SEND).setType("text/plain")
						.putExtra(Intent.EXTRA_TEXT, toConfigString())
						.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.app_name) + " Config"),
				getText(R.string.share_config)));
	}

	private void fromCfgString(String cfg) {
		final Uri cfgUri = Uri.parse(cfg);
		if ("data".equals(cfgUri.getScheme())) {
			final String[] cfgParts = cfgUri.getEncodedSchemeSpecificPart().split(",", 2);
			if (CFG_MIME_TYPE.equals(cfgParts[0])) {
				final Editor ed = mPrefs.edit();
				final ArrayList<ContentProviderOperation> cpos = new ArrayList<ContentProviderOperation>();

				// first erase everything
				cpos.add(ContentProviderOperation.newDelete(LauncherItem.CONTENT_URI).build());

				try {
					final StringEntity entity = new StringEntity(cfgParts[1]);
					entity.setContentType("application/x-www-form-urlencoded");
					final List<NameValuePair> nvp = URLEncodedUtils.parse(entity);
					for (final NameValuePair pair : nvp) {
						final String name = pair.getName();
						Log.d(TAG, "parsed pair: " + pair);
						if (CFG_K_SECRETKEY.equals(name)) {
							ed.putString(KEY_PASSWORD, pair.getValue());

						} else if (CFG_K_APPS.equals(name)) {
							final String[] app = pair.getValue().split(CFG_PKG_SEP, 2);
							final ContentProviderOperation cpo = ContentProviderOperation
									.newInsert(LauncherItem.CONTENT_URI)
									.withValue(LauncherItem.PACKAGE_NAME, app[0])
									.withValue(LauncherItem.ACTIVITY_NAME, app[1]).build();
							cpos.add(cpo);
							Log.d(TAG, "adding " + cpo);
						}
					}

					ed.commit();
					getContentResolver().applyBatch(HomescreenProvider.AUTHORITY, cpos);
				} catch (final UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final RemoteException e) {
					// TODO Auto-generated catch block

					e.printStackTrace();
				} catch (final OperationApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				Log.e(TAG, "unknown MIME type for data URI: " + cfgParts[0]);
			}

		} else {
			Log.e(TAG, "not a data URI");
		}
	}

	private void scanQRCode() {
		final IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode,
				resultCode, intent);
		if (scanResult != null && scanResult.getContents() != null) {

			fromCfgString(scanResult.getContents());

		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		final String pref = preference.getKey();
		if (KEY_SCAN_CONFIG.equals(pref)) {
			scanQRCode();
			return true;
		} else if (KEY_SHARE_CONFIG.equals(pref)) {
			shareConfig();
			return true;
		}

		return false;
	}

	private void updateDemoModeEnabled() {
		getPackageManager()
				.setComponentEnabledSetting(
						new ComponentName(this, DemoMode.class),
						mPrefs.getBoolean(KEY_ENABLED, true) ? PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
								: PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);
	}

}
