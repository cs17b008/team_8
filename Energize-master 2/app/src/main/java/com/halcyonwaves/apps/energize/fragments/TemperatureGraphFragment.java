package com.halcyonwaves.apps.energize.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.halcyonwaves.apps.energize.R;
import com.halcyonwaves.apps.energize.database.BatteryStatisticsDatabaseOpenHelper;
import com.halcyonwaves.apps.energize.database.RawBatteryStatisicsTable;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TemperatureGraphFragment extends Fragment {

	private static final String TAG = "TempGraphFrag";
	private SharedPreferences sharedPref = null;
	private GraphView graphView = null;

	private LineGraphSeries getBatteryStatisticData() {
		LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

		BatteryStatisticsDatabaseOpenHelper batteryDbOpenHelper = new BatteryStatisticsDatabaseOpenHelper(this.getActivity().getApplicationContext());
		SQLiteDatabase batteryStatisticsDatabase = batteryDbOpenHelper.getReadableDatabase();
		Cursor lastEntryMadeCursor = batteryStatisticsDatabase
				.query(RawBatteryStatisicsTable.TABLE_NAME, new String[]{RawBatteryStatisicsTable.COLUMN_EVENT_TIME, RawBatteryStatisicsTable.COLUMN_BATTERY_TEMPRATURE}, null, null, null, null, RawBatteryStatisicsTable.COLUMN_EVENT_TIME + " ASC");

		//
		final int columnIndexEventTime = lastEntryMadeCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_EVENT_TIME);
		final int columnIndexChargingLevel = lastEntryMadeCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_BATTERY_TEMPRATURE);

		//
		TemperatureUnit usedUnit = TemperatureUnit.TemperatureUnitCelsius;
		final String prefUsedUnit = TemperatureGraphFragment.this.sharedPref.getString("display.temperature_unit", "Celsius");
		if (prefUsedUnit.compareToIgnoreCase("fahrenheit") == 0) {
			usedUnit = TemperatureUnit.TemperatureUnitFahrenheit;
		} else if (prefUsedUnit.compareToIgnoreCase("kelvin") == 0) {
			usedUnit = TemperatureUnit.TemperatureUnitKelvin;
		}

		lastEntryMadeCursor.moveToFirst();
		double oldtestTime = Long.MAX_VALUE;
		while (!lastEntryMadeCursor.isAfterLast()) {
			Log.v(TemperatureGraphFragment.TAG, "Found a stored temperature: " + lastEntryMadeCursor.getInt(columnIndexChargingLevel));
			final double currentTime = lastEntryMadeCursor.getInt(columnIndexEventTime);
			if (currentTime < oldtestTime) {
				oldtestTime = (long) currentTime;
			}
			final double temperatureConverted;
			switch (usedUnit) {
				case TemperatureUnitCelsius:
					temperatureConverted = lastEntryMadeCursor.getDouble(columnIndexChargingLevel) / 10.0;
					break;
				case TemperatureUnitFahrenheit:
					temperatureConverted = ((lastEntryMadeCursor.getDouble(columnIndexChargingLevel) / 10.0f) * 1.8f) + 32.0f;
					break;
				case TemperatureUnitKelvin:
					temperatureConverted = (lastEntryMadeCursor.getDouble(columnIndexChargingLevel) / 10.0f) + 273.15f;
					break;
				default:
					temperatureConverted = 0.0;
			}

			series.appendData(
					new DataPoint(currentTime, temperatureConverted),
					true,
					Integer.MAX_VALUE); // TODO: limit the amount of data points
			lastEntryMadeCursor.moveToNext();
		}

		lastEntryMadeCursor.close();
		batteryDbOpenHelper.close();

		series.setColor(Color.RED);

		return series;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		this.sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
		final View inflatedView = inflater.inflate(R.layout.fragment_temperaturegraph, container, false);
		this.graphView = inflatedView.findViewById(R.id.layout_graph_view_temperature);

		final LineGraphSeries temperatureSeries = getBatteryStatisticData();
		this.graphView.addSeries(temperatureSeries);
		this.graphView.getViewport().setMinY(temperatureSeries.getLowestValueY() - 10.0);
		this.graphView.getViewport().setMaxY(temperatureSeries.getHighestValueY() + 10.0);
		this.graphView.getViewport().setYAxisBoundsManual(true);
		this.graphView.getViewport().setScrollable(true);
		this.graphView.getGridLabelRenderer().setLabelFormatter(new TemperatureGraphLabelFormatter());

		return inflatedView;
	}

	@Override
	public void onResume() {
		super.onResume();
		this.updateGraph();
	}

	private void updateGraph() {
		final LineGraphSeries temperatureSeries = getBatteryStatisticData();
		this.graphView.removeAllSeries();
		this.graphView.addSeries(temperatureSeries);
		this.graphView.getViewport().setMinY(temperatureSeries.getLowestValueY() - 10.0);
		this.graphView.getViewport().setMaxY(temperatureSeries.getHighestValueY() + 10.0);
	}

	private enum TemperatureUnit {
		TemperatureUnitCelsius,
		TemperatureUnitFahrenheit,
		TemperatureUnitKelvin
	}

	private class TemperatureGraphLabelFormatter implements LabelFormatter {

		@Override
		public String formatLabel(double value, boolean isValueX) {
			if (isValueX) {
				final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.US);
				return dateFormat.format(new Date((long) value * 1000));
			} else {
				//
				TemperatureUnit usedUnit = TemperatureUnit.TemperatureUnitCelsius;
				final String prefUsedUnit = TemperatureGraphFragment.this.sharedPref.getString("display.temperature_unit", "Celsius");
				if (prefUsedUnit.compareToIgnoreCase("fahrenheit") == 0) {
					usedUnit = TemperatureUnit.TemperatureUnitFahrenheit;
				} else if (prefUsedUnit.compareToIgnoreCase("kelvin") == 0) {
					usedUnit = TemperatureUnit.TemperatureUnitKelvin;
				}

				//
				switch (usedUnit) {
					case TemperatureUnitCelsius:
						return TemperatureGraphFragment.this.getString(R.string.textview_text_temperature_celsius, value);
					case TemperatureUnitFahrenheit:
						return TemperatureGraphFragment.this.getString(R.string.textview_text_temperature_fahrenheit, value);
					case TemperatureUnitKelvin:
						return TemperatureGraphFragment.this.getString(R.string.textview_text_temperature_kelvin, value);
				}

				//
				return "N/A";
			}
		}

		@Override
		public void setViewport(Viewport viewport) {
			// TODO: this
		}
	}
}
