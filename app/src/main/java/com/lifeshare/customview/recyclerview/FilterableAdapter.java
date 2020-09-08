package com.lifeshare.customview.recyclerview;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.recyclerview.widget.RecyclerView;

import com.lifeshare.R;

import java.util.ArrayList;

/**
 * Generic Recycler Adapter which handles filtering and contains default reusable methods.
 *
 * @param <ItemType>     Data type of Your List
 * @param <ListenerType> Recycler Item Listener Data Type
 */
public abstract class FilterableAdapter<ItemType, ListenerType extends BaseRecyclerListener<ItemType>>
        extends RecyclerView.Adapter implements Filterable {

    private ArrayList<ItemType> originalList;
    private ArrayList<ItemType> filteredList;

    private ItemFilter itemFilter;

    private ListenerType listener;

    private String filteredString;

    private int emptyErrorMsg, noSearchDataFoundMsg;

    public FilterableAdapter(final ListenerType listener) {
        this.listener = listener;
        this.filteredString = "";
        this.originalList = new ArrayList<>();
        this.filteredList = new ArrayList<>();

        this.emptyErrorMsg = R.string.no_data_available;
        this.noSearchDataFoundMsg = R.string.no_search_result_available;

        RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                if (originalList.size() == 0) {
                    listener.showEmptyDataView(getEmptyErrorMsg());
                } else {
                    listener.showEmptyDataView(getNoSearchDataFoundMsg());
                }
            }
        };

        this.registerAdapterDataObserver(adapterDataObserver);
    }

    /**
     * Method is used for displaying values to UI
     *
     * @param holder Generic OtherVehicleViewHolder
     * @param val    Generic DataType
     */
    public abstract void onBindData(RecyclerView.ViewHolder holder, ItemType val);

    /**
     * Overriding default oncreateViewHolder
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return onBindViewHolder(parent, viewType);
    }

    /**
     * Define raw layouts inside this method.
     *
     * @param parent
     * @param viewType
     * @return
     */
    public abstract RecyclerView.ViewHolder onBindViewHolder(ViewGroup parent, int viewType);


    /**
     * Overriding default onBindViewHolder
     * <p>This will always call onBindData method to display value</p>
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecyclerItemClick(v, holder.getAdapterPosition(), filteredList.get(holder.getAdapterPosition()));
            }
        });
        // implementation of recycler item click event. It will trigger onRecyclerItemClick() method always.
        onBindData(holder, filteredList.get(holder.getAdapterPosition()));
    }

    /**
     * @return returns items size
     */
    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    /**
     * Set Items to list
     *
     * @param items
     */
    public void setItems(ArrayList<ItemType> items) {
        this.filteredList = new ArrayList<>(items);
        this.originalList = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    /**
     * Add All Items to list
     *
     * @param items
     */
    public void addItems(ArrayList<ItemType> items) {
        this.filteredList.addAll(items);
        this.originalList.addAll(items);
        notifyDataSetChanged();
    }

    public void addItemsWithFilter(ArrayList<ItemType> items) {
        this.originalList.addAll(items);
        if (TextUtils.isEmpty(filteredString)) {
            this.filteredList.addAll(items);
            notifyDataSetChanged();
        } else {
            getFilter().filter(filteredString);
        }
    }

    /**
     * Add Item at Particular index
     *
     * @param position
     * @param item
     */
    public void addItem(int position, ItemType item) {
        originalList.add(item);
        if (position > filteredList.size()) {
            filteredList.add(item);
        } else {
            filteredList.add(position, item);
        }
        notifyDataSetChanged();
    }

    /**
     * Add item at last index
     *
     * @param item
     */
    public void addItem(ItemType item) {
        this.filteredList.add(item);
        this.originalList.add(item);
        notifyDataSetChanged();
    }

    /**
     * Remove Last element of list
     */
    public void removeLastItem() {
        ItemType removedItem = null;
        if (filteredList.size() > 0) {
            removedItem = filteredList.get(filteredList.size() - 1);
            filteredList.remove(filteredList.size() - 1);
        }

//        if (removedItem != null) {
        originalList.remove(originalList.size() - 1);
//        }
        notifyDataSetChanged();
    }

    /**
     * Remove Element from particular index
     *
     * @param position
     */
    public void removeItemAt(int position) {
        ItemType removedItem = null;
        if (filteredList.size() > position) {
            removedItem = filteredList.get(position);
            filteredList.remove(position);
            notifyDataSetChanged();
        }
        if (removedItem != null) {
            originalList.remove(removedItem);
        }
    }


    /**
     * remove All items of list
     */
    public void removeAllItems() {
        originalList.clear();
        filteredList.clear();
        notifyDataSetChanged();
    }


    public ArrayList<ItemType> getAllItems() {
        return originalList;
    }

    public ArrayList<ItemType> getAllFilterItems() {
        return filteredList;
    }

    @Override
    public Filter getFilter() {
        if (itemFilter == null) {
            itemFilter = new ItemFilter();
        }
        return itemFilter;
    }

    /**
     * Put String value which you want to compare inside filtering
     * <p>If you want to compare multiple fields value specify them in comma seperated string</p>
     *
     * @param item
     * @return
     */
    public abstract ArrayList<String> compareFieldValue(ItemType item, ArrayList<String> searchItemList);

    public int getEmptyErrorMsg() {
        return emptyErrorMsg;
    }

    public void setEmptyErrorMsg(int emptyErrorMsg) {
        this.emptyErrorMsg = emptyErrorMsg;
    }

    public int getNoSearchDataFoundMsg() {
        return noSearchDataFoundMsg;
    }

    public void setNoSearchDataFoundMsg(int noSearchDataFoundMsg) {
        this.noSearchDataFoundMsg = noSearchDataFoundMsg;
    }

    public String getFilteredString() {
        return filteredString;
    }

    public void setFilteredString(String filteredString) {
        this.filteredString = filteredString;
    }

    /**
     * Generic Filterable class which will trigger events
     * occording to input string.
     */
    private class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            filteredString = charSequence.toString().trim();

            String filterString = filteredString.toLowerCase();
            FilterResults filterResults = new FilterResults();

            final ArrayList<ItemType> mMasterList = (ArrayList<ItemType>) originalList;

            final ArrayList<ItemType> mResultedList = new ArrayList<>();

            if (filterString.isEmpty()) {
                mResultedList.clear();
                mResultedList.addAll(mMasterList);
            } else {
                String filterableString;

                for (int i = 0; i < mMasterList.size(); i++) {

                    if (mMasterList.get(i) == null) {
                        continue;
                    }
                    ArrayList<String> compareFields = compareFieldValue(mMasterList.get(i), new ArrayList<String>());

                    if (compareFields != null) {
                        for (String itemValue : compareFields) {
                            filterableString = itemValue.toLowerCase();
                            if (filterableString.contains(filterString)) {
                                mResultedList.add(mMasterList.get(i));
                                break;
                            }
                        }
                    }
                }
            }
            filterResults.values = mResultedList;

            return filterResults;
        }


        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filteredList = (ArrayList<ItemType>) filterResults.values;
            notifyDataSetChanged();
        }
    }
}
