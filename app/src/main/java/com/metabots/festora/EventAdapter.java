package com.metabots.festora;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Base64;

import com.bumptech.glide.Glide;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.VH> {

    public interface OnEventClick {
        void onEventClick(@NonNull Event e);
    }

    private final Context ctx;
    private final List<Event> data;
    private final OnEventClick onClick;
    private final LayoutInflater inflater;

    public EventAdapter(@NonNull Context ctx,
                        @NonNull List<Event> data,
                        @NonNull OnEventClick onClick) {
        this.ctx = ctx;
        this.data = data;
        this.onClick = onClick;
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_event, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Event e = data.get(position);
        h.tvTitle.setText(e.title != null ? e.title : "—");
        h.tvDate.setText(e.dateTime != null ? e.dateTime : "Date TBA");
        h.tvLocation.setText(e.location != null ? e.location : "Location TBA");

        bindImage(e.imageUrl, h.imgBanner);

        h.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onEventClick(e);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgBanner;
        TextView tvTitle, tvDate, tvLocation;
        VH(@NonNull View v) {
            super(v);
            imgBanner  = v.findViewById(R.id.imgBanner);
            tvTitle    = v.findViewById(R.id.tvTitle);
            tvDate     = v.findViewById(R.id.tvDate);
            tvLocation = v.findViewById(R.id.tvLocation);
        }
    }

    private void bindImage(String urlOrData, ImageView imageView) {
        if (TextUtils.isEmpty(urlOrData)) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }

        if (urlOrData.startsWith("data:image")) {
            int comma = urlOrData.indexOf(',');
            if (comma != -1 && comma + 1 < urlOrData.length()) {
                String base64 = urlOrData.substring(comma + 1).replaceAll("\\s", "");
                try {
                    byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                    Glide.with(ctx).load(bytes).into(imageView);
                    return;
                } catch (IllegalArgumentException ignore) {
                    imageView.setImageResource(R.drawable.ic_launcher_foreground);
                    return;
                }
            }
        }

        Glide.with(ctx).load(urlOrData).into(imageView);
    }
}
