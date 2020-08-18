package com.lifeshare.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.lifeshare.utils.PreferenceHelper;

import java.util.ArrayList;

public class ChannelArchiveAdapter extends FilterableAdapter<ChannelArchiveResponse, BaseRecyclerListener<ChannelArchiveResponse>> {

    BaseRecyclerListener<ChannelArchiveResponse> listener;
    private String userId = "";

    public ChannelArchiveAdapter(String userId, BaseRecyclerListener<ChannelArchiveResponse> listener) {
        super(listener);
        this.listener = listener;
        this.userId = userId;
    }

    @Override
    public void onBindData(RecyclerView.ViewHolder holder, ChannelArchiveResponse val) {
        MyConnectionViewHolder viewHolder = (MyConnectionViewHolder) holder;


        ViewTreeObserver viewTreeObserver = viewHolder.ivBackGround.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewHolder.ivBackGround.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width = viewHolder.ivBackGround.getMeasuredWidth();
                int height = viewHolder.ivBackGround.getMeasuredHeight();
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) viewHolder.ivBackGround.getLayoutParams();
                params.height = width;
                viewHolder.ivBackGround.setLayoutParams(params);
            }
        });

        Glide.with(LifeShare.getInstance())
                .load(val.getImage())
                .apply(new RequestOptions().error(R.drawable.ic_document).placeholder(R.drawable.ic_document))
                .into(viewHolder.ivBackGround);

        viewHolder.tvChannelName.setText(val.getTitle());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecyclerItemClick(v, holder.getAdapterPosition(), val);
            }
        });
        if (PreferenceHelper.getInstance().getUser().getUserId().equals(userId)) {
            viewHolder.ivDeleteArchive.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivDeleteArchive.setVisibility(View.GONE);
        }

        viewHolder.ivDeleteArchive.setOnClickListener(new View.OnClickListener() {
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
        private ImageView ivBackGround,ivDeleteArchive;
        private AppCompatTextView tvChannelName;
        private RelativeLayout rlMain;

        public MyConnectionViewHolder(View itemView) {
            super(itemView);
            ivBackGround = itemView.findViewById(R.id.ivBackGround);
            ivDeleteArchive = itemView.findViewById(R.id.ivDeleteArchive);
            tvChannelName = itemView.findViewById(R.id.tvChannelName);
            rlMain = itemView.findViewById(R.id.rlFullRaw);
        }
    }
}
