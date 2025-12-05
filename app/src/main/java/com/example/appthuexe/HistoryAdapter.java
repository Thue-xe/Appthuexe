package com.example.appthuexe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<HistoryItem> list;

    public HistoryAdapter(List<HistoryItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = list.get(position);

        holder.tvVehicleName.setText("Xe: " + item.vehicleName);
        holder.tvDateRange.setText(item.startDate + " → " + item.endDate);
        holder.tvAmount.setText(item.amount + "₫");
        holder.tvCreatedAt.setText("Tạo lúc: " + item.createdAt);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvVehicleName, tvDateRange, tvAmount, tvCreatedAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleName = itemView.findViewById(R.id.tvVehicleName);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }
    }
}
