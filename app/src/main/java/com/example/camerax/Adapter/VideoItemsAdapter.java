package com.example.camerax.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.camerax.Constant;
import com.example.camerax.EditVideoActivity;
import com.example.camerax.R;
import com.example.camerax.VideoRecorderActivity;

public class VideoItemsAdapter extends RecyclerView.Adapter<VideoItemsAdapter.viewholder> {

    Context context;

    public VideoItemsAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, @SuppressLint("RecyclerView") int position) {

        Uri uri = Uri.fromFile(Constant.allMediaList.get(position));
        Glide.with(context).load(uri).thumbnail(0.1f).into(((viewholder)holder).thumbanail);
    holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, EditVideoActivity.class);
            intent.putExtra("vpath", String.valueOf(Constant.allMediaList.get(position).getPath()));
            context.startActivity(intent);

          //  Toast.makeText(context, String.valueOf(Constant.allMediaList.get(position).getPath()), Toast.LENGTH_SHORT).show();

        }
    });

    }

    @Override
    public int getItemCount() {
        return Constant.allMediaList.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        ImageView thumbanail;
        public viewholder(@NonNull View itemView) {
            super(itemView);

            thumbanail = itemView.findViewById(R.id.videoImage);


        }
    }
}
