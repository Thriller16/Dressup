package com.jwbinc.app.dressupapk.cards;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jwbinc.app.dressupapk.Suggestion;
import com.jwbinc.app.dressupapk.cards.SliderCard;
import com.jwbinc.app.dressupapk.R;

import java.util.ArrayList;
import java.util.List;


public class SliderAdapter extends RecyclerView.Adapter<SliderCard> {

    private final int count;
    List<Suggestion> images;
    private final View.OnClickListener listener;

    public SliderAdapter(int count, List<Suggestion> images, View.OnClickListener listener) {
        this.count = count;
        this.images = images;
        this.listener = listener;
    }

    @Override
    public SliderCard onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_slider_card, parent, false);

        if (listener != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view);
                }
            });
        }

        return new SliderCard(view);
    }

    @Override
    public void onBindViewHolder(SliderCard holder, int position) {
        holder.setContent(images, position);
    }

    @Override
    public void onViewRecycled(SliderCard holder) {
        holder.clearContent();
    }

    @Override
    public int getItemCount() {
        return count;
    }

}
