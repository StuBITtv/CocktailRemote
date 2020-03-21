package com.stubit.cocktailremote.repositories;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.stubit.cocktailremote.models.CocktailModel;
import com.stubit.cocktailremote.models.Database;

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

    public void deleteCocktail(final CocktailModel cocktailModel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCocktailAccess.deleteModel(cocktailModel);
                Log.d(TAG, "CocktailModel deleted");
            }
        }).start();
    }

    public LiveData<Integer> latestId() {
        return mLatestId;
    }

    private static CocktailRepository mInstance;

    private static CocktailModel.Access mCocktailAccess;
    private MutableLiveData<SparseArray<CocktailModel>> mCocktails;
    private MutableLiveData<Integer> mLatestId = new MutableLiveData<>(null);
}
