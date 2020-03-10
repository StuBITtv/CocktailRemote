package com.example.cocktailremote.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.cocktailremote.R;
import com.example.cocktailremote.modelviews.ItemListMainViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

public class CocktailAdapter extends RecyclerView.Adapter<CocktailAdapter.ViewHolder> {
    final private ItemListMainViewModel mViewModel;
    private final static String TAG = "CocktailAdapter";

    public CocktailAdapter(final ItemListMainViewModel viewModel) {
        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            holder.mNameView.setText(mViewModel.getCocktailNames().getValue().get(position));

            Glide.with(holder.mImageView)
                    .load(mViewModel.getCocktailImages().getValue().get(position))
                    .into(holder.mImageView);

        } catch (final NullPointerException e) {
            Log.d(TAG, "Could not load cocktail on position " + String.valueOf(position));
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mNameView;
        CircleImageView mImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mNameView = itemView.findViewById(R.id.cocktailName);
            mImageView = itemView.findViewById(R.id.cocktailImage);
        }
    }
}
