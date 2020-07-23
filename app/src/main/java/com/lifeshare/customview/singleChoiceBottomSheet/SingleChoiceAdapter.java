package com.lifeshare.customview.singleChoiceBottomSheet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterableAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class SingleChoiceAdapter<ItemType extends Comparable<ItemType>> extends FilterableAdapter<ItemType, BaseRecyclerListener<ItemType>> {

    private String fieldName;
    private ItemType selectedItem;

    SingleChoiceAdapter(BaseRecyclerListener<ItemType> listener, String fieldName) {
        super(listener);
        this.fieldName = fieldName;
    }

    void setSelectedItemPos(int selectedItemPos) {
        if (selectedItemPos < getAllItems().size()) {
            selectedItem = super.getAllItems().get(selectedItemPos);
        }
    }

    void setSelectedItem(ItemType selectedItem) {
        this.selectedItem = selectedItem;
    }

    @Override
    public void onBindData(RecyclerView.ViewHolder holder, ItemType val) {
        SingleChoiceViewHolder singleChoiceViewHolder = (SingleChoiceViewHolder) holder;


        if (selectedItem != null && selectedItem.compareTo(val) == 0) {
            singleChoiceViewHolder.llHolder.setSelected(true);
        } else {
            singleChoiceViewHolder.llHolder.setSelected(false);

        }

        singleChoiceViewHolder.name.setText(getValueUsingReflection(val));
    }

    /**
     * @param val variable name from which user want to get value
     * @return respected variable value from class using reflection concept
     */
    private String getValueUsingReflection(ItemType val) {
        if (val instanceof String) {
            return (String) val;
        } else {
            try {
                Field field = val.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                try {
                    return (String) field.get(val);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public RecyclerView.ViewHolder onBindViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_item_single_choice_bottom_sheet, parent, false);
        return new SingleChoiceViewHolder(view);
    }

    @Override
    public ArrayList<String> compareFieldValue(ItemType item, ArrayList<String> searchItemList) {
        searchItemList.add(getValueUsingReflection(item));
        return searchItemList;
    }


    private class SingleChoiceViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private LinearLayout llHolder;

        public SingleChoiceViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_name);
            llHolder = (LinearLayout) itemView.findViewById(R.id.ll_holder);

        }
    }

}
