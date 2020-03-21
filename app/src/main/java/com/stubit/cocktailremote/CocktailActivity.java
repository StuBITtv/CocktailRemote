package com.stubit.cocktailremote;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stubit.cocktailremote.models.CocktailModel;
import com.stubit.cocktailremote.modelviews.CocktailActivityViewModel;
import com.stubit.cocktailremote.modelviews.ViewModelFactory;
import com.stubit.cocktailremote.views.TextView;

public class CocktailActivity extends AppCompatActivity {

    public static final String ID_EXTRA_KEY = "cocktailId";
    CocktailActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cocktail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(
                        getApplicationContext(),
                        this,
                        getIntent().getIntExtra(ID_EXTRA_KEY, 0)
                )
        ).get(CocktailActivityViewModel.class);

        viewModel.getCocktailName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String cocktailName) {
                TextView cocktailNameView = findViewById(R.id.cocktail_name);
                cocktailNameView.setText(cocktailName, R.string.unnamed_cocktail);
            }
        });

        viewModel.getCocktailDescription().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String cocktailDescription) {
                TextView cocktailDescriptionView = findViewById(R.id.cocktail_description);
                cocktailDescriptionView.setText(cocktailDescription, R.string.no_description);
            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cocktail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if(item.getTitle() == getString(R.string.edit)) {
            Intent editIntent = new Intent(this, EditActivity.class);
            editIntent.putExtra(ID_EXTRA_KEY, viewModel.getCocktailId());

            startActivity(editIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}
