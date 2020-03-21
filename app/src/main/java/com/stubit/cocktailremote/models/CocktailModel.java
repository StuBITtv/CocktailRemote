package com.stubit.cocktailremote.models;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Entity
public class CocktailModel {
    @PrimaryKey(autoGenerate = true)
    protected Integer mId;

    @ColumnInfo(name = "name")
    protected String mName;

    @ColumnInfo(name = "image_path")
    protected String mImagePath;

    @ColumnInfo(name = "description")
    protected String mDescription;

    public CocktailModel() {}

    @Ignore
    public CocktailModel(CocktailModel other) {
        mId = other.mId;
        mName = other.mName;
        mImagePath = other.mImagePath;
        mDescription = other.mDescription;
    }

    public Integer getId() {
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

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    @Dao
    public interface Access {
        @Query("SELECT * FROM CocktailModel")
        LiveData<List<CocktailModel>> all();

        @Query("SELECT * FROM CocktailModel WHERE mId == (:id)")
        LiveData<CocktailModel> byId(int id);

        @Insert
        Long addModel(CocktailModel model);

        @Delete
        void deleteModel(CocktailModel model);

        @Update
        void updateModel(CocktailModel model);
    }
}
