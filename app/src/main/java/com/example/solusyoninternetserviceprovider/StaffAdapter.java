package com.example.solusyoninternetserviceprovider;
import android.content.Intent; import android.graphics.Bitmap; import android.graphics.BitmapFactory; import android.util.Base64; import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup; import android.widget.ImageButton; import android.widget.PopupMenu; import android.widget.TextView; import android.widget.Toast; import androidx.annotation.NonNull; import androidx.recyclerview.widget.RecyclerView; import com.google.android.material.imageview.ShapeableImageView; import com.google.firebase.database.FirebaseDatabase; import java.util.List;
public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {
    private List<StaffModel> staffList;

    public StaffAdapter(List<StaffModel> staffList) {
        this.staffList = staffList;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        StaffModel staff = staffList.get(position);

        holder.tvName.setText(staff.getName());
        holder.tvRoleNode.setText(staff.getRole() + " • " + staff.getNode());

        // FORCE SCALE TYPE TO PREVENT STRETCHING
        holder.ivProfile.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);

        if (staff.getImageUrl() != null && !staff.getImageUrl().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(staff.getImageUrl(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivProfile.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.ivProfile.setImageResource(R.drawable.ic_person);
            }
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_person);
        }

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMore);
            popup.getMenu().add("Edit");
            popup.getMenu().add("Delete");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Edit")) {
                    Intent intent = new Intent(v.getContext(), AddStaff.class);
                    intent.putExtra("staff_data", staff);
                    v.getContext().startActivity(intent);
                } else if (item.getTitle().equals("Delete")) {
                    FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("Staff")
                            .child(staff.getStaffId())
                            .removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(v.getContext(), "Staff Deleted", Toast.LENGTH_SHORT).show());
                }
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivProfile;
        TextView tvName, tvRoleNode;
        ImageButton btnMore;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivStaffProfile);
            tvName = itemView.findViewById(R.id.tvStaffName);
            tvRoleNode = itemView.findViewById(R.id.tvStaffRoleNode);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}