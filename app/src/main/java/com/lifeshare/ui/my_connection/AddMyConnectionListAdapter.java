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
import com.lifeshare.network.response.SearchUserResponse;

import java.util.ArrayList;

public class AddMyConnectionListAdapter extends FilterableAdapter<SearchUserResponse, BaseRecyclerListener<SearchUserResponse>> {

    BaseRecyclerListener<SearchUserResponse> listener;

    public AddMyConnectionListAdapter(BaseRecyclerListener<SearchUserResponse> listener) {
        super(listener);
        this.listener = listener;
    }

    @Override
    public void onBindData(RecyclerView.ViewHolder holder, SearchUserResponse val) {
        MyConnectionViewHolder viewHolder = (MyConnectionViewHolder) holder;
        viewHolder.tvName.setText(val.getFirstName() + " " + val.getLastName());
        viewHolder.tvEmail.setText(val.getEmail());
        viewHolder.tvContact.setText(val.getUsername());
        if (val.getBlocked()) {
            viewHolder.tvBlockUser.setVisibility(View.GONE);
            viewHolder.tvUnblockUser.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tvBlockUser.setVisibility(View.VISIBLE);
            viewHolder.tvUnblockUser.setVisibility(View.GONE);
        }
        viewHolder.tvBlockUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRecyclerItemClick(view, holder.getAdapterPosition(), val);

            }
        });
        viewHolder.tvUnblockUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRecyclerItemClick(view, holder.getAdapterPosition(), val);

            }
        });
        viewHolder.tvSendRequest.setOnClickListener(new View.OnClickListener() {
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
                .inflate(R.layout.search_user_list_raw_item_new, parent, false);
        return new MyConnectionViewHolder(view);
    }

    @Override
    public ArrayList<String> compareFieldValue(SearchUserResponse item, ArrayList<String> searchItemList) {
        return null;
    }

    public class MyConnectionViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView tvName;
        AppCompatTextView tvEmail;
        AppCompatTextView tvContact;
        AppCompatTextView tvSendRequest;
        AppCompatTextView tvBlockUser;
        AppCompatTextView tvUnblockUser;
        ImageView ivProfile;

        public MyConnectionViewHolder(View itemView) {
            super(itemView);
            tvName = (AppCompatTextView) itemView.findViewById(R.id.tv_name);
            tvEmail = (AppCompatTextView) itemView.findViewById(R.id.tv_email);
            tvContact = (AppCompatTextView) itemView.findViewById(R.id.tv_contact);
            tvSendRequest = (AppCompatTextView) itemView.findViewById(R.id.tv_send_request);
            tvBlockUser = (AppCompatTextView) itemView.findViewById(R.id.tv_block_user);
            tvUnblockUser = (AppCompatTextView) itemView.findViewById(R.id.tv_unblock_user);
            ivProfile = (ImageView) itemView.findViewById(R.id.iv_profile);

        }
    }
}
