package com.jwbinc.app.dressupapk;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

public class SuggestionViewHolder extends RecyclerView.ViewHolder {

    ImageView imageView;

    public SuggestionViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.suggestionImage);
    }
}
