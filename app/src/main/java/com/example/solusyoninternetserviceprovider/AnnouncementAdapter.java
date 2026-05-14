package com.example.solusyoninternetserviceprovider;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private List<AnnouncementModel> list;

    public AnnouncementAdapter(List<AnnouncementModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false);
        return new ViewHolder(v);
    }

// Inside AnnouncementAdapter.java -> onBindViewHolder

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnnouncementModel item = list.get(position);

        holder.tvCategory.setText(item.getCategory());
        holder.tvTitle.setText(item.getTitle());
        holder.tvDesc.setText(item.getDescription());
        holder.tvTime.setText(item.getTimestamp());

        // 1. DYNAMIC ICON SELECTION
        int iconRes = R.drawable.ic_info;
        if ("WARNING".equalsIgnoreCase(item.getCategory())) {
            iconRes = R.drawable.ic_warning;
        } else if ("CRITICAL".equalsIgnoreCase(item.getCategory())) {
            iconRes = R.drawable.ic_critical;
        }
        holder.ivIcon.setImageResource(iconRes);

        // 2. STYLING
        int themeColor = Color.parseColor(item.getColorHex());
        holder.viewAccent.setBackgroundColor(themeColor);
        holder.tvCategory.setTextColor(themeColor);

        // FIX: Set a semi-transparent background color instead of setting alpha on the whole View
        // This keeps the icon fully visible while the box stays light
        int alphaColor = androidx.core.graphics.ColorUtils.setAlphaComponent(themeColor, 40); // 40 is approx 15% opacity
        holder.cardIcon.setCardBackgroundColor(alphaColor);
        holder.cardIcon.setAlpha(1.0f); // Reset view alpha to full

        // Make the icon itself the sharp theme color
        holder.ivIcon.setColorFilter(themeColor);
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View viewAccent;
        TextView tvCategory, tvTitle, tvDesc, tvTime;
        ImageView ivIcon;
        MaterialCardView cardIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewAccent = itemView.findViewById(R.id.viewAccent);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTimestamp);
            ivIcon = itemView.findViewById(R.id.ivAlertIcon);
            cardIcon = itemView.findViewById(R.id.cardIcon);
        }
    }
}