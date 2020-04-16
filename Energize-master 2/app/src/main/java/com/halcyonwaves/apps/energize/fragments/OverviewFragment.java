package com.halcyonwaves.apps.energize.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.halcyonwaves.apps.energize.R;
import com.halcyonwaves.apps.energize.animations.WaveView;
import com.halcyonwaves.apps.energize.animations.WaveView.ShapeType;
import com.halcyonwaves.apps.energize.animations.WaveViewHelper;
import com.halcyonwaves.apps.energize.database.BatteryStatisticsDatabaseOpenHelper;
import com.halcyonwaves.apps.energize.database.PowerEventsTable;
import com.halcyonwaves.apps.energize.estimators.EstimationResult;
import com.halcyonwaves.apps.energize.services.MonitorBatteryStateService;
import java.lang.ref.WeakReference;

public class OverviewFragment extends Fragment {

	private static final String TAG = "OverviewFragment";
	private final Messenger monitorServiceMessanger = new Messenger(new IncomingHandler(new WeakReference<>(this)));
	private Messenger monitorService = null;

	private final ServiceConnection monitorServiceConnection = new ServiceConnection() {

		public void onServiceConnected(final ComponentName className, final IBinder service) {
			OverviewFragment.this.monitorService = new Messenger(service);
			try {
				Log.d(OverviewFragment.TAG, "Trying to connect to the battery monitoring service...");
				final Message msg = Message.obtain(null, MonitorBatteryStateService.MSG_REGISTER_CLIENT);
				msg.replyTo = OverviewFragment.this.monitorServiceMessanger;
				OverviewFragment.this.monitorService.send(msg);
			} catch (final RemoteException e) {
				Log.e(OverviewFragment.TAG, "Failed to connect to the battery monitoring service!");
			}
		}

		public void onServiceDisconnected(final ComponentName className) {
			OverviewFragment.this.monitorService = null;
		}
	};
	private SharedPreferences sharedPref = null;
	private TextView textViewCurrentChargingState = null;
	private TextView textViewCurrentLoadingLevel = null;
	private TextView textViewRemainingTime = null;
	private TextView textViewTemp = null;
	private TextView textViewTimeOnBattery = null;
	private WaveViewHelper waveViewHelper = null;
	private WaveView waveView = null;
	private int batteryChargingLevel = -1;

