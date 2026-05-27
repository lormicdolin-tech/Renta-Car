package com.example.renta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class NotificationsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.notifications_recycler_view);
        TextView noNotificationsText = view.findViewById(R.id.no_notifications_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseConfig.getDatabase().getReference("notifications").child(user.getUid())
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        List<Notification> notifications = new ArrayList<>();
                        for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                            Notification notification = ds.getValue(Notification.class);
                            if (notification != null) {
                                notifications.add(notification);
                            }
                        }
                        
                        // Sort by timestamp descending
                        Collections.sort(notifications, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));

                        if (notifications.isEmpty()) {
                            noNotificationsText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            noNotificationsText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setAdapter(new NotificationsAdapter(notifications));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
                });
        } else {
            noNotificationsText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        return view;
    }

    private static class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
        private final List<Notification> items;

        NotificationsAdapter(List<Notification> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Notification item = items.get(position);
            holder.title.setText(item.getTitle());
            holder.message.setText(item.getMessage());
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
            holder.time.setText(sdf.format(new Date(item.getTimestamp())));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, message, time;
            ViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.notification_title);
                message = view.findViewById(R.id.notification_message);
                time = view.findViewById(R.id.notification_time);
            }
        }
    }
}