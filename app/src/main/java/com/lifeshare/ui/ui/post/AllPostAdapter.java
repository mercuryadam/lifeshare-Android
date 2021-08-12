package com.lifeshare.ui.ui.post;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.network.response.ChannelArchiveResponse;
import com.lifeshare.utils.DateTimeHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AllPostAdapter extends RecyclerView.Adapter<AllPostAdapter.AllPostViewHolder> {

    private final ArrayList<ChannelArchiveResponse> postList = new ArrayList<>();
    private Context context;

    @NonNull
    @NotNull
    @Override
    public AllPostViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_post_layout, parent, false);
        return new AllPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull AllPostViewHolder holder, int position) {
        ViewTreeObserver viewTreeObserver = holder.ivPostImage.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                holder.ivPostImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width = holder.ivPostImage.getMeasuredWidth();
                int height = holder.ivPostImage.getMeasuredHeight();
                ViewGroup.LayoutParams params = holder.ivPostImage.getLayoutParams();
                params.height = height;
                holder.ivPostImage.setLayoutParams(params);
            }
        });

        holder.tvPostTitle.setText(DateTimeHelper.getInstance().getDefaultDateTimeFromUtcDateTime(postList.get(position).getTitle()));
        holder.tvPostTimeText.setText(DateTimeHelper.getInstance().getTimeAgo(postList.get(position).getCreatedAt()));

        if (postList.get(position).getType().equals("1")) {
            Glide.with(LifeShare.getInstance())
                    .load(postList.get(position).getImage())
                    .apply(new RequestOptions().error(R.drawable.ic_archive).placeholder(R.drawable.ic_archive))
                    .into(holder.ivPostImage);
            holder.tvPostTitle.setText(postList.get(position).getTitle());
        } else {
            if (postList.get(position).getVideo_url() != null && !postList.get(position).getVideo_url().trim().isEmpty()) {
                Glide.with(LifeShare.getInstance())
                        .load(postList.get(position).getImage())
                        .apply(new RequestOptions().error(R.drawable.ic_video_chat).placeholder(R.drawable.ic_video_chat))
                        .into(holder.ivPostImage);

            } else {
                Glide.with(LifeShare.getInstance())
                        .load(postList.get(position).getImage())
                        .apply(new RequestOptions().error(R.drawable.ic_chat).placeholder(R.drawable.ic_chat))
                        .into(holder.ivPostImage);

            }
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postData", postList.get(position));
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void addItems(ArrayList<ChannelArchiveResponse> items, boolean isRefresh) {
        if (isRefresh) {
            this.postList.clear();
        }
        this.postList.addAll(items);
        notifyDataSetChanged();
    }

    public static class AllPostViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageView ivPostImage;
        private final AppCompatTextView tvPostTitle;
        private final AppCompatTextView tvPostTimeText;

        public AllPostViewHolder(View itemView) {
            super(itemView);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvPostTimeText = itemView.findViewById(R.id.tvPostTimeText);
        }

    }
}