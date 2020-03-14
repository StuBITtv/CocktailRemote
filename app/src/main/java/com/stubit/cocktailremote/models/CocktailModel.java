package com.stubit.cocktailremote.models;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Entity
public class CocktailModel {
    @PrimaryKey(autoGenerate = true)
    protected int mId;

    @ColumnInfo(name = "name")
    protected String mName;

    @ColumnInfo(name = "image_path")
    protected String mImagePath;

    public CocktailModel() {}

    @Ignore
    public CocktailModel(String name) {
        mName = name;
    }

    @Ignore
    public CocktailModel(String name, String imagePath) {
        mName = name;
        mImagePath = imagePath;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }

    @Dao
    public interface Access {
        @Query("SELECT * FROM CocktailModel")
        LiveData<List<CocktailModel>> all();

        @Query("SELECT * FROM CocktailModel WHERE mId == (:id)")
        LiveData<CocktailModel> byId(int id);

        @Insert
        void addModel(CocktailModel model);

        @Delete
        void deleteModel(CocktailModel model);

        @Update
        void updateModel(CocktailModel model);
    }
}
