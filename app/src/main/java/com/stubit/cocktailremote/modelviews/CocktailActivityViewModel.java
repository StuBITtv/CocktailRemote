package com.stubit.cocktailremote.modelviews;

import android.content.Context;
import android.net.Uri;
import android.util.SparseArray;
import androidx.lifecycle.*;
import com.stubit.cocktailremote.CocktailActivity;
import com.stubit.cocktailremote.models.CocktailModel;
import com.stubit.cocktailremote.models.IngredientModel;
import com.stubit.cocktailremote.repositories.CocktailRepository;

import java.util.ArrayList;

public class CocktailActivityViewModel extends ViewModel {
    protected CocktailModel mCocktail;
    protected final MutableLiveData<String> mCocktailName = new MutableLiveData<>();
    protected final MutableLiveData<Uri> mCocktailImageUri = new MutableLiveData<>();
    protected final MutableLiveData<String> mCocktailDescription = new MutableLiveData<>();
    protected final MutableLiveData<ArrayList<String>> mCocktailIngredientNames = new MutableLiveData<>();
    protected final MutableLiveData<CocktailModel.SignalType> mCocktailSignalType = new MutableLiveData<>();
    protected final MutableLiveData<String> mCocktailSignal = new MutableLiveData<>();

    protected Integer mCocktailId;
    protected final CocktailRepository mCocktailRepository;
    protected final LifecycleOwner mOwner;

    public CocktailActivityViewModel(Context c, LifecycleOwner owner, Integer cocktailId) {
        mCocktailId = cocktailId;
        mOwner = owner;
        mCocktailRepository = CocktailRepository.getRepository(c);

        loadCocktailInfo();

        mCocktailRepository.getCocktails().observe(mOwner, cocktailModelSparseArray -> loadCocktailInfo());
    }

    public Integer getCocktailId() {
        return mCocktail != null ? mCocktail.getId() : null;
    }

    public LiveData<String> getCocktailName() {
        return mCocktailName;
    }

    public LiveData<Uri> getCocktailImageUri() {
        return mCocktailImageUri;
    }

    public LiveData<String> getCocktailDescription() {
        return mCocktailDescription;
    }

    public LiveData<ArrayList<String>> getCocktailIngredientNames() {
        return mCocktailIngredientNames;
    }

    public LiveData<CocktailModel.SignalType> getCocktailSignalType() {
        return mCocktailSignalType;
    }

    public LiveData<String> getCocktailSignal() {
        return mCocktailSignal;
    }

    protected void loadCocktailInfo() {
        SparseArray<CocktailModel> cocktails = mCocktailRepository.getCocktails().getValue();

        if (cocktails != null) {
            if (mCocktailId != 0) {
                mCocktail = cocktails.get(mCocktailId);

                if (mCocktail == null) {
                    mCocktail = new CocktailModel();
                } else {
                    mCocktailRepository.getIngredients(mCocktailId).observe(mOwner, ingredients -> {
                        if (ingredients != null) {
                            ArrayList<String> ingredientNames = new ArrayList<>();

                            for (IngredientModel ingredient : ingredients) {
                                ingredientNames.add(ingredient.getName());
                            }

                            mCocktailIngredientNames.postValue(ingredientNames);
                        } else {
                            mCocktailIngredientNames.postValue(new ArrayList<>());
                        }
                    });
                }
            } else {
                mCocktail = new CocktailModel();
            }

            mCocktailName.setValue(mCocktail.getName());
            mCocktailDescription.setValue(mCocktail.getDescription());

            if (mCocktail.getImageUri() != null) {
                mCocktailImageUri.setValue(Uri.parse(mCocktail.getImageUri()));
            } else {
                mCocktailImageUri.setValue(null);
            }

            mCocktailSignalType.setValue(
                    mCocktail.getSignalType() != null ? mCocktail.getSignalType() : CocktailModel.SignalType.STRING
            );

            mCocktailSignal.setValue(mCocktail.getSignal());
        }
    }
}
