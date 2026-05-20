package com.app.yourhabbitbuddy.ui.habits;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.app.yourhabbitbuddy.R;
import com.app.yourhabbitbuddy.data.Habit;
import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {
    private List<Habit> habits = new ArrayList<>();
    private OnHabitClick listener;
    private Context context;

    public interface OnHabitClick {
        void onClick(Habit habit);
    }

    public HabitAdapter(OnHabitClick listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position >= habits.size()) return;

        Habit habit = habits.get(position);
        if (habit == null) return;

        holder.tvName.setText(habit.getName());

        long days = (System.currentTimeMillis() - habit.getCreatedAt()) / (24 * 60 * 60 * 1000);
        holder.tvDays.setText(days + " " + context.getString(R.string.days_abbr));

        if ("good".equals(habit.getType())) {
            holder.tvType.setText("👍 " + context.getString(R.string.good_habit));
            holder.tvType.setTextColor(0xFF10B981);
        } else {
            holder.tvType.setText("⚠️ " + context.getString(R.string.bad_habit));
            holder.tvType.setTextColor(0xFFEF4444);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(habit);
            }
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    public void submitList(List<Habit> newHabits) {
        habits = newHabits != null ? newHabits : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvDays;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_habit_name);
            tvType = itemView.findViewById(R.id.tv_habit_type);
            tvDays = itemView.findViewById(R.id.tv_habit_days);
        }
    }
}