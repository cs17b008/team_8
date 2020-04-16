package com.halcyonwaves.apps.energize.estimators;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.halcyonwaves.apps.energize.database.BatteryStatisticsDatabaseOpenHelper;
import com.halcyonwaves.apps.energize.database.RawBatteryStatisicsTable;

public class SimpleEstimationAlgorithm {

	private final static String TAG = "SimpleEstimationAlg";

	public static EstimationResult getEstimation(final Context context) {
		BatteryStatisticsDatabaseOpenHelper batteryDbOpenHelper = new BatteryStatisticsDatabaseOpenHelper(context);
		final SQLiteDatabase batteryStatisticsDatabase = batteryDbOpenHelper.getReadableDatabase();

		// query the information we need for the estimation
		final Cursor querCursor = batteryStatisticsDatabase
				.query(RawBatteryStatisicsTable.TABLE_NAME, new String[]{RawBatteryStatisicsTable.COLUMN_EVENT_TIME, RawBatteryStatisicsTable.COLUMN_CHARGING_STATE, RawBatteryStatisicsTable.COLUMN_CHARGING_LEVEL}, null, null, null, null,
						RawBatteryStatisicsTable.COLUMN_EVENT_TIME + " DESC");

		// if it fails to move to the first object, return
		if (!querCursor.moveToFirst()) {

			// close all opened databases
			querCursor.close();
			batteryDbOpenHelper.close();
			batteryDbOpenHelper = null;

			// return an invalid estimation
			return new EstimationResult();
		}

		//
		final long lastEventTime = querCursor.getLong(querCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_EVENT_TIME));
		final int lastChargingPercentage = querCursor.getInt(querCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_CHARGING_LEVEL));
		final int lastChargingState = querCursor.getInt(querCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_CHARGING_STATE));

		// if it fails to move to the next object, return
		if (!querCursor.moveToNext() || (querCursor.getInt(querCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_CHARGING_STATE)) != lastChargingState)) {

			// close all opened databases
			querCursor.close();
			batteryDbOpenHelper.close();
			batteryDbOpenHelper = null;

			// the estimation is not available but we have the current charging state and level
			return new EstimationResult(-1, lastChargingPercentage, lastChargingState != RawBatteryStatisicsTable.CHARGING_STATE_DISCHARGING);
		}

		//
		final long prevEventTime = querCursor.getLong(querCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_EVENT_TIME));
		final long diff = Math.abs(lastEventTime - prevEventTime);
		Log.v(SimpleEstimationAlgorithm.TAG, String.format("Calculated the time between the last two (%d, %d) events: %d", lastEventTime, prevEventTime, diff));

		//
		int lastRemainingMinutes = 0;
		if (RawBatteryStatisicsTable.CHARGING_STATE_DISCHARGING == lastChargingState) {
			lastRemainingMinutes = (Math.round((lastChargingPercentage * diff) / 60.0f));
			Log.v(SimpleEstimationAlgorithm.TAG, String.format("Calculated remaining battery life in minutes: %d", lastRemainingMinutes));
		} else {
			lastRemainingMinutes = (Math.round(((100.0f - lastChargingPercentage) * diff) / 60.0f));
			Log.v(SimpleEstimationAlgorithm.TAG, String.format("Calculated remaining charging time in minutes: %d", lastRemainingMinutes));
		}

		// close all opened databases
		querCursor.close();
		batteryDbOpenHelper.close();
		batteryDbOpenHelper = null;

		// return the gathered information
		return new EstimationResult(lastRemainingMinutes, lastChargingPercentage, lastChargingState != RawBatteryStatisicsTable.CHARGING_STATE_DISCHARGING);
	}

}
