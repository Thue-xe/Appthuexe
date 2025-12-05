package com.example.appthuexe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private TextView tvNoData;
    private ImageView btnBack;

    private List<HistoryItem> historyList = new ArrayList<>();
    private HistoryAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public HistoryFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHistory = view.findViewById(R.id.rvHistory);
        tvNoData = view.findViewById(R.id.tvNoData);
        btnBack = view.findViewById(R.id.btnBack);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter(historyList);
        rvHistory.setAdapter(adapter);

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadHistory();
    }

    private void loadHistory() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("orders")
                .whereEqualTo("userId", uid)
                .get()   // ❗ Bỏ orderBy để tránh lỗi index
                .addOnSuccessListener(query -> {

                    historyList.clear();

                    if (query.isEmpty()) {
                        tvNoData.setVisibility(View.VISIBLE);
                        rvHistory.setVisibility(View.GONE);
                        return;
                    }

                    tvNoData.setVisibility(View.GONE);
                    rvHistory.setVisibility(View.VISIBLE);

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        String vehicleId = doc.getString("vehicleId");
                        long amount = doc.getLong("amount");
                        String startDate = doc.getString("startDate");
                        String endDate = doc.getString("endDate");

                        Date created = doc.getDate("createdAt");
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        String createdAt = sdf.format(created);

                        Log.d("DEBUG_HISTORY", "Order vehicleId = " + vehicleId);

                        db.collection("vehicles").document(vehicleId).get()
                                .addOnSuccessListener(vDoc -> {

                                    if (!vDoc.exists()) {
                                        Log.e("DEBUG_HISTORY", "Vehicle not found: " + vehicleId);
                                        return;
                                    }

                                    String hang = vDoc.getString("hangXe");
                                    String mau  = vDoc.getString("mauXe");
                                    String tenXe = (hang + " " + mau).trim();

                                    historyList.add(new HistoryItem(
                                            tenXe,
                                            startDate,
                                            endDate,
                                            amount,
                                            createdAt
                                    ));

                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> Log.e("DEBUG_HISTORY", "Load vehicle fail", e));
                    }
                });
    }
}
