//package com.app.yourhabbitbuddy.ui.statistics;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Spinner;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.app.yourhabbitbuddy.data.Habit;
//import com.github.mikephil.charting.charts.BarChart;
//import com.github.mikephil.charting.components.XAxis;
//import com.github.mikephil.charting.data.BarData;
//import com.github.mikephil.charting.data.BarDataSet;
//import com.github.mikephil.charting.data.BarEntry;
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
//import com.yourhabbitbuddy.R;
//
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class StatisticsFragment extends Fragment {
//
//    private StatisticsViewModel viewModel;
//    private BarChart barChart;
//    private Spinner spinner;
//    private TextView tvProgress, tvStreak, tvTotal, tvEmpty;
//    private List<Habit> habits = new ArrayList<>();
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_statistics, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // Ініціалізація всіх View
//        barChart = view.findViewById(R.id.bar_chart);
//        spinner = view.findViewById(R.id.spinner_habits);
//        tvProgress = view.findViewById(R.id.tv_progress);
//        tvStreak = view.findViewById(R.id.tv_streak);
//        tvTotal = view.findViewById(R.id.tv_total);
//        tvEmpty = view.findViewById(R.id.tv_empty);  // Тепер цей ID існує
//
//        setupChart();
//        setupViewModel();
//    }
//
//    private void setupChart() {
//        if (barChart != null) {
//            barChart.getDescription().setEnabled(false);
//            barChart.setDrawGridBackground(false);
//            barChart.getAxisLeft().setAxisMinimum(0);
//            barChart.getAxisLeft().setAxisMaximum(100);
//            barChart.getAxisRight().setEnabled(false);
//            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//            barChart.animateY(800);
//        }
//    }
//
//    private void setupViewModel() {
//        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
//
//        viewModel.getHabits().observe(getViewLifecycleOwner(), list -> {
//            if (list != null) {
//                habits = list;
//            } else {
//                habits = new ArrayList<>();
//            }
//
//            if (tvEmpty != null) {
//                if (habits.isEmpty()) {
//                    tvEmpty.setVisibility(View.VISIBLE);
//                    if (spinner != null) spinner.setVisibility(View.GONE);
//                    if (barChart != null) barChart.setVisibility(View.GONE);
//                } else {
//                    tvEmpty.setVisibility(View.GONE);
//                    if (spinner != null) spinner.setVisibility(View.VISIBLE);
//                    if (barChart != null) barChart.setVisibility(View.VISIBLE);
//                    updateSpinner();
//                }
//            }
//        });
//
//        viewModel.getWeeklyData().observe(getViewLifecycleOwner(), data -> {
//            if (data != null && data.values != null && !data.values.isEmpty()) {
//                updateChart(data);
//            }
//        });
//
//        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
//            if (stats != null) {
//                if (tvProgress != null) tvProgress.setText(stats.progress + "%");
//                if (tvStreak != null) tvStreak.setText(String.valueOf(stats.streak));
//                if (tvTotal != null) tvTotal.setText(String.valueOf(stats.total));
//            }
//        });
//    }
//
//    private void updateSpinner() {
//        if (habits == null || habits.isEmpty() || spinner == null) return;
//
//        List<String> names = new ArrayList<>();
//        for (Habit habit : habits) {
//            names.add(habit.getName());
//        }
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
//                android.R.layout.simple_spinner_item, names);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position >= 0 && position < habits.size()) {
//                    viewModel.loadStats(habits.get(position).getId());
//                }
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {}
//        });
//
//        if (!habits.isEmpty()) {
//            viewModel.loadStats(habits.get(0).getId());
//        }
//    }
//
//    private void updateChart(StatisticsViewModel.WeeklyData data) {
//        if (data == null || data.values == null || data.values.isEmpty() || barChart == null) return;
//
//        List<BarEntry> entries = new ArrayList<>();
//        for (int i = 0; i < data.values.size(); i++) {
//            entries.add(new BarEntry(i, data.values.get(i)));
//        }
//
//        BarDataSet dataSet = new BarDataSet(entries, "");
//        dataSet.setColor(getResources().getColor(R.color.primary));
//        dataSet.setDrawValues(false);
//
//        barChart.setData(new BarData(dataSet));
//
//        String[] days = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд"};
//        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days));
//        barChart.invalidate();
//    }
//}