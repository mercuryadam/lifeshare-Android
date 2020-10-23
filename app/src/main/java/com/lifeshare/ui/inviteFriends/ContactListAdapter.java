package com.lifeshare.ui.inviteFriends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterableAdapter;
import com.lifeshare.model.ContactListModel;

import java.util.ArrayList;

public class ContactListAdapter extends FilterableAdapter<ContactListModel, BaseRecyclerListener<ContactListModel>> {
    BaseRecyclerListener<ContactListModel> listener;

    public ContactListAdapter(BaseRecyclerListener<ContactListModel> listener) {
        super(listener);
    }

    public ArrayList<ContactListModel> getCheckedItems() {
        ArrayList<ContactListModel> checkedItems = new ArrayList<>();
        for (ContactListModel item : getAllItems()) {
            if (item.isSelected) {
                checkedItems.add(item);
            }
        }
        return checkedItems;
    }

    public void selectAll() {
        for (ContactListModel item : getAllItems()) {
            item.setSelected(true);
        }
        notifyDataSetChanged();
    }

    public void unSelectAll() {
        for (ContactListModel item : getAllItems()) {
            item.setSelected(false);
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindData(RecyclerView.ViewHolder holder, ContactListModel val) {
        ContactListAdapter.MyConnectionViewHolder viewHolder = (ContactListAdapter.MyConnectionViewHolder) holder;
        viewHolder.tvName.setText(val.getName());
        viewHolder.tvEmail.setText(val.getEmail());

        viewHolder.cbSelect.setOnCheckedChangeListener(null);

        viewHolder.cbSelect.setChecked(val.isSelected);

        viewHolder.cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                val.setSelected(b);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (val.isSelected) {
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
                .inflate(R.layout.select_contact_list_raw_item, parent, false);
        return new ContactListAdapter.MyConnectionViewHolder(view);
    }

    @Override
    public ArrayList<String> compareFieldValue(ContactListModel item, ArrayList<String> searchItemList) {
        return null;
    }

    public class MyConnectionViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView tvName;
        AppCompatTextView tvEmail;
        CheckBox cbSelect;

        public MyConnectionViewHolder(View itemView) {
            super(itemView);
            tvName = (AppCompatTextView) itemView.findViewById(R.id.tv_name);
            tvEmail = (AppCompatTextView) itemView.findViewById(R.id.tv_email);
            cbSelect = (CheckBox) itemView.findViewById(R.id.cb_select);

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
