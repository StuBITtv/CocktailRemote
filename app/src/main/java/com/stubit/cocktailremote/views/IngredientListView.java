package com.stubit.cocktailremote.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class IngredientListView extends LinearLayout {
    public static final String TAG = "IngredientListView";

    Adapter mAdapter;
    Boolean mPlaceholder;

    public IngredientListView(Context context) {
        super(context);
    }

    public IngredientListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IngredientListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setViewHolder(Adapter adapter) {
        if(mAdapter != null) {
            throw new IllegalStateException("ViewHolder cannot be changed at runtime");
        }

        mAdapter = adapter;
        createNoIngredientPlaceholder();
    }

    public void updateIngredients(ArrayList<String> ingredientNames) {
        if(mAdapter == null) {
            throw new IllegalStateException("ViewHolder must be set first");
        }

        if(mPlaceholder) {
            removeAllViews();
            mPlaceholder = false;
        }

        if(ingredientNames == null || ingredientNames.size() == 0) {
            createNoIngredientPlaceholder();
        } else {
            for(int i = 0; i < ingredientNames.size(); ++i) {
                if(i >= getChildCount()) {
                    addView(mAdapter.inflateIngredientView(this));
                }

                Log.d(TAG, "Setup view at position " + i + " with ingredient name \"" + ingredientNames.get(i) + "\"");
                mAdapter.setupViewHolder(getChildAt(i), ingredientNames.get(i), i);
            }

            if(ingredientNames.size() < getChildCount()) {
                removeViews(ingredientNames.size(), getChildCount() - ingredientNames.size());
            }
        }
    }

    private void createNoIngredientPlaceholder() {
        removeAllViews();
        addView(mAdapter.inflateNoIngredientPlaceholderView(this));

        mPlaceholder = true;
    }


    public interface Adapter {
        View inflateIngredientView(ViewGroup rootView);
        View inflateNoIngredientPlaceholderView(ViewGroup rootView);
        void setupViewHolder(final View holder, String name, int position);
    }
}
