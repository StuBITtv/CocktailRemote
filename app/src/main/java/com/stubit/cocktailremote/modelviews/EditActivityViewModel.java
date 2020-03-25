package com.stubit.cocktailremote.modelviews;

import android.content.Context;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class EditActivityViewModel extends CocktailActivityViewModel {
    private MutableLiveData<Boolean> mUnsavedChanges = new MutableLiveData<>();

    public EditActivityViewModel(Context c, LifecycleOwner owner, Integer cocktailId) {
        super(c, owner, cocktailId);

        mUnsavedChanges.setValue(false);
    }

    public void setCocktailName(String name) {
        mCocktailName.setValue(name);
        mUnsavedChanges.setValue(true);
    }

    public void setCocktailDescription(String description) {
        mCocktailDescription.setValue(description);
        mUnsavedChanges.setValue(true);
    }

    public LiveData<Boolean> hasUnsavedChanges() {
        return mUnsavedChanges;
    }

    public void saveCocktail() {
        mCocktail.setName(mCocktailName.getValue());
        mCocktail.setDescription(mCocktailDescription.getValue());

        if(mCocktail.getId() != null) {
            mCocktailRepository.updateCocktail(mCocktail);
        } else {
            mCocktailRepository.latestId().observe(mOwner, new Observer<Integer>() {
                @Override
                public void onChanged(Integer newId) {
                    if(newId != null) {
                        mCocktailId = newId;
                        mCocktailRepository.latestId().removeObserver(this);

                        mCocktailRepository.resetLatestId();
                    }
                }
            });

            mCocktailRepository.addCocktail(mCocktail);
        }

        mUnsavedChanges.setValue(false);
    }

    public void deleteCocktail() {
        mUnsavedChanges.setValue(false);
        mCocktailRepository.deleteCocktail(mCocktail);
    }
}
