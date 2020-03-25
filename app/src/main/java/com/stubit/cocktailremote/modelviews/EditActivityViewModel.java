package com.stubit.cocktailremote.modelviews;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.*;

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

    public void setCocktailImageUri(Context c, Uri cocktailImageUri) throws IOException {
        mCocktailImageUri.setValue(mCocktailRepository.setCocktailImage(c, mCocktail, cocktailImageUri));
        mUnsavedChanges.setValue(true);
    }

    public void deleteCocktailImage() {
        mCocktailRepository.removeCocktailImage(mCocktail);
    }

    public LiveData<Boolean> hasUnsavedChanges() {
        return mUnsavedChanges;
    }

    public void saveCocktail() {
        mCocktail.setName(mCocktailName.getValue());
        mCocktail.setDescription(mCocktailDescription.getValue());

        Uri imageUri = mCocktailImageUri.getValue();

        if (imageUri != null) {
            mCocktail.setImageUri(imageUri.toString());
        } else {
            mCocktail.setImageUri(null);
        }

        if (mCocktail.getId() != null) {
            mCocktailRepository.updateCocktail(mCocktail);
        } else {
            mCocktailRepository.latestId().observe(mOwner, new Observer<Integer>() {
                @Override
                public void onChanged(Integer newId) {
                    if (newId != null) {
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
