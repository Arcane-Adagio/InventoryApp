package com.example.inventoryapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewFragment extends Fragment {
    private Activity HostActivity;
//    public static RecyclerViewAdapter recyclerAdapter;
    private static FloatingActionButton rv_fab;
    private static RecyclerView inventory_rv;
    private static List<InventoryItem> itemList;
    private static int counter = 0;
    private static RecyclerView.OnScrollListener rv_FAB_listener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy > 0 && rv_fab.isShown())
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("life", "RV: onCreate");
        HostActivity = getActivity();

        //Add drag logic to RV
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(inventory_rv);
        return inflater.inflate(R.layout.frag_inventorylist, container, false);
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(

            ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {

        @Override

        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            Toast.makeText(HostActivity, "onMove", Toast.LENGTH_SHORT).show();
            return false;

        }

        @Override

        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Toast.makeText(HostActivity, "onSwipe", Toast.LENGTH_SHORT).show();
        }

    } ;

    @Override
    public void onStart() {
/*        Log.d("life", "RV: onStart");
        inventory_rv = (RecyclerView) HostActivity.findViewById(R.id.inventory_recycleview);
        inventory_rv.addOnScrollListener(rv_FAB_listener);
        rv_fab = (FloatingActionButton) HostActivity.findViewById(R.id.frame_fab);
        rv_fab.setOnClickListener(fab_onClick);
        RecyclerViewAdapter adapter_items;

        //if there is already a list, load it
        if (itemList == null || itemList.size() == 0) {
            itemList = new ArrayList<InventoryItem>();
            adapter_items = new RecyclerViewAdapter(itemList);
            inventory_rv.setAdapter(adapter_items);
        } else {
            adapter_items = new RecyclerViewAdapter(itemList);
        }
        inventory_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        inventory_rv.scrollToPosition(0);
        inventory_rv.setAdapter(adapter_items);
        inventory_rv.setHasFixedSize(false);
        inventory_rv.setItemAnimator(new DefaultItemAnimator());*/
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
//        itemList = recyclerAdapter != null ? recyclerAdapter.getShownItems() : null;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d("life", "RV: onDestroy");
        super.onDestroy();
    }

    public static void AddNewItem() {
        // Popup Dialog
        InventoryItem a;
        if (counter == 0) {
            a = new InventoryItem("cero", "324", "234");
            counter++;
        } else if (counter % 2 == 1) {
            a = new InventoryItem("Sample Item 1", "Date", "4");
            counter++;
        } else {
            counter = counter * 2 + 1;
            a = new InventoryItem("Sample Item 2", "Date", "4");
            counter = 0;
        }
//        RecyclerViewAdapter.AddItem(a);
//        inventory_rv.smoothScrollToPosition(recyclerAdapter.getItemCount());
    }


    /*public static class RecyclerViewAdapter extends RecyclerView.Adapter<InventoryItem.ViewHolder> {
        //list of items maintained by Adapter
        static List<InventoryItem> items;


        //Used to communicate between the fragment and the recycler view
        //RecyclerViewFragment.OnItemClickListner vc = null;
        //public void setOnItemClickListener(RecyclerViewFragment.onItemClickListner _vo) {vc=_vo}


        //constructor
        public RecyclerViewAdapter(List<InventoryItem> input) {
            this.items = input;
        }

        //Listener Setup
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
                    (R.layout.tile_inventory_item), parent, false);
            final InventoryItem.ViewHolder view_holder = new InventoryItem.ViewHolder(v);
            view_holder.editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), String.valueOf(view_holder.getAdapterPosition()), Toast.LENGTH_SHORT).show();
                }
            });
            view_holder.reorderBtn.setOnClickListener(Test);
            view_holder.setIsRecyclable(true);
            return new InventoryItem.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull InventoryItem.ViewHolder holder, int position) {
            //Initializes each component of the UI for each tile to match their respective class attributes
            //InventoryItem item = items.get(holder.getAdapterPosition());
*//*            holder.itemNameTV.setText(items.get(position).getItemName());
            Log.d("error", "onBindViewHolder: " + items.get(position).getItemName() + " " + String.valueOf(position) + items.toString());
            holder.itemDataTV.setText(items.get(position).getItemData());
            holder.itemQuantityTV.setText(items.get(position).getItemQuantity());
            holder.itemStatusPB.setProgress(5);
            holder.itemNeedfulCB.setChecked(items.get(position).isItemNeedful());*//*
        }

        public static void AddItem(InventoryItem item) {
            *//*
            if(items.size() == 0){
                items.add(item);
                recyclerAdapter = new RecyclerViewFragment.RecyclerViewAdapter(items);
                inventory_rv.setAdapter(recyclerAdapter);
            }
            else{
                items.add(item);
                recyclerAdapter.notifyItemInserted(items.size() -1);
            }*//*
            if (items != null) {
                Log.d("error", "AddItem1: " + items.toString());
                Log.d("error", "AddItem2: " + item.toString());
            }
            items.add(item);
            Log.d("error", "AddItem3: " + items.toString());
            if (recyclerAdapter == null) {
                recyclerAdapter = new RecyclerViewFragment.RecyclerViewAdapter(items);
                inventory_rv.setAdapter(recyclerAdapter);
            } else
                recyclerAdapter.notifyItemInserted(items.size() - 1);
        }

        @Override
        public int getItemCount() {
            if (items != null)
                return items.size();
            else {
                Log.d("rv", "There are no inventory items in the list");
                return 0;
            }
        }

        public void RemoveItem(int i) {
            if (i != -1) {
                items.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, items.size());
            }
        }

        public List<InventoryItem> getShownItems(){
            return items;
        }
    }*/
}
