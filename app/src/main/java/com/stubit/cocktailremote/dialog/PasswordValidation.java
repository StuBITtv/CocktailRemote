package com.stubit.cocktailremote.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.stubit.cocktailremote.R;

public class PasswordValidation {
    public static void validatePassword(AppCompatActivity app, Runnable onValidation) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(app, R.style.AppTheme_Dialog);

        @SuppressLint("InflateParams")
        View inputLayout = app.getLayoutInflater().inflate(R.layout.dialog_password_validation, null);
        EditText passwordInput = inputLayout.findViewById(R.id.password_input);

        String password = getPassword(app);

        alertBuilder.setPositiveButton(R.string.ok, null);

        alertBuilder.setView(inputLayout);
        alertBuilder.setCancelable(true);
        alertBuilder.setNeutralButton(R.string.cancel, (dialog, which) -> {});

        AlertDialog dialog = alertBuilder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            View invalidPasswordNotification = inputLayout.findViewById(R.id.invalid_password);

            if(passwordInput.getText().toString().equals(password)) {
                invalidPasswordNotification.setVisibility(View.GONE);
                dialog.dismiss();

                if (onValidation != null) {
                    onValidation.run();
                }
            } else {
                invalidPasswordNotification.setVisibility(View.VISIBLE);
            }
        });
    }

    public static boolean passwordIsNotSet(Context c) {
        return getPassword(c) == null || getPassword(c).equals("");
    }

    public static boolean editIsUnlocked(Context c) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        return !settings.getBoolean("lock_edit", false);

    }

    private static String getPassword(Context c) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        return settings.getString("password", null);
    }
}
