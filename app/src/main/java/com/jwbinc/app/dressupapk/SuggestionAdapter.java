package com.jwbinc.app.dressupapk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionViewHolder> {

    private List<Suggestion> suggestions;
    private Context mContext;

    public SuggestionAdapter(List<Suggestion> suggestions, Context mContext) {
        this.suggestions = suggestions;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new SuggestionViewHolder(LayoutInflater.from(mContext).inflate(R.layout.tinderlikeswipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final SuggestionViewHolder suggestionViewHolder, int position) {
        Suggestion suggestion = suggestions.get(position);

        int imageDimension = 250;

        Picasso.get().load(suggestion.getUrl()).resize(imageDimension, imageDimension).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                assert suggestionViewHolder.imageView != null;
                suggestionViewHolder.imageView.setImageBitmap(bitmap);

                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@Nullable Palette palette) {
                        Palette.Swatch textSwatch = palette.getVibrantSwatch();
                        if(textSwatch == null){
//                            Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

}
