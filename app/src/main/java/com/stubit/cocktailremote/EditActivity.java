package com.stubit.cocktailremote;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.stubit.cocktailremote.modelviews.EditActivityViewModel;
import com.stubit.cocktailremote.modelviews.ViewModelFactory;
import com.stubit.cocktailremote.views.CocktailImageView;

import java.io.IOException;

import static com.stubit.cocktailremote.CocktailActivity.ID_EXTRA_KEY;

public class EditActivity extends AppCompatActivity {

    private EditActivityViewModel mViewModel;
    private final static int PICK_IMAGE = 1;
    private final static int REQUEST_IMAGE_ACCESS = 2;

    private Toast mToast = null;
    private Integer mSubmittedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSubmittedId = getIntent().getIntExtra(ID_EXTRA_KEY, 0);

        mViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(
                        getApplicationContext(),
                        this,
                        mSubmittedId
                )
        ).get(EditActivityViewModel.class);

        final CocktailImageView cocktailImageView = findViewById(R.id.cocktail_image);
        final AppCompatActivity self = this;

        cocktailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionStatus = ContextCompat.checkSelfPermission(
                        getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                );

                if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            self,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,},
                            REQUEST_IMAGE_ACCESS
                    );
                } else {
                    openGallery();
                }
            }
        });

        mViewModel.getCocktailImageUri().observe(this, new Observer<Uri>() {
            @Override
            public void onChanged(Uri uri) {
                cocktailImageView.setImage(getApplicationContext(), uri);
            }
        });

        setupTextInput(R.id.cocktailNameInput, new TextInputInterface() {
            @Override
            public String getText() {
                return mViewModel.getCocktailName().getValue();
            }

            @Override
            public void setText(String text) {
                mViewModel.setCocktailName(text);
            }
        });

        setupTextInput(R.id.cocktailDescriptionInput, new TextInputInterface() {
            @Override
            public String getText() {
                return mViewModel.getCocktailDescription().getValue();
            }

            @Override
            public void setText(String text) {
                mViewModel.setCocktailDescription(text);
            }
        });

        final FloatingActionButton fab = findViewById(R.id.fab);

        mViewModel.hasUnsavedChanges().observe(this, new Observer<Boolean>() {
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
                mViewModel.saveCocktail();
            }
        });

        //noinspection ConstantConditions
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
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle() != null && item.getTitle().equals(getString(R.string.delete))) {
            mViewModel.deleteCocktail();

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_IMAGE_ACCESS && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if(grantResults[0] != 0) {
                showToast(R.string.permission_needed);
            } else {
                openGallery();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                if(data != null) {
                    Uri imageUri = data.getData();

                    if(imageUri != null) {
                        try {
                            mViewModel.setCocktailImageUri(this, imageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                            showToast(R.string.not_found);
                        }
                    }
                }
            }
        }
    }

    private void showToast(int StringId) {
        if(mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(this, StringId, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void exitEdit() {
        if(mViewModel.getCocktailId() == null) {
            mViewModel.deleteCocktailImage();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            if(mSubmittedId != 0) {
                Intent showIntent = new Intent(this, CocktailActivity.class);

                showIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                showIntent.putExtra(ID_EXTRA_KEY, mViewModel.getCocktailId());

                startActivity(showIntent);
            }
        }

        finish();
    }

    private void openGallery() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }
}
