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

    private CocktailRepository() {}

    public static CocktailRepository getRepository(Context c) {
        if (mInstance == null) {
            mInstance = new CocktailRepository();

            mCocktailAccess =  Database.getDatabase(c).CocktailAccess();
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

                    if(cocktails != null) {
                        for (CocktailModel cocktail : cocktails) {
                            newCocktails.append(cocktail.getId(), cocktail);
                        }
                    }

                    mCocktails.setValue(newCocktails);
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
                mCocktailAccess.addModel(cocktailModel);
                Log.d(TAG, "CocktailModel added to database");
            }
        }).start();
    }

    private static CocktailRepository mInstance;

    private static CocktailModel.Access mCocktailAccess;
    private MutableLiveData<SparseArray<CocktailModel>> mCocktails;
}
