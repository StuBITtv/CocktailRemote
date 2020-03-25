package com.stubit.cocktailremote.views;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import com.stubit.cocktailremote.R;

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

    public void setImage(Context c, Uri imageUri) {
        if(imageUri != null) {
            this.setImageURI(imageUri);

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
