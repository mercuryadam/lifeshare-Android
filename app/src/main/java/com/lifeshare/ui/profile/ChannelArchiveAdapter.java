package com.lifeshare.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterableAdapter;
import com.lifeshare.network.response.ChannelArchiveResponse;

import java.util.ArrayList;

public class ChannelArchiveAdapter extends FilterableAdapter<ChannelArchiveResponse, BaseRecyclerListener<ChannelArchiveResponse>> {

    BaseRecyclerListener<ChannelArchiveResponse> listener;


    public ChannelArchiveAdapter(BaseRecyclerListener<ChannelArchiveResponse> listener) {
        super(listener);
        this.listener = listener;
    }

    @Override
    public void onBindData(RecyclerView.ViewHolder holder, ChannelArchiveResponse val) {
        MyConnectionViewHolder viewHolder = (MyConnectionViewHolder) holder;
        Glide.with(LifeShare.getInstance())
                .load(val.getImage())
                .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                .into(viewHolder.ivBackGround);

        viewHolder.tvChannelName.setText(val.getTitle());
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
                .inflate(R.layout.channel_archive_raw_item, parent, false);
        return new MyConnectionViewHolder(view);
    }

    @Override
    public ArrayList<String> compareFieldValue(ChannelArchiveResponse item, ArrayList<String> searchItemList) {
        return null;
    }

    public class MyConnectionViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBackGround;
        private AppCompatTextView tvChannelName;
        private RelativeLayout rlMain;

        public MyConnectionViewHolder(View itemView) {
            super(itemView);
            ivBackGround = itemView.findViewById(R.id.ivBackGround);
            tvChannelName = itemView.findViewById(R.id.tvChannelName);
            rlMain = itemView.findViewById(R.id.rl_main);
        }
    }
}
