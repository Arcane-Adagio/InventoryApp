package com.example.inventoryapp.online;

import static com.example.inventoryapp.GlobalConstants.ONLINE_KEY_GROUPID;
import static com.example.inventoryapp.GlobalConstants.ONLINE_KEY_GROUPNAME;
import static com.example.inventoryapp.GlobalConstants.OUT_OF_BOUNDS;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.GlobalConstants;
import com.example.inventoryapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class OnlineInventoryFragment extends Fragment implements FirebaseHandler.OnlineFragmentBehavior {

    Activity cActivity;
    private final String TAG = "Inventory Activity Online";
    DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mInventoryReference;
    private static String mCurrentGroupID;
    RecyclerView rv;
    InventoryRVAdapter inv_rva;
    FirebaseUser currentUser;
    FloatingActionButton addition_fab;
    FloatingActionButton edit_fab;



    public OnlineInventoryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cActivity = getActivity();
        mCurrentGroupID = this.getArguments().getString(ONLINE_KEY_GROUPID);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.online_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /* Handles behavior for when a menu option is selected */
        if (GlobalActions.DefaultMenuOptionSelection(item,cActivity, this))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_online_inventory, container, false);
    }


    @Override
    public void onStart() {
        super.onStart();
        addition_fab = (FloatingActionButton) getView().findViewById(R.id.fab_inventory);
        addition_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddInventory();
            }
        });
        edit_fab = (FloatingActionButton) getView().findViewById(R.id.fab_renameGroup);
        edit_fab.setOnClickListener(view -> ShowRenameGroupDialog());
        Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).setTitle(this.getArguments().getString(ONLINE_KEY_GROUPNAME));
        SetupRecyclerView();
    }


    private void NavigateToItemFragment(String groupID, String inventoryID, String inventoryName){
        Bundle bundle = new Bundle();
        bundle.putString("groupID", groupID);
        bundle.putString("inventoryID", inventoryID);
        bundle.putString("inventoryName", inventoryName);
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_onlineInventoryFragment_to_onlineItemFragment, bundle);
    }


    private void SetupRecyclerView(){
        rv=(RecyclerView) getView().findViewById(R.id.recyclerview_inventory);
        LinearLayoutManager layoutManager = new LinearLayoutManager(cActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);
        inv_rva =new InventoryRVAdapter(cActivity);
        rv.setAdapter(inv_rva);
    }

    private void AddInventory(){
        new FirebaseHandler().AddInventoryToGroup(mCurrentGroupID, new FirebaseHandler.Inventory(GlobalConstants.SAMPLE_INVENTORYNAME), this);
    }

    @Override
    public void HandleFragmentInvalidation() {
        Toast.makeText(getActivity(), "Group was deleted", Toast.LENGTH_SHORT).show();
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_onlineFragment_to_onlineLoginFragment);
    }

    @Override
    public void HandleInventoryInvalidation() {
        //do nothing
    }


    public class InventoryRVAdapter extends RecyclerView.Adapter<ViewHolder>{
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupsRef = mRootReference.child("Groups");
        DatabaseReference groupRef = groupsRef.child(mCurrentGroupID);
        DatabaseReference mInventoriesReference = groupRef.child("Inventories");
        List<FirebaseHandler.Inventory> inventoryData = new ArrayList<FirebaseHandler.Inventory>();
        Context mContext;
        RecyclerView rv;

        public InventoryRVAdapter(Context context){
            mContext = context;
            rv = (RecyclerView) ((AppCompatActivity)context).findViewById(R.id.recyclerview_inventory);
            mInventoriesReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(mInventoriesReference == null)
                        Log.d(TAG, "onChildAdded: invalid reference");
                    inventoryData.add(datasnapshotToInventoryConverter(snapshot));
                    rv.scrollToPosition(inventoryData.size()-1); //todo: take out if annoying
                    InventoryRVAdapter.this.notifyItemInserted(inventoryData.size()-1);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String changedInventoryID = snapshot.getKey().toString();
                    int position = getPositionInRecyclerViewByID(changedInventoryID);
                    if(position != OUT_OF_BOUNDS){
                        inventoryData.remove(position);
                        inventoryData.add(position, datasnapshotToInventoryConverter(snapshot));
                        InventoryRVAdapter.this.notifyItemChanged(position);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    String changedInventoryID = snapshot.getKey().toString();
                    int position = getPositionInRecyclerViewByID(changedInventoryID);
                    if(position != OUT_OF_BOUNDS){
                        inventoryData.remove(position);
                        InventoryRVAdapter.this.notifyItemRemoved(position);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Toast.makeText(context, "onChildMove Not Implemented", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Database cancelled updating RecyclerView", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_inventory, parent, false);
            final ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.inventoryName.setText(inventoryData.get(position).getInventoryName());
            holder.editBtn.setOnClickListener(view ->
                    NavigateToItemFragment(
                            mCurrentGroupID, inventoryData.get(holder.getAdapterPosition()).getInventoryID(),
                            inventoryData.get(holder.getAdapterPosition()).getInventoryName()));
            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //potential runtime exception if user presses button too fast
                    String inventoryID = inventoryData.get(holder.getAdapterPosition()).getInventoryID();
                    new FirebaseHandler().RemoveInventoryFromGroup(mCurrentGroupID, inventoryID);
                }
            });
        }

        @Override
        public int getItemCount() {
            return inventoryData.size();
        }

        private FirebaseHandler.Inventory datasnapshotToInventoryConverter(DataSnapshot snap){
            if(!snap.hasChild("inventoryName")){
                Log.d(TAG, "datasnapshotToInventoryConverter: no child");
                return null;
            }

            String inventoryName = Objects.requireNonNull(snap.child("inventoryName").getValue()).toString();
            FirebaseHandler.Inventory inventoryObj = new FirebaseHandler.Inventory(inventoryName);
            inventoryObj.setInventoryID(snap.getKey());
            return inventoryObj;
        }

        private int getPositionInRecyclerViewByID(String id){
            int position = GlobalConstants.OUT_OF_BOUNDS;
            for (int i = 0; i< inventoryData.size(); i++){
                if(inventoryData.get(i).getInventoryID().equals(id)){
                    position = i;
                    break;
                }
            }
            return position;
        }
    }

    public void ShowRenameGroupDialog(){
        final Dialog dialog = new Dialog(cActivity);
        dialog.setContentView(R.layout.dlog_renamegroup);
        Button submitBtn = (Button) dialog.findViewById(R.id.btn_renameGroup_submit);
        Button cancelBtn = (Button) dialog.findViewById(R.id.btn_renameGroup_cancel);
        EditText nameEditText = (EditText)dialog.findViewById(R.id.edittext_renameGroup);
        //Set Max length of each edit text to make sure it matches the length allotted by the database
        nameEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_code_length) });
        //when focus has been lost, check if code is valid
        submitBtn.setOnClickListener(v -> {
            RenameGroupBehavior(nameEditText, dialog);
        });
        cancelBtn.setOnClickListener(v -> {
            nameEditText.getBackground().clearColorFilter();
            dialog.dismiss();
        });

        dialog.show();
    }

    public void RenameGroupBehavior(EditText nameEditText, Dialog dialog){
        if(nameEditText.getText() == null)
            return;
        if(nameEditText.getText().toString().isEmpty()){
            nameEditText.setHint("Please Enter A Valid Name");
            nameEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            return;
        }
        String newName = nameEditText.getText().toString();
        new FirebaseHandler().RenameGroup(mCurrentGroupID, newName, FirebaseAuth.getInstance().getCurrentUser(), this);
        Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).setTitle(this.getArguments().getString(newName));
        dialog.dismiss();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView inventoryName;
        CardView parentLayout;
        ImageButton editBtn;
        ImageButton deleteBtn;
        ImageButton reorderBtn;

        public ViewHolder(View inventoryView) {
            super(inventoryView);
            inventoryName = inventoryView.findViewById(R.id.inventory_title_edittext);
            parentLayout = inventoryView.findViewById(R.id.tile_inventory);
            editBtn = (ImageButton) inventoryView.findViewById(R.id.inv_edit_btn);
            deleteBtn = (ImageButton) inventoryView.findViewById(R.id.inv_delete_btn);
            reorderBtn = (ImageButton) inventoryView.findViewById(R.id.inv_reorder_btn);
        }
    }
}