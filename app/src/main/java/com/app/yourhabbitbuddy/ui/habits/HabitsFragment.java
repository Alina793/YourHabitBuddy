package com.app.yourhabbitbuddy.ui.habits;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.app.yourhabbitbuddy.R;
import com.app.yourhabbitbuddy.data.Habit;

public class HabitsFragment extends Fragment {
    private HabitsViewModel viewModel;
    private HabitAdapter adapter;
    private TextView tvTotalHabits, tvTotalDays, tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habits, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalHabits = view.findViewById(R.id.tv_total_habits);
        tvTotalDays = view.findViewById(R.id.tv_total_days);
        tvEmpty = view.findViewById(R.id.tv_empty);

        setupRecyclerView(view);
        setupViewModel();

        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showAddDialog());
    }

    private void setupRecyclerView(View view) {
        adapter = new HabitAdapter(habit -> showDetails(habit), getContext());
        RecyclerView rv = view.findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HabitsViewModel.class);

        // Перевіряємо на null перед підпискою
        if (viewModel.getAllHabits() != null) {
            viewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
                if (habits != null) {
                    adapter.submitList(habits);
                    tvTotalHabits.setText(String.valueOf(habits.size()));
                    tvEmpty.setVisibility(habits.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
        }

        if (viewModel.getTotalDays() != null) {
            viewModel.getTotalDays().observe(getViewLifecycleOwner(), days -> {
                if (days != null) {
                    tvTotalDays.setText(String.valueOf(days));
                }
            });
        }
    }

    private void showAddDialog() {
        AddHabitDialogFragment dialog = new AddHabitDialogFragment();
        dialog.setOnHabitAdded((name, type) -> {
            viewModel.addHabit(name, type);
            if (getView() != null) {
                Snackbar.make(getView(), getString(R.string.habit_added), Snackbar.LENGTH_SHORT).show();
            }
        });
        dialog.show(getParentFragmentManager(), "add");
    }

    private void showDetails(Habit habit) {
        if (habit == null) return;

        long now = System.currentTimeMillis();
        long days = (now - habit.getCreatedAt()) / (24 * 60 * 60 * 1000);
        String typeStr = habit.getType().equals("good") ? getString(R.string.good_habit) : getString(R.string.bad_habit);

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(habit.getName())
                .setMessage(getString(R.string.habit_type) + ": " + typeStr + "\n\n📅 " + days + " " + getString(R.string.days_abbr))
                .setPositiveButton(getString(R.string.delete), (d, w) -> {
                    viewModel.deleteHabit(habit);
                    if (getView() != null) {
                        Snackbar.make(getView(), getString(R.string.habit_deleted), Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.close), null)
                .show();
    }
}