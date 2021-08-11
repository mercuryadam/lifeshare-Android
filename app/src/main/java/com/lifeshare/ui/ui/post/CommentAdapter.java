package com.lifeshare.ui.ui.post;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.network.response.CommentData;
import com.lifeshare.utils.DateTimeHelper;
import com.lifeshare.utils.PreferenceHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final ArrayList<CommentData> commentResponses = new ArrayList<>();
    private final OnItemClickListener listener;

    public CommentAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @NotNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_layout, parent, false);
        return new CommentViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull @NotNull CommentViewHolder holder, int position) {
        holder.tvCommentText.setText(commentResponses.get(position).getComment());
        holder.tvUserName.setText(commentResponses.get(position).getUser().getUsername());
        holder.tvTime.setText(DateTimeHelper.getInstance().getTimeAgo(commentResponses.get(position).getCommentDateTime()));
        holder.tvLikeCount.setText("" + commentResponses.get(position).getLike());
        holder.tvLoveCount.setText("" + commentResponses.get(position).getLove());

        String userId = String.valueOf(commentResponses.get(position).getUser().getId());
        if (PreferenceHelper.getInstance().getUser().getUserId().equals(userId)) {
            holder.ivEdit.setVisibility(View.VISIBLE);
            holder.ivDelete.setVisibility(View.VISIBLE);
        } else {
            holder.ivEdit.setVisibility(View.GONE);
            holder.ivDelete.setVisibility(View.GONE);
        }
        if (commentResponses.get(position).getUserLike() == 1) {
            ImageViewCompat.setImageTintList(holder.ivLike, ColorStateList.valueOf(ContextCompat.getColor(LifeShare.getInstance(), R.color.primary_green)));
        } else {
            ImageViewCompat.setImageTintList(holder.ivLike, ColorStateList.valueOf(ContextCompat.getColor(LifeShare.getInstance(), R.color.gray)));
        }

        if (commentResponses.get(position).getUserLove() == 1) {
            ImageViewCompat.setImageTintList(holder.ivLove, ColorStateList.valueOf(ContextCompat.getColor(LifeShare.getInstance(), R.color.primary_green)));
        } else {
            ImageViewCompat.setImageTintList(holder.ivLove, ColorStateList.valueOf(ContextCompat.getColor(LifeShare.getInstance(), R.color.gray)));
        }

        holder.ivDelete.setOnClickListener(v ->
                listener.onItemClick(commentResponses.get(position), "delete")
        );
        holder.ivEdit.setOnClickListener(v ->
                listener.onItemClick(commentResponses.get(position), "edit")
        );
        holder.ivLike.setOnClickListener(v ->
                listener.onItemClick(commentResponses.get(position), "like")
        );
        holder.ivLove.setOnClickListener(v ->
                listener.onItemClick(commentResponses.get(position), "love"));
    }

    @Override
    public int getItemCount() {
        return commentResponses.size();
    }

    public void addItems(List<CommentData> items) {
        this.commentResponses.clear();
        this.commentResponses.addAll(items);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(CommentData item, String type);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatTextView tvCommentText;
        private final AppCompatTextView tvUserName;
        private final AppCompatTextView tvTime;
        private final AppCompatTextView tvLikeCount;
        private final AppCompatTextView tvLoveCount;
        private final AppCompatImageView ivEdit;
        private final AppCompatImageView ivDelete;
        private final AppCompatImageView ivLike;
        private final AppCompatImageView ivLove;

        public CommentViewHolder(View itemView) {
            super(itemView);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLoveCount = itemView.findViewById(R.id.tvLoveCount);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivLove = itemView.findViewById(R.id.ivLove);
            ivLike = itemView.findViewById(R.id.ivLike);
        }

    }
}