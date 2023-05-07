package com.example.chatapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.HistoryCallModel;
import com.example.chatapp.Models.Users;
import com.example.chatapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class VoiceCallComingActivity extends AppCompatActivity {
    CircleImageView cirAvatarVoiceCalComing;
    TextView tvNameVoiceCalComing, tvEmailVoiceCallComing;
    FloatingActionButton fabDeclineVoiceCall, fabAcceptVoiceCall;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserReference, mVoiceCallReference;

    String senderID, receiverID, senderName, senderAvatar, type = "VoiceCall";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call_coming);
        setControl();
        setEvent();

    }


    private void setControl() {
        cirAvatarVoiceCalComing = findViewById(R.id.cirAvatarVoiceCalComing);
        tvNameVoiceCalComing = findViewById(R.id.tvNameVoiceCalComing);
        tvEmailVoiceCallComing = findViewById(R.id.tvEmailVoiceCallComing);
        fabDeclineVoiceCall = findViewById(R.id.fabDeclineVoiceCall);
        fabAcceptVoiceCall = findViewById(R.id.fabAcceptVoiceCall);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mVoiceCallReference = FirebaseDatabase.getInstance().getReference().child("VoiceCallComing");

        senderID = getIntent().getStringExtra("senderID");
        receiverID = mUser.getUid();
    }

    private void setEvent() {
        loadSenderProfile();
        checkResponse();


        fabAcceptVoiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String response = "yes";
                sendResponse(response);
                HistoryCallModel historyCallModel =new HistoryCallModel(senderID, senderAvatar ,senderName,"ReceiveCall",type,receiverID);
                historyCallModel.createHistoryCall();
            }
        });

        fabDeclineVoiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String response = "no";
                sendResponse(response);
                HistoryCallModel historyCallModel =new HistoryCallModel(senderID, senderAvatar,senderName,"MissedCall",type,receiverID);
                historyCallModel.createHistoryCall();
            }
        });
    }

    private void sendResponse(String response) {

        if (response.equals("yes")){
            HashMap hashMap = new HashMap();
            hashMap.put("key",senderName+receiverID);
            hashMap.put("response","yes");
            mVoiceCallReference.child(senderID).child(receiverID).child("response").updateChildren(hashMap);
            joinMeeting();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mVoiceCallReference.child(senderID).child(receiverID).removeValue();
                }
            },3000);
        } else if (response.equals("no")){
            HashMap hashMap = new HashMap();
            hashMap.put("key",senderName+receiverID);
            hashMap.put("response","no");
            mVoiceCallReference.child(senderID).child(receiverID).child("response").updateChildren(hashMap);
            Toast.makeText(this, "Từ chối cuộc gọi", Toast.LENGTH_SHORT).show();
            finish();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mVoiceCallReference.child(senderID).child(receiverID).removeValue();
                }
            },1000);
        }
    }

    private void joinMeeting() {
        try {
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom(senderName+receiverID)
                    .setFeatureFlag("welcomepage.enabled", false)
                    .setFeatureFlag("prejoinpage.enabled", false)
                    .setVideoMuted(true)
                    .build();
            JitsiMeetActivity.launch(VoiceCallComingActivity.this, options);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSenderProfile() {
        mUserReference.child(senderID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users users = snapshot.getValue(Users.class);
                    senderName = users.getUserName().trim();
                    senderAvatar =users.getProfilePic();
                    Picasso.get().load(senderAvatar).placeholder(R.drawable.default_avatar).into(cirAvatarVoiceCalComing);
                    tvNameVoiceCalComing.setText(senderName);
                    tvEmailVoiceCallComing.setText(users.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkResponse() {
        mVoiceCallReference.child(senderID).child(receiverID).child("response").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String response = snapshot.child("response").getValue().toString().trim();
                    if (response.equals("no")) {
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}