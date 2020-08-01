package com.stubit.cocktailremote.modelviews;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import androidx.lifecycle.*;
import com.stubit.cocktailremote.models.CocktailModel;
import com.stubit.cocktailremote.repositories.CocktailRepository;

import java.util.ArrayList;

public class ItemListMainViewModel extends ViewModel {
    private static final String TAG = "ItemListMainViewModel";

    private final MutableLiveData<ArrayList<Integer>> mCocktailIds = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<String>> mCocktailNames = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<String>> mCocktailDescriptions = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Uri>> mCocktailImages = new MutableLiveData<>();

    private final CocktailRepository mCocktailRepository;

    public ItemListMainViewModel(Context c, LifecycleOwner owner) {
        mCocktailRepository = CocktailRepository.getRepository(c);

        mCocktailRepository.getCocktails().observe(owner, cocktailModelSparseArray -> updateLists());
    }

    public LiveData<ArrayList<Integer>> getCocktailIds() {
        return mCocktailIds;
    }

    public LiveData<ArrayList<String>> getCocktailNames() {
        return mCocktailNames;
    }

    public LiveData<ArrayList<String>> getCocktailDescriptions() {
        return mCocktailDescriptions;
    }

    public LiveData<ArrayList<Uri>> getCocktailImageUris() {
        return mCocktailImages;
    }

    private void updateLists() {
        SparseArray<CocktailModel> cocktails = mCocktailRepository.getCocktails().getValue();

        ArrayList<Integer> cocktailIds = new ArrayList<>();
        ArrayList<String> cocktailNames = new ArrayList<>();
        ArrayList<String> cocktailDescriptions = new ArrayList<>();
        ArrayList<Uri> cocktailImages = new ArrayList<>();

        for (int i = 0; cocktails != null && i < cocktails.size(); ++i) {
            CocktailModel cocktail = cocktails.get(cocktails.keyAt(i));

            cocktailIds.add(cocktail.getId());
            cocktailNames.add(cocktail.getName());
            cocktailDescriptions.add(cocktail.getDescription());

            if(cocktail.getImageUri() != null) {
                cocktailImages.add(Uri.parse(cocktail.getImageUri()));
            } else {
                cocktailImages.add(null);
            }
        }

        mCocktailIds.setValue(cocktailIds);
        mCocktailNames.setValue(cocktailNames);
        mCocktailDescriptions.setValue(cocktailDescriptions);
        mCocktailImages.setValue(cocktailImages);

        Log.d(TAG, "Cocktail lists updated");
    }
}
