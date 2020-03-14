package com.stubit.cocktailremote.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import com.stubit.cocktailremote.R;
import com.stubit.cocktailremote.modelviews.ItemListMainViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

import java.io.File;
import java.util.ArrayList;

public class CocktailAdapter extends RecyclerView.Adapter<CocktailAdapter.ViewHolder> {
    final private ItemListMainViewModel mViewModel;
    private final static String TAG = "CocktailAdapter";

    public CocktailAdapter(LifecycleOwner owner, final ItemListMainViewModel viewModel) {
        mViewModel = viewModel;

        mViewModel.getCocktailNames().observe(owner, new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> strings) {
                Log.d(TAG, "dataset changed");
                notifyDataSetChanged();
            }
        });

        mViewModel.getCocktailImages().observe(owner, new Observer<ArrayList<File>>() {
            @Override
            public void onChanged(ArrayList<File> files) {
                Log.d(TAG, "dataset changed");
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(mViewModel.getCocktailNames().getValue() != null) {
            holder.mNameView.setText(mViewModel.getCocktailNames().getValue().get(position));
        }

        /*  Glide.with(holder.mImageView)
                .load(mViewModel.getCocktailImages().getValue().get(position))
                .into(holder.mImageView);
         */

    }

    @Override
    public int getItemCount() {
        if (mViewModel.getCocktailNames().getValue() != null) {
            return mViewModel.getCocktailNames().getValue().size();
        } else {
            return 0;
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mNameView;
        CircleImageView mImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mNameView = itemView.findViewById(R.id.cocktailName);
            mImageView = itemView.findViewById(R.id.cocktailImage);
        }
    }
}
