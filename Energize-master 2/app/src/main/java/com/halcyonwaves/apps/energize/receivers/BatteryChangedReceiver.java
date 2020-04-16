package com.halcyonwaves.apps.energize.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import com.halcyonwaves.apps.energize.services.MonitorBatteryStateService;

public class BatteryChangedReceiver extends BroadcastReceiver {

	private MonitorBatteryStateService service = null;

	public BatteryChangedReceiver(final MonitorBatteryStateService service) {
		this.service = service;
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		final int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		final int powerSource = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		final int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
		this.service.insertPowerValue(powerSource, scale, level, temp);
	}

}
