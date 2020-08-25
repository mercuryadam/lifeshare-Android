package com.lifeshare.ui.select_connection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterableAdapter;
import com.lifeshare.network.response.MyConnectionListResponse;

import java.util.ArrayList;

public class SelectConnectionListAdapter extends FilterableAdapter<MyConnectionListResponse, BaseRecyclerListener<MyConnectionListResponse>> {
    BaseRecyclerListener<MyConnectionListResponse> listener;

    public SelectConnectionListAdapter(BaseRecyclerListener<MyConnectionListResponse> listener) {
        super(listener);
    }

    public ArrayList<MyConnectionListResponse> getCheckedItems() {
        ArrayList<MyConnectionListResponse> checkedItems = new ArrayList<>();
        for (MyConnectionListResponse item : getAllItems()) {
            if (item.isSelected()) {
                checkedItems.add(item);
            }
        }
        return checkedItems;
    }


    public void selectAll() {
        for (MyConnectionListResponse item : getAllItems()) {
            item.setSelected(true);
        }
        notifyDataSetChanged();
    }

    public void unSelectAll() {
        for (MyConnectionListResponse item : getAllItems()) {
            item.setSelected(false);
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindData(RecyclerView.ViewHolder holder, MyConnectionListResponse val) {
        SelectConnectionListAdapter.MyConnectionViewHolder viewHolder = (SelectConnectionListAdapter.MyConnectionViewHolder) holder;
        viewHolder.tvName.setText(val.getFirstName() + " " + val.getLastName());
        viewHolder.tvEmail.setText(val.getEmail());
        viewHolder.tvUsername.setText(val.getUsername());

        Glide.with(LifeShare.getInstance())
                .load(val.getAvatar())
                .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                .into(viewHolder.ivProfile);

        viewHolder.cbSelect.setOnCheckedChangeListener(null);

        viewHolder.cbSelect.setChecked(val.isSelected());

        viewHolder.cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                val.setSelected(b);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (val.isSelected()) {
                    val.setSelected(false);
                    viewHolder.cbSelect.setChecked(false);
                } else {
                    val.setSelected(true);
                    viewHolder.cbSelect.setChecked(true);
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onBindViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_connection_raw_item, parent, false);
        return new SelectConnectionListAdapter.MyConnectionViewHolder(view);
    }

    @Override
    public ArrayList<String> compareFieldValue(MyConnectionListResponse item, ArrayList<String> searchItemList) {
        return null;
    }

    public class MyConnectionViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView tvName;
        AppCompatTextView tvEmail;
        AppCompatTextView tvUsername;
        CheckBox cbSelect;
        ImageView ivProfile;

        public MyConnectionViewHolder(View itemView) {
            super(itemView);
            tvName = (AppCompatTextView) itemView.findViewById(R.id.tv_name);
            tvEmail = (AppCompatTextView) itemView.findViewById(R.id.tv_email);
            tvUsername = (AppCompatTextView) itemView.findViewById(R.id.tv_contact);
            cbSelect = (CheckBox) itemView.findViewById(R.id.cb_select);
            ivProfile = (ImageView) itemView.findViewById(R.id.iv_profile);

            /*cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        getAllItems().get(getAdapterPosition()).setSelected(true);
                    } else {
                        getAllItems().get(getAdapterPosition()).setSelected(false);
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getAllItems().get(getAdapterPosition()).isSelected()) {
                        getAllItems().get(getAdapterPosition()).setSelected(false);
                    } else {
                        getAllItems().get(getAdapterPosition()).setSelected(true);
                    }
                    notifyItemChanged(getAdapterPosition());
                }
            });*/
        }
    }
}
