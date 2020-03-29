package com.stubit.cocktailremote.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.stubit.cocktailremote.models.CocktailModel;
import com.stubit.cocktailremote.models.Database;
import com.stubit.cocktailremote.models.IngredientModel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;


public class CocktailRepository {
    public static final String TAG = "CocktailRepository";

    private static CocktailRepository mInstance;

    private static CocktailModel.Access mCocktailAccess;
    private static IngredientModel.Access mIngredientAccess;

    private MutableLiveData<SparseArray<CocktailModel>> mCocktails;
    private SparseArray<MutableLiveData<List<IngredientModel>>> mIngredients = new SparseArray<>();
    private final MutableLiveData<Integer> mLatestCocktailId = new MutableLiveData<>(null);

    public static CocktailRepository getRepository(Context c) {
        if (mInstance == null) {
            mInstance = new CocktailRepository();

            mCocktailAccess = Database.getDatabase(c).getCocktailAccess();
            mIngredientAccess = Database.getDatabase(c).getIngredientAccess();
        }

        return mInstance;
    }

    public LiveData<SparseArray<CocktailModel>> getCocktails() {
        if (mCocktails == null) {
            mCocktails = new MutableLiveData<>();

            mCocktailAccess.all().observeForever(cocktails -> {
                SparseArray<CocktailModel> newCocktails = new SparseArray<>();

                if (cocktails != null) {
                    for (CocktailModel cocktail : cocktails) {
                        newCocktails.append(cocktail.getId(), cocktail);
                    }
                }

                mCocktails.postValue(newCocktails);
                Log.d(TAG, "CocktailList updated");
            });
        }

        return mCocktails;
    }

    public LiveData<List<IngredientModel>> getIngredients(final int cocktailId) {
        if (mIngredients.get(cocktailId) == null) {
            mIngredients.append(cocktailId, new MutableLiveData<>());

            mIngredientAccess.getAllFromCocktail(cocktailId).observeForever(ingredientModels -> {
                mIngredients.get(cocktailId).postValue(ingredientModels);
                Log.d(TAG, "IngredientList updated");
            });
        }

        return mIngredients.get(cocktailId);
    }


    public void addCocktail(final CocktailModel cocktailModel) {
        new Thread(() -> {
            mLatestCocktailId.postValue(mCocktailAccess.addModel(cocktailModel).intValue());
            Log.d(TAG, "CocktailModel added to database");
        }).start();
    }

    public void updateCocktail(final CocktailModel cocktailModel) {
        new Thread(() -> {
            mCocktailAccess.updateModel(cocktailModel);
            Log.d(TAG, "CocktailModel updated");
        }).start();
    }

    public void deleteCocktail(@NotNull final CocktailModel cocktailModel) {
        if(cocktailModel.getImageUri() != null) {
            deleteCocktailImage(Uri.parse(cocktailModel.getImageUri()));
        }

        new Thread(() -> {
            mCocktailAccess.deleteModel(cocktailModel);
            Log.d(TAG, "CocktailModel deleted");
        }).start();
    }

    public Uri addCocktailImage(@NotNull Context c, Uri imageUri) throws IOException {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            String fileTargetPath = c.getFilesDir().getPath() + File.separatorChar + new Date().getTime();
            Log.d(TAG, "Copying image to internal storage. The new path is " + fileTargetPath);

            inputStream = c.getContentResolver().openInputStream(imageUri);
            outputStream = new FileOutputStream(fileTargetPath, false);

            byte[] buffer = new byte[4 * 1024]; // or other buffer size
            int read;

            //noinspection ConstantConditions
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();

            return Uri.parse(fileTargetPath);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public void deleteCocktailImage(Uri cocktailImageUri) {
        if (cocktailImageUri != null) {
            File oldImage = new File(cocktailImageUri.getPath());

            //noinspection ResultOfMethodCallIgnored
            oldImage.delete();
        }
    }

    public LiveData<Integer> latestCocktailId() {
        return mLatestCocktailId;
    }

    public void resetLatestCocktailId() {
        mLatestCocktailId.setValue(null);
    }

    public void addIngredient(final IngredientModel ingredientModel) {
        new Thread(() -> {
            mIngredientAccess.addModel(ingredientModel);
            Log.d(TAG, "IngredientModel added");
        }).start();
    }

    public void updateIngredient(final IngredientModel ingredientModel) {
        new Thread(() -> {
            mIngredientAccess.updateModel(ingredientModel);
            Log.d(TAG, "IngredientModel updated");
        }).start();
    }

    public void deleteIngredient(final IngredientModel ingredientModel) {
        new Thread(() -> {
            mIngredientAccess.deleteModel(ingredientModel);
            Log.d(TAG, "IngredientModel deleted");
        }).start();
    }
}
