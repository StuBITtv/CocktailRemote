package com.stubit.cocktailremote;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
                PasswordValidation.validatePassword(
                        this,
                        () -> startActivity(new Intent(getApplicationContext(), EditActivity.class))
                );
            }
        });

        RecyclerView cocktailList = findViewById(R.id.cocktail_list);
        cocktailList.setAdapter(
                new CocktailAdapter(this, viewModel)
        );
        cocktailList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                PasswordValidation.validatePassword(this, () -> {
                    startActivity(new Intent(this, SettingsActivity.class));
                });
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
