package com.stubit.cocktailremote.modelviews;

import android.content.Context;
import android.net.Uri;
import android.util.SparseArray;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.stubit.cocktailremote.models.CocktailModel;
import com.stubit.cocktailremote.models.IngredientModel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class EditActivityViewModel extends CocktailActivityViewModel {
    private final MutableLiveData<Boolean> mUnsavedChanges = new MutableLiveData<>(false);

    private List<IngredientModel> mIngredientModels = new ArrayList<>();
    private List<IngredientModel> mDeleteIngredients = new ArrayList<>();
    private SparseArray<IngredientModel> mUpdatedIngredients = new SparseArray<>();

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

    public void setCocktailSignalType(CocktailModel.SignalType type) {
        mCocktailSignalType.setValue(type);
        mUnsavedChanges.setValue(true);
    }

    public void setCocktailSignal(String signal) {
        mCocktailSignal.setValue(signal);
        mUnsavedChanges.setValue(true);
    }

    public void setPasswordProtection(Boolean state) {
        mPasswordProtected.setValue(state);
        mUnsavedChanges.setValue(true);
    }

    public void addIngredient() {
        ArrayList<String> newIngredientNames = mCocktailIngredientNames.getValue();

        if (newIngredientNames == null) {
            newIngredientNames = new ArrayList<>();
        }

        newIngredientNames.add(null);
        mIngredientModels.add(new IngredientModel(getCocktailId()));

        mCocktailIngredientNames.setValue(newIngredientNames);

        mUnsavedChanges.setValue(true);
    }

    public void updateIngredient(int position, String ingredientName) {
        ArrayList<String> newIngredients = mCocktailIngredientNames.getValue();

        if (newIngredients != null) {
            newIngredients.set(position, ingredientName);
            mCocktailIngredientNames.setValue(newIngredients);

            IngredientModel ingredientModel = mIngredientModels.get(position);
            ingredientModel.setName(ingredientName);

            if (ingredientModel.getId() != null) {
                mUpdatedIngredients.append(ingredientModel.getId(), ingredientModel);
            }
        }

        mUnsavedChanges.setValue(true);
    }

    public void deleteIngredient(int position) {
        ArrayList<String> newIngredients = mCocktailIngredientNames.getValue();

        if (newIngredients != null) {
            newIngredients.remove(position);
            mCocktailIngredientNames.setValue(newIngredients);

            IngredientModel ingredientModel = mIngredientModels.get(position);

            if (ingredientModel.getId() != null) {
                mDeleteIngredients.add(mIngredientModels.get(position));
                mUpdatedIngredients.remove(mIngredientModels.get(position).getId());
            }

            mIngredientModels.remove(position);
        }

        mUnsavedChanges.setValue(true);
    }

    public LiveData<Boolean> hasUnsavedChanges() {
        return mUnsavedChanges;
    }

    @Override
    protected void loadCocktailInfo() {
        super.loadCocktailInfo();

        if (mCocktailId != 0) {
            mCocktailRepository.getIngredients(mCocktailId).observe(mOwner,
                    ingredientModels -> mIngredientModels = new ArrayList<>(ingredientModels)
            );
        }

        if (mCocktailIngredientNames.getValue() != null) {
            mCocktailIngredientNames.setValue(new ArrayList<>(mCocktailIngredientNames.getValue()));
        }
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

    public void saveCocktailAndIngredients() {
        mCocktail.setName(mCocktailName.getValue());
        mCocktail.setDescription(mCocktailDescription.getValue());
        mCocktail.setSignalType(mCocktailSignalType.getValue());
        mCocktail.setSignal(mCocktailSignal.getValue());
        mCocktail.setPasswordProtected(mPasswordProtected.getValue());

        Uri imageUri = mCocktailImageUri.getValue();

        if (imageUri != null && !imageUri.toString().equals(mCocktail.getImageUri())) {
            deleteCocktailImage();
            mCocktail.setImageUri(imageUri.toString());
        } else if (imageUri == null) {
            mCocktail.setImageUri(null);
        }

        if (mCocktail.getId() != null) {
            mCocktailRepository.updateCocktail(mCocktail);
            saveIngredients();
        } else {
            mCocktailRepository.latestCocktailId().observeForever(new Observer<Integer>() {
                @Override
                public void onChanged(Integer newId) {
                    if (newId != null) {
                        mCocktailId = newId;
                        mCocktailRepository.latestCocktailId().removeObserver(this);

                        mCocktailRepository.resetLatestCocktailId();

                        for (IngredientModel ingredient : mIngredientModels) {
                            ingredient.setCocktail(mCocktailId);
                        }

                        saveIngredients();
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

    private void saveIngredients() {
        for (int i = 0; i < mUpdatedIngredients.size(); ++i) {
            mCocktailRepository.updateIngredient(mUpdatedIngredients.get(mUpdatedIngredients.keyAt(i)));
        }

        for (IngredientModel ingredient : mIngredientModels) {
            if (ingredient.getId() == null) {
                mCocktailRepository.addIngredient(ingredient);
            }
        }

        for (IngredientModel ingredient : mDeleteIngredients) {
            mCocktailRepository.deleteIngredient(ingredient);
        }
    }
}
