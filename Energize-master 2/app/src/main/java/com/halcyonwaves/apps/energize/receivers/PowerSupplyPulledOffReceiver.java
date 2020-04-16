package com.halcyonwaves.apps.energize.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.halcyonwaves.apps.energize.services.MonitorBatteryStateService;

public class PowerSupplyPulledOffReceiver extends BroadcastReceiver {

	private final static String TAG = "PwrSupplyPulledOffRec";

	private MonitorBatteryStateService service = null;

	public PowerSupplyPulledOffReceiver(final MonitorBatteryStateService service) {
		this.service = service;
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		Log.v(PowerSupplyPulledOffReceiver.TAG, "An external power supply was unplugged!");
		this.service.insertPowerSupplyChangeEvent(false);
	}

}
