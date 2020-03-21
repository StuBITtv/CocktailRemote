package com.stubit.cocktailremote.modelviews;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import androidx.lifecycle.*;
import com.stubit.cocktailremote.models.CocktailModel;
import com.stubit.cocktailremote.repositories.CocktailRepository;

import java.io.File;
import java.util.ArrayList;

public class ItemListMainViewModel extends ViewModel {
    private static final String TAG = "ItemListMainViewModel";

    private MutableLiveData<ArrayList<Integer>> mCocktailIds = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> mCocktailNames = new MutableLiveData<>();
    private MutableLiveData<ArrayList<File>> mCocktailImages = new MutableLiveData<>();

    private CocktailRepository mCocktailRepository;

    public ItemListMainViewModel(Context c, LifecycleOwner owner) {
        mCocktailRepository = CocktailRepository.getRepository(c);

        mCocktailRepository.getCocktails().observe(owner, new Observer<SparseArray<CocktailModel>>() {
            @Override
            public void onChanged(SparseArray<CocktailModel> cocktailModelSparseArray) {
                updateLists();
            }
        });
    }

    public LiveData<ArrayList<Integer>> getCocktailIds() {
        return mCocktailIds;
    }

    public LiveData<ArrayList<String>> getCocktailNames() {
        return mCocktailNames;
    }

    public LiveData<ArrayList<File>> getCocktailImages() {
        return mCocktailImages;
    }

    private void updateLists() {
        SparseArray<CocktailModel> cocktails = mCocktailRepository.getCocktails().getValue();

        ArrayList<Integer> cocktailIds = new ArrayList<>();
        ArrayList<String> cocktailNames = new ArrayList<>();
        ArrayList<File> cocktailImages = new ArrayList<>();

        for (int i = 0; cocktails != null && i < cocktails.size(); ++i) {
            CocktailModel cocktail = cocktails.get(cocktails.keyAt(i));

            cocktailIds.add(cocktail.getId());
            cocktailNames.add(cocktail.getName());

            if(cocktail.getImagePath() != null) {
                cocktailImages.add(new File(cocktail.getImagePath()));
            } else {
                cocktailImages.add(null);
            }
        }

        mCocktailIds.setValue(cocktailIds);
        mCocktailNames.setValue(cocktailNames);
        mCocktailImages.setValue(cocktailImages);

        Log.d(TAG, "Cocktail lists updated");
    }
}
