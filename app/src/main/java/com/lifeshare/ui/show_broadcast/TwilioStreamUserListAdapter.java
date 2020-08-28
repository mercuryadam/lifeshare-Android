package com.lifeshare.ui.show_broadcast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterableAdapter;
import com.lifeshare.network.response.StreamUserListResponse;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class TwilioStreamUserListAdapter extends FilterableAdapter<StreamUserListResponse, BaseRecyclerListener<StreamUserListResponse>> {

    BaseRecyclerListener<StreamUserListResponse> listener;


    public TwilioStreamUserListAdapter(BaseRecyclerListener<StreamUserListResponse> listener) {
        super(listener);
        this.listener = listener;
    }

    @Override
    public void onBindData(RecyclerView.ViewHolder holder, StreamUserListResponse val) {
        MyConnectionViewHolder viewHolder = (MyConnectionViewHolder) holder;
      /*  if (val.isSelected()) {
            viewHolder.rlMain.setBackgroundColor(ContextCompat.getColor(LifeShare.getInstance(), R.color.colorPrimary));
        } else {
            viewHolder.rlMain.setBackgroundColor(ContextCompat.getColor(LifeShare.getInstance(), R.color.white));
        }
      */
        Glide.with(LifeShare.getInstance())
                .load(val.getAvatar())
                .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                .into(viewHolder.profileImage);
        viewHolder.tvName.setText(val.getUserName());
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
                .inflate(R.layout.stream_user_raw_item_new, parent, false);
        return new MyConnectionViewHolder(view);
    }

    @Override
    public ArrayList<String> compareFieldValue(StreamUserListResponse item, ArrayList<String> searchItemList) {
        return null;
    }

    public class MyConnectionViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView profileImage;
        private AppCompatTextView tvName;
        private RelativeLayout rlMain;

        public MyConnectionViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            tvName = itemView.findViewById(R.id.tv_name);
            rlMain = itemView.findViewById(R.id.rl_main);
        }
    }
}
