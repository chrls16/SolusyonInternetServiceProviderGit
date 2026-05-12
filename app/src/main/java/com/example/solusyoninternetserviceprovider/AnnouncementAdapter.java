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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnnouncementModel item = list.get(position);

        holder.tvCategory.setText(item.getCategory());
        holder.tvTitle.setText(item.getTitle());
        holder.tvDesc.setText(item.getDescription());
        holder.tvTime.setText(item.getTimestamp());
        holder.ivIcon.setImageResource(item.getIconRes());

        int color = Color.parseColor(item.getColorHex());
        holder.viewAccent.setBackgroundColor(color);
        holder.tvCategory.setTextColor(color);

        // Lighten the background for the icon card
        holder.cardIcon.setCardBackgroundColor(color);
        holder.cardIcon.setAlpha(0.2f);
        holder.ivIcon.setColorFilter(color);
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