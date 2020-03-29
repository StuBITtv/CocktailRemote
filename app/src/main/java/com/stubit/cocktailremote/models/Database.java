package com.stubit.cocktailremote.models;

import android.content.Context;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {CocktailModel.class, IngredientModel.class}, version = 1, exportSchema = false)
public abstract class Database extends RoomDatabase {
    protected Database() {}

    public static Database getDatabase(Context c) {
        if(mInstance == null) {
            mInstance = Room.databaseBuilder(c, Database.class, "database.db").build();
        }

        return mInstance;
    }

    public abstract CocktailModel.Access getCocktailAccess();

    public abstract IngredientModel.Access getIngredientAccess();

    private static Database mInstance;
}
