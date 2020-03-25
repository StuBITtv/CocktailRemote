package com.stubit.cocktailremote.views;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.stubit.cocktailremote.R;

import java.io.File;

public class CocktailImageView extends AppCompatImageView {
    public CocktailImageView(Context context) {
        super(context);
    }

    public CocktailImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CocktailImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImage(Context c, File image) {
        if(image != null) {
            Glide.with(this).load(image).into(this);

            this.setBackgroundColor(
                    ContextCompat.getColor(c, R.color.transparent)
            );
        } else {
            this.setImageDrawable(
                    ContextCompat.getDrawable(c, R.drawable.ic_photo)
            );

            this.setBackgroundColor(
                    ContextCompat.getColor(c, R.color.colorPlaceholder)
            );
        }
    }
}
