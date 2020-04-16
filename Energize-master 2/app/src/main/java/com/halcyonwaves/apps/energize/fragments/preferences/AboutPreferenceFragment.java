package com.halcyonwaves.apps.energize.fragments.preferences;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.halcyonwaves.apps.energize.R;
import com.halcyonwaves.apps.energize.dialogs.AboutDialog;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AboutPreferenceFragment extends PreferenceFragment {

	private final static String TAG = "AboutPreference";

	private String getSoftwareVersion() {
		try {
			PackageInfo packageInfo = this.getActivity().getPackageManager().getPackageInfo(this.getActivity().getPackageName(), 0);
			return packageInfo.versionName + " (" + packageInfo.versionCode + ")";
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(AboutPreferenceFragment.TAG, "Package name not found", e);
		}
		return "N/A";
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.pref_about);

		Preference appVersion = this.findPreference("developer.appVersion");
		appVersion.setSummary(this.getSoftwareVersion());

		Preference aboutPreference = this.findPreference("developer.aboutApp");
		aboutPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AboutDialog aboutDialog = new AboutDialog();
				aboutDialog.show(getFragmentManager(), "dialog");
				return false;
			}
		});

		Preference about3rdPartyLibrariesPreference = this.findPreference("developer.about3rdPartyLibraries");
		about3rdPartyLibrariesPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(AboutPreferenceFragment.this.getActivity(), OssLicensesMenuActivity.class);
				String title = getString(R.string.pref_about_3rd_party_title);
				intent.putExtra("title", title);
				startActivity(intent);
				return false;
			}
		});
	}
}
