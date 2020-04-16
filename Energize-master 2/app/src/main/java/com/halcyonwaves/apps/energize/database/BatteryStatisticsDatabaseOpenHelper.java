package com.halcyonwaves.apps.energize.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BatteryStatisticsDatabaseOpenHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "batteryUsageStatistics.db";
	public static final int DATABASE_VERSION = 5;

	public BatteryStatisticsDatabaseOpenHelper(final Context context) {
		super(context, BatteryStatisticsDatabaseOpenHelper.DATABASE_NAME, null, BatteryStatisticsDatabaseOpenHelper.DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		RawBatteryStatisicsTable.onCreate(db);
		PowerEventsTable.onCreate(db);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		RawBatteryStatisicsTable.onUpgrade(db, oldVersion, newVersion);
		PowerEventsTable.onUpgrade(db, oldVersion, newVersion);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		RawBatteryStatisicsTable.onDowngrade(db, oldVersion, newVersion);
		PowerEventsTable.onDowngrade(db, oldVersion, newVersion);
	}
}
