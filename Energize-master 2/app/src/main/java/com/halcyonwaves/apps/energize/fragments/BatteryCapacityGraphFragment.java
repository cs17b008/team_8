package com.halcyonwaves.apps.energize.fragments;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

public class BatteryCapacityGraphFragment extends Fragment {

	private GraphView graphView = null;

	private LineGraphSeries getBatteryStatisticData() {
		LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

		BatteryStatisticsDatabaseOpenHelper batteryDbOpenHelper = new BatteryStatisticsDatabaseOpenHelper(this.getActivity().getApplicationContext());
		SQLiteDatabase batteryStatisticsDatabase = batteryDbOpenHelper.getReadableDatabase();
		Cursor lastEntryMadeCursor = batteryStatisticsDatabase
				.query(RawBatteryStatisicsTable.TABLE_NAME, new String[]{RawBatteryStatisicsTable.COLUMN_EVENT_TIME, RawBatteryStatisicsTable.COLUMN_CHARGING_LEVEL}, null, null, null, null, RawBatteryStatisicsTable.COLUMN_EVENT_TIME + " ASC");

		final int columnIndexEventTime = lastEntryMadeCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_EVENT_TIME);
		final int columnIndexChargingLevel = lastEntryMadeCursor.getColumnIndex(RawBatteryStatisicsTable.COLUMN_CHARGING_LEVEL);

		lastEntryMadeCursor.moveToFirst();
		double oldtestTime = Long.MAX_VALUE;
		while (!lastEntryMadeCursor.isAfterLast()) {
			final double currentTime = lastEntryMadeCursor.getInt(columnIndexEventTime);
			if (currentTime < oldtestTime) {
				oldtestTime = (double) currentTime;
			}
			series.appendData(
					new DataPoint(currentTime, lastEntryMadeCursor.getDouble(columnIndexChargingLevel)),
					true,
					Integer.MAX_VALUE); // TODO: limit the amount of data points
			lastEntryMadeCursor.moveToNext();
		}

		lastEntryMadeCursor.close();
		batteryDbOpenHelper.close();

		return series;
	}

	@Override
	public void onResume() {
		super.onResume();
		this.updateGraph();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View inflatedView = inflater.inflate(R.layout.fragment_batterycapacitygraph, container, false);
		this.graphView = inflatedView.findViewById(R.id.layout_graph_view);

		this.graphView.addSeries(getBatteryStatisticData());
		this.graphView.getViewport().setMinY(0.0);
		this.graphView.getViewport().setMaxY(100.0);
		this.graphView.getViewport().setYAxisBoundsManual(true);
		this.graphView.getViewport().setScrollable(true);
		this.graphView.getGridLabelRenderer().setLabelFormatter(new TimeLabelFormatter());

		return inflatedView;
	}

	private void updateGraph() {
		this.graphView.removeAllSeries();
		this.graphView.addSeries(getBatteryStatisticData());
	}

	private class TimeLabelFormatter implements LabelFormatter {

		@Override
		public String formatLabel(double value, boolean isValueX) {
			if (isValueX) {
				final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.US);
				return dateFormat.format(new Date((long) value * 1000));
			} else {
				return value + " %";
			}
		}

		@Override
		public void setViewport(Viewport viewport) {
			// TODO:
		}
	}
}
