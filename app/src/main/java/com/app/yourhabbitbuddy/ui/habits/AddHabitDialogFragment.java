package com.app.yourhabbitbuddy.ui.habits;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.app.yourhabbitbuddy.R;

public class AddHabitDialogFragment extends DialogFragment {
    private OnHabitAddedListener listener;

    public interface OnHabitAddedListener {
        void onHabitAdded(String name, String type);
    }

    public void setOnHabitAdded(OnHabitAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_habit, null);

        EditText etName = view.findViewById(R.id.et_habit_name);
        RadioGroup rgType = view.findViewById(R.id.rg_type);
        Button btnAdd = view.findViewById(R.id.btn_add);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        // ВАЖЛИВО: оновлюємо текст під час показу діалогу
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.add_habit));
        etName.setHint(getString(R.string.habit_name));

        RadioButton rbGood = view.findViewById(R.id.rb_good);
        RadioButton rbBad = view.findViewById(R.id.rb_bad);
        rbGood.setText(getString(R.string.good_habit));
        rbBad.setText(getString(R.string.bad_habit));

        btnAdd.setText(getString(R.string.add));
        btnCancel.setText(getString(R.string.cancel));

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                String type = rgType.getCheckedRadioButtonId() == R.id.rb_good ? "good" : "bad";
                if (listener != null) listener.onHabitAdded(name, type);
                dismiss();
            } else {
                etName.setError(getString(R.string.habit_name));
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(getContext())
                .setView(view)
                .create();
    }
}