package com.lifeshare.ui.my_connection;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterableAdapter;
import com.lifeshare.network.response.MyConnectionListResponse;

import java.util.ArrayList;

public class MyConnectionListAdapter extends FilterableAdapter<MyConnectionListResponse, BaseRecyclerListener<MyConnectionListResponse>> {

    BaseRecyclerListener<MyConnectionListResponse> listener;

    public MyConnectionListAdapter(BaseRecyclerListener<MyConnectionListResponse> listener) {
        super(listener);
        this.listener = listener;
    }

    @Override
    public void onBindData(RecyclerView.ViewHolder holder, MyConnectionListResponse val) {
        MyConnectionViewHolder viewHolder = (MyConnectionViewHolder) holder;
        viewHolder.tvName.setText(val.getFirstName() + " " + val.getLastName());
        viewHolder.tvEmail.setText(val.getEmail());
        viewHolder.tvUsername.setText(val.getUsername());
        viewHolder.ivDelete.setOnClickListener(new View.OnClickListener() {
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
                .inflate(R.layout.my_connection_raw_item_new, parent, false);
        return new MyConnectionViewHolder(view);
    }

    @Override
    public ArrayList<String> compareFieldValue(MyConnectionListResponse item, ArrayList<String> searchItemList) {
        return null;
    }

    public class MyConnectionViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView tvName;
        AppCompatTextView tvEmail;
        AppCompatTextView tvUsername;
        ImageView ivDelete;
        ImageView ivProfile;

        public MyConnectionViewHolder(View itemView) {
            super(itemView);
            tvName = (AppCompatTextView) itemView.findViewById(R.id.tv_name);
            tvEmail = (AppCompatTextView) itemView.findViewById(R.id.tv_email);
            tvUsername = (AppCompatTextView) itemView.findViewById(R.id.tv_contact);
            ivDelete = (ImageView) itemView.findViewById(R.id.iv_delete);
            ivProfile = (ImageView) itemView.findViewById(R.id.iv_profile);
        }
    }
}
