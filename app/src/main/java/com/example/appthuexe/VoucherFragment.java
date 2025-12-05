package com.example.appthuexe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class VoucherFragment extends Fragment implements VoucherAdapter.OnVoucherClickListener {

    private RecyclerView recyclerView;
    private VoucherAdapter voucherAdapter;
    private List<Voucher> voucherList;
    private FirebaseFirestore db;

    public VoucherFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_voucher, container, false);

        // ✅ TÌM NÚT QUAY LẠI (ImageView) VÀ GÁN SỰ KIỆN
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        recyclerView = view.findViewById(R.id.rvVouchers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        voucherList = new ArrayList<>();
        voucherAdapter = new VoucherAdapter(voucherList, this);
        recyclerView.setAdapter(voucherAdapter);

        db = FirebaseFirestore.getInstance();
        loadVouchers();

        return view;
    }

    private void loadVouchers() {
        db.collection("vouchers").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Lỗi khi tải voucher!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshots != null) {
                voucherList.clear();
                voucherList.addAll(snapshots.toObjects(Voucher.class));
                voucherAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onVoucherClick(Voucher voucher) {
        if (getActivity() != null) {
            Bundle bundle = new Bundle();
            bundle.putString("voucherCode", voucher.getCode());
            bundle.putInt("voucherDiscount", voucher.getDiscountPercent());
            bundle.putString("voucherTitle", voucher.getTitle());

            getParentFragmentManager().setFragmentResult("voucherResult", bundle);

            Toast.makeText(getContext(),
                    "Đã chọn voucher: " + voucher.getTitle(),
                    Toast.LENGTH_SHORT).show();

            requireActivity().onBackPressed();
        }
    }
}
