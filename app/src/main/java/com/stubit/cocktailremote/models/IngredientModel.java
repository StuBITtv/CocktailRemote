package com.stubit.cocktailremote.models;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        foreignKeys = @ForeignKey(
                entity = CocktailModel.class,
                parentColumns = "id",
                childColumns = "cocktail_id",
                onDelete = CASCADE
        ),
        indices = {@Index("cocktail_id")}
)
public class IngredientModel {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    protected Integer mId;

    @ColumnInfo(name = "cocktail_id")
    protected Integer mCocktailId;

    @ColumnInfo(name = "name")
    protected String mName;

    public IngredientModel(Integer mCocktailId) {
        this.mCocktailId = mCocktailId;
    }

    public Integer getId() {
        return mId;
    }

    public void setCocktail(Integer mCocktailId) {
        this.mCocktailId = mCocktailId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    @Dao
    public interface Access {
        @Query("SELECT * FROM IngredientModel WHERE cocktail_id=:cocktailId")
        LiveData<List<IngredientModel>> getAllFromCocktail(int cocktailId);

        @Insert
        void addModel(IngredientModel model);

        @Delete
        void deleteModel(IngredientModel model);

        @Update
        void updateModel(IngredientModel model);
    }
}
