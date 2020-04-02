package com.stubit.cocktailremote.models;

import android.content.Context;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import org.jetbrains.annotations.NotNull;

@androidx.room.Database(entities = {CocktailModel.class, IngredientModel.class}, version = 2, exportSchema = false)
@TypeConverters({SignalTypeTypeConverter.class})
public abstract class Database extends RoomDatabase {
    protected Database() {}

    public static Database getDatabase(Context c) {
        if(mInstance == null) {
            mInstance = Room.databaseBuilder(c, Database.class, "database.db")
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }

        return mInstance;
    }

    public abstract CocktailModel.Access getCocktailAccess();

    public abstract IngredientModel.Access getIngredientAccess();

    private static Database mInstance;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NotNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE CocktailModel ADD COLUMN password_protected INTEGER");
        }
    };
}
