package com.stubit.cocktailremote.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.stubit.cocktailremote.models.CocktailModel;
import com.stubit.cocktailremote.models.Database;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;


public class CocktailRepository {
    public static final String TAG = "CocktailRepository";

    public static CocktailRepository getRepository(Context c) {
        if (mInstance == null) {
            mInstance = new CocktailRepository();

            mCocktailAccess = Database.getDatabase(c).CocktailAccess();
        }

        return mInstance;
    }

    public LiveData<SparseArray<CocktailModel>> getCocktails() {
        if (mCocktails == null) {
            mCocktails = new MutableLiveData<>();

            mCocktailAccess.all().observeForever(new Observer<List<CocktailModel>>() {
                @Override
                public void onChanged(List<CocktailModel> cocktails) {
                    SparseArray<CocktailModel> newCocktails = new SparseArray<>();

                    if (cocktails != null) {
                        for (CocktailModel cocktail : cocktails) {
                            newCocktails.append(cocktail.getId(), cocktail);
                        }
                    }

                    mCocktails.postValue(newCocktails);
                    Log.d(TAG, "CocktailList updated");
                }
            });
        }

        return mCocktails;
    }

    public void addCocktail(final CocktailModel cocktailModel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mLatestId.postValue(mCocktailAccess.addModel(cocktailModel).intValue());
                Log.d(TAG, "CocktailModel added to database");
            }
        }).start();
    }

    public void updateCocktail(final CocktailModel cocktailModel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCocktailAccess.updateModel(cocktailModel);
                Log.d(TAG, "CocktailModel updated");
            }
        }).start();
    }

    public void deleteCocktail(@NotNull final CocktailModel cocktailModel) {
        deleteCocktailImage(Uri.parse(cocktailModel.getImageUri()));

        new Thread(new Runnable() {
            @Override
            public void run() {
                mCocktailAccess.deleteModel(cocktailModel);
                Log.d(TAG, "CocktailModel deleted");
            }
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

    public LiveData<Integer> latestId() {
        return mLatestId;
    }

    public void resetLatestId() {
        mLatestId.setValue(null);
    }

    private static CocktailRepository mInstance;

    private static CocktailModel.Access mCocktailAccess;
    private MutableLiveData<SparseArray<CocktailModel>> mCocktails;
    private MutableLiveData<Integer> mLatestId = new MutableLiveData<>(null);
}
