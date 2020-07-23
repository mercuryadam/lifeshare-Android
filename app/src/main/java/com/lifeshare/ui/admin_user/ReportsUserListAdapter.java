package com.lifeshare.ui.admin_user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterableAdapter;
import com.lifeshare.network.response.ReportListResponse;

import java.util.ArrayList;

public class ReportsUserListAdapter extends FilterableAdapter<ReportListResponse, BaseRecyclerListener<ReportListResponse>> {

    BaseRecyclerListener<ReportListResponse> listener;

    public ReportsUserListAdapter(BaseRecyclerListener<ReportListResponse> listener) {
        super(listener);
        this.listener = listener;
    }

    @Override
    public void onBindData(RecyclerView.ViewHolder holder, ReportListResponse val) {
        MyConnectionViewHolder viewHolder = (MyConnectionViewHolder) holder;
        viewHolder.tvName.setText(val.getFirstName() + " " + val.getLastName());
        viewHolder.tvEmail.setText(val.getChannelName());
        viewHolder.tvUsername.setText(val.getUsername());
        viewHolder.tvCount.setText("Complains : " + val.getTotalAbuse());
        viewHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecyclerItemClick(v, holder.getAdapterPosition(), val);
            }
        });

        Glide.with(LifeShare.getInstance())
                .load(val.getAvatar())
                .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                .into(viewHolder.ivProfile);

    }

    @Override
    public RecyclerView.ViewHolder onBindViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.report_user_raw_item_new, parent, false);
        return new MyConnectionViewHolder(view);
    }

    @Override
    public ArrayList<String> compareFieldValue(ReportListResponse item, ArrayList<String> searchItemList) {
        return null;
    }

    public class MyConnectionViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView tvName;
        AppCompatTextView tvEmail;
        AppCompatTextView tvUsername;
        AppCompatTextView tvCount;
        AppCompatTextView tvDelete;
        ImageView ivProfile;

        public MyConnectionViewHolder(View itemView) {
            super(itemView);
            tvName = (AppCompatTextView) itemView.findViewById(R.id.tv_name);
            tvEmail = (AppCompatTextView) itemView.findViewById(R.id.tv_email);
            tvUsername = (AppCompatTextView) itemView.findViewById(R.id.tv_contact);
            tvCount = (AppCompatTextView) itemView.findViewById(R.id.tv_count);
            tvDelete = (AppCompatTextView) itemView.findViewById(R.id.tv_delete);
            ivProfile = (ImageView) itemView.findViewById(R.id.iv_profile);
        }
    }
}
