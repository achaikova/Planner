package com.planner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendFragment extends Fragment {
    private RecyclerView friendsList;
    private DatabaseReference friendsRef;
    private static final String TAG = "Friend Fragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View friendsView = inflater.inflate(R.layout.fragment_friend, container, false);
        friendsList = friendsView.findViewById(R.id.friend_recycle_view);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        String currentUserId = PlannerCostants.mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("friends");
        return friendsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Boolean>().setQuery(friendsRef, Boolean.class).build();
        FirebaseRecyclerAdapter<Boolean, FriendFragment.friendsViewHolder> adapter = new FirebaseRecyclerAdapter<Boolean, FriendFragment.friendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendFragment.friendsViewHolder friendsViewHolder, int i, @NonNull Boolean user) {
                String userId = getRef(i).getKey();
                PlannerCostants.userRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue().toString();
                            String imageId = snapshot.child("profileImage").getValue().toString();
                            friendsViewHolder.userName.setText(name);
                            Picasso.get()
                                    .load(imageId)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(friendsViewHolder.userImage);
                            friendsViewHolder.suggestTask.setOnClickListener(v -> {
                                Intent intent = new Intent(getContext(), NewTaskActivity.class);
                                String currentUserId = PlannerCostants.mAuth.getCurrentUser().getUid();
                                intent.putExtra("parentId", currentUserId);
                                intent.putExtra("adresseId", userId);
                                startActivity(intent);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                friendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ProfileActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public friendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
                friendsViewHolder viewHolder = new FriendFragment.friendsViewHolder(view);
                return viewHolder;
            }
        };
        friendsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class friendsViewHolder extends RecyclerView.ViewHolder {

        private final TextView userName;
        private final CircleImageView userImage;
        private final ImageView suggestTask;

        public friendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.friend_name);
            userImage = itemView.findViewById(R.id.friend_image);
            suggestTask = itemView.findViewById(R.id.suggestTask);
        }
    }
}
