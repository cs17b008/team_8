package com.halcyonwaves.apps.energize.estimators;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.halcyonwaves.apps.energize.database.BatteryStatisticsDatabaseOpenHelper;
import com.halcyonwaves.apps.energize.database.RawBatteryStatisicsTable;

/**
 * Simple estimation algorithm using the last N time-steps
 * <p/>
 * This is a very simple estimation algorithm. It uses the last N
 * battery change events to obtain a proper time estimation. Its a bit
 * more robust than just using the last change but still quite
 * inaccurate.
 *
 * @author Tim Huetz <tim@huetz.biz>
 * @since 0.8.3
 */
public class SimpleEstimationAlgorithm2 {

	/**
	 * The maximum number of events which will be taken into account for the estimation.
	 */
	private final static int MAX_N = 3;

	/**
	 * This tag is used when writing information to the log file of the device.
	 */
	private final static String TAG = "SimpleEstimationAlg2";

	/**
	 * This method tries to calculate the current time estimation based
	 * on the information stored in the database. If to few information are
	 * available, it will return just the current charging state.
	 *
	 * @param context The context in which the estimation was requested.
	 * @return The calculated time estimation.
	 */
	public static EstimationResult getEstimation(final Context context) {
		BatteryStatisticsDatabaseOpenHelper batteryDbOpenHelper = new BatteryStatisticsDatabaseOpenHelper(context);
		final SQLiteDatabase batteryStatisticsDatabase = batteryDbOpenHelper.getReadableDatabase();

		// query the information we need for the estimation
		final Cursor querCursor = batteryStatisticsDatabase
				.query(RawBatteryStatisicsTable.TABLE_NAME, new String[]{RawBatteryStatisicsTable.COLUMN_EVENT_TIME, RawBatteryStatisicsTable.COLUMN_CHARGING_STATE, RawBatteryStatisicsTable.COLUMN_CHARGING_LEVEL}, null, null, null, null,
						RawBatteryStatisicsTable.COLUMN_EVENT_TIME + " DESC");

		// if it fails to move to the first object, return with an empty estimation
		if (!querCursor.moveToFirst()) {

			// close all opened databases
			querCursor.close();
			batteryDbOpenHelper.close();
			batteryDbOpenHelper = null;

			// return an invalid estimation
			return new EstimationResult();
		}

		// get the column IDs for the column we need to do our time estimation
		final int eventTimeColumnID = querCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_EVENT_TIME);
		final int lastChargingPercentageColumnID = querCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_CHARGING_LEVEL);
		final int lastChargingStateColumnID = querCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_CHARGING_STATE);

		// get the latest event in our table
		long lastEventTime = querCursor.getLong(eventTimeColumnID);
		final int lastChargingPercentage = querCursor.getInt(lastChargingPercentageColumnID);
		final int lastChargingState = querCursor.getInt(lastChargingStateColumnID);

		// if it fails to move to the next object or the next object has a different charging state, return just with the current charging state
		if (!querCursor.moveToNext() || (querCursor.getInt(lastChargingStateColumnID) != lastChargingState)) {

			// close all opened databases
			querCursor.close();
			batteryDbOpenHelper.close();
			batteryDbOpenHelper = null;

			// the estimation is not available but we have the current charging state and level
			return new EstimationResult(-1, lastChargingPercentage, lastChargingState != RawBatteryStatisicsTable.CHARGING_STATE_DISCHARGING);
		}

		// allocate space for storing the time differences
		long[] timeDifferences = new long[SimpleEstimationAlgorithm2.MAX_N];
		for (int i = 0; i < SimpleEstimationAlgorithm2.MAX_N; ++i) {
			timeDifferences[i] = 0L;
		}

		// get the previous charging event and calculate the time difference between those events
		long prevEventTime = querCursor.getLong(eventTimeColumnID);
		timeDifferences[0] = Math.abs(lastEventTime - prevEventTime);
		Log.v(SimpleEstimationAlgorithm2.TAG, String.format("Calculated the time between the last two (%d, %d) events: %d", lastEventTime, prevEventTime, timeDifferences[0]));

		// to be more robust we will now try to get more estimates until the charging state was changed or we reach our maximum of events to take into account
		for (int currentStep = 1; currentStep < SimpleEstimationAlgorithm2.MAX_N; ++currentStep) {

			// if there are no more information or the changing state changes skip this loop
			if (!querCursor.moveToNext() || (querCursor.getInt(lastChargingStateColumnID) != lastChargingState)) {
				break;
			}

			// calculate the time difference between the next two events
			lastEventTime = prevEventTime;
			prevEventTime = querCursor.getLong(eventTimeColumnID);
			timeDifferences[currentStep] = Math.abs(lastEventTime - prevEventTime);
		}

		long averageTimeDifference = 0;
		int numberOfValidEvents = 0;

		// now calculate the average time difference for all queried events
		for (; numberOfValidEvents < SimpleEstimationAlgorithm2.MAX_N; ++numberOfValidEvents) {

			// just sum up the valid events
			if (timeDifferences[numberOfValidEvents] <= 0) {
				break;
			}
			averageTimeDifference += timeDifferences[numberOfValidEvents];

		}
		averageTimeDifference = Math.round(((double) averageTimeDifference) / ((double) numberOfValidEvents));

		// after we calculate our average time difference between the charging events we can now calculate the remaining minutes
		int lastRemainingMinutes = 0;
		if (RawBatteryStatisicsTable.CHARGING_STATE_DISCHARGING == lastChargingState) {
			lastRemainingMinutes = (Math.round((lastChargingPercentage * averageTimeDifference) / 60.0f));
			Log.v(SimpleEstimationAlgorithm2.TAG, String.format("Calculated remaining battery life in minutes: %d", lastRemainingMinutes));
		} else {
			lastRemainingMinutes = (Math.round(((100.0f - lastChargingPercentage) * averageTimeDifference) / 60.0f));
			Log.v(SimpleEstimationAlgorithm2.TAG, String.format("Calculated remaining charging time in minutes: %d", lastRemainingMinutes));
		}

		// close all opened databases
		querCursor.close();
		batteryDbOpenHelper.close();
		batteryDbOpenHelper = null;

		// tell the VM (explicitly) that we do not need the memory for some objects anymore
		timeDifferences = null;

		// return the gathered information
		return new EstimationResult(lastRemainingMinutes, lastChargingPercentage, lastChargingState != RawBatteryStatisicsTable.CHARGING_STATE_DISCHARGING);
	}

}
