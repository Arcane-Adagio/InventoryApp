package com.example.inventoryapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewFragment extends Fragment {
    private Activity HostActivity;
    private static RecyclerViewAdapter recyclerAdapter;
    private static FloatingActionButton rv_fab;
    private static RecyclerView inventory_rv;
    private static List<InventoryItem> itemList;
    private static RecyclerView.OnScrollListener rv_FAB_listener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(dy > 0 && rv_fab.isShown())
                rv_fab.hide();
            else if (dy < 0 && !rv_fab.isShown())
                rv_fab.show();
        }
    };
    private static View.OnClickListener fab_onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AddNewItem();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.d("life", "RV: onCreate");
        HostActivity = getActivity();
        return inflater.inflate(R.layout.frag_inventorylist, container, false);
    }

    @Override
    public void onStart() {
        Log.d("life", "RV: onStart");
        inventory_rv = (RecyclerView) HostActivity.findViewById(R.id.inventory_recycleview);
        inventory_rv.addOnScrollListener(rv_FAB_listener);
        rv_fab = (FloatingActionButton) HostActivity.findViewById(R.id.frame_fab);
        rv_fab.setOnClickListener(fab_onClick);
        RecyclerViewAdapter adapter_items;

        //if there is already a list, load it
        if(itemList == null || itemList.size() == 0){
            itemList = new ArrayList<InventoryItem>();
            adapter_items = new RecyclerViewAdapter(itemList);
            inventory_rv.setAdapter(adapter_items);
        }
        else{
            adapter_items = new RecyclerViewAdapter(itemList);
        }
        inventory_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        inventory_rv.scrollToPosition(0);
        inventory_rv.setAdapter(adapter_items);
        inventory_rv.setHasFixedSize(false);
        inventory_rv.setItemAnimator(new DefaultItemAnimator());
        //rv_fab.setVisibility(View.VISIBLE);
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d("life", "RV: onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("life", "RV: onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d("life", "RV: onStop");
        itemList = recyclerAdapter!=null ? recyclerAdapter.getShownItems() : null;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d("life", "RV: onDestroy");
        super.onDestroy();
    }

    public static void AddNewItem(){
        Toast.makeText(inventory_rv.getContext(), "New Item Test Button", Toast.LENGTH_SHORT).show();
        // Popup Dialog
        InventoryItem a = new InventoryItem("Sample Item", "Date", "4");
        RecyclerViewAdapter.AddItem(a);
        inventory_rv.smoothScrollToPosition(recyclerAdapter.getItemCount());
    }



    public static class RecyclerViewAdapter extends RecyclerView.Adapter<InventoryItem.ViewHolder>{

        static List<InventoryItem> items;
        public RecyclerViewAdapter(List<InventoryItem> input) {items = input;}
        private View.OnClickListener Test = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Test OnClick Listener", Toast.LENGTH_SHORT).show();
            }
        };

        @NonNull
        @Override
        public InventoryItem.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    (R.layout.inventory_tile), parent, false);
            final InventoryItem.ViewHolder view_holder = new InventoryItem.ViewHolder(v);
            return new InventoryItem.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull InventoryItem.ViewHolder holder, int position) {
            //Initializes each component of the UI for each tile to match their respective class attributes
            holder.itemNameTV.setText(items.get(position).getItemName());
            holder.itemDataTV.setText(items.get(position).getItemData());
            holder.itemQuantityTV.setText(items.get(position).getItemQuantity());
            holder.itemStatusPB.setProgress(5);
            holder.itemNeedfulCB.setChecked(items.get(position).isItemNeedful());


            //add listeners to the tile buttons
            holder.editBtn.setOnClickListener(Test);
            holder.deleteBtn.setOnClickListener(Test);
            holder.reorderBtn.setOnClickListener(Test);
        }

        public static void AddItem(InventoryItem item){
            /*
            if(items.size() == 0){
                items.add(item);
                recyclerAdapter = new RecyclerViewFragment.RecyclerViewAdapter(items);
                inventory_rv.setAdapter(recyclerAdapter);
            }
            else{
                items.add(item);
                recyclerAdapter.notifyItemInserted(items.size() -1);
            }*/
            items.add(item);
            if(recyclerAdapter == null){
                recyclerAdapter = new RecyclerViewFragment.RecyclerViewAdapter(items);
                inventory_rv.setAdapter(recyclerAdapter);
            }
            else
                recyclerAdapter.notifyItemInserted(items.size() -1);
        }

        @Override
        public int getItemCount() {
            if (items != null)
                return items.size();
            else {
                Log.d("rv","There are no inventory items in the list");
                return 0;
            }
        }

        public List<InventoryItem> getShownItems(){
            return items;
        }
    }
}
