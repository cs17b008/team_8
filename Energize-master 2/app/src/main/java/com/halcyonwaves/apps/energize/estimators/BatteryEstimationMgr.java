package com.halcyonwaves.apps.energize.estimators;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.halcyonwaves.apps.energize.R;

public class BatteryEstimationMgr {

	final static String TAG = "BatteryEstimationMgr";

	public static EstimationResult getEstimation(final Context context) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final String estimationMethod = prefs.getString("batstatistics.usedestimator", "NO_PREFERENCE_FOUND");

		final String lastChangeEstimate = context.getString(R.string.pref_list_batteryestimationmethod_value_lastchangeestimate);
		final String lastNChangeEstimate = context.getString(R.string.pref_list_batteryestimationmethod_value_lastnchangeestimate);

		// obtain the estimation from the estimator the user selected
		if (0 == lastChangeEstimate.compareToIgnoreCase(estimationMethod)) {
			return SimpleEstimationAlgorithm.getEstimation(context);
		} else if (0 == lastNChangeEstimate.compareToIgnoreCase(estimationMethod)) {
			return SimpleEstimationAlgorithm2.getEstimation(context);
		}

		// it seems that no time estimator could be initialized
		Log.e(BatteryEstimationMgr.TAG, "Unknown battery time estimator found in preferences: " + estimationMethod);
		return new EstimationResult();
	}
}
