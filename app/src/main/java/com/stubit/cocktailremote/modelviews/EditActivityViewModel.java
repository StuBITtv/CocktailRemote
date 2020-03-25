package com.stubit.cocktailremote.modelviews;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.*;

public class EditActivityViewModel extends CocktailActivityViewModel {
    private MutableLiveData<Boolean> mUnsavedChanges = new MutableLiveData<>(false);

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
        deleteTempImage();

        mCocktailImageUri.setValue(mCocktailRepository.addCocktailImage(c, cocktailImageUri));
        mUnsavedChanges.setValue(true);
    }

    public void deleteCocktailImage() {
        String filepath = mCocktail.getImageUri();

        if (filepath != null) {
            mCocktailRepository.deleteCocktailImage(Uri.parse(filepath));
        }

    }

    public LiveData<Boolean> hasUnsavedChanges() {
        return mUnsavedChanges;
    }

    public void cleanUp() {
        //noinspection ConstantConditions
        if (mUnsavedChanges.getValue() && mCocktailImageUri.getValue() != null) {
            deleteTempImage();
        }
    }

    private void deleteTempImage() {
        if (
                mCocktailImageUri.getValue() != null &&
                !mCocktailImageUri.getValue().toString().equals(mCocktail.getImageUri())
        ) {
            mCocktailRepository.deleteCocktailImage(mCocktailImageUri.getValue());
        }
    }

    public void saveCocktail() {
        mCocktail.setName(mCocktailName.getValue());
        mCocktail.setDescription(mCocktailDescription.getValue());


        Uri imageUri = mCocktailImageUri.getValue();

        if (imageUri != null && !imageUri.toString().equals(mCocktail.getImageUri())) {
            deleteCocktailImage();
            mCocktail.setImageUri(imageUri.toString());
        } else if (imageUri == null) {
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
