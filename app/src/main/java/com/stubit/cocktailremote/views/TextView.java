package com.stubit.cocktailremote.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class TextView extends AppCompatTextView {
    public TextView(Context context) {
        super(context);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setText(String text, int placeholderResId) {
        if(text != null && !text.equals("")) {
            setText(text);
            setTypeface(getTypeface(), Typeface.NORMAL);
        } else {
            setText(placeholderResId);
            setTypeface(getTypeface(), Typeface.ITALIC);
        }
    }
}
