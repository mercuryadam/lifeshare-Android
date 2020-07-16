package com.lifeshare.ui.invitation;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterableAdapter;
import com.lifeshare.network.response.InvitationListResponse;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyInvitationListAdapter extends FilterableAdapter<InvitationListResponse, BaseRecyclerListener<InvitationListResponse>> {

    BaseRecyclerListener<InvitationListResponse> listener;

    public MyInvitationListAdapter(BaseRecyclerListener<InvitationListResponse> listener) {
        super(listener);
        this.listener = listener;
    }

    @Override
    public void onBindData(RecyclerView.ViewHolder holder, InvitationListResponse val) {
        MyConnectionViewHolder viewHolder = (MyConnectionViewHolder) holder;
        viewHolder.tvName.setText(val.getFirstName() + " " + val.getLastName());
        viewHolder.tvEmail.setText(val.getEmail());
        viewHolder.tvEmail.setVisibility(View.GONE);
        viewHolder.tvUsername.setText(val.getUsername());
        viewHolder.tvDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecyclerItemClick(v, holder.getAdapterPosition(), val);
            }
        });
        viewHolder.tvAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecyclerItemClick(v, holder.getAdapterPosition(), val);
            }
        });

    }

    @Override
    public RecyclerView.ViewHolder onBindViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.invitation_list_raw_item_new, parent, false);
        return new MyConnectionViewHolder(view);
    }

    @Override
    public ArrayList<String> compareFieldValue(InvitationListResponse item, ArrayList<String> searchItemList) {
        return null;
    }

    public class MyConnectionViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView tvName;
        AppCompatTextView tvEmail;
        AppCompatTextView tvUsername;
        AppCompatTextView tvDecline;
        AppCompatTextView tvAccept;
        CircleImageView ivProfile;

        public MyConnectionViewHolder(View itemView) {
            super(itemView);
            tvName = (AppCompatTextView) itemView.findViewById(R.id.tv_name);
            tvEmail = (AppCompatTextView) itemView.findViewById(R.id.tv_email);
            tvUsername = (AppCompatTextView) itemView.findViewById(R.id.tv_contact);
            tvDecline = (AppCompatTextView) itemView.findViewById(R.id.tv_decline);
            tvAccept = (AppCompatTextView) itemView.findViewById(R.id.tv_accept);
            ivProfile = (CircleImageView) itemView.findViewById(R.id.iv_profile);

        }
    }
}
