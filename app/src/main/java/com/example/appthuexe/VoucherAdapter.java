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

    public VoucherAdapter(List<Voucher> voucherList) {
        this.voucherList = voucherList;
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

        holder.imgVoucher.setImageResource(R.drawable.ic_voucher);
        holder.tvTitle.setText(vc.getTitle());
        holder.tvDesc.setText(vc.getDescription());
        holder.tvDate.setText("HSD: " + vc.getStartDate() + " - " + vc.getEndDate());
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
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
}
