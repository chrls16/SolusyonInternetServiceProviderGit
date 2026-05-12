package com.example.solusyoninternetserviceprovider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class ProblemAdapter extends RecyclerView.Adapter<ProblemAdapter.ProblemViewHolder> {

    private List<ProblemModel> problemList;
    private int selectedPosition = -1; // Tracks which item is clicked

    public ProblemAdapter(List<ProblemModel> problemList) {
        this.problemList = problemList;
    }

    @NonNull
    @Override
    public ProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_problem, parent, false);
        return new ProblemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemViewHolder holder, int position) {
        ProblemModel problem = problemList.get(position);

        holder.tvTitle.setText(problem.getTitle());
        holder.tvDescription.setText(problem.getDescription());
        holder.ivIcon.setImageResource(problem.getIconRes());

        // VISUAL SELECTION LOGIC
        if (selectedPosition == position) {
            // Selected State: Blue border and visible checkmark
            holder.cardView.setStrokeWidth(6);
            holder.ivCheck.setVisibility(View.VISIBLE);
        } else {
            // Unselected State: No border and hidden checkmark
            holder.cardView.setStrokeWidth(0);
            holder.ivCheck.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Refresh the previous and new selection to update the UI
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return problemList.size();
    }

    // Method to get the selected problem title for submission
    public String getSelectedProblem() {
        if (selectedPosition != -1) {
            return problemList.get(selectedPosition).getTitle();
        }
        return null;
    }

    public static class ProblemViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvTitle, tvDescription;
        ImageView ivIcon, ivCheck;

        public ProblemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.problemCard);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivCheck = itemView.findViewById(R.id.ivCheck);
        }
    }
}