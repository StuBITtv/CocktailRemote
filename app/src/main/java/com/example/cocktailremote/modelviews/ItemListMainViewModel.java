package com.example.cocktailremote.modelviews;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.ArrayList;

public class ItemListMainViewModel extends ViewModel {
    private MutableLiveData<ArrayList<String>> cocktailNames;
    private MutableLiveData<ArrayList<File>> cocktailImages;

    public ItemListMainViewModel() {}

    public LiveData<ArrayList<String>> getCocktailNames() {
        return cocktailNames;
    }

    public LiveData<ArrayList<File>> getCocktailImages() {
        return cocktailImages;
    }
}
