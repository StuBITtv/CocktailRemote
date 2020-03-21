package com.stubit.cocktailremote.modelviews;

import android.content.Context;
import android.util.SparseArray;
import androidx.lifecycle.*;
import com.stubit.cocktailremote.CocktailActivity;
import com.stubit.cocktailremote.models.CocktailModel;
import com.stubit.cocktailremote.repositories.CocktailRepository;

import java.io.File;
import java.util.List;

public class CocktailActivityViewModel extends ViewModel {
    protected CocktailModel mCocktail;
    protected MutableLiveData<String> mCocktailName = new MutableLiveData<>();
    protected MutableLiveData<File> mCocktailImage = new MutableLiveData<>();
    protected MutableLiveData<String> mCocktailDescription = new MutableLiveData<>();
    protected MutableLiveData<List<String>> mCocktailIngredients = new MutableLiveData<>();


    protected Integer mCocktailId;
    protected CocktailRepository mCocktailRepository;
    protected LifecycleOwner mOwner;

    public CocktailActivityViewModel(Context c, LifecycleOwner owner, Integer cocktailId) {
        mCocktailId = cocktailId;
        mOwner = owner;
        mCocktailRepository = CocktailRepository.getRepository(c);

        updateCocktail();

        mCocktailRepository.getCocktails().observe(mOwner, new Observer<SparseArray<CocktailModel>>() {
            @Override
            public void onChanged(SparseArray<CocktailModel> cocktailModelSparseArray) {
                updateCocktail();
            }
        });
    }

    public Integer getCocktailId() {
        return mCocktail.getId();
    }

    public LiveData<String> getCocktailName() {
        return mCocktailName;
    }

    public LiveData<File> getCocktailImage() {
        return mCocktailImage;
    }

    public LiveData<String> getCocktailDescription() {
        return mCocktailDescription;
    }

    public LiveData<List<String>> getCocktailIngredients() {
        return mCocktailIngredients;
    }

    private void updateCocktail() {
        SparseArray<CocktailModel> cocktails = mCocktailRepository.getCocktails().getValue();

        if(cocktails != null) {
            if(mCocktailId != 0) {
                mCocktail = cocktails.get(mCocktailId);
            } else {
                mCocktail = new CocktailModel();
            }

            if(mCocktail != null) {
                mCocktailName.setValue(mCocktail.getName());
                mCocktailDescription.setValue(mCocktail.getDescription());
                mCocktailImage.setValue(null);
                mCocktailIngredients.setValue(null);
            }
        }
    }
}
