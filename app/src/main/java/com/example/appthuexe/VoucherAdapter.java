package com.example.appthuexe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private List<Voucher> voucherList;
    private OnVoucherClickListener listener;

    // Constructor đầy đủ
    public VoucherAdapter(List<Voucher> voucherList, OnVoucherClickListener listener) {
        this.voucherList = voucherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher vc = voucherList.get(position);
        if (vc == null) return;

        // Gán đúng hình theo layout
        holder.imgVoucher.setImageResource(R.drawable.ic_voucher);

        // Gán text
        holder.tvTitle.setText(vc.getTitle());
        holder.tvDesc.setText(vc.getDescription());

        String date = "HSD: " + vc.getStartDate() + " - " + vc.getEndDate();
        holder.tvDate.setText(date);

        // Bắt sự kiện chọn voucher
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onVoucherClick(vc);
        });
    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {

        ImageView imgVoucher;
        TextView tvTitle, tvDesc, tvDate;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);

            imgVoucher = itemView.findViewById(R.id.imgVoucher);
            tvTitle = itemView.findViewById(R.id.tvVoucherTitle);
            tvDesc = itemView.findViewById(R.id.tvVoucherDesc);
            tvDate = itemView.findViewById(R.id.tvVoucherDate);
        }
    }

    // Interface callback khi bấm voucher
    public interface OnVoucherClickListener {
        void onVoucherClick(Voucher voucher);
    }
}
