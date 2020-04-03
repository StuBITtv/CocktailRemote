package com.stubit.cocktailremote;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.stubit.cocktailremote.adapters.CocktailAdapter;
import com.stubit.cocktailremote.bluetooth.BluetoothManager;
import com.stubit.cocktailremote.dialog.PasswordValidation;
import com.stubit.cocktailremote.modelviews.ItemListMainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.stubit.cocktailremote.modelviews.ViewModelFactory;
import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ItemListMainViewModel viewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(getApplicationContext(), this)
        ).get(ItemListMainViewModel.class);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if(PasswordValidation.passwordIsNotSet(this) || PasswordValidation.editIsUnlocked(this)) {
                startActivity(new Intent(getApplicationContext(), EditActivity.class));
            } else {
                pushDialog(PasswordValidation.validatePassword(
                        this,
                        () -> startActivity(new Intent(getApplicationContext(), EditActivity.class))
                ));
            }
        });

        RecyclerView cocktailList = findViewById(R.id.cocktail_list);
        cocktailList.setAdapter(
                new CocktailAdapter(this, viewModel)
        );
        cocktailList.setLayoutManager(new LinearLayoutManager(this));

        BluetoothManager.getInstance().registerBluetoothDisconnectWatcher(this, () -> {
            if(ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                Log.d(TAG, "App in foreground, notify about disconnect");
                Toast.makeText(this, R.string.bluetooth_disconnected, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        pushDialog(null);
        BluetoothManager.getInstance().unregisterBluetoothDisconnectWatcher(this);
        BluetoothManager.getInstance().cleanup(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getTitle() != null && item.getTitle().equals(getString(R.string.settings))) {
            if(PasswordValidation.passwordIsNotSet(this)) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else {
                //noinspection CodeBlock2Expr
                pushDialog(PasswordValidation.validatePassword(this, () -> {
                    startActivity(new Intent(this, SettingsActivity.class));
                }));
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void pushDialog(Dialog dialog) {
        if(mDialog != null) {
            mDialog.dismiss();
        }

        mDialog = dialog;
    }
}
