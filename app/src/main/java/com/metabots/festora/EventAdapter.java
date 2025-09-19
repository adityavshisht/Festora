package com.metabots.festora;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.VH> {

    public interface OnEventClick {
        void onEventClick(Event e);
    }

    private final Context ctx;
    private final List<Event> data;
    private final OnEventClick listener;

    public EventAdapter(Context ctx, List<Event> data, OnEventClick listener) {
        this.ctx = ctx;
        this.data = data;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_event, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Event e = data.get(pos);
        h.tvTitle.setText(e.title);
        h.tvDate.setText(e.dateTime);
        h.tvLocation.setText(e.location);

        // For now, use local drawable; later switch to Glide for URLs
        h.imgBanner.setImageResource(R.drawable.logo1);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEventClick(e);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgBanner;
        TextView tvTitle, tvDate, tvLocation;
        VH(@NonNull View v) {
            super(v);
            imgBanner = v.findViewById(R.id.imgBanner);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvDate = v.findViewById(R.id.tvDate);
            tvLocation = v.findViewById(R.id.tvLocation);
        }
    }
}
