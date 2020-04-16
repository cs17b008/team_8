package com.halcyonwaves.apps.energize.database;

import android.database.sqlite.SQLiteDatabase;

public final class PowerEventsTable {

	public static final String COLUMN_BATTERY_IS_CHARGING = "isCharging";
	public static final String COLUMN_EVENT_TIME = "eventTime";
	public static final String COLUMN_ID = "_id";
	public static final int POWER_EVENT_IS_CHARGING = 1;

	public static final int POWER_EVENT_IS_NOT_CHARGING = 0;
	public static final String TABLE_NAME = "powerEvents";
	private static final String TABLE_CREATE =
			"CREATE TABLE " + PowerEventsTable.TABLE_NAME + "( " + PowerEventsTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PowerEventsTable.COLUMN_EVENT_TIME + " INTEGER, " + PowerEventsTable.COLUMN_BATTERY_IS_CHARGING + " INTEGER );";
	private static final String TABLE_DROP = "DROP TABLE IF EXISTS " + PowerEventsTable.TABLE_NAME + ";";

	public static void onCreate(final SQLiteDatabase db) {
		db.execSQL(PowerEventsTable.TABLE_CREATE);
	}

	public static void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		// the only upgrade option is to delete the old database...
		db.execSQL(PowerEventsTable.TABLE_DROP);

		// ... and to create a new one
		db.execSQL(PowerEventsTable.TABLE_CREATE);
	}

	public static void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// the only downgrade option is to delete the old database...
		db.execSQL(PowerEventsTable.TABLE_DROP);

		// ... and to create a new one
		db.execSQL(PowerEventsTable.TABLE_CREATE);
	}
}
