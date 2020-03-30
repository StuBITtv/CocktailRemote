package com.stubit.cocktailremote.models;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Entity
public class CocktailModel {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    protected Integer mId;

    @ColumnInfo(name = "name")
    protected String mName;

    @ColumnInfo(name = "image_uri")
    protected String mImageUri;

    @ColumnInfo(name = "description")
    protected String mDescription;

    @ColumnInfo(name = "signal_type")
    protected SignalType mSignalType;

    @ColumnInfo(name = "signal")
    protected String mSignal;

    public CocktailModel() {}

    public Integer getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getImageUri() {
        return mImageUri;
    }

    public void setImageUri(String mImageUri) {
        this.mImageUri = mImageUri;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public SignalType getSignalType() {
        return mSignalType;
    }

    public void setSignalType(SignalType mSignalType) {
        this.mSignalType = mSignalType;
    }

    public String getSignal() {
        return mSignal;
    }

    public void setSignal(String mSignal) {
        this.mSignal = mSignal;
    }

    public enum SignalType {
        BINARY, INTEGER, STRING
    }

    @Dao
    public interface Access {
        @Query("SELECT * FROM CocktailModel")
        LiveData<List<CocktailModel>> all();

        @Insert
        Long addModel(CocktailModel model);

        @Delete
        void deleteModel(CocktailModel model);

        @Update
        void updateModel(CocktailModel model);
    }
}
