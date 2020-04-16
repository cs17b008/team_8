package com.halcyonwaves.apps.energize.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.halcyonwaves.apps.energize.MainActivity;
import com.halcyonwaves.apps.energize.R;
import com.halcyonwaves.apps.energize.database.BatteryStatisticsDatabaseOpenHelper;
import com.halcyonwaves.apps.energize.database.PowerEventsTable;
import com.halcyonwaves.apps.energize.database.RawBatteryStatisicsTable;
import com.halcyonwaves.apps.energize.estimators.BatteryEstimationMgr;
import com.halcyonwaves.apps.energize.estimators.EstimationResult;
import com.halcyonwaves.apps.energize.receivers.BatteryChangedReceiver;
import com.halcyonwaves.apps.energize.receivers.PowerSupplyPluggedInReceiver;
import com.halcyonwaves.apps.energize.receivers.PowerSupplyPulledOffReceiver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MonitorBatteryStateService extends Service implements OnSharedPreferenceChangeListener {

	public static final int MSG_CLEAR_STATISTICS = 3;
	public static final int MSG_COPY_DB_TO_SDCARD = 4;
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_REQUEST_REMAINING_TIME = 5;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_UPDATE_WIDGETS = 6;
	private static final int MY_NOTIFICATION_ID = 1;
	private static final String TAG = "BatteryStateService";
	private final Messenger serviceMessenger = new Messenger(new IncomingHandler());
	private SharedPreferences appPreferences = null;
	private BatteryStatisticsDatabaseOpenHelper batteryDbOpenHelper = null;
	private SQLiteDatabase batteryStatisticsDatabase = null;
	private Notification myNotification = null;
	private NotificationManager notificationManager = null;

	private void copyDatabaseToSDCard() {
		final File extStorePath = Environment.getExternalStorageDirectory();

		// close the database before copying it
		this.batteryStatisticsDatabase.close();

		// get a correct input and output stream
		try {
			final FileInputStream in = new FileInputStream(new File(this.batteryStatisticsDatabase.getPath()));
			final FileOutputStream out = new FileOutputStream(extStorePath.getAbsolutePath() + File.separator + "energizeStatisticcs.db");

			// copy the database to the SD card
			final byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}

			// close the streams again
			out.flush();
			out.close();
			in.close();
		} catch (final FileNotFoundException e) {
			Log.e(MonitorBatteryStateService.TAG, "Cannot open an input and/or output file. The exception message was: " + e.getMessage());
		} catch (final IOException e) {
			Log.e(MonitorBatteryStateService.TAG, "I/O error during copying the database.");
		} finally {
			// the last step is to reopen the database
			this.batteryStatisticsDatabase = this.batteryDbOpenHelper.getWritableDatabase();
		}

	}

	public void insertPowerSupplyChangeEvent(final boolean isChargingNow) {
		final ContentValues values = new ContentValues();
		final long currentUnixTime = System.currentTimeMillis() / 1000;
		values.put(PowerEventsTable.COLUMN_EVENT_TIME, currentUnixTime);
		values.put(PowerEventsTable.COLUMN_BATTERY_IS_CHARGING, isChargingNow ? PowerEventsTable.POWER_EVENT_IS_CHARGING : PowerEventsTable.POWER_EVENT_IS_NOT_CHARGING);
		if (-1 == this.batteryStatisticsDatabase.insert(PowerEventsTable.TABLE_NAME, null, values)) {
			Log.e(MonitorBatteryStateService.TAG, "Failed to log the event that the power cord was plugged in or unplugged.");
		} else {
			Log.v(MonitorBatteryStateService.TAG, "Successfully inserted a power cord plugging event into the database.");
		}
	}

	public void insertPowerValue(final int powerSource, final int scale, final int level, final int temprature) {
		// if the database is not open, skip the insertion process
		if ((null == this.batteryStatisticsDatabase) || !this.batteryStatisticsDatabase.isOpen()) {
			Log.e(MonitorBatteryStateService.TAG, "Tried to insert a dataset into a closed database, skipping...");
			return;
		}

		// get the last entry we made on our database, if the entries are the same we want to insert, skip the insertion process
		final Cursor lastEntryMadeCursor = this.batteryStatisticsDatabase
				.query(RawBatteryStatisicsTable.TABLE_NAME, new String[]{RawBatteryStatisicsTable.COLUMN_CHARGING_LEVEL}, null, null, null, null, RawBatteryStatisicsTable.COLUMN_EVENT_TIME + " DESC");

		// if the level changed, we can insert the entry into our database
		if (!lastEntryMadeCursor.moveToFirst() || (level != lastEntryMadeCursor.getInt(lastEntryMadeCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_CHARGING_LEVEL)))) {
			final long currentUnixTime = System.currentTimeMillis() / 1000;
			final ContentValues values = new ContentValues();
			values.put(RawBatteryStatisicsTable.COLUMN_EVENT_TIME, currentUnixTime);
			values.put(RawBatteryStatisicsTable.COLUMN_CHARGING_STATE, powerSource);
			values.put(RawBatteryStatisicsTable.COLUMN_CHARGING_SCALE, scale);
			values.put(RawBatteryStatisicsTable.COLUMN_CHARGING_LEVEL, level);
			values.put(RawBatteryStatisicsTable.COLUMN_BATTERY_TEMPRATURE, temprature);
			this.batteryStatisticsDatabase.insert(RawBatteryStatisicsTable.TABLE_NAME, null, values);
		}

		// close the database cursor again
		lastEntryMadeCursor.close();

		// show the notification
		this.showNewPercentageNotification();
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return this.serviceMessenger.getBinder();
	}

	@Override
	public void onDestroy() {
		if (null != this.batteryDbOpenHelper) {
			this.batteryDbOpenHelper.close();
			this.batteryStatisticsDatabase = null;
		}
		super.onDestroy();
	}

	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (0 == key.compareTo("advance.show_notification_bar")) {
			final boolean showShowIcon = sharedPreferences.getBoolean("advance.show_notification_bar", true);
			Log.v(MonitorBatteryStateService.TAG, "Notification icon setting chaanged to: " + showShowIcon);
			if (!showShowIcon) {
				this.notificationManager.cancel(MonitorBatteryStateService.MY_NOTIFICATION_ID);
				this.myNotification = null;
			} else {
				this.showNewPercentageNotification();
			}
		}
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startid) {
		//
		Log.v(MonitorBatteryStateService.TAG, "Starting service for collecting battery statistics...");

		//
		this.appPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		this.appPreferences.registerOnSharedPreferenceChangeListener(this);

		//
		this.notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		//
		this.batteryDbOpenHelper = new BatteryStatisticsDatabaseOpenHelper(this.getApplicationContext());
		this.batteryStatisticsDatabase = this.batteryDbOpenHelper.getWritableDatabase();

		//
		BatteryChangedReceiver batteryChangedReceiver = new BatteryChangedReceiver(this);
		this.registerReceiver(batteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		//
		PowerSupplyPluggedInReceiver powerPluggedInReceiver = new PowerSupplyPluggedInReceiver(this);
		PowerSupplyPulledOffReceiver powerUnpluggedReceiver = new PowerSupplyPulledOffReceiver(this);
		this.registerReceiver(powerPluggedInReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
		this.registerReceiver(powerUnpluggedReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));

		//
		Log.v(MonitorBatteryStateService.TAG, "Service successfully started");
		return Service.START_STICKY;
	}

	private void showNewPercentageNotification() {
		// query the current estimation values
		final EstimationResult estimation = BatteryEstimationMgr.getEstimation(this.getApplicationContext());

		// if we should not show the notification, skip the method here
		if (!this.appPreferences.getBoolean("advance.show_notification_bar", true)) {
			return;
		}

		// be sure that it is a valid percentage
		if (!estimation.isValid) {
			Log.w(MonitorBatteryStateService.TAG, "The application tried to show an invalid loading level. Showing a placeholder!");

			// build a basic notification indicating that there is not enough information to show up an estimate
			final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.getApplicationContext());
			notificationBuilder.setContentTitle(this.getString(R.string.notification_title_missingstatistics));
			notificationBuilder.setContentText(this.getString(R.string.notification_text_missingstatistics));
			notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
			notificationBuilder.setOngoing(true);
			notificationBuilder.setContentIntent(PendingIntent.getActivity(this.getApplicationContext(), 0, new Intent(this.getApplicationContext(), MainActivity.class), 0));
			notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);

			// show up the notification we just set up
			this.myNotification = notificationBuilder.build();
			this.notificationManager.notify(MonitorBatteryStateService.MY_NOTIFICATION_ID, this.myNotification);

			// skip the rest of the application code
			return;
		}

		// determine the correct title string for the notification
		int notificationTitleId = R.string.notification_title_discharges;
		if (estimation.charging) {
			notificationTitleId = R.string.notification_title_charges;
		}

		// prepare the notification object
		final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.getApplicationContext());
		notificationBuilder.setContentTitle(this.getString(notificationTitleId));
		notificationBuilder.setSmallIcon(R.mipmap.ic_stat_00_pct_charged + estimation.level);
		notificationBuilder.setOngoing(true);
		notificationBuilder.setContentIntent(PendingIntent.getActivity(this.getApplicationContext(), 0, new Intent(this.getApplicationContext(), MainActivity.class), 0));
		notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);

		// if the capacity reaches 15%, use a high priority
		if (estimation.level <= 15) {
			notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
		}

		// show the notification
		if (estimation.remainingMinutes <= -1) {
			notificationBuilder.setContentText(this.getString(R.string.notification_text_estimate_na));
		} else {
			notificationBuilder.setContentText(this.getString(R.string.notification_text_estimate, estimation.remainingHours, estimation.remainingMinutes));
		}

		// get the created notification and show it
		this.myNotification = notificationBuilder.build();
		this.notificationManager.notify(MonitorBatteryStateService.MY_NOTIFICATION_ID, this.myNotification);
	}

	private class IncomingHandler extends Handler {

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case MonitorBatteryStateService.MSG_REGISTER_CLIENT:
					Log.d(MonitorBatteryStateService.TAG, "Registering new client to the battery monitoring service...");
					try {
						msg.replyTo.send(Message.obtain(null, MonitorBatteryStateService.MSG_REGISTER_CLIENT));
					} catch (final RemoteException e) {
						Log.e(MonitorBatteryStateService.TAG, "Failed to tell the client that the client was successfully registered.");
					} catch (final NullPointerException e) {
						Log.e(MonitorBatteryStateService.TAG, "Failed to tell the client that the client was successfully registered (NullPointerException).");
					}
					break;
				case MonitorBatteryStateService.MSG_UNREGISTER_CLIENT:
					Log.d(MonitorBatteryStateService.TAG, "Unregistering client from the battery monitoring service...");
					try {
						msg.replyTo.send(Message.obtain(null, MonitorBatteryStateService.MSG_UNREGISTER_CLIENT));
					} catch (final RemoteException e) {
						Log.e(MonitorBatteryStateService.TAG, "Failed to tell the client that the client was successfully unregistered.");
					} catch (final NullPointerException e) {
						Log.e(MonitorBatteryStateService.TAG, "Failed to tell the client that the client was successfully unregistered (NullPointerException).");
					}
					break;
				case MonitorBatteryStateService.MSG_CLEAR_STATISTICS:
					Log.d(MonitorBatteryStateService.TAG, "Clearing battery statistics database...");
					try {
						MonitorBatteryStateService.this.batteryStatisticsDatabase.delete(PowerEventsTable.TABLE_NAME, null, null);
						MonitorBatteryStateService.this.batteryStatisticsDatabase.delete(RawBatteryStatisicsTable.TABLE_NAME, null, null);
						msg.replyTo.send(Message.obtain(null, MonitorBatteryStateService.MSG_CLEAR_STATISTICS));
					} catch (final RemoteException e) {
						Log.e(MonitorBatteryStateService.TAG, "Failed to clear battery statistics database!");
					} catch (final NullPointerException e) {
						Log.e(MonitorBatteryStateService.TAG, "Failed to clear battery statistics database (NullPointerException)!");
					}
					break;
				case MonitorBatteryStateService.MSG_COPY_DB_TO_SDCARD:
					Log.d(MonitorBatteryStateService.TAG, "Copying battery statistics database...");
					try {
						MonitorBatteryStateService.this.copyDatabaseToSDCard();
						msg.replyTo.send(Message.obtain(null, MonitorBatteryStateService.MSG_COPY_DB_TO_SDCARD));
					} catch (final RemoteException e) {
						Log.e(MonitorBatteryStateService.TAG, "Failed to copy battery statistics database!");
					} catch (final NullPointerException e) {
						Log.e(MonitorBatteryStateService.TAG, "Failed to copy battery statistics database (NullPointerException)!");
					}
					break;
				case MonitorBatteryStateService.MSG_REQUEST_REMAINING_TIME:
					Log.d(MonitorBatteryStateService.TAG, "Sending remaining time...");
					try {

						// request the current time estimate and package it to send it to the requesting object
						final EstimationResult estimation = BatteryEstimationMgr.getEstimation(MonitorBatteryStateService.this.getApplicationContext());
						final Bundle returningData = estimation.toBundle();

						// prepare the message which should be send to the requesting object
						final Message returningMessage = Message.obtain(null, MonitorBatteryStateService.MSG_REQUEST_REMAINING_TIME);
						returningMessage.setData(returningData);

						// reply with the time estimation
						msg.replyTo.send(returningMessage);
					} catch (final RemoteException e) {
						Log.e(MonitorBatteryStateService.TAG, "Failed so send the time estimation to the requesting object.");
					} catch (final NullPointerException e) {
						Log.e(MonitorBatteryStateService.TAG, "Failed so send the time estimation to the requesting object (NullPointerException).");
					}
					break;
				case MonitorBatteryStateService.MSG_UPDATE_WIDGETS:
					Log.d(MonitorBatteryStateService.TAG, "Updating widgets...");
					MonitorBatteryStateService.this.showNewPercentageNotification();
					break;
				default:
					super.handleMessage(msg);
			}
		}
	}
}
