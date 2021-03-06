package com.planner;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class FeedFragment extends Fragment {

    private View feedView;
    private RecyclerView recyclerView;
    private DatabaseReference userRef;
    private Query doneTasksRef;
    private String currentUserId;
    private static final String TAG = "Feed Fragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        feedView = inflater.inflate(R.layout.fragment_feed, container, false);
        recyclerView = feedView.findViewById(R.id.feed_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        currentUserId = PlannerCostants.mAuth.getCurrentUser().getUid();
        userRef = PlannerCostants.databaseReference.child("users");
        doneTasksRef = PlannerCostants.databaseReference.child("completedTasks").orderByChild("timestamp");
        return feedView;
    }

    @Override
    public void onStart() {
        super.onStart();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<CompletedTask>().setQuery(doneTasksRef, CompletedTask.class).build();
        FirebaseRecyclerAdapter<CompletedTask, FeedFragment.tasksViewHolder> adapter = new FirebaseRecyclerAdapter<CompletedTask, tasksViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull tasksViewHolder tasksViewHolder, int i, @NonNull CompletedTask s) {
                String userId = s.getOwner();
                //  Log.d(TAG, s.getOwner() + " " + s.getUploadId());
                userRef.child(currentUserId).child("friends").child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            tasksViewHolder.itemView.setVisibility(View.VISIBLE);
                            userRef.child(userId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String name = snapshot.child("name").getValue().toString();
                                    String imageId = snapshot.child("profileImage").getValue().toString();
                                    tasksViewHolder.userName.setText(name);
                                    Picasso.get()
                                            .load(imageId)
                                            .placeholder(R.drawable.ic_launcher_foreground)
                                            .into(tasksViewHolder.userImage);

                                    Calendar cal = Calendar.getInstance();
                                    cal.setTimeInMillis(s.getTimestampLong());
                                    SimpleDateFormat fmt = new SimpleDateFormat("dd MMM. kk:mm", Locale.US);
                                    String time = fmt.format(cal.getTime());
                                    //   Log.d(TAG, "TIME : " + time);

                                    tasksViewHolder.taskTime.setText(time);
                                    if (s.getUploadId() != null) {
                                        DatabaseReference uploadRef = PlannerCostants.databaseReference.child("uploads").child(s.getUploadId());
                                        uploadRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    UploadFile file = snapshot.getValue(UploadFile.class);
                                                    Picasso.get()
                                                            .load(file.getFileUrl())
                                                            .into(tasksViewHolder.taskImage);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            PlannerCostants.databaseReference.child("tasks").child(getRef(i).getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot innerSnapshot) {
                                    if (innerSnapshot.exists()) {
                                        Task task = innerSnapshot.getValue(Task.class);
                                        String title = "Title: " + task.getTitle();
                                        String description = "Description:\n" + task.getDescription();
                                        tasksViewHolder.taskTitle.setText(title);
                                        tasksViewHolder.taskDescription.setText(description);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            tasksViewHolder.itemView.setVisibility(View.GONE);
                            tasksViewHolder.itemView.getLayoutParams().height = 0;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public tasksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.done_task_item, parent, false);
                FeedFragment.tasksViewHolder viewHolder = new FeedFragment.tasksViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    public static class tasksViewHolder extends RecyclerView.ViewHolder {

        private final TextView taskTitle, taskDescription, userName, taskTime;
        private final CircleImageView userImage;
        private final ImageView taskImage;

        public tasksViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.task_user_name);
            userImage = itemView.findViewById(R.id.task_user_image);
            taskTitle = itemView.findViewById(R.id.user_task_title);
            taskDescription = itemView.findViewById(R.id.user_task_description);
            taskImage = itemView.findViewById(R.id.user_task_image);
            taskTime = itemView.findViewById(R.id.user_task_time);
        }
    }
}