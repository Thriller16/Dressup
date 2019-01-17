package com.jwbinc.app.dressupapk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GalleryImageAdapter extends ArrayAdapter<GalleryImage> {

    DatabaseReference mGalleryDatabase;
    String url = "";

    public GalleryImageAdapter(@NonNull Context context, @NonNull List<GalleryImage> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.gallery_list_item, parent, false);
        }

        GalleryImage galleryImage = getItem(position);

        ImageView clothImageView = convertView.findViewById(R.id.galleryImage);
        Button deleteButton = convertView.findViewById(R.id.delete_from_gallery);
        TextView typeTextView = convertView.findViewById(R.id.galleryclothtype);
        TextView dateAddedTextView = convertView.findViewById(R.id.gallerydateadded);


        Picasso.get().load(galleryImage.getUrl()).centerCrop().resize(150, 150).into(clothImageView);
        typeTextView.setText(galleryImage.getClothType());
        dateAddedTextView.setText("Added: " +galleryImage.getDateAdded());

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mFireAuth = FirebaseAuth.getInstance();
                FirebaseUser mCurrentUser = mFireAuth.getCurrentUser();
                mGalleryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).child("gallery");

                mGalleryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for(DataSnapshot gallerySnapshot: dataSnapshot.getChildren()){
                               url = gallerySnapshot.getKey();

                            }


                            mGalleryDatabase.child(url).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        Toast.makeText(getContext(), "Photo removed from gallery", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                        else {
//                            Toast.makeText(getContext(), "Does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        return convertView;
    }
}
