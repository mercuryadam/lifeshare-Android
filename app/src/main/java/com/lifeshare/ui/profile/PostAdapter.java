package com.lifeshare.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterableAdapter;
import com.lifeshare.network.response.ChannelArchiveResponse;
import com.lifeshare.utils.DateTimeHelper;

import java.util.ArrayList;

public class PostAdapter extends FilterableAdapter<ChannelArchiveResponse, BaseRecyclerListener<ChannelArchiveResponse>> {

    BaseRecyclerListener<ChannelArchiveResponse> listener;

    public PostAdapter(BaseRecyclerListener<ChannelArchiveResponse> listener) {
        super(listener);
        this.listener = listener;
    }

    @Override
    public void onBindData(RecyclerView.ViewHolder holder, ChannelArchiveResponse val) {
        PostViewHolder viewHolder = (PostViewHolder) holder;

        viewHolder.tvChannelName.setText(DateTimeHelper.getInstance().getDefaultDateTimeFromUtcDateTime(val.getTitle()));
        viewHolder.tvTime.setText(DateTimeHelper.getInstance().getTimeAgo(val.getCreatedAt()));

        if (val.getType().equals("1")) {
            Glide.with(LifeShare.getInstance())
                    .load(val.getImage())
                    .apply(new RequestOptions().error(R.drawable.ic_archive).placeholder(R.drawable.ic_archive).fitCenter())
                    .into(viewHolder.ivBackGround);
            viewHolder.tvChannelName.setText(val.getTitle());
        } else {
            if (val.getVideo_url() != null && !val.getVideo_url().trim().isEmpty()) {
                Glide.with(LifeShare.getInstance())
                        .load(val.getImage())
                        .apply(new RequestOptions().error(R.drawable.ic_video_chat).placeholder(R.drawable.ic_video_chat).fitCenter())
                        .into(viewHolder.ivBackGround);

            } else {
                Glide.with(LifeShare.getInstance())
                        .load(val.getImage())
                        .apply(new RequestOptions().error(R.drawable.ic_chat).placeholder(R.drawable.ic_chat).fitCenter())
                        .into(viewHolder.ivBackGround);

            }
        }

        viewHolder.tvChannelName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecyclerItemClick(v, holder.getAdapterPosition(), val);
            }
        });
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecyclerItemClick(v, holder.getAdapterPosition(), val);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onBindViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_raw_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public ArrayList<String> compareFieldValue(ChannelArchiveResponse item, ArrayList<String> searchItemList) {
        return null;
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView ivBackGround;
        private AppCompatTextView tvChannelName, tvTime;

        public PostViewHolder(View itemView) {
            super(itemView);
            ivBackGround = itemView.findViewById(R.id.ivBackGround);
            tvChannelName = itemView.findViewById(R.id.tvChannelName);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
