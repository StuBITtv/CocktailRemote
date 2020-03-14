package com.example.cocktailremote.modelviews;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.example.cocktailremote.models.CocktailModel;
import com.example.cocktailremote.repositories.CocktailRepository;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class ItemListMainViewModel extends ViewModel {
    private static final String TAG = "ItemListMainViewModel";

    private MutableLiveData<ArrayList<String>> mCocktailNames;
    private MutableLiveData<ArrayList<File>> mCocktailImages;

    private CocktailRepository mCocktailRepository;

    public ItemListMainViewModel(Context c, LifecycleOwner owner) {
        mCocktailRepository = CocktailRepository.getRepository(c);

        updateLists();

        mCocktailRepository.getCocktails().observe(owner, new Observer<SparseArray<CocktailModel>>() {
            @Override
            public void onChanged(SparseArray<CocktailModel> cocktailModelSparseArray) {
                updateLists();
            }
        });
    }

    public LiveData<ArrayList<String>> getCocktailNames() {
        return mCocktailNames;
    }

    public LiveData<ArrayList<File>> getCocktailImages() {
        return mCocktailImages;
    }

    public void addCocktail(CocktailModel cocktailModel) {
        mCocktailRepository.addCocktail(cocktailModel);
    }

    private void updateLists() {
        SparseArray<CocktailModel> cocktails = mCocktailRepository.getCocktails().getValue();

        ArrayList<String> cocktailNames = new ArrayList<>();
        ArrayList<File> cocktailImages = new ArrayList<>();

        for (int i = 0; cocktails != null && i < cocktails.size(); ++i) {
            CocktailModel cocktail = cocktails.get(cocktails.keyAt(i));

            cocktailNames.add(cocktail.getName());

            if(cocktail.getImagePath() != null) {
                cocktailImages.add(new File(cocktail.getImagePath()));
            } else {
                cocktailImages.add(null);
            }
        }

        if(mCocktailNames == null) {
            mCocktailNames = new MutableLiveData<>();
        }

        if (mCocktailImages == null) {
            mCocktailImages = new MutableLiveData<>();
        }

        Log.d(TAG, "Cocktail lists updated");
        mCocktailNames.setValue(cocktailNames);
        mCocktailImages.setValue(cocktailImages);
    }

    public static class Factory implements ViewModelProvider.Factory {
        public Factory(Context c, LifecycleOwner lifecycleOwner) {
            mC = c;
            mLifecycleOwner = lifecycleOwner;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) throws RuntimeException {
            Constructor<T> constructor;
            try {
                constructor = modelClass.getConstructor(Context.class, LifecycleOwner.class);
            } catch (NoSuchMethodException e) {
                Log.d(TAG, "Constructor not found");
                e.printStackTrace();
                throw new RuntimeException();
            }

            String exception;

            try {
                return constructor.newInstance(mC, mLifecycleOwner);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                exception = e.getClass().getName();
            } catch (InstantiationException e) {
                e.printStackTrace();
                exception = e.getClass().getName();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                exception = e.getCause().toString();
            }

            Log.d(TAG, "Constructor could not be called: " + exception);
            throw new RuntimeException();
        }

        private Context mC;
        private LifecycleOwner mLifecycleOwner;
    }
}
