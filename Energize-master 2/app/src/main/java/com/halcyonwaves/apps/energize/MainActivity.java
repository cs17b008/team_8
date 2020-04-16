package com.halcyonwaves.apps.energize;

import static java.text.MessageFormat.format;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.halcyonwaves.apps.energize.dialogs.ChangeLogDialog;
import com.halcyonwaves.apps.energize.fragments.BatteryCapacityGraphFragment;
import com.halcyonwaves.apps.energize.fragments.OverviewFragment;
import com.halcyonwaves.apps.energize.fragments.TemperatureGraphFragment;
import com.halcyonwaves.apps.energize.services.MonitorBatteryStateService;
import java.util.List;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {

	private static final String TAG = "MainActivity";

	private static final String LAST_FRAGMENT_BUNDLE_CONST = "LastFragmentPosition";

	private int lastSelectedFragment = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		// check if the service is running, if not start it
		if (!ApplicationCore.isServiceRunning(this, MonitorBatteryStateService.class.getName())) {
			Log.v(MainActivity.TAG, "Monitoring service is not running, starting it...");
			this.getApplicationContext().startService(new Intent(this.getApplicationContext(), MonitorBatteryStateService.class));
		}

		// show the changelog dialog
		ChangeLogDialog changeDlg = new ChangeLogDialog(this);
		changeDlg.show();

		// ensure the correct item will be displayed
		if ("com.halcyonwaves.apps.energize.fragments.BatteryCapacityGraphFragment".equals(getIntent().getAction())) {
			navigationView.getMenu().performIdentifierAction(R.id.nav_battery_graph, 0);
		} else {
			navigationView.getMenu().performIdentifierAction(R.id.nav_overview, 0);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(LAST_FRAGMENT_BUNDLE_CONST, lastSelectedFragment);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			final int restoreFragmentId = savedInstanceState.getInt(LAST_FRAGMENT_BUNDLE_CONST);
			selectItem(restoreFragmentId);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}


	private static String defaultIfNull(final String inputValue, final String defaultValue) {
		return (null == inputValue || "null".equals(inputValue)) ? defaultValue : inputValue;
	}


	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.nav_overview) {
			selectItem(0);
		} else if (id == R.id.nav_battery_graph) {
			selectItem(1);
		} else if (id == R.id.nav_settings) {
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
		}
		if (item.isChecked()) {
			item.setChecked(false);
		} else {
			item.setChecked(true);
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	private void selectItem(int position) {
		if (this.findViewById(R.id.fragment_container) != null) {
			Fragment firstFragment = null;
			switch (position) {
				case 1:
					firstFragment = new BatteryCapacityGraphFragment();
					break;
				case 0:
				default:
					firstFragment = new OverviewFragment();
					break;
			}
			firstFragment.setArguments(this.getIntent().getExtras());
			this.getFragmentManager().beginTransaction().replace(R.id.fragment_container, firstFragment).commit();
			lastSelectedFragment = position;
		}
	}
}
