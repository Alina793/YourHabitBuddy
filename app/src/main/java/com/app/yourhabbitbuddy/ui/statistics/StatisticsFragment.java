package com.app.yourhabbitbuddy.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.app.yourhabbitbuddy.R;
import com.app.yourhabbitbuddy.data.Habit;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel viewModel;
    private BarChart barChart;
    private LineChart lineChart;
    private Spinner spinnerHabits;
    private TextView tvProgress, tvStreak, tvTotal, tvEmpty;
    private List<Habit> habits = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barChart = view.findViewById(R.id.bar_chart);
        lineChart = view.findViewById(R.id.line_chart);
        spinnerHabits = view.findViewById(R.id.spinner_habits);
        tvProgress = view.findViewById(R.id.tv_progress);
        tvStreak = view.findViewById(R.id.tv_streak);
        tvTotal = view.findViewById(R.id.tv_total);
        tvEmpty = view.findViewById(R.id.tv_empty);

        setupCharts();
        setupViewModel();
    }

    private void setupCharts() {
        if (barChart != null) {
            barChart.getDescription().setEnabled(false);
            barChart.setDrawGridBackground(false);
            barChart.setDrawBarShadow(false);
            barChart.setDrawValueAboveBar(true);
            barChart.setPinchZoom(false);
            barChart.setScaleEnabled(false);
            barChart.animateY(800);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            xAxis.setTextSize(11f);

            barChart.getAxisRight().setEnabled(false);
            barChart.getLegend().setEnabled(false);
            barChart.getAxisLeft().setAxisMinimum(0);
            barChart.getAxisLeft().setAxisMaximum(100);
        }

        if (lineChart != null) {
            lineChart.getDescription().setEnabled(false);
            lineChart.setDrawGridBackground(false);
            lineChart.setTouchEnabled(true);
            lineChart.setDragEnabled(true);
            lineChart.setScaleEnabled(true);
            lineChart.animateXY(800, 800);

            XAxis lineXAxis = lineChart.getXAxis();
            lineXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            lineXAxis.setGranularity(1f);
            lineXAxis.setTextSize(10f);

            lineChart.getAxisRight().setEnabled(false);
            lineChart.getLegend().setEnabled(true);
            lineChart.getLegend().setTextSize(11f);
            lineChart.getAxisLeft().setAxisMinimum(0);
            lineChart.getAxisLeft().setAxisMaximum(100);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        viewModel.getHabits().observe(getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty()) {
                habits = list;
                updateSpinner();
                if (tvEmpty != null) {
                    tvEmpty.setVisibility(View.GONE);
                }
            } else {
                if (tvEmpty != null) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText(getString(R.string.no_habits_data));
                }
            }
        });

        // ВИПРАВЛЕНО: weekData - це об'єкт WeeklyData, потрібно брати weekData.values
        viewModel.getWeeklyData().observe(getViewLifecycleOwner(), weekData -> {
            if (weekData != null && weekData.values != null && !weekData.values.isEmpty() && barChart != null) {
                updateBarChart(weekData.values);
            }
        });

        // ВИПРАВЛЕНО: monthData - це об'єкт MonthlyData, потрібно брати monthData.values
        viewModel.getMonthlyData().observe(getViewLifecycleOwner(), monthData -> {
            if (monthData != null && monthData.values != null && !monthData.values.isEmpty() && lineChart != null) {
                updateLineChart(monthData.values);
            }
        });

        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                if (tvProgress != null) tvProgress.setText(stats.progress + "%");
                if (tvStreak != null) tvStreak.setText(String.valueOf(stats.streak));
                if (tvTotal != null) tvTotal.setText(String.valueOf(stats.total));
            }
        });
    }

    private void updateSpinner() {
        if (habits.isEmpty() || spinnerHabits == null) return;

        List<String> names = new ArrayList<>();
        for (Habit habit : habits) {
            names.add(habit.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHabits.setAdapter(adapter);

        spinnerHabits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.loadStats(habits.get(position).getId());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (!habits.isEmpty()) {
            viewModel.loadStats(habits.get(0).getId());
        }
    }

    private void updateBarChart(List<Integer> values) {
        if (barChart == null) return;

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new BarEntry(i, values.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(getResources().getColor(R.color.primary));
        dataSet.setValueTextColor(getResources().getColor(R.color.text_primary));
        dataSet.setValueTextSize(11f);
        dataSet.setDrawValues(true);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "%";
            }
        });

        barChart.setData(new BarData(dataSet));

        String[] days = {
                getString(R.string.monday),
                getString(R.string.tuesday),
                getString(R.string.wednesday),
                getString(R.string.thursday),
                getString(R.string.friday),
                getString(R.string.saturday),
                getString(R.string.sunday)
        };
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days));
        barChart.getXAxis().setLabelCount(days.length, true);

        barChart.invalidate();
    }

    private void updateLineChart(List<Integer> values) {
        if (lineChart == null) return;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new Entry(i, values.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.progress_over_month));
        dataSet.setColor(getResources().getColor(R.color.gradient_end));
        dataSet.setCircleColor(getResources().getColor(R.color.primary));
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircleHole(true);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.primary_light));
        dataSet.setFillAlpha(80);
        dataSet.setDrawValues(false);

        lineChart.setData(new LineData(dataSet));

        String[] days = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            days[i] = String.valueOf(i + 1);
        }
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days));
        lineChart.getXAxis().setLabelCount(6, true);

        lineChart.invalidate();
    }
}