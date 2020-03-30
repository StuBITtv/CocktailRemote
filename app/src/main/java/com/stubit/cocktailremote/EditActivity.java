package com.stubit.cocktailremote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.stubit.cocktailremote.models.CocktailModel;
import com.stubit.cocktailremote.modelviews.EditActivityViewModel;
import com.stubit.cocktailremote.modelviews.ViewModelFactory;
import com.stubit.cocktailremote.views.CocktailImageView;
import com.stubit.cocktailremote.views.IngredientListView;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import static com.stubit.cocktailremote.CocktailActivity.ID_EXTRA_KEY;

public class EditActivity extends AppCompatActivity {
    public static final String TAG = "EditActivity";

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

        // region setup viewModel
        mSubmittedId = getIntent().getIntExtra(ID_EXTRA_KEY, 0);

        mViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(
                        getApplicationContext(),
                        this,
                        mSubmittedId
                )
        ).get(EditActivityViewModel.class);
        // end region

        // region setup cocktail image
        final CocktailImageView cocktailImageView = findViewById(R.id.cocktail_image);
        final AppCompatActivity self = this;

        cocktailImageView.setOnClickListener(v -> {
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
        });

        mViewModel.getCocktailImageUri().observe(this, uri -> cocktailImageView.setImage(getApplicationContext(), uri));
        // endregion

        // region setup name and description input
        setupTextInput(null, R.id.cocktail_name_input, new TextInputInterface() {
            @Override
            public String getText() {
                return mViewModel.getCocktailName().getValue();
            }

            @Override
            public void setText(String text) {
                mViewModel.setCocktailName(text);
            }
        });

        setupTextInput(null, R.id.cocktail_description_input, new TextInputInterface() {
            @Override
            public String getText() {
                return mViewModel.getCocktailDescription().getValue();
            }

            @Override
            public void setText(String text) {
                mViewModel.setCocktailDescription(text);
            }
        });
        // endregion

        // region setup ingredient list
        final IngredientListView ingredientListView = findViewById(R.id.ingredient_list);
        ingredientListView.setViewHolder(new IngredientListView.Adapter() {
            @Override
            public View inflateIngredientView(ViewGroup rootView) {
                return getLayoutInflater().inflate(R.layout.item_ingredient_edit, rootView, false);
            }

            @Override
            public View inflateNoIngredientPlaceholderView(ViewGroup rootView) {
                return getLayoutInflater().inflate(R.layout.item_ingredient_placeholder, rootView, false);
            }

            @Override
            public void setupViewHolder(final View holder, final String name, final int position) {
                setupTextInput(holder, R.id.ingredient_name_input, new TextInputInterface() {
                    @Override
                    public String getText() {
                        return name;
                    }

                    @Override
                    public void setText(String newIngredientName) {
                        mViewModel.updateIngredient(position, newIngredientName);
                    }
                });

                holder.findViewById(R.id.delete_button).setOnClickListener(v -> {
                    mViewModel.deleteIngredient(position);

                    ArrayList<String> newCocktailNames = mViewModel.getCocktailIngredientNames().getValue();
                    ingredientListView.updateIngredients(newCocktailNames);

                    if (newCocktailNames != null && newCocktailNames.size() != 0) {
                        int newFocusedPosition = position;

                        if(position >= ingredientListView.getChildCount()) {
                            newFocusedPosition--;
                        }

                        ingredientListView.getChildAt(newFocusedPosition).requestFocus();
                        EditText ingredientInput = ingredientListView.getChildAt(newFocusedPosition).findViewById(
                                R.id.ingredient_name_input
                        );

                        ingredientInput.setSelection(ingredientInput.getText().length());
                    } else {
                        findViewById(R.id.cocktail_name_input).clearFocus();    // else gains focus automatically
                    }
                });
            }
        });

        mViewModel.getCocktailIngredientNames().observe(this, ingredients -> {
            mViewModel.getCocktailIngredientNames().removeObservers(this);
            ingredientListView.updateIngredients(ingredients);
        });

        findViewById(R.id.add_ingredient_button).setOnClickListener(v -> {
            mViewModel.addIngredient();
            ingredientListView.updateIngredients(mViewModel.getCocktailIngredientNames().getValue());
            ingredientListView.getChildAt(ingredientListView.getChildCount() - 1).requestFocus();
        });
        // endregion

        // region setup save button
        final FloatingActionButton fab = findViewById(R.id.fab);

        mViewModel.hasUnsavedChanges().observe(this, unsavedChanges -> {
            if (unsavedChanges) {
                fab.setVisibility(View.VISIBLE);
            } else {
                fab.setVisibility(View.GONE);
            }
        });

        fab.setOnClickListener(view -> mViewModel.saveCocktailAndIngredients());
        // endregion

        // region setup signal input
        final EditText signalInput = findViewById(R.id.signal_input);
        final TextWatcher[] signalChangeWatcher = {null};

        final RadioButton typeInputBinary = findViewById(R.id.type_input_binary);
        typeInputBinary.setOnCheckedChangeListener((v, checked) -> {
            if(checked) {
                mViewModel.setCocktailSignalType(CocktailModel.SignalType.BINARY);

                signalInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                signalInput.setKeyListener(DigitsKeyListener.getInstance("01"));
                signalInput.removeTextChangedListener(signalChangeWatcher[0]);
            }
        });

        final RadioButton typeInputInteger = findViewById(R.id.type_input_number);
        typeInputInteger.setOnCheckedChangeListener((v, checked) -> {
            if(checked) {
                mViewModel.setCocktailSignalType(CocktailModel.SignalType.INTEGER);

                signalInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                signalInput.setKeyListener(DigitsKeyListener.getInstance("-0123456789"));
                signalInput.removeTextChangedListener(signalChangeWatcher[0]);

                // region setup number negative converter
                signalChangeWatcher[0] = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        int cursorPosition = signalInput.getSelectionStart();

                        if(s.length() > 1) {
                            String numberEnding = s.toString().substring(1);

                            if(numberEnding.contains("-")) {
                                numberEnding = s.toString().substring(1).replace("-", "");
                                char numberStart = s.toString().charAt(0);

                                int positionOffset = 0;

                                if(numberStart != '-') {
                                    signalInput.setText("-" + numberStart + numberEnding);
                                } else {
                                    signalInput.setText(numberEnding);
                                    positionOffset = 2;
                                }

                                int textLength = signalInput.getText().length();

                                if(cursorPosition - positionOffset > textLength) {
                                    positionOffset = cursorPosition - textLength;
                                } else if (cursorPosition - positionOffset < 0) {
                                    positionOffset = cursorPosition;
                                }

                                signalInput.setSelection(cursorPosition - positionOffset);
                            }
                        }

                        setRadioButtonEnable(typeInputBinary, isBinary(signalInput.getText().toString()));
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                };
                // endregion

                signalInput.addTextChangedListener(signalChangeWatcher[0]);
            }
        });

        final RadioButton typeInputString = findViewById(R.id.type_input_string);
        typeInputString.setOnCheckedChangeListener((v, checked) -> {
            if(checked) {
                mViewModel.setCocktailSignalType(CocktailModel.SignalType.STRING);

                signalInput.setInputType(InputType.TYPE_CLASS_TEXT);
                signalInput.removeTextChangedListener(signalChangeWatcher[0]);

                signalChangeWatcher[0] = getStringTextWatcher(typeInputInteger, typeInputBinary);

                signalInput.addTextChangedListener(signalChangeWatcher[0]);
            }
        });

        signalChangeWatcher[0] = getStringTextWatcher(typeInputInteger, typeInputBinary);
        signalInput.addTextChangedListener(signalChangeWatcher[0]);

        signalInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setCocktailSignal(s.toString());
            }
        });

        mViewModel.getCocktailSignalType().observe(this, type -> {
            switch (type) {
                case BINARY:
                    typeInputBinary.setChecked(true);
                    break;

                case INTEGER:
                    typeInputInteger.setChecked(true);
                    break;

                default:
                    typeInputString.setChecked(true);
                    break;
            }

            mViewModel.getCocktailSignalType().removeObservers(this);
        });

        mViewModel.getCocktailSignal().observe(this, signal -> {
            signalInput.setText(signal);

            mViewModel.getCocktailSignal().removeObservers(this);
        });

        // endregion

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {}

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

    private void setupTextInput(final View viewRoot, int resId, @NotNull final TextInputInterface inputInterface) {
        EditText textInput;

        if (viewRoot != null) {
            textInput = viewRoot.findViewById(resId);
        } else {
            textInput = findViewById(resId);
        }

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
                            showToast(R.string.image_not_found);
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
        mViewModel.cleanUp();

        if(mViewModel.getCocktailId() == null) {
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

    private void setRadioButtonEnable(RadioButton radioButton, boolean enable) {
            radioButton.setEnabled(enable);

            if(enable) {
                radioButton.setVisibility(View.VISIBLE);
            } else {
                radioButton.setVisibility(View.GONE);
            }
    }

    private TextWatcher getStringTextWatcher(final RadioButton radioButtonInteger, final RadioButton radioButtonBinary) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setRadioButtonEnable(radioButtonInteger, isInteger(s.toString()));
                setRadioButtonEnable(radioButtonBinary, isBinary(s.toString()));
            }
        };
    }

    private boolean isBinary(String input) {
        if(input == null || input.equals("")) {
            return true;
        }

        return input.matches("[01]+");
    }

    private boolean isInteger(String input) {
        if(input == null || input.equals("")) {
            return true;
        }

        return input.matches("[-0123456789]+") && (input.length() <= 1 || !input.substring(1).contains("-"));
    }
}
