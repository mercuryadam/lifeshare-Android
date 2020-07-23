package com.lifeshare.customview.singleChoiceBottomSheet;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.CustomSearchView;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;

import java.util.ArrayList;

/**
 * Generic Class For Single Item List
 *
 * @param <ItemType>
 */
public class SingleChoiceBottomSheet<ItemType extends Comparable<ItemType>> extends BottomSheetDialogFragment
        implements SearchView.OnQueryTextListener, BaseRecyclerListener<ItemType>, View.OnClickListener {

    private ImageView ivClose;
    private TextView tvTitle;
    private CustomSearchView searchView;
    private FilterRecyclerView recyclerView;
    private TextView tvNoData;

    private SingleChoiceAdapter<ItemType> itemAdapter;

    private ArrayList<ItemType> itemList;
    private String fieldName, dialogTitle;
    private SingleChoiceDialogListener<ItemType> listener;
    private int selectedItem = -1;
    private ItemType selectedItemObject;
    private String mNoDataMessage = "No data found";

    //Don't remove default constructor it is usefull for fragments
    public SingleChoiceBottomSheet() {
    }

    private static SingleChoiceBottomSheet newInstance() {
        Bundle args = new Bundle();
        SingleChoiceBottomSheet fragment = new SingleChoiceBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    public static void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) LifeShare.getInstance().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Adds click listener for spinner item
     *
     * @param listener
     */
    private void addSpinnerClickLister(SingleChoiceDialogListener<ItemType> listener) {
        this.listener = listener;
    }

    /**
     * sets dialog Title
     *
     * @param dialogTitle
     */
    private void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    /**
     * This method is usefull when you are passing custom class and
     * want to show particular variables value in list
     *
     * @param fieldName
     */
    private void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * sets item Data
     *
     * @param spinnerItems
     */
    private void setSpinnerItems(ArrayList<ItemType> spinnerItems) {
        this.itemList = spinnerItems;
    }

    /**
     * sets Selected Item Position
     *
     * @return
     */
    private int getSelectedItemPos() {
        return selectedItem;
    }

    /**
     * Sets Predefined Selected Item From its position
     *
     * @param selectedItem
     */
    private void setSelectedItemPos(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    /**
     * sets selected item
     *
     * @return
     */
    private ItemType getSelectedItem() {
        return selectedItemObject;
    }

    /**
     * Sets Predefind Selected Item
     *
     * @param selectedItemObject
     */
    private void setSelectedItem(ItemType selectedItemObject) {
        this.selectedItemObject = selectedItemObject;
    }

    /**
     * sets dialog Title
     *
     * @param message
     */
    public void setNoDataMessage(String message) {
        this.mNoDataMessage = message;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_dialog_single_choice, container, false);
        initView(view);
        bindRecyclerView();
        return view;
    }

    private void initView(View view) {
        ivClose = (ImageView) view.findViewById(R.id.iv_close);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        searchView = (CustomSearchView) view.findViewById(R.id.search_view);
        recyclerView = (FilterRecyclerView) view.findViewById(R.id.recyclerView);
        tvNoData = (TextView) view.findViewById(R.id.tv_no_data);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setEmptyMsgHolder(tvNoData);

        searchView.setOnQueryTextListener(this);
        ivClose.setOnClickListener(this);

        tvTitle.setText(dialogTitle);

    }

    private void bindRecyclerView() {
        itemAdapter = new SingleChoiceAdapter<>(this, fieldName);
        recyclerView.setAdapter(itemAdapter);
        itemAdapter.addItems(itemList);


        if (getSelectedItemPos() != -1) {
            itemAdapter.setSelectedItemPos(getSelectedItemPos());
        } else if (getSelectedItem() != null) {
            itemAdapter.setSelectedItem(getSelectedItem());
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        itemAdapter.getFilter().filter(newText);
        return false;
    }

    @Override
    public void showEmptyDataView(@StringRes int resId) {
        recyclerView.showEmptyDataView(mNoDataMessage);
    }

    @Override
    public void onRecyclerItemClick(View view, int position, ItemType item) {
        hideKeyboard(view);
        listener.onSpinnerItemClick(position, item);
        dismissDialog();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismissDialog();
                break;
        }
    }

    private void dismissDialog() {
        this.getDialog().cancel();
    }

    /**
     * Builder Class For Single Choice Bottom Sheet
     * Helps to set Title of dialog, items List of dialog,
     *
     * @param <ItemType>
     */
    public static class Builder<ItemType extends Comparable<ItemType>> {
        String fieldName, dialogTitle, noDataMessage = "No data found";
        ArrayList<ItemType> items;
        SingleChoiceDialogListener<ItemType> listener;
        ItemType selectedItem;
        int selectedItemPos = -1;

        /**
         * Adds click listener for spinner item
         *
         * @param listener
         */
        public Builder(SingleChoiceDialogListener<ItemType> listener) {
            this.listener = listener;
        }

        /**
         * sets dialog Title
         *
         * @param dialogTitle
         */
        public Builder<ItemType> addDialogTitle(String dialogTitle) {
            this.dialogTitle = dialogTitle;
            return this;
        }

        /**
         * sets dialog Title
         *
         * @param message
         */
        public Builder<ItemType> setNoDataMessage(String message) {
            this.noDataMessage = message;
            return this;
        }

        /**
         * sets item Data
         *
         * @param items
         */
        public Builder<ItemType> addItems(@NonNull ArrayList<ItemType> items) {
            this.items = items;
            return this;
        }

        /**
         * This method is usefull when you are passing custom class and
         * want to show particular variables value in list
         *
         * @param fieldName
         */
        public Builder<ItemType> addFieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        /**
         * Sets Predefinded Selected Item
         *
         * @param selectedItem
         * @return
         */
        public Builder<ItemType> setSelectedItem(ItemType selectedItem) {
            this.selectedItem = selectedItem;
            return this;
        }

        /**
         * Sets Predefined Selected Item Based On Position
         *
         * @param selectedItemPos
         * @return
         */
        public Builder<ItemType> setSelectedItemPos(int selectedItemPos) {
            this.selectedItemPos = selectedItemPos;
            return this;
        }

        /**
         * Creates SingleChoiceBottomSheet Object
         *
         * @return SingleChoiceBottomSheet Fragment
         */
        public SingleChoiceBottomSheet build() {
            if (fieldName == null) {
                fieldName = "";
            }
            SingleChoiceBottomSheet<ItemType> bottomSheet = SingleChoiceBottomSheet.newInstance();
            bottomSheet.setFieldName(fieldName);
            bottomSheet.setSpinnerItems(items);
            bottomSheet.setDialogTitle(dialogTitle);
            bottomSheet.setNoDataMessage(noDataMessage);
            bottomSheet.addSpinnerClickLister(listener);
            if (selectedItemPos != -1) {
                bottomSheet.setSelectedItemPos(selectedItemPos);
            } else if (selectedItem != null) {
                bottomSheet.setSelectedItem(selectedItem);
            }
            return bottomSheet;
        }

    }
}
