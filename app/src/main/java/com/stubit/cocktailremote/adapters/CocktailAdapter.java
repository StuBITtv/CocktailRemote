package com.stubit.cocktailremote.adapters;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import com.stubit.cocktailremote.CocktailActivity;
import com.stubit.cocktailremote.R;
import com.stubit.cocktailremote.modelviews.ItemListMainViewModel;
import com.stubit.cocktailremote.views.TextView;

import java.util.ArrayList;

public class CocktailAdapter extends RecyclerView.Adapter<CocktailAdapter.ViewHolder> {
    final private ItemListMainViewModel mViewModel;
    private final static String TAG = "CocktailAdapter";

    private ArrayList<String> mCocktailNames;
    private ArrayList<String> mCocktailDescriptions;
    private ArrayList<Uri> mCocktailImageUris;

    public CocktailAdapter(LifecycleOwner owner, final ItemListMainViewModel viewModel) {
        mViewModel = viewModel;

        mViewModel.getCocktailNames().observe(owner, cocktailNames -> {
            Log.d(TAG, "dataset changed");

            mCocktailNames = cocktailNames;
            notifyDataSetChanged();
        });

        mViewModel.getCocktailDescriptions().observe(owner, cocktailDescription -> {
            Log.d(TAG, "dataset changed");

            mCocktailDescriptions = cocktailDescription;
            notifyDataSetChanged();
        });

        mViewModel.getCocktailImageUris().observe(owner, uris -> {
            Log.d(TAG, "dataset changed");

            mCocktailImageUris = uris;
            notifyDataSetChanged();
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if (mCocktailNames != null) {
            String cocktailName = mCocktailNames.get(position);
            holder.mNameView.setText(cocktailName, R.string.unnamed_cocktail);
        }

        if (mCocktailDescriptions != null) {
            String cocktailDescription = mCocktailDescriptions.get(position);
            holder.mDescriptionView.setText(cocktailDescription, R.string.no_description);
        }

        if(mCocktailImageUris != null) {
            Uri cocktailImageUri = mCocktailImageUris.get(position);

            if (cocktailImageUri != null) {
                holder.mImageView.setImageURI(cocktailImageUri);
            } else {
                holder.mImageView.setImageDrawable(
                        ContextCompat.getDrawable(holder.mImageView.getContext(), R.drawable.ic_photo)
                );
            }
        }

        holder.mHolder.setOnClickListener(v -> {
            Intent cocktailDetailsIntent = new Intent(
                    v.getContext(), CocktailActivity.class
            );

            if (mViewModel.getCocktailIds().getValue() != null) {
                cocktailDetailsIntent.putExtra(
                        CocktailActivity.ID_EXTRA_KEY,
                        mViewModel.getCocktailIds().getValue().get(position)
                );
            }

            v.getContext().startActivity(cocktailDetailsIntent);
        });
    }

    @Override
    public int getItemCount() {
        if (mViewModel.getCocktailNames().getValue() != null) {
            return mViewModel.getCocktailNames().getValue().size();
        } else {
            return 0;
        }

    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        final View mHolder;
        final TextView mNameView;
        final TextView mDescriptionView;
        final ImageView mImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mHolder = itemView;
            mNameView = itemView.findViewById(R.id.cocktail_name);
            mDescriptionView = itemView.findViewById(R.id.description_text);
            mImageView = itemView.findViewById(R.id.cocktail_image);
        }
    }
}
