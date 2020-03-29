package com.stubit.cocktailremote.modelviews;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

public class ViewModelFactory implements ViewModelProvider.Factory {
    public static final String TAG = "ViewModelFactory";

    private final Context mC;
    private final LifecycleOwner mLifecycleOwner;
    private final Object[] mExtras;

    public ViewModelFactory(Context c, LifecycleOwner lifecycleOwner, Object ...extras) {
        mC = c;
        mLifecycleOwner = lifecycleOwner;
        mExtras = extras;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) throws RuntimeException {
        Constructor<T> constructor;
        try {
            ArrayList<Class<?>> parameterClasses = new ArrayList<Class<?>>() {{
                add(Context.class);
                add(LifecycleOwner.class);
            }};

            for (Object extra:mExtras) {
                parameterClasses.add(extra.getClass());
            }

            constructor = modelClass.getConstructor(parameterClasses.toArray(new Class<?>[0]));
        } catch (NoSuchMethodException e) {
            Log.d(TAG, "Constructor not found");
            e.printStackTrace();
            throw new RuntimeException();
        }

        String exception;

        try {
            ArrayList<Object> arguments = new ArrayList<Object>() {{
                add(mC);
                add(mLifecycleOwner);
            }};

            arguments.addAll(Arrays.asList(mExtras));

            return constructor.newInstance(arguments.toArray());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            exception = e.getClass().getName();
        } catch (InstantiationException e) {
            e.printStackTrace();
            exception = e.getClass().getName();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            exception = e.getCause().toString();
        }

        Log.d(TAG, "Constructor could not be called: " + exception);
        throw new RuntimeException();
    }
}
