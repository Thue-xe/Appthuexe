package com.example.appthuexe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import android.widget.ImageView;

public class VoucherFragment extends Fragment {

    private RecyclerView rvVouchers;
    private VoucherAdapter adapter;
    private List<Voucher> voucherList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voucher, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvVouchers = view.findViewById(R.id.rvVouchers);
        rvVouchers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new VoucherAdapter(voucherList);
        rvVouchers.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
// 1. Ánh xạ nút back từ layout XML
        ImageView btnBack = view.findViewById(R.id.btnBack);

        // 2. Bắt sự kiện khi nhấn vào nút
        btnBack.setOnClickListener(v -> {
            // Cách 1: Quay lại Fragment trước đó trong ngăn xếp (Back Stack)
            getParentFragmentManager().popBackStack();

            // Cách 2: Nếu bạn muốn hành vi giống hệt nút Back vật lý của điện thoại:
            // requireActivity().onBackPressed();
        });

        // ---------------------------------

        loadVouchers();
    }


    private void loadVouchers() {
        db.collection("vouchers")
                .get()
                .addOnSuccessListener(query -> {
                    voucherList.clear();
                    for (var doc : query) {
                        Voucher vc = doc.toObject(Voucher.class);
                        voucherList.add(vc);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
