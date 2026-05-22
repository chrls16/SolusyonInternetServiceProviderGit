package com.example.solusyoninternetserviceprovider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class BarangayReportAdapter extends RecyclerView.Adapter<BarangayReportAdapter.ViewHolder> {
    private List<Map.Entry<String, Integer>> barangayList;
    private int totalSubscribers;

    public BarangayReportAdapter(List<Map.Entry<String, Integer>> list, int total) {
        this.barangayList = list;
        this.totalSubscribers = total;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_barangay_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<String, Integer> entry = barangayList.get(position);
        int percentage = (int) (((float) entry.getValue() / totalSubscribers) * 100);

        holder.tvName.setText(entry.getKey().toUpperCase());
        holder.tvPercent.setText(percentage + "%");
        holder.progressBar.setProgress(percentage);
    }

    @Override
    public int getItemCount() { return barangayList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPercent;
        ProgressBar progressBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBarangayName);
            tvPercent = itemView.findViewById(R.id.tvBarangayPercent);
            progressBar = itemView.findViewById(R.id.pbBarangay);
        }
    }
}