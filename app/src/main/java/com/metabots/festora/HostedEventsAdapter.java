package com.metabots.festora;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class HostedEventsAdapter extends RecyclerView.Adapter<HostedEventsAdapter.ViewHolder> {

    private final Context context;
    private final List<Event> events;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public HostedEventsAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_hosted_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event e = events.get(position);

        holder.tvTitle.setText(e.title);
        holder.tvDate.setText(e.dateTime);
        holder.tvLocation.setText(e.location);

        if (e.imageUrl != null && !e.imageUrl.isEmpty()) {
            Glide.with(context).load(e.imageUrl).into(holder.imgBanner);
        }

        holder.btnDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete \"" + e.title + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.collection("events").document(e.id)
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    events.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(ex ->
                                        Toast.makeText(context, "Failed: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvLocation;
        ImageView imgBanner;
        MaterialButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            imgBanner = itemView.findViewById(R.id.imgBanner);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
