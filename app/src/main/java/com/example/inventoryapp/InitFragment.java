package com.example.inventoryapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class InitFragment extends Fragment {

    private Button createBtn;
    private Activity HostActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.d("life", "RV: onCreate");
        HostActivity = getActivity();
        return inflater.inflate(R.layout.frag_empty_inv, container, false);
    }

    @Override
    public void onStart() {
        Log.d("life", "initFrag: onStart");
         createBtn = (Button) HostActivity.findViewById(R.id.CreateInventoryBtn);
         createBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 CreateInventory();
             }
         });
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d("life", "initFrag: onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("life", "initFrag: onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d("life", "initFrag: onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d("life", "initFrag: onDestroy");
        super.onDestroy();
    }

    private void CreateInventory(){
        // Displays a form for a user to Title and navigate to a newly created Inventory
        int layout = R.layout.dialog_inventorycreation;
        DialogFragment newFragment = GlobalActions.MyAlertDialogFragmentWithCustomLayout.newInstance(layout,
                new Callable<Void>() {
                    @Override
                    public Void call() {
                        MainActivity a = (MainActivity) getActivity();
                        View view = getActivity().getLayoutInflater().inflate(layout,null);
                        a.Testb(view);
                        /*
                        //get the context of the dialog view, and use that to find the edit text
                        View view = getActivity().getLayoutInflater().inflate(layout,null);
                        final EditText eT = (EditText) view.findViewById(R.id.newInventoryNameEditText);
                        String inventoryName = String.valueOf(eT.getText());
                        //use the text entered as the header for a new activity
                        Intent intent = new Intent(HostActivity, InventoryActivity.class);
                        Toast.makeText(HostActivity, inventoryName, Toast.LENGTH_SHORT).show();
                        intent.putExtra("name","Sample Inventory");
                        startActivity(intent);
                         */
                        return null;
                    }
                });
        newFragment.show(getParentFragmentManager(), "dialog");
    }
}
