package com.example.appthuexe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<RentalFragment.Vehicle> vehicleList;

    // Listener khi click vào item
    public interface OnItemClickListener {
        void onItemClick(String vehicleId);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public VehicleAdapter(List<RentalFragment.Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        RentalFragment.Vehicle v = vehicleList.get(position);

        holder.tvName.setText(v.tenXe);
        holder.tvPrice.setText(NumberFormat.getInstance().format(v.giaThue) + "đ/ngày");
        holder.tvLocation.setText(v.diaDiem);

        Glide.with(holder.itemView.getContext())
                .load(v.hinhAnh)
                .placeholder(R.drawable.ic_motorbike)
                .into(holder.imgVehicle);

        holder.itemView.setOnClickListener(view -> {
            if (listener != null) listener.onItemClick(v.id);
        });
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public static class VehicleViewHolder extends RecyclerView.ViewHolder {

        ImageView imgVehicle;
        TextView tvName, tvPrice, tvLocation;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);

            imgVehicle = itemView.findViewById(R.id.img_vehicle);
            tvName = itemView.findViewById(R.id.tv_vehicle_name);
            tvPrice = itemView.findViewById(R.id.tv_vehicle_price);
            tvLocation = itemView.findViewById(R.id.tv_vehicle_location);
        }
    }
}