	private void doBindService() {
		this.getActivity().bindService(new Intent(this.getActivity(), MonitorBatteryStateService.class), this.monitorServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void doUnbindService() {
		if (this.monitorService != null) {
			try {
				final Message msg = Message.obtain(null, MonitorBatteryStateService.MSG_UNREGISTER_CLIENT);
				msg.replyTo = this.monitorServiceMessanger;
				this.monitorService.send(msg);
			} catch (final RemoteException ignored) {
			}
		}
		this.getActivity().unbindService(this.monitorServiceConnection);
		this.monitorService = null;
	}

	@Override
	public void onResume() {
		super.onResume();

		// update the basic information about the battery
		this.updateBatteryInformation();

		// query the remaining time
		try {
			if (this.monitorService != null) {
				final Message msg2 = Message.obtain(null, MonitorBatteryStateService.MSG_REQUEST_REMAINING_TIME);
				msg2.replyTo = OverviewFragment.this.monitorServiceMessanger;
				this.monitorService.send(msg2);
			}
		} catch (RemoteException e) {
			Log.e(OverviewFragment.TAG, "Failed to query the current time estimation.");
		}

		//
		final boolean shouldShowAnimation = sharedPref.getBoolean("display.show_battery_animation", true);
		final boolean isInLandscapeMode = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		if (null != waveViewHelper) {
			if (shouldShowAnimation && !isInLandscapeMode) {
				waveViewHelper.start();
			} else {
				waveViewHelper.cancel();
				waveViewHelper = null;
				waveView.setVisibility(View.GONE);
			}
		} else {
			Log.d(TAG, "onResume: should show animation = " + shouldShowAnimation);
			if (shouldShowAnimation) {
				initializeBatteryAnimation(waveView);
				waveViewHelper.setBatteryPercentage(batteryChargingLevel);
				waveViewHelper.start();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (null != waveViewHelper) {
			waveViewHelper.cancel();
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		this.sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());

		// inflate the static part of the view
		final View inflatedView = inflater.inflate(R.layout.fragment_maininformation, container, false);

		// get the handles to some important controls
		this.textViewCurrentLoadingLevel = (TextView) inflatedView.findViewById(R.id.textview_text_current_charginglvl);
		this.textViewCurrentChargingState = (TextView) inflatedView.findViewById(R.id.textview_text_current_chargingstate);
		this.textViewTemp = (TextView) inflatedView.findViewById(R.id.textview_text_temperature);
		this.textViewTimeOnBattery = (TextView) inflatedView.findViewById(R.id.textview_text_timeonbattery);
		this.textViewRemainingTime = (TextView) inflatedView.findViewById(R.id.textview_text_remainingtime);

		// get the time on battery and set it
		BatteryStatisticsDatabaseOpenHelper batteryDbHelper = new BatteryStatisticsDatabaseOpenHelper(this.getActivity().getApplicationContext());
		SQLiteDatabase batteryDB = batteryDbHelper.getReadableDatabase();
		final Cursor queryCursor = batteryDB.query(PowerEventsTable.TABLE_NAME, new String[]{PowerEventsTable.COLUMN_EVENT_TIME}, PowerEventsTable.COLUMN_BATTERY_IS_CHARGING + " = " + PowerEventsTable.POWER_EVENT_IS_NOT_CHARGING, null, null, null,
				PowerEventsTable.COLUMN_EVENT_TIME + " DESC");
		if (queryCursor.moveToFirst()) {
			final long timeGoneToBattery = queryCursor.getInt(queryCursor.getColumnIndex(PowerEventsTable.COLUMN_EVENT_TIME));
			final long currentUnixTime = System.currentTimeMillis() / 1000;
			final long difference = Math.round((currentUnixTime - timeGoneToBattery) / 60.0);
			final long remainingHours = difference > 0 ? (int) Math.floor(difference / 60.0) : 0;
			final long remainingMinutesNew = difference - (60 * remainingHours);
			this.textViewTimeOnBattery.setText(this.getString(R.string.textview_text_timeonbattery, remainingHours, remainingMinutesNew));
		} else {
			this.textViewTimeOnBattery.setText("-");
		}
		queryCursor.close();
		batteryDbHelper.close();

		// bind to the service and ask for the current time estimation
		this.doBindService();

		//
		this.updateBatteryInformation();

		//
		waveView = (WaveView) inflatedView.findViewById(R.id.wave);
		if (sharedPref.getBoolean("display.show_battery_animation", true)) {
			initializeBatteryAnimation(waveView);
		} else {
			waveView.setVisibility(View.GONE);
		}

		// return the inflated view
		return inflatedView;
	}

	private void initializeBatteryAnimation(WaveView waveView) {
		final int frontWaveColor;
		final int backWaveColor;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			frontWaveColor = getResources().getColor(R.color.colorPrimaryDark, null);
			backWaveColor = getResources().getColor(R.color.colorPrimary, null);
		} else {
			frontWaveColor = getResources().getColor(R.color.colorPrimaryDark);
			backWaveColor = getResources().getColor(R.color.colorPrimary);
		}
		waveView.setShapeType(ShapeType.CIRCLE);
		waveView.setWaveColor(backWaveColor, frontWaveColor);
		waveView.setBorder(5, frontWaveColor);
		waveView.setVisibility(View.VISIBLE);
		waveViewHelper = new WaveViewHelper(waveView);
	}

	private void updateBatteryInformation() {
		// get the current battery state and show it on the main activity
		final BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(final Context context, final Intent intent) {
				try {
					// ensure that we're not updating this receiver anymore (to save battery)
					context.unregisterReceiver(this);

					// get some important values into local variables
					final int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
					final int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
					final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
					final float temp = (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)) / 10.0f;
					final int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

					// if the device is plugged in, remember that
					if (plugged > 0) {
						OverviewFragment.this.textViewTimeOnBattery.setText("-");
					}

					// do a potential level scaling (most of the times not required, but to be sure)

					if ((rawlevel >= 0) && (scale > 0)) {
						batteryChargingLevel = (rawlevel * 100) / scale;
					}

					// set the text for the state of he main battery
					switch (status) {
						case BatteryManager.BATTERY_STATUS_CHARGING:
							OverviewFragment.this.textViewCurrentChargingState.setText(OverviewFragment.this.getString(R.string.battery_state_charging));
							break;
						case BatteryManager.BATTERY_STATUS_DISCHARGING:
							OverviewFragment.this.textViewCurrentChargingState.setText(OverviewFragment.this.getString(R.string.battery_state_discharging));
							break;
						case BatteryManager.BATTERY_STATUS_FULL:
							OverviewFragment.this.textViewCurrentChargingState.setText(OverviewFragment.this.getString(R.string.battery_state_full));
							break;
						default:
							OverviewFragment.this.textViewCurrentChargingState.setText(OverviewFragment.this.getString(R.string.battery_state_unknown));
							break;
					}

					//
					OverviewFragment.this.textViewCurrentLoadingLevel.setText(String.valueOf(batteryChargingLevel));

					//
					if (null != waveViewHelper) {
						waveViewHelper.setBatteryPercentage(batteryChargingLevel);
					}

					final String prefUsedUnit = OverviewFragment.this.sharedPref.getString("display.temperature_unit", "Celsius");
					if (prefUsedUnit.compareToIgnoreCase("celsius") == 0) {
						OverviewFragment.this.textViewTemp.setText(OverviewFragment.this.getString(R.string.textview_text_temperature_celsius, temp));
					} else if (prefUsedUnit.compareToIgnoreCase("fahrenheit") == 0) {
						final float newTemp = (temp * 1.8f) + 32.0f;
						OverviewFragment.this.textViewTemp.setText(OverviewFragment.this.getString(R.string.textview_text_temperature_fahrenheit, newTemp));
					}
				} catch (final IllegalStateException e) {
					Log.e(OverviewFragment.TAG, "The fragment was in an illegal state while it received the battery information. This should be handled in a different (and better way), The exception message was: ", e); // TODO
				}
			}
		};
		final IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		this.getActivity().registerReceiver(batteryLevelReceiver, batteryLevelFilter);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		this.doUnbindService();
	}

	private void updateEstimationLabel(final EstimationResult estimation) {
		this.textViewRemainingTime.setText(String.format(this.getString(R.string.textview_text_remainingtime), estimation.remainingHours, estimation.remainingMinutes));
	}

	private static class IncomingHandler extends Handler {

		private WeakReference<OverviewFragment> overviewFragmentWeakReference;

		IncomingHandler(WeakReference<OverviewFragment> weakReference) {
			overviewFragmentWeakReference = weakReference;
		}

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case MonitorBatteryStateService.MSG_REGISTER_CLIENT:
					// since the client is now registered, we can ask the service about the remaining time we have
					try {
						// be sure that the monitor service is available, sometimes (I don't know why) this is not the case
						if (null == overviewFragmentWeakReference || null == overviewFragmentWeakReference.get().monitorService) {
							Log.e(OverviewFragment.TAG, "Tried to query the remaining time but the monitor service was not available!");
							return;
						}

						// query the remaining time
						final Message msg2 = Message.obtain(null, MonitorBatteryStateService.MSG_REQUEST_REMAINING_TIME);
						msg2.replyTo = overviewFragmentWeakReference.get().monitorServiceMessanger;
						overviewFragmentWeakReference.get().monitorService.send(msg2);
					} catch (final RemoteException e1) {
						Log.e(OverviewFragment.TAG, "Failed to query the current time estimation.");
					}
					break;
				case MonitorBatteryStateService.MSG_REQUEST_REMAINING_TIME:
					final EstimationResult remainingTimeEstimation = EstimationResult.fromBundle(msg.getData());
					Log.d(OverviewFragment.TAG, String.format("Received an time estimation of %d minutes.", remainingTimeEstimation.minutes));
					overviewFragmentWeakReference.get().updateEstimationLabel(remainingTimeEstimation);
					break;
				default:
					super.handleMessage(msg);
			}
		}
	}
}
