package com.example.chatapp.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.chatapp.Adapter.ContactAdapter;
import com.example.chatapp.Adapter.HistoryCallAdapter;
import com.example.chatapp.Models.HistoryCall;
import com.example.chatapp.Models.Users;
import com.example.chatapp.R;
import com.example.chatapp.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CallFragment extends Fragment {
    RecyclerView rvListCallHistory;
    ArrayList<HistoryCall> listCallHistory = new ArrayList<>();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    HistoryCallAdapter historyCallAdapter;

    DatabaseReference mHistoryCallReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView  = inflater.inflate(R.layout.fragment_call, container, false);
        setControl(mView);
        setEvent();
        return mView;
    }

    private void setControl(View mView) {
        rvListCallHistory = (RecyclerView) mView.findViewById(R.id.rvListCallHistory);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mHistoryCallReference = FirebaseDatabase.getInstance().getReference().child("HistoryCall");

        /* Khởi tạo đối tượng Adapter*/
        historyCallAdapter = new HistoryCallAdapter(getContext(), listCallHistory);
        rvListCallHistory.setAdapter(historyCallAdapter);

        /* Tạo ngăn cách giữa 2 đối tượng*/
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rvListCallHistory.addItemDecoration(itemDecoration);
        /* Khởi tạo một LinearLayout và gán vào RecycleView */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvListCallHistory.setLayoutManager(layoutManager);
    }

    private void setEvent() {
        loadHistoryCall();
    }

    private void loadHistoryCall() {
        mHistoryCallReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listCallHistory.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        HistoryCall historyCall = dataSnapshot.getValue(HistoryCall.class);
                            historyCall.setUserID(dataSnapshot.getKey());
                            listCallHistory.add(historyCall);
                    }
                    historyCallAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "Không có cuộc gọi nào gần đây", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}