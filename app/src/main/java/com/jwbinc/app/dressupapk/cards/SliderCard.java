package com.jwbinc.app.dressupapk.cards;

import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;


import com.jwbinc.app.dressupapk.R;
import com.jwbinc.app.dressupapk.Suggestion;
import com.jwbinc.app.dressupapk.utils.DecodeBitmapTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SliderCard extends RecyclerView.ViewHolder{

    private static int viewWidth = 0;
    private static int viewHeight = 0;

    private final ImageView imageView;

    private DecodeBitmapTask task;

    public SliderCard(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.image);
    }

    void setContent(List<Suggestion> suggestions, int position) {
        if (viewWidth == 0) {
                Picasso.get().load(suggestions.get(position).getUrl()).into(imageView);
        } else {
        }
    }

    void clearContent() {
        if (task != null) {
            task.cancel(true);
        }
    }
}