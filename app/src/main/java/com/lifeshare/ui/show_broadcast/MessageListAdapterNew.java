package com.lifeshare.ui.show_broadcast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterableAdapter;
import com.lifeshare.model.ChatMessage;
import com.lifeshare.utils.PreferenceHelper;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListAdapterNew extends FilterableAdapter<ChatMessage, BaseRecyclerListener<ChatMessage>> {

    BaseRecyclerListener<ChatMessage> listener;


    public MessageListAdapterNew(BaseRecyclerListener<ChatMessage> listener) {
        super(listener);
        this.listener = listener;
    }

    @Override
    public void onBindData(RecyclerView.ViewHolder holder, ChatMessage val) {
        MyConnectionViewHolder viewHolder = (MyConnectionViewHolder) holder;
        if (val.getUserId().equalsIgnoreCase(PreferenceHelper.getInstance().getUser().getUserId())) {
            viewHolder.llChatOther.setVisibility(View.GONE);
            viewHolder.llChatSelf.setVisibility(View.VISIBLE);

            if (holder.getAdapterPosition() > 0) {
                if (getAllFilterItems().get(holder.getAdapterPosition() - 1).getUserId().equalsIgnoreCase(val.getUserId())) {
                    viewHolder.tvUsernameSelf.setVisibility(View.GONE);
                    viewHolder.ivProfileSelf.setVisibility(View.INVISIBLE);
                }
            }

            Glide.with(LifeShare.getInstance())
                    .load(val.getProfileUrl())
                    .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                    .into(viewHolder.ivProfileSelf);

            viewHolder.tvUsernameSelf.setText(val.getUsername());
            viewHolder.tvMessageSelf.setText(val.getMessage());
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onRecyclerItemClick(v, holder.getAdapterPosition(), val);
                }
            });
        } else {
            viewHolder.llChatOther.setVisibility(View.VISIBLE);
            viewHolder.llChatSelf.setVisibility(View.GONE);


            if (holder.getAdapterPosition() > 0) {
                if (getAllFilterItems().get(holder.getAdapterPosition() - 1).getUserId().equalsIgnoreCase(val.getUserId())) {
                    viewHolder.tvUsernameOther.setVisibility(View.GONE);
                    viewHolder.ivProfileOther.setVisibility(View.INVISIBLE);
                }
            }

            Glide.with(LifeShare.getInstance())
                    .load(val.getProfileUrl())
                    .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                    .into(viewHolder.ivProfileOther);

            viewHolder.tvUsernameOther.setText(val.getUsername());
            viewHolder.tvMessageOther.setText(val.getMessage());
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onRecyclerItemClick(v, holder.getAdapterPosition(), val);
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onBindViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_raw_item_new, parent, false);
        return new MyConnectionViewHolder(view);
    }

    @Override
    public ArrayList<String> compareFieldValue(ChatMessage item, ArrayList<String> searchItemList) {
        return null;
    }

    public class MyConnectionViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout llChatOther;
        private CircleImageView ivProfileOther;
        private AppCompatTextView tvUsernameOther;
        private AppCompatTextView tvMessageOther;
        private LinearLayout llChatSelf;
        private AppCompatTextView tvUsernameSelf;
        private AppCompatTextView tvMessageSelf;
        private CircleImageView ivProfileSelf;


        public MyConnectionViewHolder(View itemView) {
            super(itemView);

            llChatOther = (LinearLayout) itemView.findViewById(R.id.ll_chat_other);
            ivProfileOther = (CircleImageView) itemView.findViewById(R.id.iv_profile_other);
            tvUsernameOther = (AppCompatTextView) itemView.findViewById(R.id.tv_username_other);
            tvMessageOther = (AppCompatTextView) itemView.findViewById(R.id.tv_message_other);
            llChatSelf = (LinearLayout) itemView.findViewById(R.id.ll_chat_self);
            tvUsernameSelf = (AppCompatTextView) itemView.findViewById(R.id.tv_username_self);
            tvMessageSelf = (AppCompatTextView) itemView.findViewById(R.id.tv_message_self);
            ivProfileSelf = (CircleImageView) itemView.findViewById(R.id.iv_profile_self);

        }
    }
}
