package com.example.inventoryapp.online;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.R;
import com.example.inventoryapp.data.Dialogs;
import com.google.firebase.auth.FirebaseAuth;

public class GroupMembersFragment extends OnlineFragment {
    public static final String TAG = "Group Members Fragment";
    RecyclerView rv;
    MembersRVAOnline members_rva;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate Menu
        requireActivity().addMenuProvider(new OnlineMenuProvider(this), getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        return inflater.inflate(R.layout.frag_online_members, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        SetupMembersRecyclerView();
        RenameAppBar(currentGroupName+" Members");
        SetupBottomNav();
    }

    void SetupMembersRecyclerView(){
        rv=(RecyclerView) getView().findViewById(R.id.recyclerview_members);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);
        members_rva =new MembersRVAOnline(getContext(), args -> RemoveUser(new FirebaseHandler.User(args[1], args[0])));
        rv.setAdapter(members_rva);
    }


    private void NavigateToGroupFragment(){
        try{ // potential: java.lang.IllegalStateException: Fragment OfflineInventoryFragment{52a50fc} (d1f88a5a-919c-4574-9e0d-7f8606eb1559) not associated with a fragment manager.
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_groupMembersFragment_to_onlineGroupFragment);
        }
        catch (Exception e){
            Log.d(TAG, "NavigateToInventoryFragment: "+e.getMessage());
        }
    }

    private void RemoveUser(FirebaseHandler.User selectedUser){
        Dialogs.AreYouSureDialog(getContext(), new Dialogs.DialogListener() {
            @Override
            public boolean submissionCallback(String[] args) {
                FirebaseHandler.RemoveUser(currentGroupID, selectedUser, currentUser, GroupMembersFragment.this);
                return true;
            }

            @Override
            public void cancelCallback() {

            }
        });
    }
}
