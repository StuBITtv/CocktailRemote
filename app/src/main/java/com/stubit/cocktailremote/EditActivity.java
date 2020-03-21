package com.stubit.cocktailremote;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.stubit.cocktailremote.models.CocktailModel;
import com.stubit.cocktailremote.modelviews.CocktailActivityViewModel;
import com.stubit.cocktailremote.modelviews.EditActivityViewModel;
import com.stubit.cocktailremote.modelviews.ViewModelFactory;

import static com.stubit.cocktailremote.CocktailActivity.ID_EXTRA_KEY;

public class EditActivity extends AppCompatActivity {

    private EditActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(
                        getApplicationContext(),
                        this,
                        getIntent().getIntExtra(ID_EXTRA_KEY, 0)
                )
        ).get(EditActivityViewModel.class);

        setupTextInput(R.id.cocktailNameInput, new TextInputInterface() {
            @Override
            public String getText() {
                return viewModel.getCocktailName().getValue();
            }

            @Override
            public void setText(String text) {
                viewModel.setCocktailName(text);
            }
        });

        setupTextInput(R.id.cocktailDescriptionInput, new TextInputInterface() {
            @Override
            public String getText() {
                return viewModel.getCocktailDescription().getValue();
            }

            @Override
            public void setText(String text) {
                viewModel.setCocktailDescription(text);
            }
        });

        final FloatingActionButton fab = findViewById(R.id.fab);

        viewModel.hasUnsavedChanges().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean unsavedChanges) {
                if (unsavedChanges) {
                    fab.setVisibility(View.VISIBLE);
                } else {
                    fab.setVisibility(View.GONE);
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.saveCocktail();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        exitEdit();
        return true;
    }

    @Override
    public void onBackPressed() {
        exitEdit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle() != null && item.getTitle().equals(getString(R.string.delete))) {
            viewModel.deleteCocktail();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupTextInput(int resId, final TextInputInterface inputInterface) {
        EditText textInput = findViewById(resId);
        textInput.setText(inputInterface.getText());

        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                inputInterface.setText(s.toString());
            }
        });
    }

    private interface TextInputInterface {
        String getText();
        void setText(String text);
    }

    private void exitEdit() {
        if(viewModel.getCocktailId() == null) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Intent showIntent = new Intent(this, CocktailActivity.class);
            showIntent.putExtra(ID_EXTRA_KEY, viewModel.getCocktailId());

            startActivity(showIntent);
        }

        finish();
    }
}
