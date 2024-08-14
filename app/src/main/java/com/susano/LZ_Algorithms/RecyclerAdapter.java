package com.susano.LZ_Algorithms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.TagViewHolder> {
    Context context;
    ArrayList<Tag> tags;
    Animation animation;

    RecyclerAdapter(Context context, ArrayList tags){
        this.context = context;
        this.tags = tags;
        animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
    }
    public class TagViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout tagRoot;
        CardView tagCard;
        TextView tag;
        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.tagContent);
            tagCard = itemView.findViewById(R.id.tagCard);
            tagRoot = itemView.findViewById(R.id.tagRoot);
        }
    }

    @NonNull
    @Override
    public RecyclerAdapter.TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View tagView = inflater.inflate(R.layout.tag, parent, false);
        return new TagViewHolder(tagView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.TagViewHolder holder, int position) {
        Tag currentTag = tags.get(position);
        holder.tag.setText("< " + currentTag.offset + ", " + currentTag.length + ", " + currentTag.nextChar + " >");
        holder.tagRoot.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

}
