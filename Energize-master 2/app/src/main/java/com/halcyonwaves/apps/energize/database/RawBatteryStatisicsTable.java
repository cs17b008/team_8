package com.halcyonwaves.apps.energize.database;

import android.database.sqlite.SQLiteDatabase;

public final class RawBatteryStatisicsTable {

	public static final int CHARGING_STATE_CHARGING_AC = 1;
	public static final int CHARGING_STATE_CHARGING_USB = 2;
	public static final int CHARGING_STATE_DISCHARGING = 0;
	public static final String COLUMN_BATTERY_TEMPRATURE = "batteryTemprature";
	public static final String COLUMN_CHARGING_LEVEL = "chargingLevel";
	public static final String COLUMN_CHARGING_SCALE = "chargingScale";
	public static final String COLUMN_CHARGING_STATE = "chargingState";
	public static final String COLUMN_EVENT_TIME = "eventTime";

	public static final String COLUMN_ID = "_id";
	public static final String TABLE_NAME = "rawBatteryStats";
	private static final String TABLE_CREATE = "CREATE TABLE " + RawBatteryStatisicsTable.TABLE_NAME + "( " + RawBatteryStatisicsTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + RawBatteryStatisicsTable.COLUMN_EVENT_TIME + " INTEGER, "
			+ RawBatteryStatisicsTable.COLUMN_CHARGING_STATE + " INTEGER, " + RawBatteryStatisicsTable.COLUMN_CHARGING_LEVEL + " INTEGER, " + RawBatteryStatisicsTable.COLUMN_CHARGING_SCALE + " INTEGER, "
			+ RawBatteryStatisicsTable.COLUMN_BATTERY_TEMPRATURE + " INTEGER );";
	private static final String TABLE_DROP = "DROP TABLE IF EXISTS " + RawBatteryStatisicsTable.TABLE_NAME + ";";

	public static void onCreate(final SQLiteDatabase db) {
		db.execSQL(RawBatteryStatisicsTable.TABLE_CREATE);
	}

	public static void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		// the only upgrade option is to delete the old database...
		db.execSQL(RawBatteryStatisicsTable.TABLE_DROP);

		// ... and to create a new one
		db.execSQL(RawBatteryStatisicsTable.TABLE_CREATE);
	}

	public static void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// the only downgrade option is to delete the old database...
		db.execSQL(RawBatteryStatisicsTable.TABLE_DROP);

		// ... and to create a new one
		db.execSQL(RawBatteryStatisicsTable.TABLE_CREATE);
	}
}
