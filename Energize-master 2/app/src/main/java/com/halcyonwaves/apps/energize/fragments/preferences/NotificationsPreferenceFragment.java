package com.halcyonwaves.apps.energize.fragments.preferences;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.halcyonwaves.apps.energize.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NotificationsPreferenceFragment extends PreferenceFragment {

	private final static String TAG = "NotificationsPreference";

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.pref_notifications);
	}
}
