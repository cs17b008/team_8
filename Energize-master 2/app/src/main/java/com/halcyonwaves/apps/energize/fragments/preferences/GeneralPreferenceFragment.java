package com.halcyonwaves.apps.energize.fragments.preferences;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import com.halcyonwaves.apps.energize.R;
import com.halcyonwaves.apps.energize.services.MonitorBatteryStateService;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeneralPreferenceFragment extends PreferenceFragment {

	private final static String TAG = "GeneralPreference";
	private final Messenger monitorServiceMessanger = new Messenger(new IncomingHandler());
	private Messenger monitorService = null;

	private final ServiceConnection monitorServiceConnection = new ServiceConnection() {

		public void onServiceConnected(final ComponentName className, final IBinder service) {
			GeneralPreferenceFragment.this.monitorService = new Messenger(service);
			try {
				Log.d(GeneralPreferenceFragment.TAG, "Trying to connect to the battery monitoring service...");
				final Message msg = Message.obtain(null, MonitorBatteryStateService.MSG_REGISTER_CLIENT);
				msg.replyTo = GeneralPreferenceFragment.this.monitorServiceMessanger;
				GeneralPreferenceFragment.this.monitorService.send(msg);
			} catch (final RemoteException e) {
				Log.e(GeneralPreferenceFragment.TAG, "Failed to connect to the battery monitoring service!");
			}
		}

		public void onServiceDisconnected(final ComponentName className) {
			GeneralPreferenceFragment.this.monitorService = null;
		}
	};

	private void doBindService() {
		this.getActivity().bindService(new Intent(this.getActivity(), MonitorBatteryStateService.class), this.monitorServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void doUnbindService() {
		if (this.monitorService != null) {
			try {
				final Message msg = Message.obtain(null, MonitorBatteryStateService.MSG_UNREGISTER_CLIENT);
				msg.replyTo = this.monitorServiceMessanger;
				this.monitorService.send(msg);
			} catch (final RemoteException e) {
			}
		}
		this.getActivity().unbindService(this.monitorServiceConnection);
		this.monitorService = null;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.pref_general);

		Preference sendDatabasePreference = this.findPreference("batstatistics.cleardb");
		sendDatabasePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(final Preference preference) {
				Log.v(GeneralPreferenceFragment.TAG, "Clearing battery statistics database...");
				try {
					final Message msg = Message.obtain(null, MonitorBatteryStateService.MSG_CLEAR_STATISTICS);
					msg.replyTo = GeneralPreferenceFragment.this.monitorServiceMessanger;
					GeneralPreferenceFragment.this.monitorService.send(msg);
				} catch (final RemoteException e) {
					Log.e(GeneralPreferenceFragment.TAG, "Failed to clear the battery statistics database!");
				}
				return false;
			}
		});

		this.doBindService();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.doUnbindService();
	}

	private /*static*/ class IncomingHandler extends Handler {

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case MonitorBatteryStateService.MSG_CLEAR_STATISTICS:
					final AlertDialog.Builder builder = new AlertDialog.Builder(GeneralPreferenceFragment.this.getActivity());

					builder.setTitle(R.string.dialog_title_cleardb_successfull).setMessage(R.string.dialog_text_cleardb_successfull).setPositiveButton(android.R.string.ok, new OnClickListener() {

						public void onClick(final DialogInterface dialog, final int which) {
							dialog.dismiss();

						}
					});
					builder.create().show();
					// TODO: show notification
					break;
				case MonitorBatteryStateService.MSG_COPY_DB_TO_SDCARD:
					final AlertDialog.Builder builderCopy = new AlertDialog.Builder(GeneralPreferenceFragment.this.getActivity());

					builderCopy.setTitle(R.string.dialog_title_copydb_successfull).setMessage(R.string.dialog_text_copydb_successfull).setPositiveButton(android.R.string.ok, new OnClickListener() {

						public void onClick(final DialogInterface dialog, final int which) {
							dialog.dismiss();

						}
					});
					builderCopy.create().show();
					// TODO: show notification
					break;
				default:
					super.handleMessage(msg);
			}
		}
	}
}
