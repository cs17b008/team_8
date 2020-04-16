package com.halcyonwaves.apps.energize;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.halcyonwaves.apps.energize.estimators.EstimationResult;
import com.halcyonwaves.apps.energize.services.MonitorBatteryStateService;
import java.lang.ref.WeakReference;

public class EnergizeExtension extends DashClockExtension {

	private static final String TAG = "EnergizeExtension";
	private final Messenger monitorServiceMessanger = new Messenger(new IncomingHandler(new WeakReference<>(this)));
	private int percentageLoaded = 0;
	private EstimationResult remainingTimeEstimation = new EstimationResult();
	private Messenger monitorService = null;

	private final ServiceConnection monitorServiceConnection = new ServiceConnection() {

		public void onServiceConnected(final ComponentName className, final IBinder service) {
			EnergizeExtension.this.monitorService = new Messenger(service);
			try {
				Log.d(EnergizeExtension.TAG, "Trying to connect to the battery monitoring service...");
				final Message msg = Message.obtain(null, MonitorBatteryStateService.MSG_REGISTER_CLIENT);
				msg.replyTo = EnergizeExtension.this.monitorServiceMessanger;
				EnergizeExtension.this.monitorService.send(msg);
			} catch (final RemoteException e) {
				Log.e(EnergizeExtension.TAG, "Failed to connect to the battery monitoring service!");
			}
		}

		public void onServiceDisconnected(final ComponentName className) {
			EnergizeExtension.this.monitorService = null;
		}
	};

	private void doBindService() {
		this.getApplicationContext().bindService(new Intent(this.getApplicationContext(), MonitorBatteryStateService.class), this.monitorServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onInitialize(boolean isReconnect) {
		this.setUpdateWhenScreenOn(true);
		super.onInitialize(isReconnect);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//
		this.doBindService();

		//
		this.updateBatteryInformation();

		//
		return super.onStartCommand(intent, flags, startId);
	}

	private void updateBatteryInformation() {
		// query the remaining time
		try {
			if (this.monitorService != null) {
				final Message msg2 = Message.obtain(null, MonitorBatteryStateService.MSG_REQUEST_REMAINING_TIME);
				msg2.replyTo = EnergizeExtension.this.monitorServiceMessanger;
				this.monitorService.send(msg2);
			} else {
				Log.w(EnergizeExtension.TAG, "No monitor service connected, trying to bind service!");
				this.doBindService();
			}
		} catch (RemoteException e) {
			Log.e(EnergizeExtension.TAG, "Failed to query the current time estimation.");
		}

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

					// do a potential level scaling (most of the times not required, but to be sure)
					int level = -1;
					if ((rawlevel >= 0) && (scale > 0)) {
						level = (rawlevel * 100) / scale;
					}

					Log.d(EnergizeExtension.TAG, "Updating the DashClock widget (basic information).");
					EnergizeExtension.this.percentageLoaded = level;
					EnergizeExtension.this.sendPublishedData();
				} catch (final IllegalStateException e) {
					Log.e(EnergizeExtension.TAG, "The fragment was in an illegal state while it received the battery information. This should be handled in a different (and better way), The exception message was: ", e); // TODO
				}
			}
		};
		final IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		this.getApplicationContext().registerReceiver(batteryLevelReceiver, batteryLevelFilter);
	}

	private void sendPublishedData() {
		Log.d(EnergizeExtension.TAG, "Constructing and sending DashClock widget update...");

		//
		if (this.remainingTimeEstimation.isValid) {
			this.publishUpdate(
					new ExtensionData().visible(true).icon(R.drawable.ic_battery_charging_full_black_24dp).status(this.percentageLoaded + "%")
							.expandedTitle(String.format(this.getApplicationContext().getString(R.string.dc_widget_charged_long), this.percentageLoaded))
							.expandedBody(String.format(EnergizeExtension.this.getApplicationContext().getString(R.string.notification_text_estimate), remainingTimeEstimation.remainingHours, remainingTimeEstimation.remainingMinutes))
							.clickIntent(new Intent(this.getApplicationContext(), MainActivity.class)));
		} else {
			this.publishUpdate(
					new ExtensionData().visible(true).icon(R.drawable.ic_battery_charging_full_black_24dp).status(this.percentageLoaded + "%")
							.expandedTitle(String.format(this.getApplicationContext().getString(R.string.dc_widget_charged_long), this.percentageLoaded))
							.expandedBody(EnergizeExtension.this.getApplicationContext().getString(R.string.notification_text_estimate_na)).clickIntent(new Intent(this.getApplicationContext(), MainActivity.class)));
		}
	}

	@Override
	public void onDestroy() {
		//this.doUnbindService();
		super.onDestroy();
	}

	protected void onUpdateData(int reason) {
		Log.i(EnergizeExtension.TAG, "Update requested... ");
		this.updateBatteryInformation();
		//this.publishUpdate(this.currentExtensionData);
	}

	private static class IncomingHandler extends Handler {

		private WeakReference<EnergizeExtension> energizeExtensionWeakReference;

		IncomingHandler(WeakReference<EnergizeExtension> weakReference) {
			energizeExtensionWeakReference = weakReference;
		}

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case MonitorBatteryStateService.MSG_REGISTER_CLIENT:
					// since the client is now registered, we can ask the service about the remaining time we have
					try {
						// be sure that the monitor service is available, sometimes (I don't know why) this is not the case
						if (null == energizeExtensionWeakReference || null == energizeExtensionWeakReference.get().monitorService) {
							Log.e(EnergizeExtension.TAG, "Tried to query the remaining time but the monitor service was not available!");
							return;
						}

						// query the remaining time
						final Message msg2 = Message.obtain(null, MonitorBatteryStateService.MSG_REQUEST_REMAINING_TIME);
						msg2.replyTo = energizeExtensionWeakReference.get().monitorServiceMessanger;
						energizeExtensionWeakReference.get().monitorService.send(msg2);
					} catch (final RemoteException e1) {
						Log.e(EnergizeExtension.TAG, "Failed to query the current time estimation.");
					}
					break;
				case MonitorBatteryStateService.MSG_REQUEST_REMAINING_TIME:
					if (null == energizeExtensionWeakReference) {
						Log.e(EnergizeExtension.TAG, "Tried to query the remaining time but the monitor service was not available!");
						return;
					}
					energizeExtensionWeakReference.get().remainingTimeEstimation = EstimationResult.fromBundle(msg.getData());
					Log.d(EnergizeExtension.TAG, String.format("Received an time estimation of %d minutes.", energizeExtensionWeakReference.get().remainingTimeEstimation.minutes));
					energizeExtensionWeakReference.get().sendPublishedData();
					break;
				default:
					super.handleMessage(msg);
			}
		}
	}

}
