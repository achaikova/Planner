package com.planner;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment {

    private View requestsView;
    private RecyclerView requestsList;
    private DatabaseReference requestsRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requestsView = inflater.inflate(R.layout.fragment_request, container, false);
        requestsList = requestsView.findViewById(R.id.request_recycle_view);
        requestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        requestsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("requests");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        return requestsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Boolean>().setQuery(requestsRef, Boolean.class).build();
        FirebaseRecyclerAdapter<Boolean, RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Boolean, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestsViewHolder requestsViewHolder, int i, @NonNull Boolean user) {
                String userId = getRef(i).getKey();
                userRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue().toString();
                            String imageId = snapshot.child("profileImage").getValue().toString();
                            requestsViewHolder.userName.setText(name);
                            Picasso.get()
                                    .load(imageId)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(requestsViewHolder.userImage);
                            requestsViewHolder.currentUserId = userId;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_friend_item, parent, false);
                RequestsViewHolder viewHolder = new RequestsViewHolder(view);
                return viewHolder;
            }
        };
        requestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        private String currentUserId, friendId;
        private final TextView userName;
        private final CircleImageView userImage;
        private DatabaseReference requestsRef, userRef;
        private FirebaseAuth mAuth;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.friend_name);
            userImage = itemView.findViewById(R.id.friend_image);
            Button acceptRequest = itemView.findViewById(R.id.button_accept_request);
            Button declineRequest = itemView.findViewById(R.id.button_decline_request);
            acceptRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("hi", "Clicked accept " + currentUserId);
                    mAuth = FirebaseAuth.getInstance();
                    currentUserId = mAuth.getCurrentUser().getUid();
                    requestsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("requests");
                    userRef = FirebaseDatabase.getInstance().getReference().child("users");
                    //TODO add friend, delete request
                }
            });
            declineRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("hi", "Clicked decline " + currentUserId);
                }
            });
        }


    }


}