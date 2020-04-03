package com.stubit.cocktailremote;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stubit.cocktailremote.bluetooth.BluetoothManager;
import com.stubit.cocktailremote.dialog.PasswordValidation;
import com.stubit.cocktailremote.models.CocktailModel;
import com.stubit.cocktailremote.modelviews.CocktailActivityViewModel;
import com.stubit.cocktailremote.modelviews.ViewModelFactory;
import com.stubit.cocktailremote.views.CocktailImageView;
import com.stubit.cocktailremote.views.IngredientListView;
import com.stubit.cocktailremote.views.TextView;

import static com.stubit.cocktailremote.BluetoothDevicePickerActivity.BLUETOOTH_DEVICE;

public class CocktailActivity extends AppCompatActivity {
    public static final int BLUETOOTH_DEVICE_REQUEST = 1;
    public static final String ID_EXTRA_KEY = "cocktailId";
    private static final String TAG = "CocktailActivity";

    private CocktailActivityViewModel mViewModel;

    private Toast mToast;

    private Boolean mPasswordProtectedCocktail;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cocktail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // region setup viewModel
        mViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(
                        getApplicationContext(),
                        this,
                        getIntent().getIntExtra(ID_EXTRA_KEY, 0)
                )
        ).get(CocktailActivityViewModel.class);
        // endregion

        // region setup name, description and image
        mViewModel.getCocktailName().observe(this, cocktailName -> {
            TextView cocktailNameView = findViewById(R.id.cocktail_name);
            cocktailNameView.setText(cocktailName, R.string.unnamed_cocktail);
        });

        mViewModel.getCocktailDescription().observe(this, cocktailDescription -> {
            TextView cocktailDescriptionView = findViewById(R.id.cocktail_description);
            cocktailDescriptionView.setText(cocktailDescription, R.string.no_description);
        });

        mViewModel.getCocktailImageUri().observe(this, uri -> (
                (CocktailImageView) findViewById(R.id.cocktail_image)).setImage(getApplicationContext(), uri)
        );
        // endregion

        // region setup ingredient list
        final IngredientListView ingredientListView = findViewById(R.id.ingredient_list);
        ingredientListView.setViewHolder(new IngredientListView.Adapter() {
            @Override
            public View inflateIngredientView(ViewGroup rootView) {
                return getLayoutInflater().inflate(R.layout.item_ingredient, rootView, false);
            }

            @Override
            public View inflateNoIngredientPlaceholderView(ViewGroup rootView) {
                return getLayoutInflater().inflate(R.layout.item_ingredient_placeholder, rootView, false);
            }

            @Override
            public void setupViewHolder(View holder, String name, int position) {
                ((TextView) holder.findViewById(R.id.ingredient_name)).setText(name, R.string.unnamed_ingredient);
            }
        });

        mViewModel.getCocktailIngredientNames().observe(this, ingredientListView::updateIngredients);
        // endregion

        // region setup bluetooth action
        mViewModel.getPasswordProtectionStatus().observe(this, passwordProtected -> {
            FloatingActionButton fab = findViewById(R.id.fab);
            setPasswordProtection(passwordProtected);

            fab.setOnClickListener(view -> {
                if (PasswordValidation.passwordIsNotSet(this) || !passwordProtected) {
                    //noinspection CodeBlock2Expr
                    new Thread(() -> {
                        sendBluetoothSignal(BluetoothManager.getInstance().getConnectedDeviceAddress().getValue());
                    }).start();
                } else {
                    //noinspection CodeBlock2Expr
                    pushDialog(PasswordValidation.validatePassword(this, () -> new Thread(() -> {
                        sendBluetoothSignal(BluetoothManager.getInstance().getConnectedDeviceAddress().getValue());
                    }).start()));
                }
            });
        });
        // endregion

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        pushDialog(null);
        BluetoothManager.getInstance().cleanup(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cocktail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle() != null && item.getTitle().equals(getString(R.string.edit))) {
            if (mPasswordProtectedCocktail == null) {
                mViewModel.getPasswordProtectionStatus().observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean passwordProtected) {
                        mViewModel.getPasswordProtectionStatus().removeObserver(this);
                        setPasswordProtection(passwordProtected);

                        authoriseEdit();
                    }
                });
            } else {
                authoriseEdit();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void pushDialog(Dialog dialog) {
        if (mDialog != null) {
            mDialog.dismiss();
        }

        mDialog = dialog;
    }

    private void setPasswordProtection(Boolean savedState) {
        mPasswordProtectedCocktail = savedState != null ? savedState : false;
    }

    private void authoriseEdit() {
        if (PasswordValidation.passwordIsNotSet(this) || (PasswordValidation.editIsUnlocked(this)) && !mPasswordProtectedCocktail) {
            startEditActivity();
        } else {
            pushDialog(PasswordValidation.validatePassword(this, this::startEditActivity));
        }
    }

    private void startEditActivity() {
        Intent editIntent = new Intent(this, EditActivity.class);

        editIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        editIntent.putExtra(ID_EXTRA_KEY, mViewModel.getCocktailId());

        startActivity(editIntent);
    }

    private void createToast(int ResId) {
        runOnUiThread(() -> {
            if (mToast != null) {
                mToast.cancel();
            }

            mToast = Toast.makeText(this, ResId, Toast.LENGTH_SHORT);
            mToast.show();
        });


    }

    private void sendBluetoothSignal(String bluetoothDeviceAddress) {
        if (bluetoothDeviceAddress != null && !bluetoothDeviceAddress.equals("")) {
            CocktailModel.SignalType signalType = mViewModel.getCocktailSignalType().getValue();
            String signal = mViewModel.getCocktailSignal().getValue();

            if (signalType != null && signal != null && !signal.equals("")) {
                switch (signalType) {
                    case BINARY:
                        // convert string into actual bytes
                        byte[] bytes;
                        if (signal.length() % 8 != 0) {
                            bytes = new byte[signal.length() / 8 + 1];
                        } else {
                            bytes = new byte[signal.length() / 8];
                        }

                        for (int bit = 0; bit < signal.length(); ++bit) {
                            if (signal.charAt(bit) == '1') {
                                bytes[bit / 8] |= (1 << (7 - bit % 8));
                            }
                        }

                        Log.d(TAG, "Signal consist out of those bytes: ");

                        for (byte singleByte : bytes) {
                            StringBuilder value = new StringBuilder();

                            for (int i = 7; i > -1; --i) {
                                if ((singleByte & (1 << i)) != 0) {
                                    value.append("1");
                                } else {
                                    value.append("0");
                                }
                            }

                            Log.d(TAG, value.toString());
                        }

                        BluetoothManager.getInstance().send(
                                this,
                                bluetoothDeviceAddress,
                                bytes,
                                this::unableToConnect
                        );
                        break;

                    case INTEGER:
                        BluetoothManager.getInstance().send(
                                this,
                                bluetoothDeviceAddress,
                                Integer.valueOf(signal),
                                this::unableToConnect
                        );
                        break;

                    default:
                        BluetoothManager.getInstance().send(
                                this,
                                bluetoothDeviceAddress,
                                signal,
                                this::unableToConnect
                        );
                        break;
                }
            } else {
                createToast(R.string.no_bluetooth_signal);
            }
        } else {
            startActivityForResult(new Intent(this, BluetoothDevicePickerActivity.class), BLUETOOTH_DEVICE_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BLUETOOTH_DEVICE_REQUEST && data != null && resultCode == RESULT_OK) {
            new Thread(() -> sendBluetoothSignal(data.getStringExtra(BLUETOOTH_DEVICE))).start();
        }

    }

    public void unableToConnect() {
        createToast(R.string.bluetooth_connection_failure);
        startActivityForResult(new Intent(this, BluetoothDevicePickerActivity.class), BLUETOOTH_DEVICE_REQUEST);
    }
}
