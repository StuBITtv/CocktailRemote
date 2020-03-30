package com.stubit.cocktailremote.models;

import androidx.room.TypeConverter;

public class SignalTypeTypeConverter {
    @TypeConverter
    public static CocktailModel.SignalType toSignalType(int value) {
        switch (value) {
            case 1:
                return CocktailModel.SignalType.BINARY;
            case 2:
                return CocktailModel.SignalType.INTEGER;
            default:
                return CocktailModel.SignalType.STRING;
        }
    }

    @TypeConverter
    public static Integer toInteger(CocktailModel.SignalType signalType) {
        switch (signalType) {
            case BINARY:
                return 1;
            case INTEGER:
                return 2;
            default:
                return 3;
        }
    }
}
