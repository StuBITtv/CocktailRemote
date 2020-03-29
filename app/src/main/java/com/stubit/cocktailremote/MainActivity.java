package com.stubit.cocktailremote;

import android.content.Intent;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.stubit.cocktailremote.adapters.CocktailAdapter;
import com.stubit.cocktailremote.modelviews.ItemListMainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.stubit.cocktailremote.modelviews.ViewModelFactory;

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
        fab.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), EditActivity.class)));

        RecyclerView cocktailList = findViewById(R.id.cocktail_list);
        cocktailList.setAdapter(
                new CocktailAdapter(this, viewModel)
        );
        cocktailList.setLayoutManager(new LinearLayoutManager(this));
    }
}
